from typing import Optional, List
from pydantic import BaseModel


class LectureRequest(BaseModel):
    lecture_url: str
    lecture_title: str
    lecture_topic: str
    video_filename: Optional[str] = None


# Quiz-related models
class QuizOption(BaseModel):
    id: str
    text: str


class QuizQuestion(BaseModel):
    type: str  # "mcq" or "short_answer"
    question: str
    options: Optional[List[QuizOption]] = None
    answer: str
    explanation: str
    difficulty: str  # "easy", "medium", "hard"
    learning_objective: Optional[str] = None
    question_number: Optional[int] = None  # Unique question number within quiz


class QuizDocument(BaseModel):
    quiz_id: str
    video_id: str
    segment_id: int
    source_window_minutes: int
    questions: List[QuizQuestion]
    created_at: str


class QuizListResponse(BaseModel):
    video_id: str
    segment_id: Optional[int] = None
    quizzes: List[QuizDocument]
    total_quizzes: int

class JobStatusResponse(BaseModel):
    job_id: str
    status: str
    created_at: str
    completed_at: Optional[str] = None
    failed_at: Optional[str] = None
    error: Optional[str] = None

class SegmentQuestionsRequest(BaseModel):
    video_id: str
    segment_id: int
    num_questions: int = 3

class VideoQuestionsRequest(BaseModel):
    video_id: str
    num_questions: int = 5

class SegmentSolutionRequest(BaseModel):
    video_id: str
    segment_id: int
    question: str
    user_answer: str


# Direct segment data for testing
class TranscriptEntryData(BaseModel):
    start_timestamp: float
    end_timestamp: float
    text: str


class SegmentData(BaseModel):
    segment_number: int
    segment_title: str
    segment_start_timestamp: float
    segment_end_timestamp: float
    transcript: List[TranscriptEntryData]


class DirectSegmentQuestionsRequest(BaseModel):
    """Request for generating questions directly from segment data (for testing)."""
    video_id: str
    segment: SegmentData


# Answer submission models
class AnswerSubmission(BaseModel):
    """Request for submitting an answer to a quiz question."""
    video_id: str
    quiz_id: str
    question_number: int
    user_answer: str


class AnswerValidationResponse(BaseModel):
    """Response from answer validation."""
    video_id: str
    quiz_id: str
    question_number: int
    is_correct: bool
    user_answer: str
    correct_answer: str
    feedback: str
    explanation: str