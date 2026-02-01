#!/usr/bin/env python3
"""
Quick test script for Quiz Generation and Answer Validation endpoints.
Runs individual tests step by step with user prompts.
"""

import requests
import json
import time

API_SERVER_URL = "http://localhost:8000"
QUIZ_SERVICE_URL = "http://localhost:8080"
TEST_VIDEO_ID = "test-video-001"


def test_services():
    """Test if both services are running."""
    print("\n=== TEST: Service Health Check ===")
    
    try:
        r = requests.get(f"{QUIZ_SERVICE_URL}/health", timeout=5)
        print(f"✅ Quiz Service: {r.json()}")
    except Exception as e:
        print(f"❌ Quiz Service: {e}")
        return False
    
    try:
        r = requests.get(f"{API_SERVER_URL}/", timeout=5)
        print(f"✅ API Server: Running")
    except Exception as e:
        print(f"❌ API Server: {e}")
        return False
    
    return True


def test_get_existing_quizzes():
    """Check for existing quizzes."""
    print(f"\n=== TEST: Get Existing Quizzes for {TEST_VIDEO_ID} ===")
    
    try:
        r = requests.get(f"{API_SERVER_URL}/video/{TEST_VIDEO_ID}/quizzes", timeout=10)
        result = r.json()
        quizzes = result.get('quizzes', [])
        print(f"Found {len(quizzes)} quiz(zes)")
        
        for q in quizzes[:3]:
            print(f"  Quiz: {q.get('quiz_id')}")
            print(f"    Questions: {len(q.get('questions', []))}")
        
        return quizzes
    except Exception as e:
        print(f"Error: {e}")
        return []


def test_generate_quiz():
    """Generate a new quiz."""
    print("\n=== TEST: Generate Quiz ===")
    
    segment = {
        "segment_number": 1,
        "segment_title": "Introduction to Deep Learning",
        "segment_start_timestamp": 0.0,
        "segment_end_timestamp": 60.0,
        "transcript": [
            {"start_timestamp": 0.0, "end_timestamp": 18.4,
             "text": "This is the first content lecture in deep learning. We'll talk about the background of deep learning and why it's such a hot topic right now."},
            {"start_timestamp": 18.4, "end_timestamp": 36.36,
             "text": "At its core, the promise of deep learning is quite simple: collect enough data, label it, and you get a model capable of quite astonishing tasks at human-level performance."},
            {"start_timestamp": 36.36, "end_timestamp": 50.56,
             "text": "This mainly holds true in supervised learning. Where deep learning and big data converge, the big money basically follows."}
        ]
    }
    
    payload = {"video_id": TEST_VIDEO_ID, "segment": segment}
    
    print(f"Generating quiz for segment: {segment['segment_title']}")
    print("This may take 30-60 seconds...")
    
    try:
        start = time.time()
        r = requests.post(f"{API_SERVER_URL}/video/segment-questions-direct",
                         json=payload, timeout=120)
        elapsed = time.time() - start
        
        if r.status_code == 200:
            result = r.json()
            quiz_id = result.get('quiz_id')
            questions = result.get('quiz_data', {}).get('questions', [])
            
            print(f"✅ Quiz generated in {elapsed:.1f}s")
            print(f"   Quiz ID: {quiz_id}")
            print(f"   Questions: {len(questions)}")
            
            for i, q in enumerate(questions, 1):
                print(f"\n   Q{i} ({q.get('type')}, {q.get('difficulty')}):")
                print(f"      {q.get('question', '')[:80]}...")
                print(f"      Answer: {q.get('answer')}")
            
            return result
        else:
            print(f"❌ Failed: {r.status_code} - {r.text[:200]}")
            return None
    except Exception as e:
        print(f"❌ Error: {e}")
        return None


def test_submit_answer(video_id: str, quiz_id: str, question_number: int, answer: str):
    """Submit an answer and get validation."""
    print(f"\n=== TEST: Submit Answer ===")
    print(f"Question: {question_number}, Answer: {answer}")
    
    payload = {
        "video_id": video_id,
        "quiz_id": quiz_id,
        "question_number": question_number,
        "user_answer": answer
    }
    
    try:
        start = time.time()
        r = requests.post(f"{API_SERVER_URL}/video/submit-answer",
                         json=payload, timeout=60)
        elapsed = time.time() - start
        
        if r.status_code == 200:
            result = r.json()
            print(f"✅ Validated in {elapsed:.1f}s")
            print(f"   Is Correct: {'✓' if result.get('is_correct') else '✗'}")
            print(f"   Feedback: {result.get('feedback', 'N/A')[:100]}")
            print(f"   Correct Answer: {result.get('correct_answer', 'N/A')}")
            return result
        else:
            print(f"❌ Failed: {r.status_code} - {r.text[:200]}")
            return None
    except Exception as e:
        print(f"❌ Error: {e}")
        return None


def test_direct_validation():
    """Test the direct validation endpoint on quiz-service."""
    print("\n=== TEST: Direct Answer Validation (Quiz Service) ===")
    
    payload = {
        "transcript": "Deep learning is a subset of machine learning using neural networks with multiple layers to learn from data.",
        "question_text": "What is deep learning?",
        "question_type": "short_answer",
        "correct_answer": "A subset of machine learning using multi-layer neural networks",
        "user_answer": "It uses neural networks with many layers to learn from data",
        "options": None
    }
    
    try:
        start = time.time()
        r = requests.post(f"{QUIZ_SERVICE_URL}/quiz/validate-answer",
                         json=payload, timeout=60)
        elapsed = time.time() - start
        
        if r.status_code == 200:
            result = r.json()
            print(f"✅ Validated in {elapsed:.1f}s")
            print(f"   Is Correct: {'✓' if result.get('is_correct') else '✗'}")
            print(f"   Feedback: {result.get('feedback', 'N/A')[:150]}")
            return result
        else:
            print(f"❌ Failed: {r.status_code} - {r.text[:200]}")
            return None
    except Exception as e:
        print(f"❌ Error: {e}")
        return None


def main():
    print("=" * 50)
    print("LECTURE AI - QUICK QUIZ ENDPOINT TESTS")
    print("=" * 50)
    
    # 1. Check services
    if not test_services():
        print("\n⚠️  Services not running. Start them first:")
        print("  Terminal 1: cd api-server && uvicorn main:app --reload --port 8000")
        print("  Terminal 2: cd quiz-service && ANTHROPIC_API_KEY=... ./gradlew run")
        return
    
    # 2. Check existing quizzes
    quizzes = test_get_existing_quizzes()
    
    # 3. Generate a quiz if none exist
    quiz_id = None
    quiz = None
    
    if quizzes:
        quiz = quizzes[0]
        quiz_id = quiz.get('quiz_id')
        print(f"\nUsing existing quiz: {quiz_id}")
    else:
        result = test_generate_quiz()
        if result:
            quiz_id = result.get('quiz_id')
            quiz = result.get('quiz_data', {})
            # Add question numbers
            questions = quiz.get('questions', [])
            for i, q in enumerate(questions, 1):
                q['question_number'] = i
    
    # 4. Test answer submission
    if quiz_id and quiz:
        questions = quiz.get('questions', [])
        if questions:
            q = questions[0]
            q_num = q.get('question_number', 1)
            correct_ans = q.get('answer', 'A')
            
            # Test with correct answer
            test_submit_answer(TEST_VIDEO_ID, quiz_id, q_num, correct_ans)
            
            # Test with wrong answer for MCQ
            if q.get('type') == 'mcq':
                wrong = 'B' if correct_ans != 'B' else 'A'
                test_submit_answer(TEST_VIDEO_ID, quiz_id, q_num, wrong)
    
    # 5. Test direct validation
    test_direct_validation()
    
    print("\n" + "=" * 50)
    print("TESTS COMPLETE")
    print("=" * 50)


if __name__ == "__main__":
    main()
