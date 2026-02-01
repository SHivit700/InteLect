package com.lectureai.quiz.validation

import com.lectureai.quiz.models.ClaudeOption
import com.lectureai.quiz.models.ClaudeQuestion
import com.lectureai.quiz.models.ClaudeQuizOutput
import com.lectureai.quiz.models.Difficulty
import com.lectureai.quiz.models.Option
import com.lectureai.quiz.models.Question
import com.lectureai.quiz.models.QuestionType
import com.lectureai.quiz.models.QuizResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

/**
 * Result of quiz validation.
 */
sealed class ValidationResult {
    data class Success(val questions: List<Question>) : ValidationResult()
    data class Failure(val errors: List<String>) : ValidationResult()
}

/**
 * Strict JSON parser that does not ignore unknown keys.
 */
private val strictJson = Json {
    ignoreUnknownKeys = false
    isLenient = false
    encodeDefaults = true
}

/**
 * Validates and parses Claude's JSON output into validated Question objects.
 */
object QuizValidator {
    
    private val validMcqAnswers = setOf("A", "B", "C", "D")
    private val validOptionIds = listOf("A", "B", "C", "D")
    private val validDifficulties = setOf("easy", "medium", "hard")
    private val validQuestionTypes = setOf("mcq", "short_answer")
    
    /**
     * Parse JSON string into ClaudeQuizOutput.
     * Returns null if parsing fails.
     */
    fun parseJson(jsonString: String): Result<ClaudeQuizOutput> {
        return try {
            val cleaned = cleanJsonString(jsonString)
            val output = strictJson.decodeFromString<ClaudeQuizOutput>(cleaned)
            Result.success(output)
        } catch (e: Exception) {
            logger.warn { "JSON parsing failed: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Clean JSON string by removing any markdown code fences or extra whitespace.
     */
    private fun cleanJsonString(input: String): String {
        var cleaned = input.trim()
        
        // Remove markdown code fences if present
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        
        return cleaned.trim()
    }
    
    /**
     * Validate the parsed quiz output and convert to Question objects.
     */
    fun validate(output: ClaudeQuizOutput): ValidationResult {
        val errors = mutableListOf<String>()
        
        // Rule 1: 3-5 questions total
        if (output.questions.size < 3) {
            errors.add("Must have at least 3 questions, got ${output.questions.size}")
        }
        if (output.questions.size > 5) {
            errors.add("Must have at most 5 questions, got ${output.questions.size}")
        }
        
        // Rule 2: At least 2 MCQs
        val mcqCount = output.questions.count { it.type.lowercase() == "mcq" }
        if (mcqCount < 2) {
            errors.add("Must have at least 2 MCQ questions, got $mcqCount")
        }
        
        // Validate each question
        output.questions.forEachIndexed { index, question ->
            validateQuestion(question, index, errors)
        }
        
        if (errors.isNotEmpty()) {
            return ValidationResult.Failure(errors)
        }
        
        // Convert to validated Question objects
        val questions = output.questions.map { convertToQuestion(it) }
        return ValidationResult.Success(questions)
    }
    
    private fun validateQuestion(question: ClaudeQuestion, index: Int, errors: MutableList<String>) {
        val prefix = "Question ${index + 1}"
        
        // Validate type
        val type = question.type.lowercase()
        if (type !in validQuestionTypes) {
            errors.add("$prefix: Invalid type '$type'. Must be 'mcq' or 'short_answer'")
        }
        
        // Validate difficulty
        val difficulty = question.difficulty.lowercase()
        if (difficulty !in validDifficulties) {
            errors.add("$prefix: Invalid difficulty '$difficulty'. Must be 'easy', 'medium', or 'hard'")
        }
        
        // Validate question text
        if (question.question.isBlank()) {
            errors.add("$prefix: Question text cannot be empty")
        }
        
        // Validate answer
        if (question.answer.isBlank()) {
            errors.add("$prefix: Answer cannot be empty")
        }
        
        // Validate explanation
        if (question.explanation.isBlank()) {
            errors.add("$prefix: Explanation cannot be empty")
        }
        
        // Type-specific validation
        if (type == "mcq") {
            validateMcq(question, prefix, errors)
        } else if (type == "short_answer") {
            validateShortAnswer(question, prefix, errors)
        }
    }
    
    private fun validateMcq(question: ClaudeQuestion, prefix: String, errors: MutableList<String>) {
        val options = question.options
        
        // MCQ must have exactly 4 options
        if (options == null) {
            errors.add("$prefix: MCQ must have options, got null")
            return
        }
        
        if (options.size != 4) {
            errors.add("$prefix: MCQ must have exactly 4 options, got ${options.size}")
        }
        
        // Options must have ids A, B, C, D
        val optionIds = options.map { it.id.uppercase() }
        if (optionIds.toSet() != validOptionIds.toSet()) {
            errors.add("$prefix: MCQ options must have ids A, B, C, D. Got: $optionIds")
        }
        
        // Validate each option has non-empty text
        options.forEach { opt ->
            if (opt.text.isBlank()) {
                errors.add("$prefix: Option ${opt.id} has empty text")
            }
        }
        
        // Answer must be exactly one of A, B, C, D
        val answer = question.answer.uppercase().trim()
        if (answer !in validMcqAnswers) {
            errors.add("$prefix: MCQ answer must be A, B, C, or D. Got: '${question.answer}'")
        }
    }
    
    private fun validateShortAnswer(question: ClaudeQuestion, prefix: String, errors: MutableList<String>) {
        // Short answer must have options = null
        if (question.options != null) {
            errors.add("$prefix: short_answer must have options=null, got options with ${question.options.size} items")
        }
    }
    
    private fun convertToQuestion(claude: ClaudeQuestion): Question {
        val type = when (claude.type.lowercase()) {
            "mcq" -> QuestionType.MCQ
            else -> QuestionType.SHORT_ANSWER
        }
        
        val difficulty = when (claude.difficulty.lowercase()) {
            "easy" -> Difficulty.EASY
            "medium" -> Difficulty.MEDIUM
            else -> Difficulty.HARD
        }
        
        val options = claude.options?.map { opt ->
            Option(id = opt.id.uppercase(), text = opt.text)
        }
        
        val answer = if (type == QuestionType.MCQ) {
            claude.answer.uppercase().trim()
        } else {
            claude.answer.trim()
        }
        
        return Question(
            type = type,
            question = claude.question.trim(),
            options = options,
            answer = answer,
            explanation = claude.explanation.trim(),
            difficulty = difficulty,
            learningObjective = claude.learningObjective?.trim()
        )
    }
    
    /**
     * Generate error message for repair prompt.
     */
    fun formatErrorsForRepair(errors: List<String>): String {
        return buildString {
            appendLine("The previous output had the following validation errors:")
            errors.forEach { error ->
                appendLine("- $error")
            }
            appendLine()
            appendLine("Please fix these issues and return ONLY valid JSON with no markdown formatting.")
        }
    }
}
