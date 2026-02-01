package com.lectureai.quiz

import com.lectureai.quiz.models.ErrorResponse
import com.lectureai.quiz.models.HealthResponse
import com.lectureai.quiz.models.QuizRequest
import com.lectureai.quiz.models.StructuredQuizRequest
import com.lectureai.quiz.models.AnswerValidationRequest
import com.lectureai.quiz.models.AnswerValidationResponse
import com.lectureai.quiz.service.QuizGenerationException
import com.lectureai.quiz.service.QuizService
import com.lectureai.quiz.service.TranscriptProcessor
import com.lectureai.quiz.service.TranscriptValidationException
import com.lectureai.quiz.workflow.AnswerValidationWorkflow
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

private val logger = KotlinLogging.logger {}

/**
 * Application version.
 */
const val APP_VERSION = "1.0.0"

/**
 * Main entry point.
 */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    val host = System.getenv("HOST") ?: "0.0.0.0"
    
    logger.info { "Starting Lecture Quiz Service on $host:$port" }
    
    // Validate API key is present
    val apiKey = System.getenv("ANTHROPIC_API_KEY")
    if (apiKey.isNullOrBlank()) {
        logger.error { "ANTHROPIC_API_KEY environment variable is not set" }
        System.err.println("ERROR: ANTHROPIC_API_KEY environment variable must be set")
        System.exit(1)
    }
    
    embeddedServer(Netty, port = port, host = host) {
        configureApplication(apiKey)
    }.start(wait = true)
}

/**
 * Configure the Ktor application.
 */
fun Application.configureApplication(apiKey: String) {
    val quizService = QuizService(apiKey)
    
    // Install JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
    
    // Install status pages for error handling
    install(StatusPages) {
        exception<TranscriptValidationException> { call, cause ->
            logger.warn { "Validation error: ${cause.message}" }
            
            // Check if it's a "too long" error for 413
            if (cause.message?.contains("too long") == true) {
                call.respond(
                    HttpStatusCode.PayloadTooLarge,
                    ErrorResponse(
                        error = "payload_too_large",
                        message = cause.message ?: "Transcript too long"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "validation_error",
                        message = cause.message ?: "Invalid request"
                    )
                )
            }
        }
        
        exception<QuizGenerationException> { call, cause ->
            logger.error { "Quiz generation failed: ${cause.message}" }
            call.respond(
                HttpStatusCode.BadGateway,
                ErrorResponse(
                    error = "generation_failed",
                    message = "Quiz generation failed. Please try again later."
                )
            )
        }
        
        exception<Throwable> { call, cause ->
            logger.error(cause) { "Unexpected error: ${cause.message}" }
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "internal_error",
                    message = "An unexpected error occurred"
                )
            )
        }
    }
    
    // Configure routing
    routing {
        // Health check endpoint
        get("/health") {
            call.respond(
                HttpStatusCode.OK,
                HealthResponse(
                    status = "healthy",
                    version = APP_VERSION
                )
            )
        }
        
        // Quiz generation endpoint (simple text transcript)
        post("/quiz") {
            logger.info { "Received quiz generation request" }
            
            val request = try {
                call.receive<QuizRequest>()
            } catch (e: Exception) {
                logger.warn { "Failed to parse request: ${e.message}" }
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_request",
                        message = "Invalid JSON request body: ${e.message}"
                    )
                )
                return@post
            }
            
            // Validate the request
            quizService.validateRequest(request)
            
            // Generate the quiz
            val response = quizService.generateQuiz(request)
            
            call.respond(HttpStatusCode.OK, response)
        }
        
        // Quiz generation endpoint (structured transcript with segments)
        post("/quiz/structured") {
            logger.info { "Received structured quiz generation request" }
            
            val request = try {
                call.receive<StructuredQuizRequest>()
            } catch (e: Exception) {
                logger.warn { "Failed to parse structured request: ${e.message}" }
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_request",
                        message = "Invalid JSON request body: ${e.message}"
                    )
                )
                return@post
            }
            
            // Validate segments exist
            if (request.segments.isEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "validation_error",
                        message = "At least one transcript segment is required"
                    )
                )
                return@post
            }
            
            // Process the structured transcript
            val processed = TranscriptProcessor.process(request.segments)
            
            // Build optimized prompt
            val optimizedTranscript = TranscriptProcessor.buildOptimizedPrompt(
                processed,
                request.targetSegment
            )
            
            logger.info { 
                "Processed ${request.segments.size} segments, " +
                "${processed.keyTopics.size} topics, " +
                "total duration: ${String.format("%.1f", processed.totalDurationMinutes)} min" 
            }
            
            // Create a QuizRequest with the optimized transcript
            val quizRequest = QuizRequest(
                transcript = optimizedTranscript,
                quizId = request.quizId,
                sourceWindowMinutes = processed.totalDurationMinutes.toInt()
            )
            
            // Validate and generate
            quizService.validateRequest(quizRequest)
            val response = quizService.generateQuiz(quizRequest)
            
            call.respond(HttpStatusCode.OK, response)
        }
        
        // Answer validation endpoint
        post("/quiz/validate-answer") {
            logger.info { "Received answer validation request" }
            
            val request = try {
                call.receive<AnswerValidationRequest>()
            } catch (e: Exception) {
                logger.warn { "Failed to parse validation request: ${e.message}" }
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        error = "invalid_request",
                        message = "Invalid JSON request body: ${e.message}"
                    )
                )
                return@post
            }
            
            val workflow = AnswerValidationWorkflow(apiKey)
            try {
                val optionMaps = request.options?.map { mapOf("id" to it.id, "text" to it.text) }
                
                val result = workflow.validate(
                    transcript = request.transcript,
                    questionText = request.questionText,
                    questionType = request.questionType,
                    correctAnswer = request.correctAnswer,
                    userAnswer = request.userAnswer,
                    options = optionMaps
                )
                
                call.respond(
                    HttpStatusCode.OK,
                    AnswerValidationResponse(
                        isCorrect = result.isCorrect,
                        feedback = result.feedback
                    )
                )
            } catch (e: Exception) {
                logger.error(e) { "Answer validation failed: ${e.message}" }
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse(
                        error = "validation_failed",
                        message = "Answer validation failed: ${e.message}"
                    )
                )
            } finally {
                workflow.close()
            }
        }
    }
    
    // Cleanup on shutdown
    environment.monitor.subscribe(ApplicationStopped) {
        logger.info { "Application stopping, cleaning up resources..." }
        quizService.close()
    }
}
