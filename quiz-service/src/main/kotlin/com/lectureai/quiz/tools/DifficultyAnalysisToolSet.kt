package com.lectureai.quiz.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.lectureai.quiz.models.PreviousQuizPerformance
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Koog ToolSet for analyzing previous quiz performance and determining
 * the appropriate difficulty level for the next quiz.
 * 
 * Difficulty rules:
 * - First segment (no previous performance): Always "easy"
 * - Accuracy >= 80%: Increase difficulty (easy→medium, medium→hard, hard stays hard)
 * - Accuracy 50-79%: Keep same difficulty
 * - Accuracy < 50%: Decrease difficulty (hard→medium, medium→easy, easy stays easy)
 * 
 * @param previousPerformance Performance data from the previous segment's quiz (null for first segment)
 */
class DifficultyAnalysisToolSet(
    private val previousPerformance: PreviousQuizPerformance?
) : ToolSet {

    /**
     * Analyzes the previous quiz performance and returns performance metrics.
     */
    @Tool
    @LLMDescription(
        """Retrieves the performance metrics from the previous segment's quiz.
        Returns the previous quiz difficulty, accuracy percentage, total questions,
        correct count, and a summary of incorrect answers with their feedback.
        
        If this is the first segment (no previous quiz), returns "NO_PREVIOUS_QUIZ".
        
        Use this information to determine the appropriate difficulty for the next quiz."""
    )
    fun getPreviousPerformance(): String {
        logger.info { "🔧 TOOL CALLED: getPreviousPerformance" }
        
        if (previousPerformance == null) {
            logger.info { "No previous performance data - this is the first segment" }
            return "NO_PREVIOUS_QUIZ: This is the first segment, so there is no previous quiz performance."
        }
        
        val incorrectAttempts = previousPerformance.attempts.filter { !it.isCorrect }
        val incorrectSummary = if (incorrectAttempts.isEmpty()) {
            "All questions were answered correctly."
        } else {
            incorrectAttempts.joinToString("\n") { attempt ->
                """
                - Question ${attempt.questionNumber}: "${attempt.questionText}"
                  User answered: "${attempt.userAnswer}" (Correct: "${attempt.correctAnswer}")
                  Feedback: ${attempt.feedback}
                """.trimIndent()
            }
        }
        
        val result = """
PREVIOUS QUIZ PERFORMANCE:
- Segment Number: ${previousPerformance.segmentNumber}
- Quiz Difficulty: ${previousPerformance.quizDifficulty.uppercase()}
- Total Questions: ${previousPerformance.totalQuestions}
- Correct Answers: ${previousPerformance.correctCount}
- Accuracy: ${String.format("%.1f", previousPerformance.accuracy * 100)}%

INCORRECT ANSWERS:
$incorrectSummary
        """.trimIndent()
        
        logger.info { "Previous performance: ${previousPerformance.accuracy * 100}% accuracy on ${previousPerformance.quizDifficulty} quiz" }
        return result
    }

    /**
     * Determines the recommended difficulty for the next quiz based on rules.
     */
    @Tool
    @LLMDescription(
        """Calculates the recommended difficulty level for the next quiz based on:
        - Previous quiz difficulty
        - User's accuracy on the previous quiz
        
        Difficulty adjustment rules:
        - Accuracy >= 80%: Increase difficulty (easy→medium, medium→hard)
        - Accuracy 50-79%: Keep same difficulty
        - Accuracy < 50%: Decrease difficulty (hard→medium, medium→easy)
        - First segment (no previous): Always "easy"
        
        Returns the recommended difficulty: "easy", "medium", or "hard"."""
    )
    fun calculateTargetDifficulty(): String {
        logger.info { "🔧 TOOL CALLED: calculateTargetDifficulty" }
        
        if (previousPerformance == null) {
            logger.info { "First segment - recommending EASY difficulty" }
            return """
RECOMMENDED_DIFFICULTY: easy
REASONING: This is the first segment of the video, so we start with easy questions to build the student's confidence.
            """.trimIndent()
        }
        
        val previousDifficulty = previousPerformance.quizDifficulty.lowercase()
        val accuracy = previousPerformance.accuracy
        
        val newDifficulty = when {
            accuracy >= 0.80 -> {
                // Increase difficulty
                when (previousDifficulty) {
                    "easy" -> "medium"
                    "medium" -> "hard"
                    else -> "hard"  // Already hard, stay hard
                }
            }
            accuracy >= 0.50 -> {
                // Keep same difficulty
                previousDifficulty
            }
            else -> {
                // Decrease difficulty
                when (previousDifficulty) {
                    "hard" -> "medium"
                    "medium" -> "easy"
                    else -> "easy"  // Already easy, stay easy
                }
            }
        }
        
        val reasoning = when {
            accuracy >= 0.80 -> "Student achieved ${String.format("%.0f", accuracy * 100)}% accuracy, demonstrating strong understanding. Increasing difficulty to challenge them."
            accuracy >= 0.50 -> "Student achieved ${String.format("%.0f", accuracy * 100)}% accuracy, showing moderate understanding. Maintaining current difficulty level."
            else -> "Student achieved ${String.format("%.0f", accuracy * 100)}% accuracy, indicating some concepts need reinforcement. Decreasing difficulty to build confidence."
        }
        
        logger.info { "Calculated difficulty: $newDifficulty (from $previousDifficulty with ${accuracy * 100}% accuracy)" }
        
        return """
RECOMMENDED_DIFFICULTY: $newDifficulty
PREVIOUS_DIFFICULTY: $previousDifficulty
ACCURACY: ${String.format("%.1f", accuracy * 100)}%
REASONING: $reasoning
        """.trimIndent()
    }

    /**
     * Validates that a quiz has uniform difficulty.
     */
    @Tool
    @LLMDescription(
        """Validates that all questions in a quiz JSON have the same difficulty level.
        The quiz must have a uniform difficulty - all questions must be at the same level.
        
        Returns "VALID: All questions at [difficulty] difficulty" if uniform,
        or "INVALID: Mixed difficulties found" with details if not uniform."""
    )
    fun validateUniformDifficulty(
        @LLMDescription("The complete quiz JSON string to validate for uniform difficulty")
        quizJson: String,
        @LLMDescription("The expected difficulty level: 'easy', 'medium', or 'hard'")
        expectedDifficulty: String
    ): String {
        logger.info { "🔧 TOOL CALLED: validateUniformDifficulty for $expectedDifficulty" }
        
        // Simple pattern matching to find difficulties in the JSON
        val difficultyPattern = """"difficulty"\s*:\s*"(\w+)"""".toRegex()
        val matches = difficultyPattern.findAll(quizJson).map { it.groupValues[1].lowercase() }.toList()
        
        if (matches.isEmpty()) {
            return "INVALID: No difficulty fields found in the quiz JSON"
        }
        
        val uniqueDifficulties = matches.toSet()
        val expectedLower = expectedDifficulty.lowercase()
        
        return when {
            uniqueDifficulties.size > 1 -> {
                "INVALID: Mixed difficulties found: ${uniqueDifficulties.joinToString(", ")}. All questions must be at '$expectedLower' difficulty."
            }
            uniqueDifficulties.first() != expectedLower -> {
                "INVALID: Questions are at '${uniqueDifficulties.first()}' difficulty but should be at '$expectedLower' difficulty."
            }
            else -> {
                "VALID: All ${matches.size} questions are at '$expectedLower' difficulty."
            }
        }
    }
}
