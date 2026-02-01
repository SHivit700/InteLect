# 1. API Server to send a url to the transcription pipeline, the video title and video topic 
# Extension: the video topic -> we use segments based on multiple video topics
# Extension: Checking for focus
# receives the segments back (its timestamps), title , the transcript timestamped (JSON array of objects timestamp range to transcript)

# Send segments as context (segments up to segment to be watched), segment to be watched, then a series of questions with text answers based on all the prerequisite segments
# Should be received to understand the segment to be watched

import asyncio
import uuid
from datetime import datetime
from typing import Dict, Any, Optional, List

import requests
from fastapi import FastAPI, HTTPException, Query, UploadFile, File
from pydantic import BaseModel

import firebase_admin
from firebase_admin import credentials, firestore
from data_models import LectureRequest, JobStatusResponse, SegmentQuestionsRequest, VideoQuestionsRequest, SegmentSolutionRequest, QuizDocument, QuizListResponse, DirectSegmentQuestionsRequest, AnswerSubmission, AnswerValidationResponse
import json
import os

# Try to import full_pipeline - may not be available in all environments
try:
    from full_pipeline import get_data
except ImportError:
    print("Warning: full_pipeline not available. Transcription endpoints will not work.")
    get_data = None
# Initialize FastAPI app
app = FastAPI(title="LectureAI API", description="API for lecture transcription and analysis", version="1.0.0")

from fastapi.middleware.cors import CORSMiddleware

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allow all origins for local development
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Firebase setup
cred = credentials.Certificate("ic_hack.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# Job tracking storage
jobs: Dict[str, Dict[str, Any]] = {}

QUESTION_GENERATION_ENDPOINT = "http://localhost:8080/quiz"

async def download_and_save_video(job_id: str, video_url: str) -> Optional[str]:
    """Download video from Panopto and save it locally."""
    try:
        import yt_dlp
        
        # Generate unique filename
        video_filename = f"{job_id}.mp4"
        video_dir = os.path.join(os.path.dirname(__file__), '..', 'FrontEnd', 'public', 'videos')
        os.makedirs(video_dir, exist_ok=True)
        
        output_path = os.path.join(video_dir, video_filename)
        base_no_ext = os.path.splitext(output_path)[0]
        
        ydl_opts = {
            "format": "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]/best",
            "cookiesfrombrowser": ("chrome",),
            "outtmpl": base_no_ext,
            "merge_output_format": "mp4",
            "noplaylist": True,
            "quiet": False,
        }
        
        loop = asyncio.get_running_loop()
        await loop.run_in_executor(None, lambda: yt_dlp.YoutubeDL(ydl_opts).download([video_url]))
        
        # Check if file exists
        if os.path.exists(output_path):
            print(f"Video downloaded successfully: {video_filename}")
            return video_filename
        else:
            print(f"Video file not found after download: {output_path}")
            return None
            
    except Exception as e:
        print(f"Failed to download video: {e}")
        return None



async def lecture_processing_task(job_id: str, lecture_url: str):
    """Run the synchronous pipeline in a thread pool and then process results."""
    # Ensure get_data is available
    if get_data is None:
        jobs[job_id]['status'] = 'failed'
        jobs[job_id]['error'] = 'Transcription pipeline not available'
        return

    try:
        loop = asyncio.get_running_loop()
        
        # Download video first
        print(f"[{job_id}] Starting video download...")
        video_filename = await download_and_save_video(job_id, lecture_url)
        
        # Update Firebase with video filename
        if video_filename:
            print(f"[{job_id}] Updating Firebase with video filename: {video_filename}")
            db.collection('videos').document(job_id).update({
                'video_filename': video_filename
            })
        
        # Run blocking pipeline in executor
        print(f"[{job_id}] Starting transcription pipeline (get_data)...")
        await loop.run_in_executor(None, get_data, lecture_url, f"{job_id}_chapters.json")
        
        # Process results (also blocking, run in executor)
        print(f"[{job_id}] Processing results (process_lecture_job)...")
        await loop.run_in_executor(None, process_lecture_job, job_id)
        print(f"[{job_id}] Job processing complete.")
    except Exception as e:
        print(f"Background task failed for job {job_id}: {e}")
        import traceback
        traceback.print_exc()
        jobs[job_id]['status'] = 'failed'
        jobs[job_id]['error'] = str(e)
        jobs[job_id]['failed_at'] = datetime.now().isoformat()

@app.post("/submit-lecture", response_model=dict)
async def submit_lecture_url(request: LectureRequest):
    """Submit a lecture URL for processing and return a job ID for polling."""
    print(f"Received submission for: {request.lecture_url}")
    job_id = str(uuid.uuid4())
    
    # Initialize job status
    jobs[job_id] = {
        'status': 'pending',
        'created_at': datetime.now().isoformat(),
        'lecture_url': request.lecture_url,
        'lecture_title': request.lecture_title,
        'lecture_topic': request.lecture_topic,
        'segments': None,
        'error': None
    }
    
    try:
        # Create the main document
        video_data = {
            'lecture_url': request.lecture_url,
            'lecture_title': request.lecture_title,
            'lecture_topic': request.lecture_topic,
            'segments_collection': f'videos/{job_id}/segments',  # Reference to subcollection
            'segment_count': 0,
            'status': 'pending',
            'created_at': datetime.now().isoformat()
        }
        
        # Add video filename if provided
        if request.video_filename:
            video_data['video_filename'] = request.video_filename
        
        db.collection('videos').document(job_id).set(video_data)
        
        # Start processing asynchronously (task handles its own exceptions)
        asyncio.create_task(lecture_processing_task(job_id, request.lecture_url))
        
        return {"job_id": job_id, "status": "submitted"}
        
    except Exception as e:
        print(f"Error submitting lecture: {e}")
        raise HTTPException(status_code=500, detail=f"Failed to submit lecture: {str(e)}")

def process_lecture_job(job_id: str) -> None:
    """Process the lecture transcription job (sync - runs in thread pool)."""
    try:
        jobs[job_id]['status'] = 'processing'
        
        # Get job details
        job = jobs[job_id]
        
        # Read the processed data from the JSON file created by get_data
        json_file_path = f"{job_id}_chapters.json"

        
        if not os.path.exists(json_file_path):
            raise FileNotFoundError(f"Processed data file not found: {json_file_path}")
        
        with open(json_file_path, 'r', encoding='utf-8') as f:
            transcription_result = json.load(f)
        
        # Store each segment as a separate document in subcollection
        segments_ref = db.collection('videos').document(job_id).collection('segments')
        

        segments = transcription_result if isinstance(transcription_result, list) else transcription_result.get('segments', [])
        
        for segment in segments:
            segments_ref.document(str(segment.get("segment_number"))).set(segment)
        
        # Update main document with metadata
        db.collection('videos').document(job_id).update({
            'status': 'completed',
            'segment_count': len(segments),
            'processed_at': datetime.now().isoformat()
        })
        
        # Update job with results
        jobs[job_id]['status'] = 'completed'
        jobs[job_id]['completed_at'] = datetime.now().isoformat()
        
        # Clean up the temporary JSON file
        try:
            os.remove(json_file_path)
        except OSError:
            pass  # File cleanup failed, but job succeeded
    except Exception as e:
        jobs[job_id]['status'] = 'failed'
        jobs[job_id]['error'] = str(e)
        jobs[job_id]['failed_at'] = datetime.now().isoformat()
        
        # Clean up the temporary JSON file even on failure
        json_file_path = f"{job_id}_chapters.json"
        try:
            if os.path.exists(json_file_path):
                os.remove(json_file_path)
        except OSError:
            pass  # File cleanup failed


@app.get("/job-status/{job_id}")
async def get_job_status_endpoint(job_id: str):
    """Get the status of a job by its ID."""
    if job_id not in jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    job = jobs[job_id]
    result = {
        'job_id': job_id,
        'status': job['status'],
        'created_at': job['created_at']
    }
    
    if job['status'] == 'completed':
        result['status'] = 'completed'
    elif job['status'] == 'failed':
        result['error'] = job['error']
        result['failed_at'] = job['failed_at']
    
    return result

@app.delete("/job/{job_id}")
async def delete_job_endpoint(job_id: str):
    """Delete a job from storage."""
    if job_id not in jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    del jobs[job_id]
    return {"message": "Job deleted successfully"}

@app.get("/video/{video_id}/segments")
async def get_video_segments_endpoint(
    video_id: str, 
    start_segment: Optional[int] = Query(None, description="Start segment ID"),
    end_segment: Optional[int] = Query(None, description="End segment ID")
):
    """Get segments for a video from the subcollection."""
    try:
        segments_ref = db.collection('videos').document(video_id).collection('segments')
        
        if start_segment is not None and end_segment is not None:
            # Get a range of segments
            segments_query = segments_ref.where('segment_number', '>=', start_segment).where('segment_number', '<=', end_segment).order_by('segment_number')
        else:
            # Get all segments ordered by segment_number
            segments_query = segments_ref.order_by('segment_number')
        
        segments = []
        for segment_doc in segments_query.stream():
            segment_data = segment_doc.to_dict()
            segments.append({
                'segment_id': segment_doc.id,
                "segment_title": segment_data.get("segment_title", ""),
                "segment_start_timestamp": segment_data.get("segment_start_timestamp", 0.0),
                "segment_end_timestamp": segment_data.get("segment_end_timestamp", 0.0)
            })
        
        return {
            'video_id': video_id,
            'segments': segments,
            'segment_count': len(segments)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f'Failed to retrieve segments: {str(e)}')

@app.get("/video/{video_id}/metadata")
async def get_video_metadata_endpoint(video_id: str):
    """Get video metadata without segments."""
    try:
        video_doc = db.collection('videos').document(video_id).get()
        if video_doc.exists:
            video_data = video_doc.to_dict()
            return {
                
                'lecture_url': video_data.get('lecture_url', ''),
                
                'lecture_title': video_data.get('lecture_title', ''),
                
                'lecture_topic': video_data.get('lecture_topic', ''),
            
                'video_filename': video_doc.get('video_filename', '')
            }
        else:
            raise HTTPException(status_code=404, detail='Video not found')
    except Exception as e:
        raise HTTPException(status_code=500, detail=f'Failed to retrieve video: {str(e)}')

@app.get("/video/{video_id}/segment-at-time")
async def get_current_lecture_segment_endpoint(video_id: str, timestamp: float = Query(..., description="Timestamp in seconds")):
    """Retrieve the current lecture segment based on video ID and timestamp."""
    try:
        # Get segments from subcollection
        segments_ref = db.collection('videos').document(video_id).collection('segments')
        segments = segments_ref.stream()
        
        for segment_doc in segments:
            segment = segment_doc.to_dict()

            if segment.get('segment_start_timestamp', 0) <= timestamp <= segment.get('segment_end_timestamp', 0):
                return {
                    "segment_id": segment_doc.id,
                    "segment_title": segment.get("segment_title", ""),
                    "segment_start_timestamp": segment.get("segment_start_timestamp", 0.0),
                    "segment_end_timestamp": segment.get("segment_end_timestamp", 0.0)
                }
        
        raise HTTPException(status_code=404, detail="No segment found at the specified timestamp")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving segment: {str(e)}")

@app.post("/video/segment-questions")
def generate_segment_questions_endpoint(request: SegmentQuestionsRequest):
    """Generate questions for a specific segment of a video and store in Firestore."""
    try:
        # Get specific segment from subcollection
        segment_ref = db.collection('videos').document(request.video_id).collection('segments').document(str(request.segment_id))
        segment_doc = segment_ref.get()
        
        if segment_doc.exists:
            segment = segment_doc.to_dict()

            # Construct clean segment object for Kotlin API
            clean_segment = {
                "segment_title": segment.get("segment_title", "Untitled Segment"),
                "segment_start_timestamp": segment.get("segment_start_timestamp", 0.0),
                "segment_end_timestamp": segment.get("segment_end_timestamp", 0.0),
                "transcript": segment.get("transcript", [])
            }

            # Make the API call to the quiz generation service
            payload = {
                "segments": [clean_segment],
                "questions_per_segment": request.num_questions
            }
            response = requests.post(
                "http://localhost:8080/quiz/structured",
                json=payload,
                headers={"Content-Type": "application/json"}
            )
            
            if response.status_code == 200:
                quiz_data = response.json()
                
                # Store the quiz in Firestore
                quiz_id = quiz_data.get('quiz_id')
                
                # Add unique question numbers to each question
                questions = quiz_data.get('questions', [])
                for idx, question in enumerate(questions, start=1):
                    question['question_number'] = idx
                
                quiz_doc = {
                    'quiz_id': quiz_id,
                    'video_id': request.video_id,
                    'segment_id': request.segment_id,
                    'source_window_minutes': quiz_data.get('source_window_minutes', 0),
                    'questions': questions,
                    'created_at': datetime.now().isoformat()
                }
                
                # Store in subcollection: videos/{video_id}/quizzes/{quiz_id}
                quiz_ref = db.collection('videos').document(request.video_id) \
                            .collection('quizzes').document(quiz_id)
                quiz_ref.set(quiz_doc)
                
                return {
                    "video_id": request.video_id,
                    "segment_id": request.segment_id,
                    "quiz_id": quiz_id,
                    "quiz_data": quiz_data,
                    "stored": True
                }
            else:
                raise HTTPException(status_code=500, detail=f"Quiz generation failed: {response.text}")
        
        raise HTTPException(status_code=404, detail="Segment not found")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating questions: {str(e)}")


@app.post("/video/segment-questions-direct")
async def generate_segment_questions_direct(request: DirectSegmentQuestionsRequest):
    """Generate questions directly from segment data (for testing without Firestore lookup)."""
    try:
        # Build segment in the format expected by quiz-service
        segment = {
            "segment_number": request.segment.segment_number,
            "segment_title": request.segment.segment_title,
            "segment_start_timestamp": request.segment.segment_start_timestamp,
            "segment_end_timestamp": request.segment.segment_end_timestamp,
            "transcript": [
                {
                    "start_timestamp": entry.start_timestamp,
                    "end_timestamp": entry.end_timestamp,
                    "text": entry.text
                }
                for entry in request.segment.transcript
            ]
        }
        
        # Make the API call to the quiz generation service
        response = requests.post(
            "http://localhost:8080/quiz/structured",
            json={"segments": [segment]},
            headers={"Content-Type": "application/json"}
        )
        
        if response.status_code == 200:
            quiz_data = response.json()
            
            # Store the quiz in Firestore
            quiz_id = quiz_data.get('quiz_id')
            segment_id = request.segment.segment_number
            
            # Add unique question numbers to each question
            questions = quiz_data.get('questions', [])
            for idx, question in enumerate(questions, start=1):
                question['question_number'] = idx
            
            quiz_doc = {
                'quiz_id': quiz_id,
                'video_id': request.video_id,
                'segment_id': segment_id,
                'source_window_minutes': quiz_data.get('source_window_minutes', 0),
                'questions': questions,
                'created_at': datetime.now().isoformat()
            }
            
            # Store in subcollection: videos/{video_id}/quizzes/{quiz_id}
            quiz_ref = db.collection('videos').document(request.video_id) \
                        .collection('quizzes').document(quiz_id)
            quiz_ref.set(quiz_doc)
            
            return {
                "video_id": request.video_id,
                "segment_id": segment_id,
                "quiz_id": quiz_id,
                "quiz_data": quiz_data,
                "stored": True
            }
        else:
            raise HTTPException(status_code=500, detail=f"Quiz generation failed: {response.text}")
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Quiz service unavailable. Make sure quiz-service is running on port 8080.")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating questions: {str(e)}")


# @app.post("/video/segment-solution")
# async def get_segment_question_solution_endpoint(request: SegmentSolutionRequest):
#     """Retrieve the solution for a specific question related to a segment."""
#     try:
#         # Get specific segment from subcollection
#         segment_ref = db.collection('videos').document(request.video_id).collection('segments').document(str(request.segment_id))
#         segment_doc = segment_ref.get()
        
#         if segment_doc.exists:
#             segment = segment_doc.to_dict()
#             # Placeholder for solution retrieval logic based on segment text
#             solutions = {
#                 f"What is the main topic of segment {request.segment_id}?": f"Based on the transcript: '{segment.get('text', '')[:100]}...'",
#                 f"Can you summarize the key points from segment {request.segment_id}?": f"Key points from: '{segment.get('text', '')[:100]}...'"
#             }
            
#             solution = "MOCKED"
            
#             return {
#                 "video_id": request.video_id,
#                 "segment_id": request.segment_id,
#                 "question": request.question,
#                 "solution": solution,
#             }
        
#         raise HTTPException(status_code=404, detail="Segment not found")
#     except Exception as e:
#         raise HTTPException(status_code=500, detail=f"Error retrieving solution: {str(e)}")

@app.post("/video/video-questions")
async def generate_video_questions_endpoint(request: VideoQuestionsRequest):
    """Generate questions for the entire video."""
    try:
        # Get video metadata
        video_doc = db.collection('videos').document(request.video_id).get()
        if not video_doc.exists:
            raise HTTPException(status_code=404, detail="Video not found")
            
        video_data = video_doc.to_dict()
        
        # Get all segments from subcollection
        segments_ref = db.collection('videos').document(request.video_id).collection('segments')
        segments = [segment.to_dict() for segment in segments_ref.stream()]
        
        # Placeholder for question generation logic, based on video and segments
        questions = [
            f"What is the main topic of the lecture '{video_data.get('lecture_title', '')}'?",
            f"Can you summarize the key points from the video on {video_data.get('lecture_topic', '')}?",
            f"What are the main concepts covered across all {len(segments)} segments?",
            f"How would you explain the overall structure of this lecture?",
            f"What are the most important takeaways from this lecture?"
        ]
        
        return {
            "video_id": request.video_id,
            "video_title": video_data.get('lecture_title', ''),
            "video_topic": video_data.get('lecture_topic', ''),
            "questions": questions[:request.num_questions],
            "total_segments": len(segments)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error generating video questions: {str(e)}")


@app.get("/video/feed")
def get_video_feed_endpoint():
    """Retrieve a feed of all processed videos with metadata."""
    try:
        videos_ref = db.collection('videos')
        videos = []
        for video_doc in videos_ref.stream():
            video_data = video_doc.to_dict()
            videos.append({
                'video_id': video_doc.id,
                'lecture_title': video_data.get('lecture_title', ''),
                'lecture_topic': video_data.get('lecture_topic', ''),
                'segment_count': video_data.get('segment_count', 0),
                'status': video_data.get('status', ''),
                'created_at': video_data.get('created_at', '')
            })
        
        return {
            'videos': videos,
            'total_videos': len(videos)
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving video feed: {str(e)}")


def get_next_segment_id(video_id: str, timestamp: float) -> Optional[int]:
    """Helper function to get the next segment ID after a given timestamp."""
    try:
        segments_ref = db.collection('videos').document(video_id).collection('segments')
        segments_query = segments_ref.where('segment_start_timestamp', '>', timestamp).order_by('segment_start_timestamp').limit(1)
        
        for segment_doc in segments_query.stream():
            segment = segment_doc.to_dict()
            return segment.get('segment_number')
        
        return None  # No next segment found
    except Exception as e:
        print(f"Error retrieving next segment: {str(e)}")
        return None


@app.get("/video/{video_id}/segment/{segment_id}/quizzes")
async def get_segment_quizzes_endpoint(video_id: str, segment_id: int):
    """Retrieve all quizzes stored for a specific segment."""
    try:
        # Query quizzes collection filtered by segment_id
        quizzes_ref = db.collection('videos').document(video_id) \
                       .collection('quizzes') \
                       .where('segment_id', '==', segment_id)
        
        quizzes = []
        for quiz_doc in quizzes_ref.stream():
            quiz_data = quiz_doc.to_dict()
            quizzes.append(quiz_data)
        
        # Sort by created_at descending
        quizzes.sort(key=lambda x: x.get('created_at', ''), reverse=True)
        
        return {
            'video_id': video_id,
            'segment_id': segment_id,
            'quizzes': quizzes,
            'total_quizzes': len(quizzes)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving quizzes: {str(e)}")


@app.get("/video/{video_id}/quizzes")
async def get_video_quizzes_endpoint(video_id: str):
    """Retrieve all quizzes stored for a video."""
    try:
        # Get all quizzes from the video's quizzes subcollection
        quizzes_ref = db.collection('videos').document(video_id).collection('quizzes')
        
        all_quizzes = []
        for quiz_doc in quizzes_ref.order_by('created_at', direction=firestore.Query.DESCENDING).stream():
            quiz_data = quiz_doc.to_dict()
            all_quizzes.append(quiz_data)
        
        return {
            'video_id': video_id,
            'quizzes': all_quizzes,
            'total_quizzes': len(all_quizzes)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving quizzes: {str(e)}")


@app.get("/video/{video_id}/quiz/{quiz_id}")
async def get_quiz_by_id_endpoint(video_id: str, quiz_id: str):
    """Retrieve a specific quiz by its ID."""
    try:
        quiz_ref = db.collection('videos').document(video_id) \
                    .collection('quizzes').document(quiz_id)
        
        quiz_doc = quiz_ref.get()
        
        if quiz_doc.exists:
            return quiz_doc.to_dict()
        else:
            raise HTTPException(status_code=404, detail="Quiz not found")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error retrieving quiz: {str(e)}")






@app.post("/upload-video")
async def upload_video_endpoint(file: UploadFile = File(...)):
    """Upload a video file and return the filename."""
    try:
        # Generate unique filename
        file_extension = os.path.splitext(file.filename)[1] if file.filename else '.mp4'
        unique_filename = f"{uuid.uuid4()}{file_extension}"
        
        # Save to FrontEnd/public/videos directory
        video_dir = os.path.join(os.path.dirname(__file__), '..', 'FrontEnd', 'public', 'videos')
        os.makedirs(video_dir, exist_ok=True)
        
        file_path = os.path.join(video_dir, unique_filename)
        
        # Write file in chunks
        with open(file_path, 'wb') as f:
            while chunk := await file.read(1024 * 1024):  # Read 1MB at a time
                f.write(chunk)
        
        return {
            "filename": unique_filename,
            "original_filename": file.filename,
            "size": os.path.getsize(file_path)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to upload video: {str(e)}")

@app.get("/")
async def root():
    """Root endpoint with API information."""
    return {
        "message": "LectureAI API",
        "version": "1.0.0",
        "endpoints": {
            "submit_lecture": "POST /submit-lecture",
            "upload_video": "POST /upload-video",
            "job_status": "GET /job-status/{job_id}",
            "delete_job": "DELETE /job/{job_id}",
            "video_segments": "GET /video/{video_id}/segments",
            "video_metadata": "GET /video/{video_id}/metadata",
            "segment_at_time": "GET /video/{video_id}/segment-at-time",
            "segment_questions": "POST /video/segment-questions",
            "segment_solution": "POST /video/segment-solution",
            "video_questions": "POST /video/video-questions",
            "segment_quizzes": "GET /video/{video_id}/segment/{segment_id}/quizzes",
            "video_quizzes": "GET /video/{video_id}/quizzes",
            "get_quiz": "GET /video/{video_id}/quiz/{quiz_id}",
            "submit_answer": "POST /video/submit-answer"
        }
    }


@app.post("/video/submit-answer", response_model=AnswerValidationResponse)
async def submit_answer(request: AnswerSubmission):
    """Submit a user's answer and get AI-powered validation with feedback."""
    try:
        # Get the quiz from Firestore
        quiz_ref = db.collection('videos').document(request.video_id) \
                    .collection('quizzes').document(request.quiz_id)
        quiz_doc = quiz_ref.get()
        
        if not quiz_doc.exists:
            raise HTTPException(status_code=404, detail="Quiz not found")
        
        quiz_data = quiz_doc.to_dict()
        questions = quiz_data.get('questions', [])
        
        # Find the question by question_number
        question = None
        for q in questions:
            if q.get('question_number') == request.question_number:
                question = q
                break
        
        if not question:
            raise HTTPException(status_code=404, detail=f"Question {request.question_number} not found")
        
        # Get transcript context from segment if available
        segment_id = quiz_data.get('segment_id')
        transcript = ""
        if segment_id:
            segment_ref = db.collection('videos').document(request.video_id) \
                          .collection('segments').document(str(segment_id))
            segment_doc = segment_ref.get()
            if segment_doc.exists:
                segment_data = segment_doc.to_dict()
                transcript_entries = segment_data.get('transcript', [])
                transcript = " ".join([t.get('text', '') for t in transcript_entries])
        
        # Call the Kotlin validation service
        validation_payload = {
            "transcript": transcript,
            "question_text": question.get('question', ''),
            "question_type": question.get('type', 'mcq'),
            "correct_answer": question.get('answer', ''),
            "user_answer": request.user_answer,
            "options": question.get('options')
        }
        
        response = requests.post(
            "http://localhost:8080/quiz/validate-answer",
            json=validation_payload,
            headers={"Content-Type": "application/json"},
            timeout=30
        )
        
        if response.status_code == 200:
            result = response.json()
            
            return {
                "video_id": request.video_id,
                "quiz_id": request.quiz_id,
                "question_number": request.question_number,
                "is_correct": result.get('is_correct', False),
                "user_answer": request.user_answer,
                "correct_answer": question.get('answer', ''),
                "feedback": result.get('feedback', ''),
                "explanation": question.get('explanation', '')
            }
        else:
            # Fallback to lenient comparison
            correct_answer = question.get('answer', '')
            question_type = question.get('type', 'mcq')
            
            if question_type == 'mcq':
                is_correct = request.user_answer.strip().upper() == correct_answer.strip().upper()
            else:
                # Lenient short answer comparison - check for word overlap
                user_words = set(w.lower() for w in request.user_answer.split() if len(w) > 2)
                correct_words = set(w.lower() for w in correct_answer.split() if len(w) > 2)
                overlap = user_words & correct_words
                # Check for substring matches or stem matches
                has_overlap = len(overlap) > 0
                has_substring = request.user_answer.lower()[:15] in correct_answer.lower() or correct_answer.lower()[:15] in request.user_answer.lower()
                has_stem_match = any(uw in cw or cw in uw for uw in user_words for cw in correct_words)
                is_correct = has_overlap or has_substring or has_stem_match
            
            return {
                "video_id": request.video_id,
                "quiz_id": request.quiz_id,
                "question_number": request.question_number,
                "is_correct": is_correct,
                "user_answer": request.user_answer,
                "correct_answer": correct_answer,
                "feedback": "Good answer!" if is_correct else f"The expected answer was: {correct_answer}",
                "explanation": question.get('explanation', '')
            }
            
    except requests.exceptions.ConnectionError:
        raise HTTPException(status_code=503, detail="Validation service unavailable")
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error validating answer: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000) 

