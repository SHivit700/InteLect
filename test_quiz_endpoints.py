#!/usr/bin/env python3
"""
Comprehensive test script for Quiz Generation and Answer Validation endpoints.

This script tests:
1. Direct segment question generation (POST /video/segment-questions-direct)
2. Answer submission and validation (POST /video/submit-answer)
3. Quiz retrieval endpoints (GET /video/{video_id}/quizzes, GET /video/{video_id}/quiz/{quiz_id})

Prerequisites:
- API server running on port 8000 (cd api-server && uvicorn main:app --reload)
- Quiz service running on port 8080 (cd quiz-service && ./gradlew run with ANTHROPIC_API_KEY)

Usage:
    python test_quiz_endpoints.py [--skip-generation]
    
    --skip-generation: Skip quiz generation and use existing quizzes for testing
"""

import requests
import json
import time
import sys
from typing import Optional

# Configuration
API_SERVER_URL = "http://localhost:8000"
QUIZ_SERVICE_URL = "http://localhost:8080"

# Test data - shorter sample segment for faster testing
SAMPLE_SEGMENT = {
    "segment_number": 1,
    "segment_title": "Introduction to Deep Learning",
    "segment_start_timestamp": 0.0,
    "segment_end_timestamp": 100.0,
    "transcript": [
        {
            "start_timestamp": 0.0,
            "end_timestamp": 8.6,
            "text": "So hello everybody, this is the first content lecture in deep learning and we'll talk about the background of deep learning and why do we need deep learning at all in this lecture?"
        },
        {
            "start_timestamp": 9.36,
            "end_timestamp": 18.4,
            "text": "Now why is deep learning such a hot topic right now and really I think most of you are actually here because you heard that deep learning is this thing to do now?"
        },
        {
            "start_timestamp": 18.4,
            "end_timestamp": 36.36,
            "text": "Why is this? Well at its core the promise really of deep learning is quite simple collect enough data, label it and voila you get what seems to be like a magic black box, predict the capable of well quite astonishing tasks very often that kind of human level performance levels."
        },
        {
            "start_timestamp": 36.36,
            "end_timestamp": 50.56,
            "text": "But let's add a caveat my this mainly holds true in supervised learning right now and this is crucial to understand that where deep learning and big data converges the big money basically follows."
        },
        {
            "start_timestamp": 50.56,
            "end_timestamp": 65.16,
            "text": "So this has basically made the field not only highly competitive but also unfortunately sometimes very stressful especially for us and you people working in this environment."
        }
    ]
}

TEST_VIDEO_ID = "test-video-001"
SKIP_GENERATION = "--skip-generation" in sys.argv


def print_header(title: str):
    """Print a formatted section header."""
    print("\n" + "=" * 60)
    print(f"  {title}")
    print("=" * 60)


def print_result(success: bool, message: str):
    """Print a result with pass/fail indicator."""
    status = "✅ PASS" if success else "❌ FAIL"
    print(f"{status}: {message}")


def check_services():
    """Check if API server and quiz service are running."""
    print_header("Checking Services")
    
    # Check API server
    try:
        response = requests.get(f"{API_SERVER_URL}/", timeout=5)
        print_result(response.status_code == 200, f"API Server is running on {API_SERVER_URL}")
    except requests.exceptions.ConnectionError:
        print_result(False, f"API Server is NOT running on {API_SERVER_URL}")
        print("  → Start it with: cd api-server && uvicorn main:app --reload --port 8000")
        return False
    
    # Check quiz service
    try:
        response = requests.get(f"{QUIZ_SERVICE_URL}/health", timeout=5)
        print_result(response.status_code == 200, f"Quiz Service is running on {QUIZ_SERVICE_URL}")
        if response.status_code == 200:
            health_data = response.json()
            print(f"     Version: {health_data.get('version', 'unknown')}")
    except requests.exceptions.ConnectionError:
        print_result(False, f"Quiz Service is NOT running on {QUIZ_SERVICE_URL}")
        print("  → Start it with: cd quiz-service && ANTHROPIC_API_KEY=your_key ./gradlew run")
        return False
    
    return True


def test_direct_question_generation() -> Optional[dict]:
    """Test the direct segment question generation endpoint."""
    print_header("Test 1: Direct Segment Question Generation")
    
    payload = {
        "video_id": TEST_VIDEO_ID,
        "segment": SAMPLE_SEGMENT
    }
    
    print(f"Sending request to POST /video/segment-questions-direct")
    print(f"  Video ID: {TEST_VIDEO_ID}")
    print(f"  Segment: {SAMPLE_SEGMENT['segment_title']}")
    print(f"  Transcript entries: {len(SAMPLE_SEGMENT['transcript'])}")
    print("")
    
    try:
        start_time = time.time()
        response = requests.post(
            f"{API_SERVER_URL}/video/segment-questions-direct",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=120  # Quiz generation can take time
        )
        elapsed = time.time() - start_time
        
        print(f"Response Status: {response.status_code}")
        print(f"Response Time: {elapsed:.2f}s")
        
        if response.status_code == 200:
            result = response.json()
            print_result(True, "Quiz generated successfully!")
            
            # Print quiz details
            quiz_id = result.get('quiz_id')
            quiz_data = result.get('quiz_data', {})
            questions = quiz_data.get('questions', [])
            
            print(f"\n  Quiz ID: {quiz_id}")
            print(f"  Stored in Firestore: {result.get('stored', False)}")
            print(f"  Number of questions: {len(questions)}")
            
            print("\n  Generated Questions:")
            for i, q in enumerate(questions, 1):
                print(f"\n  Q{i} ({q.get('type', 'unknown')}, {q.get('difficulty', 'unknown')}):")
                print(f"      {q.get('question', 'N/A')[:100]}...")
                if q.get('options'):
                    print(f"      Options: {[opt.get('id') for opt in q.get('options', [])]}")
                print(f"      Answer: {q.get('answer', 'N/A')}")
            
            return result
        else:
            print_result(False, f"Quiz generation failed: {response.text[:200]}")
            return None
            
    except requests.exceptions.ConnectionError as e:
        print_result(False, f"Connection error: {e}")
        return None
    except requests.exceptions.Timeout:
        print_result(False, "Request timed out (>120s)")
        return None
    except Exception as e:
        print_result(False, f"Unexpected error: {e}")
        return None


def test_get_video_quizzes(video_id: str):
    """Test retrieving all quizzes for a video."""
    print_header("Test 2: Get Video Quizzes")
    
    print(f"Sending request to GET /video/{video_id}/quizzes")
    
    try:
        response = requests.get(f"{API_SERVER_URL}/video/{video_id}/quizzes", timeout=10)
        
        print(f"Response Status: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            quizzes = result.get('quizzes', [])
            
            print_result(True, f"Retrieved {len(quizzes)} quiz(zes) for video")
            
            for quiz in quizzes:
                print(f"\n  Quiz: {quiz.get('quiz_id')}")
                print(f"    Segment ID: {quiz.get('segment_id')}")
                print(f"    Questions: {len(quiz.get('questions', []))}")
                print(f"    Created: {quiz.get('created_at')}")
            
            return result
        else:
            print_result(False, f"Failed to retrieve quizzes: {response.text[:200]}")
            return None
            
    except Exception as e:
        print_result(False, f"Error: {e}")
        return None


def test_get_quiz_by_id(video_id: str, quiz_id: str):
    """Test retrieving a specific quiz by ID."""
    print_header("Test 3: Get Quiz by ID")
    
    print(f"Sending request to GET /video/{video_id}/quiz/{quiz_id}")
    
    try:
        response = requests.get(f"{API_SERVER_URL}/video/{video_id}/quiz/{quiz_id}", timeout=10)
        
        print(f"Response Status: {response.status_code}")
        
        if response.status_code == 200:
            quiz = response.json()
            print_result(True, "Quiz retrieved successfully")
            
            print(f"\n  Quiz ID: {quiz.get('quiz_id')}")
            print(f"  Questions: {len(quiz.get('questions', []))}")
            
            return quiz
        else:
            print_result(False, f"Failed to retrieve quiz: {response.text[:200]}")
            return None
            
    except Exception as e:
        print_result(False, f"Error: {e}")
        return None


def test_answer_submission(video_id: str, quiz_id: str, question_number: int, user_answer: str, expected_correct: bool):
    """Test the answer submission and validation endpoint."""
    print_header(f"Test 4: Answer Submission (Q{question_number})")
    
    payload = {
        "video_id": video_id,
        "quiz_id": quiz_id,
        "question_number": question_number,
        "user_answer": user_answer
    }
    
    print(f"Sending request to POST /video/submit-answer")
    print(f"  User Answer: {user_answer}")
    
    try:
        start_time = time.time()
        response = requests.post(
            f"{API_SERVER_URL}/video/submit-answer",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=60
        )
        elapsed = time.time() - start_time
        
        print(f"Response Status: {response.status_code}")
        print(f"Response Time: {elapsed:.2f}s")
        
        if response.status_code == 200:
            result = response.json()
            is_correct = result.get('is_correct', False)
            
            print_result(True, "Answer validated successfully")
            
            print(f"\n  Is Correct: {'✓ Yes' if is_correct else '✗ No'}")
            print(f"  Correct Answer: {result.get('correct_answer', 'N/A')}")
            print(f"  Feedback: {result.get('feedback', 'N/A')}")
            print(f"  Explanation: {result.get('explanation', 'N/A')[:200]}...")
            
            return result
        else:
            print_result(False, f"Answer submission failed: {response.text[:200]}")
            return None
            
    except Exception as e:
        print_result(False, f"Error: {e}")
        return None


def test_answer_validation_direct():
    """Test the quiz service's direct answer validation endpoint."""
    print_header("Test 5: Direct Answer Validation (Quiz Service)")
    
    payload = {
        "transcript": "Deep learning is a subset of machine learning that uses neural networks with multiple layers.",
        "question_text": "What is deep learning?",
        "question_type": "short_answer",
        "correct_answer": "A subset of machine learning using neural networks",
        "user_answer": "It's machine learning with neural networks",
        "options": None
    }
    
    print(f"Sending request to POST {QUIZ_SERVICE_URL}/quiz/validate-answer")
    
    try:
        start_time = time.time()
        response = requests.post(
            f"{QUIZ_SERVICE_URL}/quiz/validate-answer",
            json=payload,
            headers={"Content-Type": "application/json"},
            timeout=60
        )
        elapsed = time.time() - start_time
        
        print(f"Response Status: {response.status_code}")
        print(f"Response Time: {elapsed:.2f}s")
        
        if response.status_code == 200:
            result = response.json()
            print_result(True, "Direct validation successful")
            
            print(f"\n  Is Correct: {'✓ Yes' if result.get('is_correct') else '✗ No'}")
            print(f"  Feedback: {result.get('feedback', 'N/A')}")
            
            return result
        else:
            print_result(False, f"Direct validation failed: {response.text[:200]}")
            return None
            
    except Exception as e:
        print_result(False, f"Error: {e}")
        return None


def test_mcq_answer(video_id: str, quiz_id: str, quiz: dict):
    """Test MCQ answer submission with both correct and incorrect answers."""
    questions = quiz.get('questions', [])
    
    # Find an MCQ question
    mcq_question = None
    for q in questions:
        if q.get('type') == 'mcq' and q.get('options'):
            mcq_question = q
            break
    
    if not mcq_question:
        print_header("Test 6: MCQ Answer Test - Skipped (no MCQ found)")
        return None
    
    print_header("Test 6: MCQ Answer Test")
    
    question_number = mcq_question.get('question_number', 1)
    correct_answer = mcq_question.get('answer', '')
    options = mcq_question.get('options', [])
    
    print(f"Question {question_number}: {mcq_question.get('question', 'N/A')[:100]}...")
    print(f"Options: {[(o.get('id'), o.get('text')[:30]) for o in options]}")
    print(f"Correct Answer: {correct_answer}")
    
    # Test with correct answer
    print("\n--- Testing with CORRECT answer ---")
    test_answer_submission(video_id, quiz_id, question_number, correct_answer, expected_correct=True)
    
    # Test with incorrect answer
    wrong_answers = [o.get('id') for o in options if o.get('id') != correct_answer]
    if wrong_answers:
        print("\n--- Testing with INCORRECT answer ---")
        test_answer_submission(video_id, quiz_id, question_number, wrong_answers[0], expected_correct=False)


def test_short_answer(video_id: str, quiz_id: str, quiz: dict):
    """Test short answer submission."""
    questions = quiz.get('questions', [])
    
    # Find a short answer question
    sa_question = None
    for q in questions:
        if q.get('type') == 'short_answer':
            sa_question = q
            break
    
    if not sa_question:
        print_header("Test 7: Short Answer Test - Skipped (no short answer found)")
        return None
    
    print_header("Test 7: Short Answer Test")
    
    question_number = sa_question.get('question_number', 1)
    
    print(f"Question {question_number}: {sa_question.get('question', 'N/A')[:100]}...")
    print(f"Expected Answer: {sa_question.get('answer', 'N/A')[:100]}...")
    
    # Test with a reasonable answer
    test_answer = "Deep learning uses neural networks with multiple layers to learn patterns from data"
    print(f"\n--- Testing with user answer ---")
    test_answer_submission(video_id, quiz_id, question_number, test_answer, expected_correct=True)


def run_all_tests():
    """Run all tests in sequence."""
    print("\n" + "=" * 60)
    print("  LECTURE AI - QUIZ ENDPOINTS TEST SUITE")
    print("=" * 60)
    print(f"\nTest Video ID: {TEST_VIDEO_ID}")
    print(f"API Server: {API_SERVER_URL}")
    print(f"Quiz Service: {QUIZ_SERVICE_URL}")
    print(f"Skip Generation: {SKIP_GENERATION}")
    
    # Check services first
    if not check_services():
        print("\n❌ Services are not running. Please start them and try again.")
        return
    
    quiz_id = None
    video_id = TEST_VIDEO_ID
    quiz = None
    
    if SKIP_GENERATION:
        # Try to get existing quizzes
        print_header("Looking for Existing Quizzes")
        result = test_get_video_quizzes(video_id)
        if result and result.get('quizzes'):
            quiz = result['quizzes'][0]
            quiz_id = quiz.get('quiz_id')
            print(f"\nUsing existing quiz: {quiz_id}")
        else:
            print("No existing quizzes found. Running generation...")
            gen_result = test_direct_question_generation()
            if gen_result:
                quiz_id = gen_result.get('quiz_id')
                video_id = gen_result.get('video_id', TEST_VIDEO_ID)
    else:
        # Test 1: Generate questions directly
        gen_result = test_direct_question_generation()
        
        if not gen_result:
            print("\n❌ Question generation failed. Cannot continue with remaining tests.")
            return
        
        quiz_id = gen_result.get('quiz_id')
        video_id = gen_result.get('video_id', TEST_VIDEO_ID)
    
    # Test 2: Get all quizzes for video
    test_get_video_quizzes(video_id)
    
    # Test 3: Get specific quiz
    if quiz_id:
        quiz = test_get_quiz_by_id(video_id, quiz_id)
    
    if quiz:
        # Test 4-7: Answer submission tests
        test_mcq_answer(video_id, quiz_id, quiz)
        test_short_answer(video_id, quiz_id, quiz)
    
    # Test 5: Direct validation endpoint
    test_answer_validation_direct()
    
    print("\n" + "=" * 60)
    print("  TEST SUITE COMPLETE")
    print("=" * 60 + "\n")


if __name__ == "__main__":
    run_all_tests()
