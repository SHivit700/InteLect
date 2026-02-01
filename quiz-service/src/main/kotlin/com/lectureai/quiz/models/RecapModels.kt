package com.lectureai.quiz.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request for generating a recap quiz from entire lecture.
 */
@Serializable
data class RecapQuizRequest(
    @SerialName("video_id") val videoId: String,
    val segments: List<TranscriptSegment>,
    @SerialName("num_questions") val numQuestions: Int = 10
)

/**
 * Request for getting segment recommendations based on quiz answers.
 */
@Serializable
data class RecapRecommendationRequest(
    @SerialName("video_id") val videoId: String,
    val segments: List<SegmentInfo>,
    val answers: List<QuizAnswerResult>
)

/**
 * Simplified segment info for recommendations (without full transcript).
 */
@Serializable
data class SegmentInfo(
    @SerialName("segment_number") val segmentNumber: Int,
    @SerialName("segment_title") val segmentTitle: String,
    @SerialName("start_timestamp") val startTimestamp: Double,
    @SerialName("end_timestamp") val endTimestamp: Double,
    @SerialName("key_topics") val keyTopics: List<String> = emptyList()
)

/**
 * Result of a quiz answer - whether correct or incorrect.
 */
@Serializable
data class QuizAnswerResult(
    @SerialName("question_number") val questionNumber: Int,
    @SerialName("question_text") val questionText: String,
    @SerialName("user_answer") val userAnswer: String,
    @SerialName("correct_answer") val correctAnswer: String,
    @SerialName("is_correct") val isCorrect: Boolean,
    @SerialName("source_segment") val sourceSegment: Int? = null
)

/**
 * A segment recommendation for the student to re-watch.
 */
@Serializable
data class SegmentRecommendation(
    @SerialName("segment_number") val segmentNumber: Int,
    @SerialName("segment_title") val segmentTitle: String,
    @SerialName("start_timestamp") val startTimestamp: Double,
    @SerialName("end_timestamp") val endTimestamp: Double,
    val reason: String,
    val priority: Int = 1  // 1 = highest priority
)

/**
 * Response containing segment recommendations.
 */
@Serializable
data class RecapRecommendationResponse(
    @SerialName("video_id") val videoId: String,
    val recommendations: List<SegmentRecommendation>,
    val summary: String,
    @SerialName("total_incorrect") val totalIncorrect: Int,
    @SerialName("total_questions") val totalQuestions: Int
)
