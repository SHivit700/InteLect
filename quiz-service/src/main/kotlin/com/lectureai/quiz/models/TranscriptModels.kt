package com.lectureai.quiz.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Structured transcript format with segments and timestamped entries.
 */
@Serializable
data class TranscriptEntry(
    @SerialName("start_timestamp") val startTimestamp: Double,
    @SerialName("end_timestamp") val endTimestamp: Double,
    val text: String
)

@Serializable
data class TranscriptSegment(
    @SerialName("segment_number") val segmentNumber: Int? = null,
    @SerialName("segment_title") val segmentTitle: String,
    @SerialName("segment_start_timestamp") val segmentStartTimestamp: Double,
    @SerialName("segment_end_timestamp") val segmentEndTimestamp: Double,
    val transcript: List<TranscriptEntry>
)

/**
 * Extended quiz request that supports structured transcript format.
 */
@Serializable
data class StructuredQuizRequest(
    val segments: List<TranscriptSegment>,
    @SerialName("quiz_id") val quizId: String? = null,
    @SerialName("target_segment") val targetSegment: String? = null, // Optional: generate quiz for specific segment
    @SerialName("questions_per_segment") val questionsPerSegment: Int? = null // Optional: distribute questions
)

/**
 * Processed transcript data ready for LLM consumption.
 */
data class ProcessedTranscript(
    val segments: List<ProcessedSegment>,
    val fullText: String,
    val keyTopics: List<String>,
    val totalDurationMinutes: Double
)

data class ProcessedSegment(
    val title: String,
    val text: String,
    val durationMinutes: Double,
    val keyPoints: List<String>
)
