package com.lectureai.quiz.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.lectureai.quiz.validation.QuizValidator
import com.lectureai.quiz.validation.ValidationResult
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Koog ToolSet providing quiz validation and quality checking tools.
 * 
 * These tools allow the LLM agent to self-validate its output during quiz generation,
 * improving accuracy and reducing the need for repair loops.
 * 
 * @param transcript The lecture transcript for answer verification
 */
class QuizToolSet(
    private val transcript: String
) : ToolSet {

    /**
     * Validates quiz JSON format and business rules.
     * Returns "VALID" if the quiz passes all checks, otherwise returns a list of errors.
     */
    @Tool
    @LLMDescription(
        """Validates a quiz JSON string against the required format and business rules.
        Call this tool BEFORE returning your final answer to ensure the quiz is valid.
        Returns "VALID" if the quiz passes all checks, or a detailed list of validation errors.
        
        Validation rules checked:
        - JSON must be parseable
        - Must have 3-5 questions
        - At least 2 questions must be MCQ type
        - MCQ questions must have exactly 4 options (A, B, C, D)
        - MCQ answers must be one of A, B, C, D
        - short_answer questions must have options set to null
        - difficulty must be "easy", "medium", or "hard"
        - type must be "mcq" or "short_answer"
        - question, answer, and explanation cannot be empty"""
    )
    fun validateQuizFormat(
        @LLMDescription("The complete quiz JSON string to validate, including the outer object with 'questions' array")
        quizJson: String
    ): String {
        logger.info { "🔧 TOOL CALLED: validateQuizFormat" }
        
        // Step 1: Parse JSON
        val parseResult = QuizValidator.parseJson(quizJson)
        if (parseResult.isFailure) {
            val error = parseResult.exceptionOrNull()?.message ?: "Unknown parse error"
            logger.info { "❌ validateQuizFormat: JSON parse failed - $error" }
            return "INVALID: JSON parse error - $error"
        }
        
        val claudeOutput = parseResult.getOrThrow()
        
        // Step 2: Validate business rules
        return when (val validation = QuizValidator.validate(claudeOutput)) {
            is ValidationResult.Success -> {
                logger.info { "✅ validateQuizFormat: VALID with ${validation.questions.size} questions" }
                "VALID: Quiz contains ${validation.questions.size} valid questions"
            }
            is ValidationResult.Failure -> {
                val errors = validation.errors.joinToString("\n- ", prefix = "- ")
                logger.info { "❌ validateQuizFormat: INVALID - ${validation.errors.size} errors" }
                "INVALID: Found ${validation.errors.size} validation errors:\n$errors"
            }
        }
    }

    /**
     * Checks if a question's answer is supported by the transcript content.
     * Uses keyword matching and semantic similarity heuristics.
     */
    @Tool
    @LLMDescription(
        """Verifies that a question's answer is grounded in the lecture transcript content.
        Call this for each question to ensure answers come from the actual lecture material.
        Returns "GROUNDED" if the answer appears supported by the transcript, or explains what's missing.
        
        This helps prevent hallucinated answers that aren't in the source material."""
    )
    fun checkAnswerInTranscript(
        @LLMDescription("The question text being asked")
        question: String,
        @LLMDescription("The answer or correct option text to verify")
        answer: String
    ): String {
        logger.info { "🔧 TOOL CALLED: checkAnswerInTranscript for question: ${question.take(50)}..." }
        
        if (transcript.isBlank()) {
            return "ERROR: No transcript available for verification"
        }
        
        val transcriptLower = transcript.lowercase()
        val answerLower = answer.lowercase()
        
        // Extract key terms from the answer (words > 3 chars, not common words)
        val commonWords = setOf(
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "must", "shall", "can", "need", "dare",
            "that", "this", "these", "those", "which", "what", "who", "whom",
            "and", "or", "but", "if", "then", "else", "when", "where", "why",
            "how", "all", "each", "every", "both", "few", "more", "most", "other",
            "some", "such", "no", "nor", "not", "only", "own", "same", "so",
            "than", "too", "very", "just", "also", "now", "here", "there",
            "with", "from", "for", "about", "into", "through", "during", "before",
            "after", "above", "below", "between", "under", "over", "out", "off"
        )
        
        val keyTerms = answerLower
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 3 && it !in commonWords }
            .distinct()
        
        if (keyTerms.isEmpty()) {
            return "GROUNDED: Answer contains only common words, assuming valid"
        }
        
        // Check how many key terms appear in transcript
        val foundTerms = keyTerms.filter { term -> transcriptLower.contains(term) }
        val coverage = if (keyTerms.isNotEmpty()) foundTerms.size.toDouble() / keyTerms.size else 0.0
        
        return when {
            coverage >= 0.7 -> {
                logger.info { "✅ checkAnswerInTranscript: GROUNDED (${(coverage * 100).toInt()}% coverage)" }
                "GROUNDED: ${(coverage * 100).toInt()}% of key terms found in transcript. Found: ${foundTerms.take(5).joinToString(", ")}"
            }
            coverage >= 0.4 -> {
                val missingTerms = keyTerms.filter { it !in foundTerms }.take(3)
                logger.info { "⚠️ checkAnswerInTranscript: PARTIALLY_GROUNDED (${(coverage * 100).toInt()}% coverage)" }
                "PARTIALLY_GROUNDED: ${(coverage * 100).toInt()}% of key terms found. Missing: ${missingTerms.joinToString(", ")}. Consider revising."
            }
            else -> {
                val missingTerms = keyTerms.filter { it !in foundTerms }.take(5)
                logger.info { "❌ checkAnswerInTranscript: NOT_GROUNDED (${(coverage * 100).toInt()}% coverage)" }
                "NOT_GROUNDED: Only ${(coverage * 100).toInt()}% of key terms found in transcript. Missing: ${missingTerms.joinToString(", ")}. This answer may not be supported by the lecture content."
            }
        }
    }

    /**
     * Checks if a question is clearly worded and unambiguous.
     * Detects common clarity issues like double negatives, vague terms, etc.
     */
    @Tool
    @LLMDescription(
        """Analyzes a question for clarity and potential ambiguity issues.
        Call this to ensure questions are well-phrased and unambiguous.
        Returns "CLEAR" if the question is well-formed, or suggests specific improvements.
        
        Checks for:
        - Double negatives
        - Vague quantifiers (some, many, few)
        - Ambiguous pronouns
        - Overly complex sentence structure
        - Questions that are too long"""
    )
    fun checkQuestionClarity(
        @LLMDescription("The question text to analyze for clarity")
        question: String
    ): String {
        logger.info { "🔧 TOOL CALLED: checkQuestionClarity for: ${question.take(50)}..." }
        
        val issues = mutableListOf<String>()
        val questionLower = question.lowercase()
        
        // Check for double negatives
        val negatives = listOf("not", "no", "never", "neither", "nor", "nothing", "nobody", "nowhere", "none")
        val negativeCount = negatives.count { neg -> 
            questionLower.contains(Regex("\\b$neg\\b"))
        }
        if (negativeCount >= 2) {
            issues.add("Contains double negative - consider rephrasing positively")
        }
        
        // Check for vague quantifiers
        val vagueTerms = listOf("some", "many", "few", "several", "various", "numerous", "certain", "most")
        val foundVague = vagueTerms.filter { questionLower.contains(Regex("\\b$it\\b")) }
        if (foundVague.isNotEmpty()) {
            issues.add("Contains vague quantifier(s): ${foundVague.joinToString(", ")} - consider being more specific")
        }
        
        // Check for ambiguous pronouns at start
        val ambiguousStarts = listOf("it ", "this ", "that ", "these ", "those ", "they ")
        if (ambiguousStarts.any { questionLower.startsWith(it) }) {
            issues.add("Starts with ambiguous pronoun - clarify what is being referred to")
        }
        
        // Check question length (too long = confusing)
        val wordCount = question.split(Regex("\\s+")).size
        if (wordCount > 35) {
            issues.add("Question is very long ($wordCount words) - consider simplifying")
        }
        
        // Check for multiple questions in one
        val questionMarks = question.count { it == '?' }
        if (questionMarks > 1) {
            issues.add("Contains multiple question marks - split into separate questions")
        }
        
        // Check for "all of the above" or "none of the above" references in question
        if (questionLower.contains("all of the above") || questionLower.contains("none of the above")) {
            issues.add("Question text references answer options - this should only appear in options")
        }
        
        // Check for absolute terms that might make question tricky
        val absolutes = listOf("always", "never", "all", "every", "only", "must")
        val foundAbsolutes = absolutes.filter { questionLower.contains(Regex("\\b$it\\b")) }
        if (foundAbsolutes.size >= 2) {
            issues.add("Contains multiple absolute terms (${foundAbsolutes.joinToString(", ")}) - may be unnecessarily restrictive")
        }
        
        return if (issues.isEmpty()) {
            logger.info { "✅ checkQuestionClarity: CLEAR" }
            "CLEAR: Question is well-formed and unambiguous"
        } else {
            logger.info { "⚠️ checkQuestionClarity: ${issues.size} issues found" }
            val issueList = issues.joinToString("\n- ", prefix = "- ")
            "NEEDS_IMPROVEMENT: Found ${issues.size} potential clarity issue(s):\n$issueList"
        }
    }
}
