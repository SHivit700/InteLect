package com.lectureai.quiz.service

import com.lectureai.quiz.models.QuizRequest
import com.lectureai.quiz.models.QuizResponse
import com.lectureai.quiz.workflow.QuizWorkflow
import com.lectureai.quiz.workflow.WorkflowResult
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Exception thrown when quiz generation fails.
 */
class QuizGenerationException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when transcript validation fails.
 */
class TranscriptValidationException(message: String) : Exception(message)

/**
 * Service layer for quiz generation.
 */
class QuizService(
    private val apiKey: String
) {
    companion object {
        const val MAX_TRANSCRIPT_LENGTH = 50_000 // ~12,500 tokens at 4 chars/token
        const val MIN_TRANSCRIPT_LENGTH = 50
    }
    
    private val workflow = QuizWorkflow(apiKey)
    
    /**
     * Validate the quiz request.
     * @throws TranscriptValidationException if validation fails
     */
    fun validateRequest(request: QuizRequest) {
        // Check for empty transcript
        if (request.transcript.isBlank()) {
            throw TranscriptValidationException("Transcript cannot be empty")
        }
        
        // Check minimum length
        if (request.transcript.length < MIN_TRANSCRIPT_LENGTH) {
            throw TranscriptValidationException(
                "Transcript too short. Minimum length is $MIN_TRANSCRIPT_LENGTH characters."
            )
        }
        
        // Check maximum length (for 413 response)
        if (request.transcript.length > MAX_TRANSCRIPT_LENGTH) {
            throw TranscriptValidationException(
                "Transcript too long. Maximum length is $MAX_TRANSCRIPT_LENGTH characters. " +
                "Received ${request.transcript.length} characters."
            )
        }
        
        // Validate quiz_id if provided
        if (request.quizId != null && request.quizId.isBlank()) {
            throw TranscriptValidationException("quiz_id cannot be empty if provided")
        }
        
        // Validate source_window_minutes if provided
        if (request.sourceWindowMinutes != null && request.sourceWindowMinutes <= 0) {
            throw TranscriptValidationException("source_window_minutes must be positive if provided")
        }
    }
    
    /**
     * Check if the transcript is too long (for 413 response).
     */
    fun isTranscriptTooLong(request: QuizRequest): Boolean {
        return request.transcript.length > MAX_TRANSCRIPT_LENGTH
    }
    
    /**
     * Generate a quiz from the transcript.
     * @throws QuizGenerationException if generation fails
     */
    suspend fun generateQuiz(request: QuizRequest): QuizResponse {
        logger.info { "Generating quiz for quiz_id=${request.quizId ?: "auto-generated"}" }
        
        val result = workflow.execute(request.transcript)
        
        return when (result) {
            is WorkflowResult.Success -> {
                logger.info { "Quiz generated successfully with ${result.questions.size} questions" }
                QuizResponse(
                    quizId = request.quizId ?: java.util.UUID.randomUUID().toString(),
                    sourceWindowMinutes = request.sourceWindowMinutes ?: 0,
                    questions = result.questions
                )
            }
            is WorkflowResult.Failure -> {
                logger.error { "Quiz generation failed: ${result.message}" }
                throw QuizGenerationException(result.message)
            }
        }
    }
    
    /**
     * Clean up resources.
     */
    fun close() {
        workflow.close()
    }
}
