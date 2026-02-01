package com.lectureai.quiz.service

import com.lectureai.quiz.models.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Processes structured transcripts to extract key information for quiz generation.
 * 
 * This processor:
 * 1. Combines transcript entries into coherent text
 * 2. Extracts key topics from segment titles
 * 3. Identifies important concepts and terminology
 * 4. Formats the content optimally for LLM consumption
 */
object TranscriptProcessor {

    /**
     * Process a structured transcript into a format optimized for quiz generation.
     */
    fun process(segments: List<TranscriptSegment>): ProcessedTranscript {
        logger.info { "Processing ${segments.size} transcript segments" }
        
        val processedSegments = segments.map { segment ->
            processSegment(segment)
        }
        
        val fullText = buildFullText(processedSegments)
        val keyTopics = extractKeyTopics(segments)
        val totalDuration = segments.maxOfOrNull { it.segmentEndTimestamp }?.let { end ->
            segments.minOfOrNull { it.segmentStartTimestamp }?.let { start ->
                (end - start) / 60.0
            }
        } ?: 0.0
        
        return ProcessedTranscript(
            segments = processedSegments,
            fullText = fullText,
            keyTopics = keyTopics,
            totalDurationMinutes = totalDuration
        )
    }

    /**
     * Process a single segment, combining entries and extracting key points.
     */
    private fun processSegment(segment: TranscriptSegment): ProcessedSegment {
        // Combine all transcript entries into coherent text
        val combinedText = segment.transcript
            .map { it.text.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
        
        // Calculate duration
        val duration = (segment.segmentEndTimestamp - segment.segmentStartTimestamp) / 60.0
        
        // Extract key points from the segment
        val keyPoints = extractKeyPoints(combinedText)
        
        return ProcessedSegment(
            title = segment.segmentTitle,
            text = combinedText,
            durationMinutes = duration,
            keyPoints = keyPoints
        )
    }

    /**
     * Extract key topics from segment titles.
     */
    private fun extractKeyTopics(segments: List<TranscriptSegment>): List<String> {
        return segments.map { it.segmentTitle }
    }

    /**
     * Extract key points from text using simple heuristics.
     * Looks for:
     * - Definitions (X is Y, X means Y)
     * - Key terms (capitalized words, technical terms)
     * - Important statements (must, always, never, important, key)
     */
    private fun extractKeyPoints(text: String): List<String> {
        val keyPoints = mutableListOf<String>()
        
        // Split into sentences
        val sentences = text.split(Regex("[.!?]"))
            .map { it.trim() }
            .filter { it.length > 20 }
        
        // Find definition-like sentences
        val definitionPatterns = listOf(
            Regex("\\b(is|are|means|refers to|defined as)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(basically|essentially|fundamentally)\\b", RegexOption.IGNORE_CASE)
        )
        
        // Find emphasis sentences
        val emphasisPatterns = listOf(
            Regex("\\b(important|key|critical|crucial|must|always|never|remember)\\b", RegexOption.IGNORE_CASE),
            Regex("\\b(the main|the key|the important|the crucial)\\b", RegexOption.IGNORE_CASE)
        )
        
        for (sentence in sentences) {
            // Check for definitions
            if (definitionPatterns.any { it.containsMatchIn(sentence) }) {
                keyPoints.add(sentence.take(150))
            }
            // Check for emphasis
            else if (emphasisPatterns.any { it.containsMatchIn(sentence) }) {
                keyPoints.add(sentence.take(150))
            }
        }
        
        return keyPoints.take(5) // Limit to top 5 key points per segment
    }

    /**
     * Build a full text representation with segment headers.
     */
    private fun buildFullText(segments: List<ProcessedSegment>): String {
        return segments.joinToString("\n\n") { segment ->
            "## ${segment.title}\n${segment.text}"
        }
    }

    /**
     * Build an optimized prompt for the LLM based on processed transcript.
     * This creates a structured summary that helps the LLM generate better questions.
     */
    fun buildOptimizedPrompt(processed: ProcessedTranscript, targetSegment: String? = null): String {
        val sb = StringBuilder()
        
        sb.appendLine("Generate a quiz from the following lecture content.")
        sb.appendLine()
        
        // Add topic overview
        sb.appendLine("LECTURE TOPICS:")
        processed.keyTopics.forEachIndexed { index, topic ->
            sb.appendLine("${index + 1}. $topic")
        }
        sb.appendLine()
        
        // Add content - either specific segment or all
        if (targetSegment != null) {
            val segment = processed.segments.find { 
                it.title.equals(targetSegment, ignoreCase = true) 
            }
            if (segment != null) {
                sb.appendLine("FOCUS SEGMENT: ${segment.title}")
                sb.appendLine()
                sb.appendLine("CONTENT:")
                sb.appendLine(segment.text)
                
                if (segment.keyPoints.isNotEmpty()) {
                    sb.appendLine()
                    sb.appendLine("KEY POINTS TO COVER:")
                    segment.keyPoints.forEach { point ->
                        sb.appendLine("- $point")
                    }
                }
            }
        } else {
            sb.appendLine("LECTURE CONTENT:")
            sb.appendLine()
            
            for (segment in processed.segments) {
                sb.appendLine("### ${segment.title}")
                sb.appendLine(segment.text)
                sb.appendLine()
            }
        }
        
        sb.appendLine()
        sb.appendLine("Create 3-5 questions covering the main concepts. Include at least 2 MCQ questions.")
        sb.appendLine("Questions should test understanding, not just memorization.")
        sb.appendLine()
        sb.appendLine("Remember: Output ONLY valid JSON, no markdown formatting.")
        
        return sb.toString()
    }

    /**
     * Convert plain text transcript to a simple segment structure.
     * Used for backward compatibility with simple text input.
     */
    fun fromPlainText(text: String, title: String = "Lecture Content"): ProcessedTranscript {
        val segment = ProcessedSegment(
            title = title,
            text = text,
            durationMinutes = 0.0,
            keyPoints = extractKeyPoints(text)
        )
        
        return ProcessedTranscript(
            segments = listOf(segment),
            fullText = text,
            keyTopics = listOf(title),
            totalDurationMinutes = 0.0
        )
    }
}
