package com.lectureai.quiz.workflow

import com.lectureai.quiz.models.Question
import com.lectureai.quiz.tools.QuizToolSet
import com.lectureai.quiz.validation.QuizValidator
import com.lectureai.quiz.validation.ValidationResult
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
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
 * Defaults to Haiku_3 which has the widest availability.
 * 
 * Supported values: haiku3, haiku35, sonnet35, sonnet37, sonnet4, opus3
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
 * Result of the quiz generation workflow.
 */
sealed class WorkflowResult {
    data class Success(val questions: List<Question>) : WorkflowResult()
    data class Failure(val message: String) : WorkflowResult()
}

/**
 * The system prompt that instructs Claude to generate quiz questions.
 */
/**
 * Check if tools should be used based on environment variable.
 * Set USE_TOOLS=true to enable tool-assisted generation.
 */
private fun useTools(): Boolean {
    return System.getenv("USE_TOOLS")?.lowercase() == "true"
}

private val SYSTEM_PROMPT = """
You are a quiz generation assistant. Your task is to generate quiz questions from lecture transcript content.

CRITICAL RULES:
1. Output ONLY valid JSON. No markdown, no code fences, no explanatory text.
2. Generate 3 to 5 questions total.
3. At least 2 questions MUST be MCQ (multiple choice).
4. MCQ questions MUST have exactly 4 options with ids "A", "B", "C", "D".
5. MCQ answers MUST be exactly one of: "A", "B", "C", or "D".
6. short_answer questions MUST have options set to null.
7. All questions must be answerable using ONLY the given transcript content.
8. Do NOT reference the transcript in questions (e.g., no "as mentioned in the transcript").
9. Explanations must be 1-2 sentences.
10. difficulty must be exactly one of: "easy", "medium", "hard".
11. type must be exactly one of: "mcq" or "short_answer".

OUTPUT FORMAT (strict JSON, no markdown):
{
  "questions": [
    {
      "type": "mcq",
      "question": "What is...?",
      "options": [
        {"id": "A", "text": "First option"},
        {"id": "B", "text": "Second option"},
        {"id": "C", "text": "Third option"},
        {"id": "D", "text": "Fourth option"}
      ],
      "answer": "A",
      "explanation": "Brief explanation in 1-2 sentences.",
      "difficulty": "medium",
      "learning_objective": "Optional learning objective"
    },
    {
      "type": "short_answer",
      "question": "Explain...?",
      "options": null,
      "answer": "The expected answer text",
      "explanation": "Brief explanation.",
      "difficulty": "hard",
      "learning_objective": null
    }
  ]
}

Remember: Output ONLY the JSON object. No other text.
""".trimIndent()

/**
 * System prompt for tool-enabled mode.
 * Instructs the LLM to use ALL validation tools before finalizing output.
 */
private val SYSTEM_PROMPT_WITH_TOOLS = """
You are a quiz generation assistant with access to validation tools. Your task is to generate quiz questions from lecture transcript content.

AVAILABLE TOOLS (YOU MUST USE ALL OF THEM):
1. validateQuizFormat - Validates your quiz JSON structure and business rules
2. checkAnswerInTranscript - Verifies answers are grounded in the transcript  
3. checkQuestionClarity - Checks if questions are clearly worded

MANDATORY WORKFLOW - Follow these steps in order:

STEP 1: Generate initial quiz JSON with 3-5 questions (at least 2 MCQ)

STEP 2: For EACH question you generated, call checkQuestionClarity with the question text.
        If any question needs improvement, revise it.

STEP 3: For EACH question, call checkAnswerInTranscript with the question and answer.
        If any answer is NOT_GROUNDED, revise it to use transcript content.

STEP 4: Call validateQuizFormat with your complete quiz JSON.
        If validation fails, fix ALL errors and call validateQuizFormat again.

STEP 5: Only after ALL tools report success, output your final JSON.

CRITICAL: You MUST call each tool at least once. Do not skip any validation step.

QUIZ FORMAT RULES:
1. Output ONLY valid JSON. No markdown, no code fences, no explanatory text.
2. Generate 3 to 5 questions total.
3. At least 2 questions MUST be MCQ (multiple choice).
4. MCQ questions MUST have exactly 4 options with ids "A", "B", "C", "D".
5. MCQ answers MUST be exactly one of: "A", "B", "C", or "D".
6. short_answer questions MUST have options set to null.
7. All questions must be answerable using ONLY the given transcript content.
8. Do NOT reference the transcript in questions.
9. difficulty must be exactly one of: "easy", "medium", "hard".
10. type must be exactly one of: "mcq" or "short_answer".

OUTPUT FORMAT:
{
  "questions": [
    {
      "type": "mcq",
      "question": "What is...?",
      "options": [
        {"id": "A", "text": "First option"},
        {"id": "B", "text": "Second option"},
        {"id": "C", "text": "Third option"},
        {"id": "D", "text": "Fourth option"}
      ],
      "answer": "A",
      "explanation": "Brief explanation.",
      "difficulty": "medium",
      "learning_objective": null
    }
  ]
}

REMEMBER: You MUST use checkQuestionClarity, checkAnswerInTranscript, and validateQuizFormat before your final output.
""".trimIndent()

/**
 * Koog-based quiz generation workflow using a graph strategy.
 * Implements: Generate -> Validate -> (Repair loop up to 2 times) -> Success/Failure
 * 
 * The workflow uses Koog's strategy DSL to define a state machine graph:
 * - generateNode: Calls Claude to generate quiz
 * - validateNode: Parses and validates the response
 * - repairNode: Prepares repair prompt if validation fails
 * - successNode/failureNode: Terminal states
 */
class QuizWorkflow(
    private val apiKey: String
) {
    private val anthropicClient = AnthropicLLMClient(apiKey)
    private val promptExecutor = SingleLLMPromptExecutor(anthropicClient)
    
    // Get model from environment variable (defaults to Haiku_3 for widest compatibility)
    private val model: LLModel = getModelFromEnv().also {
        logger.info { "Using Claude model: ${it.id}" }
    }

    companion object {
        private const val MAX_REPAIR_ATTEMPTS = 2
    }

    /**
     * Execute the quiz generation workflow.
     * 
     * If USE_TOOLS=true, uses AIAgent with tool registry for self-validation.
     * Otherwise, uses direct execution with manual repair loop.
     */
    suspend fun execute(transcript: String): WorkflowResult {
        logger.info { "Starting quiz generation workflow for transcript of ${transcript.length} chars" }
        
        return if (useTools()) {
            logger.info { "Tool-enabled mode: Using AIAgent with validation tools" }
            executeWithTools(transcript)
        } else {
            logger.info { "Standard mode: Using direct execution with repair loop" }
            executeWorkflowDirectly(transcript)
        }
    }
    
    /**
     * Execute quiz generation using Koog AIAgent with tools.
     * The agent MUST use all tools to self-validate before returning.
     */
    private suspend fun executeWithTools(transcript: String): WorkflowResult {
        // Create tool set with transcript context
        val quizToolSet = QuizToolSet(transcript)
        
        // Build tool registry from our ToolSet
        val toolRegistry = ToolRegistry {
            tools(quizToolSet.asTools())
        }
        
        logger.info { "Tools mode enabled - registered ${toolRegistry.tools.size} tools: ${toolRegistry.tools.map { it.name }}" }
        
        // Create and run the agent with more iterations to allow for tool usage
        val agent = AIAgent(
            promptExecutor = promptExecutor,
            llmModel = model,
            toolRegistry = toolRegistry,
            systemPrompt = SYSTEM_PROMPT_WITH_TOOLS,
            maxIterations = 25  // Allow enough iterations for: generate + clarity checks + grounding checks + validation
        )
        
        // Use the tool-specific prompt that emphasizes mandatory tool usage
        val userPrompt = buildUserPromptWithTools(transcript)
        
        logger.info { "Starting agent execution with mandatory tool validation..." }
        
        return try {
            val result = agent.run(userPrompt)
            logger.info { "Agent completed. Result length: ${result.length} chars" }
            logger.debug { "Agent result: ${result.take(500)}..." }
            
            // Parse and validate the final result
            val parseResult = QuizValidator.parseJson(result)
            if (parseResult.isFailure) {
                val error = parseResult.exceptionOrNull()?.message ?: "Unknown error"
                return WorkflowResult.Failure("Agent returned invalid JSON: $error")
            }
            
            when (val validation = QuizValidator.validate(parseResult.getOrThrow())) {
                is ValidationResult.Success -> WorkflowResult.Success(validation.questions)
                is ValidationResult.Failure -> WorkflowResult.Failure(
                    "Agent output failed validation: ${validation.errors.joinToString("; ")}"
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "Agent execution failed" }
            WorkflowResult.Failure("Agent error: ${e.message}")
        }
    }

    /**
     * Direct workflow execution using Koog prompt executor.
     * Implements the generate -> validate -> repair loop pattern.
     */
    private suspend fun executeWorkflowDirectly(transcript: String): WorkflowResult {
        var currentPrompt = buildUserPrompt(transcript)
        var repairAttempts = 0
        var lastErrors: List<String> = emptyList()

        while (repairAttempts <= MAX_REPAIR_ATTEMPTS) {
            logger.info { "Attempt ${repairAttempts + 1}/${MAX_REPAIR_ATTEMPTS + 1}: Generating quiz" }

            // Step 1: Call Claude
            val claudeResponse = try {
                callClaude(SYSTEM_PROMPT, currentPrompt)
            } catch (e: Exception) {
                logger.error(e) { "Claude API call failed" }
                return WorkflowResult.Failure("Claude API error: ${e.message}")
            }

            logger.debug { "Claude response: ${claudeResponse.take(300)}..." }

            // Step 2: Parse JSON
            val parseResult = QuizValidator.parseJson(claudeResponse)
            
            if (parseResult.isFailure) {
                val parseError = parseResult.exceptionOrNull()?.message ?: "Unknown parse error"
                logger.warn { "JSON parse failed: $parseError" }
                lastErrors = listOf("JSON parse error: $parseError")
                
                if (repairAttempts >= MAX_REPAIR_ATTEMPTS) {
                    return WorkflowResult.Failure(
                        "Quiz generation failed after ${MAX_REPAIR_ATTEMPTS + 1} attempts. Last error: $parseError"
                    )
                }
                
                // Prepare repair prompt
                currentPrompt = buildRepairPrompt(currentPrompt, lastErrors, claudeResponse)
                repairAttempts++
                continue
            }

            val claudeOutput = parseResult.getOrThrow()

            // Step 3: Validate schema and business rules
            when (val validation = QuizValidator.validate(claudeOutput)) {
                is ValidationResult.Success -> {
                    logger.info { "Validation successful: ${validation.questions.size} questions generated" }
                    return WorkflowResult.Success(validation.questions)
                }
                is ValidationResult.Failure -> {
                    logger.warn { "Validation failed: ${validation.errors}" }
                    lastErrors = validation.errors
                    
                    if (repairAttempts >= MAX_REPAIR_ATTEMPTS) {
                        return WorkflowResult.Failure(
                            "Quiz generation failed after ${MAX_REPAIR_ATTEMPTS + 1} attempts. Validation errors: ${validation.errors.joinToString("; ")}"
                        )
                    }
                    
                    // Prepare repair prompt
                    currentPrompt = buildRepairPrompt(currentPrompt, lastErrors, claudeResponse)
                    repairAttempts++
                }
            }
        }

        return WorkflowResult.Failure("Quiz generation failed after max attempts. Last errors: ${lastErrors.joinToString("; ")}")
    }

    private fun buildUserPrompt(transcript: String): String {
        return """
Generate a quiz from the following lecture transcript. Create 3-5 questions with at least 2 MCQs.

TRANSCRIPT:
$transcript

Remember: Output ONLY valid JSON, no markdown formatting.
""".trimIndent()
    }
    
    /**
     * Build user prompt for tool-enabled mode with explicit tool usage instructions.
     */
    private fun buildUserPromptWithTools(transcript: String): String {
        return """
Generate a quiz from the following lecture transcript.

TRANSCRIPT:
$transcript

IMPORTANT: Before providing your final answer, you MUST:
1. Generate 3-5 questions (at least 2 MCQ)
2. Call checkQuestionClarity for EACH question to verify clarity
3. Call checkAnswerInTranscript for EACH question to verify the answer is in the transcript
4. Call validateQuizFormat with your complete JSON to validate the structure
5. Fix any issues reported by the tools
6. Only then provide your final JSON output

Start by generating your initial quiz, then use the tools to validate each part.
""".trimIndent()
    }

    private fun buildRepairPrompt(originalPrompt: String, errors: List<String>, previousOutput: String): String {
        val errorMessage = QuizValidator.formatErrorsForRepair(errors)
        return """
$originalPrompt

$errorMessage

Your previous (invalid) output was:
$previousOutput

Please fix these issues and output ONLY valid JSON.
""".trimIndent()
    }

    private suspend fun callClaude(systemPrompt: String, userPrompt: String): String {
        val p = prompt("quiz-generation", LLMParams(temperature = 0.2, maxTokens = 900)) {
            system(systemPrompt)
            user(userPrompt)
        }
        
        val responses = promptExecutor.execute(
            prompt = p,
            model = model,
            tools = emptyList()
        )
        
        // Extract text content from response
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
