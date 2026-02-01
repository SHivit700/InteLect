package com.lectureai.quiz.workflow

import com.lectureai.quiz.tools.AnswerValidationToolSet
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Result of answer validation.
 */
data class ValidationResult(
    val isCorrect: Boolean,
    val feedback: String
)

/**
 * Workflow for validating user answers using AI agent with tools.
 */
class AnswerValidationWorkflow(private val apiKey: String) {

    private val model: LLModel = AnthropicModels.Haiku_3
    
    private val promptExecutor = SingleLLMPromptExecutor(
        AnthropicLLMClient(apiKey)
    )

    private val SYSTEM_PROMPT = """
You are an educational answer validation assistant. Your job is to fairly evaluate student answers.

AVAILABLE TOOLS:
1. checkMCQAnswer - For multiple choice questions, checks if user's A/B/C/D answer is correct
2. getAnswerContext - For short answer questions, retrieves the expected answer for YOU to evaluate semantically
3. generateFeedback - Generates personalized learning feedback based on the result

CRITICAL EVALUATION PRINCIPLES:
- Be LENIENT and FAIR - students may phrase things differently but still be correct
- Accept synonyms (e.g., "ML" = "machine learning", "AI" = "artificial intelligence")
- Accept equivalent meanings even with different wording
- Accept partial answers if they capture the CORE concept
- Only mark INCORRECT if the answer is fundamentally wrong or off-topic

WORKFLOW:
1. For MCQ: Call checkMCQAnswer directly
2. For Short Answer: 
   a. Call getAnswerContext to get the expected answer and context
   b. YOU perform semantic comparison (not keyword matching!)
   c. Decide: CORRECT (captures key concept), PARTIALLY_CORRECT (partial understanding), or INCORRECT (wrong)
3. Call generateFeedback with appropriate result
4. Return JSON response

IMPORTANT: For short answers, treat PARTIALLY_CORRECT as a passing grade - the student showed understanding!

OUTPUT FORMAT (strict JSON only):
{
  "is_correct": true/false,
  "feedback": "Encouraging and educational feedback"
}

Remember: Be generous in interpretation. If a student demonstrates understanding of the concept, mark it CORRECT.
""".trimIndent()

    suspend fun validate(
        transcript: String,
        questionText: String,
        questionType: String,
        correctAnswer: String,
        userAnswer: String,
        options: List<Map<String, String>>? = null
    ): ValidationResult {
        logger.info { "Starting answer validation for question type: $questionType" }
        
        val toolSet = AnswerValidationToolSet(
            transcript = transcript,
            questionText = questionText,
            questionType = questionType,
            correctAnswer = correctAnswer,
            options = options
        )
        
        val toolRegistry = ToolRegistry {
            tools(toolSet.asTools())
        }
        
        logger.info { "Registered ${toolRegistry.tools.size} validation tools" }
        
        val agent = AIAgent(
            promptExecutor = promptExecutor,
            llmModel = model,
            toolRegistry = toolRegistry,
            systemPrompt = SYSTEM_PROMPT,
            maxIterations = 10
        )
        
        val userPrompt = """
Validate this answer:

Question Type: $questionType
Question: $questionText
User's Answer: $userAnswer
Correct Answer: $correctAnswer

Use the appropriate validation tool based on the question type, then generate feedback.
""".trimIndent()
        
        return try {
            val result = agent.run(userPrompt)
            logger.info { "Agent completed validation: ${result.take(200)}" }
            
            // Parse the JSON result
            parseResult(result, questionType, correctAnswer, userAnswer)
        } catch (e: Exception) {
            logger.error(e) { "Validation failed: ${e.message}" }
            // Fallback to simple comparison
            fallbackValidation(questionType, correctAnswer, userAnswer)
        }
    }
    
    private fun parseResult(
        result: String,
        questionType: String,
        correctAnswer: String,
        userAnswer: String
    ): ValidationResult {
        return try {
            // Try to extract JSON from result
            val jsonMatch = Regex("""\{[\s\S]*"is_correct"[\s\S]*\}""").find(result)
            if (jsonMatch != null) {
                val json = jsonMatch.value
                val isCorrect = json.contains(""""is_correct"\s*:\s*true""".toRegex())
                val feedbackMatch = Regex(""""feedback"\s*:\s*"([^"]+)"""").find(json)
                val feedback = feedbackMatch?.groupValues?.get(1) ?: "Answer validated."
                ValidationResult(isCorrect, feedback)
            } else {
                // Fallback parsing based on tool output
                // IMPORTANT: PARTIALLY_CORRECT counts as correct (student showed understanding)
                val hasCorrect = result.contains("CORRECT") || result.contains("correct")
                val hasIncorrect = result.contains("INCORRECT") || result.contains("incorrect")
                val hasPartial = result.contains("PARTIALLY_CORRECT") || result.contains("partially")
                
                val isCorrect = (hasCorrect && !hasIncorrect) || hasPartial
                
                val feedback = when {
                    result.contains("CORRECT:") && !result.contains("INCORRECT") -> 
                        "Great job! You demonstrated a solid understanding of this concept."
                    hasPartial -> 
                        "Good effort! You're on the right track and showed understanding of the key concepts."
                    else -> 
                        "Review the explanation carefully to understand the correct answer."
                }
                ValidationResult(isCorrect, feedback)
            }
        } catch (e: Exception) {
            logger.warn { "Failed to parse result, using fallback: ${e.message}" }
            fallbackValidation(questionType, correctAnswer, userAnswer)
        }
    }
    
    private fun fallbackValidation(
        questionType: String,
        correctAnswer: String,
        userAnswer: String
    ): ValidationResult {
        if (questionType == "mcq") {
            val isCorrect = userAnswer.trim().uppercase() == correctAnswer.trim().uppercase()
            return ValidationResult(
                isCorrect = isCorrect,
                feedback = if (isCorrect) "Correct!" else "The correct answer is: $correctAnswer"
            )
        } else {
            // For short answer, be lenient in fallback - check for any word overlap
            val userWords = userAnswer.lowercase().split(Regex("\\W+")).filter { it.length > 2 }.toSet()
            val correctWords = correctAnswer.lowercase().split(Regex("\\W+")).filter { it.length > 2 }.toSet()
            
            // Check if there's meaningful overlap or substring match
            val overlap = userWords.intersect(correctWords)
            val hasSubstring = userAnswer.lowercase().contains(correctAnswer.lowercase().take(15)) ||
                               correctAnswer.lowercase().contains(userAnswer.lowercase().take(15))
            
            // Be generous: any overlap counts as partially correct
            val isCorrect = overlap.isNotEmpty() || hasSubstring || userWords.any { w -> correctWords.any { it.contains(w) || w.contains(it) } }
            
            return ValidationResult(
                isCorrect = isCorrect,
                feedback = if (isCorrect) 
                    "Good answer! You showed understanding of the concept." 
                else 
                    "The expected answer was: $correctAnswer"
            )
        }
    }
    
    fun close() {
        // Cleanup if needed
    }
}
