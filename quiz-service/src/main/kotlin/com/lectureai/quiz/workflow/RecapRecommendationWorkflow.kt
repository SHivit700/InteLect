package com.lectureai.quiz.workflow

import com.lectureai.quiz.models.RecapRecommendationRequest
import com.lectureai.quiz.models.RecapRecommendationResponse
import com.lectureai.quiz.models.SegmentRecommendation
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.anthropic.AnthropicLLMClient
import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import ai.koog.prompt.message.Message
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.double

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
        else -> AnthropicModels.Haiku_3
    }
}

/**
 * Workflow for generating segment recommendations based on quiz performance.
 */
class RecapRecommendationWorkflow(private val apiKey: String) {
    
    private val anthropicClient = AnthropicLLMClient(apiKey)
    private val promptExecutor = SingleLLMPromptExecutor(anthropicClient)
    private val model: LLModel = getModelFromEnv()
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    companion object {
        private val SYSTEM_PROMPT = """
You are an educational assistant that analyzes quiz results and recommends lecture segments for students to review.

Given:
1. A list of lecture segments with their titles, timestamps, and key topics
2. Quiz answers showing which questions were answered correctly or incorrectly

Your task:
- Analyze the incorrectly answered questions
- Match each incorrect answer to the most relevant segment(s) based on the question content and segment topics
- Prioritize segments that cover foundational concepts needed for understanding other material
- Provide clear, encouraging reasons for why each segment should be reviewed

OUTPUT FORMAT (strict JSON, no markdown):
{
  "recommendations": [
    {
      "segment_number": 1,
      "segment_title": "Introduction to Topic",
      "start_timestamp": 0.0,
      "end_timestamp": 300.0,
      "reason": "This segment covers the fundamental concepts needed to understand question 3.",
      "priority": 1
    }
  ],
  "summary": "Brief encouraging summary of what the student should focus on."
}

RULES:
1. Output ONLY valid JSON. No markdown, no code fences.
2. Only recommend segments related to incorrectly answered questions.
3. If all answers are correct, return empty recommendations with a congratulatory summary.
4. Priority 1 = most important, higher numbers = less urgent.
5. Keep reasons concise and encouraging (1-2 sentences).
6. Do not recommend more than 5 segments.
""".trimIndent()
    }
    
    init {
        logger.info { "RecapRecommendationWorkflow initialized with model: $model" }
    }
    
    /**
     * Generate segment recommendations based on quiz performance.
     */
    suspend fun execute(request: RecapRecommendationRequest): RecapRecommendationResponse {
        val incorrectAnswers = request.answers.filter { !it.isCorrect }
        val totalQuestions = request.answers.size
        
        // If all answers are correct, return congratulations
        if (incorrectAnswers.isEmpty()) {
            return RecapRecommendationResponse(
                videoId = request.videoId,
                recommendations = emptyList(),
                summary = "Excellent work! You answered all questions correctly. No review needed.",
                totalIncorrect = 0,
                totalQuestions = totalQuestions
            )
        }
        
        // Build the prompt
        val userPrompt = buildUserPrompt(request.segments, request.answers)
        
        logger.info { "Generating recommendations for ${incorrectAnswers.size} incorrect answers out of $totalQuestions" }
        
        // Call Claude
        val responseText = callClaude(SYSTEM_PROMPT, userPrompt)
        
        // Parse the response
        return parseResponse(request.videoId, responseText, incorrectAnswers.size, totalQuestions)
    }
    
    private fun buildUserPrompt(
        segments: List<com.lectureai.quiz.models.SegmentInfo>,
        answers: List<com.lectureai.quiz.models.QuizAnswerResult>
    ): String {
        val sb = StringBuilder()
        
        sb.appendLine("LECTURE SEGMENTS:")
        segments.forEach { segment ->
            sb.appendLine("- Segment ${segment.segmentNumber}: \"${segment.segmentTitle}\"")
            sb.appendLine("  Time: ${formatTime(segment.startTimestamp)} - ${formatTime(segment.endTimestamp)}")
            if (segment.keyTopics.isNotEmpty()) {
                sb.appendLine("  Topics: ${segment.keyTopics.joinToString(", ")}")
            }
        }
        
        sb.appendLine()
        sb.appendLine("QUIZ RESULTS:")
        answers.forEach { answer ->
            val status = if (answer.isCorrect) "✓ CORRECT" else "✗ INCORRECT"
            sb.appendLine("Question ${answer.questionNumber}: $status")
            sb.appendLine("  Q: ${answer.questionText}")
            if (!answer.isCorrect) {
                sb.appendLine("  User answered: ${answer.userAnswer}")
                sb.appendLine("  Correct answer: ${answer.correctAnswer}")
            }
        }
        
        sb.appendLine()
        sb.appendLine("Based on the incorrect answers, recommend which segments the student should review.")
        
        return sb.toString()
    }
    
    private fun formatTime(seconds: Double): String {
        val mins = (seconds / 60).toInt()
        val secs = (seconds % 60).toInt()
        return String.format("%d:%02d", mins, secs)
    }
    
    private suspend fun callClaude(systemPrompt: String, userPrompt: String): String {
        val p = prompt("recap-recommendations", LLMParams(temperature = 0.3, maxTokens = 1000)) {
            system(systemPrompt)
            user(userPrompt)
        }
        
        val responses = promptExecutor.execute(
            prompt = p,
            model = model,
            tools = emptyList()
        )
        
        return when (val response = responses.firstOrNull()) {
            is Message.Assistant -> response.content
            else -> throw IllegalStateException("Unexpected response type from Claude")
        }
    }
    
    private fun parseResponse(
        videoId: String,
        responseText: String,
        totalIncorrect: Int,
        totalQuestions: Int
    ): RecapRecommendationResponse {
        try {
            // Clean up the response (remove any markdown if present)
            val cleanJson = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()
            
            val jsonElement = json.parseToJsonElement(cleanJson)
            val obj = jsonElement.jsonObject
            
            val recommendations = obj["recommendations"]?.jsonArray?.map { rec ->
                val recObj = rec.jsonObject
                SegmentRecommendation(
                    segmentNumber = recObj["segment_number"]?.jsonPrimitive?.int ?: 0,
                    segmentTitle = recObj["segment_title"]?.jsonPrimitive?.content ?: "",
                    startTimestamp = recObj["start_timestamp"]?.jsonPrimitive?.double ?: 0.0,
                    endTimestamp = recObj["end_timestamp"]?.jsonPrimitive?.double ?: 0.0,
                    reason = recObj["reason"]?.jsonPrimitive?.content ?: "",
                    priority = recObj["priority"]?.jsonPrimitive?.int ?: 1
                )
            } ?: emptyList()
            
            val summary = obj["summary"]?.jsonPrimitive?.content 
                ?: "Review the recommended segments to strengthen your understanding."
            
            return RecapRecommendationResponse(
                videoId = videoId,
                recommendations = recommendations.sortedBy { it.priority },
                summary = summary,
                totalIncorrect = totalIncorrect,
                totalQuestions = totalQuestions
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to parse recommendations response: $responseText" }
            
            // Return a fallback response
            return RecapRecommendationResponse(
                videoId = videoId,
                recommendations = emptyList(),
                summary = "Unable to generate specific recommendations. Please review the material for questions you answered incorrectly.",
                totalIncorrect = totalIncorrect,
                totalQuestions = totalQuestions
            )
        }
    }
    
    fun close() {
        // Cleanup if needed
    }
}
