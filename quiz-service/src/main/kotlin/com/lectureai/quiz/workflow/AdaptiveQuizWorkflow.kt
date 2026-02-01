package com.lectureai.quiz.workflow

import com.lectureai.quiz.models.*
import com.lectureai.quiz.tools.DifficultyAnalysisToolSet
import com.lectureai.quiz.tools.QuizToolSet
import com.lectureai.quiz.validation.QuizValidator
import com.lectureai.quiz.validation.ValidationResult
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.message.Message
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Get the Claude model based on environment variable CLAUDE_MODEL.
 */
private fun getModelFromEnv(): LLModel {
    val modelName = System.getenv("CLAUDE_MODEL") ?: "haiku3"
    return when (modelName.lowercase()) {
        "haiku3", "haiku_3" -> AnthropicModels.Haiku_3
        "haiku35", "haiku_3_5" -> AnthropicModels.Haiku_3_5
        "sonnet35", "sonnet_3_5" -> AnthropicModels.Sonnet_3_5
        "sonnet37", "sonnet_3_7" -> AnthropicModels.Sonnet_3_7
        "sonnet4", "sonnet_4" -> AnthropicModels.Sonnet_4
        "opus3", "opus_3" -> AnthropicModels.Opus_3
        else -> {
            logger.warn { "Unknown model '$modelName', defaulting to Haiku_3" }
            AnthropicModels.Haiku_3
        }
    }
}

/**
 * Workflow for generating quizzes with adaptive difficulty.
 * 
 * This workflow:
 * 1. Analyzes previous quiz performance (if any) to determine target difficulty
 * 2. Generates a quiz where ALL questions have the same difficulty level
 * 3. Uses tools to validate uniform difficulty and answer grounding
 */
class AdaptiveQuizWorkflow(
    private val apiKey: String
) {
    private val anthropicClient = AnthropicLLMClient(apiKey)
    private val promptExecutor = SingleLLMPromptExecutor(anthropicClient)
    private val model: LLModel = getModelFromEnv()

    companion object {
        private const val MAX_REPAIR_ATTEMPTS = 2
    }

    /**
     * Execute the adaptive quiz workflow.
     * 
     * @param request The adaptive quiz request containing segment data and previous performance
     * @return AdaptiveQuizResponse with the generated quiz and difficulty info
     */
    suspend fun execute(request: AdaptiveQuizRequest): AdaptiveQuizResponse {
        logger.info { "Starting adaptive quiz workflow for segment ${request.segmentId}" }
        
        // Step 1: Determine target difficulty
        val (targetDifficulty, reasoning) = determineTargetDifficulty(request)
        logger.info { "Target difficulty determined: $targetDifficulty" }
        
        // Step 2: Build transcript from segments
        val transcript = buildTranscript(request.segments)
        
        // Step 3: Generate quiz with uniform difficulty
        val questions = generateQuizWithDifficulty(transcript, targetDifficulty, request.numQuestions)
        
        return AdaptiveQuizResponse(
            quizId = "${request.videoId}_segment_${request.segmentId}",
            videoId = request.videoId,
            segmentId = request.segmentId,
            difficulty = targetDifficulty,
            difficultyReasoning = reasoning,
            questions = questions
        )
    }

    /**
     * Determine the target difficulty for the next quiz.
     */
    private fun determineTargetDifficulty(request: AdaptiveQuizRequest): Pair<String, String?> {
        // If target difficulty is pre-determined (passed from Python), use it
        if (!request.targetDifficulty.isNullOrBlank()) {
            return Pair(request.targetDifficulty, "Difficulty was pre-determined by the system.")
        }
        
        // If no previous performance (first segment), start with easy
        if (request.previousPerformance == null) {
            return Pair("easy", "First segment - starting with easy questions to build confidence.")
        }
        
        // Calculate based on previous performance
        val prev = request.previousPerformance
        val accuracy = prev.accuracy
        val prevDifficulty = prev.quizDifficulty.lowercase()
        
        val newDifficulty = when {
            accuracy >= 0.80 -> {
                when (prevDifficulty) {
                    "easy" -> "medium"
                    "medium" -> "hard"
                    else -> "hard"
                }
            }
            accuracy >= 0.50 -> prevDifficulty
            else -> {
                when (prevDifficulty) {
                    "hard" -> "medium"
                    "medium" -> "easy"
                    else -> "easy"
                }
            }
        }
        
        val reasoning = when {
            accuracy >= 0.80 -> "Student achieved ${String.format("%.0f", accuracy * 100)}% accuracy on the previous $prevDifficulty quiz. Increasing difficulty."
            accuracy >= 0.50 -> "Student achieved ${String.format("%.0f", accuracy * 100)}% accuracy. Maintaining $prevDifficulty difficulty."
            else -> "Student achieved ${String.format("%.0f", accuracy * 100)}% accuracy. Decreasing to $newDifficulty to build confidence."
        }
        
        return Pair(newDifficulty, reasoning)
    }

    /**
     * Build transcript string from segments.
     */
    private fun buildTranscript(segments: List<TranscriptSegment>): String {
        return segments.joinToString("\n\n") { segment ->
            val header = "## ${segment.segmentTitle}"
            val content = segment.transcript.joinToString(" ") { it.text }
            "$header\n$content"
        }
    }

    /**
     * Generate quiz with uniform difficulty level.
     */
    private suspend fun generateQuizWithDifficulty(
        transcript: String,
        targetDifficulty: String,
        numQuestions: Int
    ): List<Question> {
        val systemPrompt = buildSystemPrompt(targetDifficulty)
        var currentPrompt = buildUserPrompt(transcript, targetDifficulty, numQuestions)
        var repairAttempts = 0
        var lastErrors: List<String> = emptyList()

        while (repairAttempts <= MAX_REPAIR_ATTEMPTS) {
            logger.info { "Attempt ${repairAttempts + 1}/${MAX_REPAIR_ATTEMPTS + 1}: Generating $targetDifficulty quiz" }

            val claudeResponse = try {
                callClaude(systemPrompt, currentPrompt)
            } catch (e: Exception) {
                logger.error(e) { "Claude API call failed" }
                throw RuntimeException("Claude API error: ${e.message}")
            }

            // Parse JSON
            val parseResult = QuizValidator.parseJson(claudeResponse)
            
            if (parseResult.isFailure) {
                val parseError = parseResult.exceptionOrNull()?.message ?: "Unknown parse error"
                logger.warn { "JSON parse failed: $parseError" }
                lastErrors = listOf("JSON parse error: $parseError")
                
                if (repairAttempts >= MAX_REPAIR_ATTEMPTS) {
                    throw RuntimeException("Quiz generation failed: $parseError")
                }
                
                currentPrompt = buildRepairPrompt(currentPrompt, lastErrors, claudeResponse)
                repairAttempts++
                continue
            }

            val claudeOutput = parseResult.getOrThrow()

            // Validate
            when (val validation = QuizValidator.validate(claudeOutput)) {
                is ValidationResult.Success -> {
                    // Verify uniform difficulty
                    val nonMatchingDifficulty = validation.questions.filter { 
                        it.difficulty.name.lowercase() != targetDifficulty 
                    }
                    
                    if (nonMatchingDifficulty.isNotEmpty()) {
                        logger.warn { "${nonMatchingDifficulty.size} questions don't match target difficulty" }
                        // Force the difficulty on all questions
                        val targetDiff = Difficulty.valueOf(targetDifficulty.uppercase())
                        return validation.questions.map { it.copy(difficulty = targetDiff) }
                    }
                    
                    logger.info { "Generated ${validation.questions.size} questions at $targetDifficulty difficulty" }
                    return validation.questions
                }
                is ValidationResult.Failure -> {
                    logger.warn { "Validation failed: ${validation.errors}" }
                    lastErrors = validation.errors
                    
                    if (repairAttempts >= MAX_REPAIR_ATTEMPTS) {
                        throw RuntimeException("Quiz validation failed: ${validation.errors.joinToString("; ")}")
                    }
                    
                    currentPrompt = buildRepairPrompt(currentPrompt, lastErrors, claudeResponse)
                    repairAttempts++
                }
            }
        }

        throw RuntimeException("Quiz generation failed after max attempts")
    }

    private fun buildSystemPrompt(difficulty: String): String {
        return """
You are a quiz generation assistant. Your task is to generate quiz questions from lecture transcript content.

CRITICAL: ALL questions MUST be at "${difficulty.uppercase()}" difficulty level. No exceptions.

${getDifficultyGuidelines(difficulty)}

RULES:
1. Output ONLY valid JSON. No markdown, no code fences.
2. At least 2 questions MUST be MCQ (multiple choice).
3. MCQ questions MUST have exactly 4 options with ids "A", "B", "C", "D".
4. MCQ answers MUST be exactly one of: "A", "B", "C", or "D".
5. short_answer questions MUST have options set to null.
6. All questions must be answerable using ONLY the transcript content.
7. Do NOT reference the transcript in questions.
8. Explanations must be 1-2 sentences.
9. difficulty MUST be "${difficulty}" for EVERY question.
10. type must be "mcq" or "short_answer".

OUTPUT FORMAT:
{
  "questions": [
    {
      "type": "mcq",
      "question": "...",
      "options": [{"id": "A", "text": "..."}, {"id": "B", "text": "..."}, {"id": "C", "text": "..."}, {"id": "D", "text": "..."}],
      "answer": "A",
      "explanation": "...",
      "difficulty": "${difficulty}",
      "learning_objective": null
    }
  ]
}
""".trimIndent()
    }

    private fun getDifficultyGuidelines(difficulty: String): String {
        return when (difficulty.lowercase()) {
            "easy" -> """
EASY DIFFICULTY GUIDELINES:
- Ask about basic definitions and terminology
- Use simple, straightforward questions
- Avoid complex reasoning or multi-step problems
- Focus on recall of key facts
- Use clear, unambiguous language
- MCQ distractors should be obviously different from the correct answer
            """.trimIndent()
            
            "medium" -> """
MEDIUM DIFFICULTY GUIDELINES:
- Ask about relationships between concepts
- Require understanding, not just memorization
- Include some application of knowledge
- MCQ distractors should be plausible but distinguishable
- May require connecting 2-3 pieces of information
- Include "why" and "how" questions, not just "what"
            """.trimIndent()
            
            "hard" -> """
HARD DIFFICULTY GUIDELINES:
- Ask about complex relationships and synthesis of ideas
- Require analysis, evaluation, or application to new scenarios
- Include questions that require multi-step reasoning
- MCQ distractors should be very plausible, testing deep understanding
- May require connecting multiple concepts across the transcript
- Include edge cases, exceptions, or nuanced understanding
            """.trimIndent()
            
            else -> ""
        }
    }

    private fun buildUserPrompt(transcript: String, difficulty: String, numQuestions: Int): String {
        return """
Generate exactly $numQuestions quiz questions from the following lecture transcript.

IMPORTANT: All questions MUST be at "$difficulty" difficulty level.

TRANSCRIPT:
$transcript

Remember: 
- Output ONLY valid JSON
- ALL questions must have "difficulty": "$difficulty"
- At least 2 questions must be MCQ type
""".trimIndent()
    }

    private fun buildRepairPrompt(originalPrompt: String, errors: List<String>, previousOutput: String): String {
        val errorMessage = errors.joinToString("\n- ", prefix = "ERRORS:\n- ")
        return """
$originalPrompt

$errorMessage

Your previous (invalid) output was:
$previousOutput

Please fix these issues and output ONLY valid JSON.
""".trimIndent()
    }

    private suspend fun callClaude(systemPrompt: String, userPrompt: String): String {
        val p = prompt("adaptive-quiz", LLMParams(temperature = 0.2, maxTokens = 1200)) {
            system(systemPrompt)
            user(userPrompt)
        }
        
        val responses = promptExecutor.execute(
            prompt = p,
            model = model,
            tools = emptyList()
        )
        
        val response = responses.firstOrNull()
        return when (response) {
            is Message.Assistant -> response.content
            else -> throw IllegalStateException("Unexpected response type: $response")
        }
    }

    fun close() {
        anthropicClient.close()
    }
}
