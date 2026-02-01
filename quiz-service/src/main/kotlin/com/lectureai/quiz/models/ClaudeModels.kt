package com.lectureai.quiz.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Internal model for Claude's raw JSON output (questions array only).
 * This is used for strict parsing before conversion to QuizResponse.
 */
@Serializable
data class ClaudeQuizOutput(
    val questions: List<ClaudeQuestion>
)

/**
 * Internal question model from Claude's output.
 */
@Serializable
data class ClaudeQuestion(
    val type: String,
    val question: String,
    val options: List<ClaudeOption>? = null,
    val answer: String,
    val explanation: String,
    val difficulty: String,
    @SerialName("learning_objective")
    val learningObjective: String? = null
)

/**
 * Internal option model from Claude's output.
 */
@Serializable
data class ClaudeOption(
    val id: String,
    val text: String
)
