package com.lectureai.quiz.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request payload for quiz generation endpoint.
 */
@Serializable
data class QuizRequest(
    @SerialName("quiz_id")
    val quizId: String? = null,
    @SerialName("source_window_minutes")
    val sourceWindowMinutes: Int? = null,
    val transcript: String
)

/**
 * Response payload for quiz generation endpoint.
 */
@Serializable
data class QuizResponse(
    @SerialName("quiz_id")
    val quizId: String,
    @SerialName("source_window_minutes")
    val sourceWindowMinutes: Int,
    val questions: List<Question>
)

/**
 * Represents a single quiz question.
 */
@Serializable
data class Question(
    val type: QuestionType,
    val question: String,
    val options: List<Option>? = null,
    val answer: String,
    val explanation: String,
    val difficulty: Difficulty,
    @SerialName("learning_objective")
    val learningObjective: String? = null
)

/**
 * MCQ option with id and text.
 */
@Serializable
data class Option(
    val id: String,
    val text: String
)

/**
 * Question type enum.
 */
@Serializable
enum class QuestionType {
    @SerialName("mcq")
    MCQ,
    @SerialName("short_answer")
    SHORT_ANSWER
}

/**
 * Difficulty level enum.
 */
@Serializable
enum class Difficulty {
    @SerialName("easy")
    EASY,
    @SerialName("medium")
    MEDIUM,
    @SerialName("hard")
    HARD
}

/**
 * Error response payload.
 */
@Serializable
data class ErrorResponse(
    val error: String,
    val message: String
)

/**
 * Health check response.
 */
@Serializable
data class HealthResponse(
    val status: String,
    val version: String
)

/**
 * Request for answer validation.
 */
@Serializable
data class AnswerValidationRequest(
    val transcript: String,
    @SerialName("question_text")
    val questionText: String,
    @SerialName("question_type")
    val questionType: String,
    @SerialName("correct_answer")
    val correctAnswer: String,
    @SerialName("user_answer")
    val userAnswer: String,
    val options: List<Option>? = null
)

/**
 * Response from answer validation.
 */
@Serializable
data class AnswerValidationResponse(
    @SerialName("is_correct")
    val isCorrect: Boolean,
    val feedback: String
)
