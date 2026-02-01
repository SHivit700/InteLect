package com.lectureai.quiz.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Koog ToolSet for validating user answers to quiz questions.
 * 
 * Provides AI-powered answer validation with semantic matching for short answers
 * and direct comparison for MCQ questions.
 */
class AnswerValidationToolSet(
    private val transcript: String,
    private val questionText: String,
    private val questionType: String,
    private val correctAnswer: String,
    private val options: List<Map<String, String>>? = null
) : ToolSet {

    @Tool
    @LLMDescription(
        """Checks if a user's MCQ answer matches the correct answer.
        Use this for multiple choice questions where the answer is A, B, C, or D.
        Returns CORRECT or INCORRECT with the correct answer."""
    )
    fun checkMCQAnswer(
        @LLMDescription("The user's answer (should be A, B, C, or D)")
        userAnswer: String
    ): String {
        logger.info { "🔧 TOOL CALLED: checkMCQAnswer for answer: $userAnswer" }
        
        val normalizedUser = userAnswer.trim().uppercase()
        val normalizedCorrect = correctAnswer.trim().uppercase()
        
        return if (normalizedUser == normalizedCorrect) {
            logger.info { "✅ checkMCQAnswer: CORRECT" }
            "CORRECT: The answer $normalizedCorrect is correct."
        } else {
            val correctOptionText = options?.find { it["id"] == normalizedCorrect }?.get("text") ?: ""
            logger.info { "❌ checkMCQAnswer: INCORRECT (expected $normalizedCorrect)" }
            "INCORRECT: The correct answer is $normalizedCorrect: $correctOptionText"
        }
    }

    @Tool
    @LLMDescription(
        """Retrieves the expected answer and relevant context for YOU to semantically evaluate the user's short answer.
        DO NOT rely on exact keyword matching - use your semantic understanding to judge if the user's answer 
        conveys the same meaning/concepts as the expected answer, even if worded differently.
        
        After calling this tool, YOU should determine if the answer is:
        - CORRECT: User's answer conveys the same key concepts (even with different wording, synonyms, or partial phrasing)
        - PARTIALLY_CORRECT: User shows understanding but is missing important concepts
        - INCORRECT: User's answer is fundamentally wrong or completely off-topic
        
        Be LENIENT - if the user demonstrates understanding of the core concept, mark it CORRECT."""
    )
    fun getAnswerContext(
        @LLMDescription("The user's short answer text")
        userAnswer: String
    ): String {
        logger.info { "🔧 TOOL CALLED: getAnswerContext for: ${userAnswer.take(50)}..." }
        
        if (userAnswer.isBlank()) {
            return """
                |USER_ANSWER: (empty - no answer provided)
                |EXPECTED_ANSWER: $correctAnswer
                |QUESTION: $questionText
                |VERDICT: INCORRECT (no answer given)
            """.trimMargin()
        }
        
        // Return context for the AI to make a semantic judgment
        return """
            |=== SEMANTIC EVALUATION REQUIRED ===
            |
            |USER_ANSWER: $userAnswer
            |
            |EXPECTED_ANSWER: $correctAnswer
            |
            |QUESTION: $questionText
            |
            |RELEVANT_CONTEXT: ${transcript.take(500)}
            |
            |=== YOUR TASK ===
            |Compare the USER_ANSWER to the EXPECTED_ANSWER semantically.
            |- Accept synonyms and equivalent phrasings (e.g., "ML" = "machine learning")
            |- Accept partial answers if they capture the core concept
            |- Accept different word order if meaning is preserved
            |- Only mark INCORRECT if the answer is fundamentally wrong
            |
            |Provide your verdict: CORRECT, PARTIALLY_CORRECT, or INCORRECT
        """.trimMargin()
    }

    @Tool
    @LLMDescription(
        """Generates personalized feedback for the user based on their answer.
        Provides encouraging and educational feedback to help them learn.
        Use this after determining if the answer is correct or incorrect."""
    )
    fun generateFeedback(
        @LLMDescription("Whether the answer was correct: CORRECT, PARTIALLY_CORRECT, or INCORRECT")
        result: String,
        @LLMDescription("The user's original answer")
        userAnswer: String
    ): String {
        logger.info { "🔧 TOOL CALLED: generateFeedback for result: $result" }
        
        val feedback = when {
            result.startsWith("CORRECT") -> {
                "Great job! You demonstrated a solid understanding of this concept."
            }
            result.startsWith("PARTIALLY") -> {
                "You're on the right track! Review the explanation to strengthen your understanding of the missing concepts."
            }
            else -> {
                "This is a learning opportunity! Review the explanation carefully and try to understand why the correct answer applies here."
            }
        }
        
        logger.info { "✅ generateFeedback: Generated feedback" }
        return "FEEDBACK: $feedback"
    }
}
