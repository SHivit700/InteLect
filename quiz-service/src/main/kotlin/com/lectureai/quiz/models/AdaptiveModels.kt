package com.lectureai.quiz.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A single attempt on a previous quiz question.
 */
@Serializable
data class PreviousQuizAttempt(
    @SerialName("question_number")
    val questionNumber: Int,
    @SerialName("question_text")
    val questionText: String,
    @SerialName("user_answer")
    val userAnswer: String,
    @SerialName("correct_answer")
    val correctAnswer: String,
    @SerialName("is_correct")
    val isCorrect: Boolean,
    val feedback: String,
    val explanation: String
)

/**
 * Performance data from the previous segment's quiz.
 */
@Serializable
data class PreviousQuizPerformance(
    @SerialName("segment_number")
    val segmentNumber: Int,
    @SerialName("quiz_difficulty")
    val quizDifficulty: String,  // "easy", "medium", "hard"
    @SerialName("total_questions")
    val totalQuestions: Int,
    @SerialName("correct_count")
    val correctCount: Int,
    val accuracy: Double,  // 0.0 to 1.0
    val attempts: List<PreviousQuizAttempt>
)

/**
 * Request for adaptive quiz generation.
 */
@Serializable
data class AdaptiveQuizRequest(
    @SerialName("video_id")
    val videoId: String,
    @SerialName("segment_id")
    val segmentId: Int,
    val segments: List<TranscriptSegment>,
    @SerialName("num_questions")
    val numQuestions: Int = 3,
    @SerialName("previous_performance")
    val previousPerformance: PreviousQuizPerformance? = null,
    @SerialName("target_difficulty")
    val targetDifficulty: String? = null  // If already determined by Python
)

/**
 * Response from difficulty analysis.
 */
@Serializable
data class DifficultyAnalysisResponse(
    @SerialName("recommended_difficulty")
    val recommendedDifficulty: String,  // "easy", "medium", "hard"
    val reasoning: String
)

/**
 * Response from adaptive quiz generation.
 */
@Serializable
data class AdaptiveQuizResponse(
    @SerialName("quiz_id")
    val quizId: String,
    @SerialName("video_id")
    val videoId: String,
    @SerialName("segment_id")
    val segmentId: Int,
    val difficulty: String,  // The uniform difficulty for all questions
    @SerialName("difficulty_reasoning")
    val difficultyReasoning: String?,
    val questions: List<Question>
)
