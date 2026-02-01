# LectureAI

An AI-powered lecture assistant that transcribes, processes, and generates quizzes from lecture content.

## Project Structure

```
LectureAI/
├── frontend/          # React + Vite web application
├── quiz-service/      # Kotlin microservice for quiz generation
└── transcription/     # Python scripts for transcript processing
```

---

## Quick Start

### Quiz Service (Kotlin)

```bash
cd quiz-service
export ANTHROPIC_API_KEY="your-api-key-here"
./gradlew build
./gradlew run
```

Server runs at **http://localhost:8080**

### Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

### Transcription (Python)

```bash
cd transcription
pip install -r requirements.txt
python process_transcript.py
```

---

## Quiz Service API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET`  | `/health` | Health check |
| `POST` | `/quiz` | Generate quiz from plain text transcript |
| `POST` | `/quiz/structured` | Generate quiz from structured transcript with segments |

---

## API Reference

### GET /health

Health check endpoint.

```bash
curl http://localhost:8080/health
```

**Response:**
```json
{"status": "healthy", "version": "1.0.0"}
```

---

### POST /quiz

Generate a quiz from a plain text transcript.

```bash
curl -X POST http://localhost:8080/quiz \
  -H "Content-Type: application/json" \
  -d '{
    "transcript": "Machine learning is a subset of artificial intelligence...",
    "quiz_id": "lecture-01",
    "source_window_minutes": 15
  }'
```

**Request Body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `transcript` | string | ✅ | The lecture transcript text (max 50,000 chars) |
| `quiz_id` | string | ❌ | Optional identifier for the quiz |
| `source_window_minutes` | integer | ❌ | Optional window duration in minutes |

**Response (200 OK):**
```json
{
  "quiz_id": "lecture-01",
  "source_window_minutes": 15,
  "questions": [
    {
      "type": "mcq",
      "question": "What is machine learning?",
      "options": [
        {"id": "A", "text": "A type of hardware"},
        {"id": "B", "text": "A subset of AI that learns from experience"},
        {"id": "C", "text": "A programming language"},
        {"id": "D", "text": "A database system"}
      ],
      "answer": "B",
      "explanation": "Machine learning is a subset of AI...",
      "difficulty": "easy",
      "learning_objective": "Understand ML definition"
    }
  ]
}
```

---

### POST /quiz/structured

Generate a quiz from a structured transcript with segments and timestamps.

```bash
curl -X POST http://localhost:8080/quiz/structured \
  -H "Content-Type: application/json" \
  -d '{
    "segments": [
      {
        "segment_title": "Introduction to N-Gram Modeling",
        "segment_start_timestamp": 616.88,
        "segment_end_timestamp": 862.88,
        "transcript": [
          {"start_timestamp": 616.88, "end_timestamp": 630.88, "text": "N-gram modeling is the most basic way..."},
          {"start_timestamp": 630.88, "end_timestamp": 651.88, "text": "We predict word likelihood given history..."}
        ]
      },
      {
        "segment_title": "Evaluating with Perplexity",
        "segment_start_timestamp": 1174.88,
        "segment_end_timestamp": 1514.88,
        "transcript": [
          {"start_timestamp": 1174.88, "end_timestamp": 1191.88, "text": "How do we evaluate models? Perplexity..."}
        ]
      }
    ],
    "quiz_id": "nlp-lecture-01",
    "target_segment": null
  }'
```

**Request Body:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `segments` | array | ✅ | Array of transcript segments |
| `segments[].segment_title` | string | ✅ | Title of the segment |
| `segments[].segment_start_timestamp` | number | ✅ | Start time in seconds |
| `segments[].segment_end_timestamp` | number | ✅ | End time in seconds |
| `segments[].transcript` | array | ✅ | Array of transcript entries |
| `segments[].transcript[].start_timestamp` | number | ✅ | Entry start time |
| `segments[].transcript[].end_timestamp` | number | ✅ | Entry end time |
| `segments[].transcript[].text` | string | ✅ | Transcript text |
| `quiz_id` | string | ❌ | Optional quiz identifier |
| `target_segment` | string | ❌ | Generate quiz for specific segment only |

**Response:** Same format as `/quiz`

---

## Error Responses

| Status | Error | Description |
|--------|-------|-------------|
| 400 | `validation_error` | Invalid request (empty transcript, etc.) |
| 413 | `payload_too_large` | Transcript exceeds 50,000 characters |
| 502 | `generation_failed` | Claude API or generation failure |

**Example error:**
```json
{
  "error": "validation_error",
  "message": "Transcript cannot be empty"
}
```

---

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ANTHROPIC_API_KEY` | (required) | Your Anthropic API key |
| `CLAUDE_MODEL` | `haiku3` | Claude model to use |
| `PORT` | `8080` | Server port |
| `HOST` | `0.0.0.0` | Server host |

### Available Models

Set `CLAUDE_MODEL` to one of:

| Value | Model |
|-------|-------|
| `haiku3` | Claude 3 Haiku (default, fastest) |
| `haiku35` | Claude 3.5 Haiku |
| `sonnet35` | Claude 3.5 Sonnet |
| `sonnet37` | Claude 3.7 Sonnet |
| `sonnet4` | Claude 4 Sonnet |
| `opus3` | Claude 3 Opus |

```bash
export CLAUDE_MODEL="sonnet35"
cd quiz-service && ./gradlew run
```

---

## Tech Stack

### Quiz Service
- **Kotlin 2.1.0** - Language
- **Ktor 3.0.3** - HTTP Server (Netty) & Client (CIO)
- **JetBrains Koog 0.6.1** - Agent workflow framework
- **kotlinx.serialization 1.7.3** - JSON serialization
- **Anthropic Claude** - LLM for quiz generation

### Frontend
- **React** + **TypeScript**
- **Vite** - Build tool
- **Tailwind CSS** - Styling

### Transcription
- **Python** - Transcript processing scripts

---

## Quiz Service Structure

```
quiz-service/src/main/kotlin/com/lectureai/quiz/
├── Application.kt              # Ktor entry point & routes
├── models/
│   ├── Models.kt               # Request/Response DTOs
│   ├── ClaudeModels.kt         # Internal Claude models
│   └── TranscriptModels.kt     # Structured transcript models
├── validation/
│   └── QuizValidator.kt        # JSON parsing & validation
├── workflow/
│   ├── QuizWorkflow.kt         # Koog-based generation workflow
│   └── TranscriptProcessor.kt  # Structured transcript processing
└── service/
    └── QuizService.kt          # Business logic layer
```

---

## Features

- ✅ Generate 3-5 quiz questions (MCQ + short answer)
- ✅ Automatic validation with repair loop (max 2 retries)
- ✅ Structured transcript support with segments
- ✅ Key point extraction from segment titles
- ✅ Configurable Claude model
- ✅ Comprehensive error handling

---

## License

MIT License