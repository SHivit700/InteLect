import { useState } from 'react';
import { X, CheckCircle, XCircle } from 'lucide-react';
import type { Segment } from '../App';

interface QuizModalProps {
  segment: Segment;
  onClose: () => void;
  onComplete?: (results: { correct: number; total: number }) => void;
}

export function QuizModal({ segment, onClose, onComplete }: QuizModalProps) {
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  // selectedAnswers can be number (MCQ index) or string (Short Answer text) or null
  const [selectedAnswers, setSelectedAnswers] = useState<(number | string | null)[]>(
    segment.questions.map(() => null)
  );
  // Store validation results for each question (true = correct, false = incorrect)
  const [questionResults, setQuestionResults] = useState<(boolean | null)[]>(
    segment.questions.map(() => null)
  );
  const [explanation, setExplanation] = useState('');
  const [submitted, setSubmitted] = useState(false);
  const [quizComplete, setQuizComplete] = useState(false);
  // Feedback message for short answers
  const [feedback, setFeedback] = useState('');

  const [isValidating, setIsValidating] = useState(false);

  const currentQuestion = segment.questions[currentQuestionIndex];
  console.log('Current Question Debug:', {
    question: currentQuestion,
    difficulty: currentQuestion.difficulty,
    type: currentQuestion.type,
    allKeys: Object.keys(currentQuestion)
  });
  const isLastQuestion = currentQuestionIndex === segment.questions.length - 1;

  const handleAnswerSelect = (answer: number | string) => {
    if (!submitted && !isValidating) {
      const newAnswers = [...selectedAnswers];
      newAnswers[currentQuestionIndex] = answer;
      setSelectedAnswers(newAnswers);
    }
  };

  const handleSubmit = async () => {
    const answer = selectedAnswers[currentQuestionIndex];
    if (answer === null) return;

    setIsValidating(true);

    try {
      if (currentQuestion.type === 'short_answer') {
        const validOptionText = answer as string;
        try {
          const validationResponse = await fetch(`http://localhost:8000/video/submit-answer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              video_id: (segment as any).videoId || "3",
              quiz_id: (segment as any).quizId,
              question_number: currentQuestionIndex + 1,
              user_answer: validOptionText
            })
          });

          if (validationResponse.ok) {
            const data = await validationResponse.json();
            setExplanation(data.explanation || "");
            setFeedback(data.feedback || "");

            const newResults = [...questionResults];
            newResults[currentQuestionIndex] = data.is_correct;
            setQuestionResults(newResults);
          }
        } catch (e) {
          console.error("Failed to submit answer", e);
        }
      } else {
        // MCQ: Validate locally
        const isCorrect = answer === currentQuestion.correctAnswer;
        const newResults = [...questionResults];
        newResults[currentQuestionIndex] = isCorrect;
        setQuestionResults(newResults);

        if (currentQuestion.explanation) {
          setExplanation(currentQuestion.explanation);
        }
      }
    } finally {
      setIsValidating(false);
      setSubmitted(true);
    }
  };

  const handleNext = () => {
    if (isLastQuestion) {
      setQuizComplete(true);
      if (onComplete) {
        // Calculate final result
        const results = calculateScore();
        onComplete({ correct: results.correct, total: results.total });
      }
    } else {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
      setSubmitted(false);
      setExplanation('');
      setFeedback('');
    }
  };

  const handleSubmitExplanation = () => {
    handleNext();
  };

  const calculateScore = () => {
    let correct = 0;
    segment.questions.forEach((q, index) => {
      // Use server validation result if available
      if (questionResults[index] === true) {
        correct++;
      } else if (questionResults[index] === null) {
        // Fallback for locally checking MCQ if server check failed or wasn't recorded
        // (Should not happen in normal flow as we force submit)
        if (q.type !== 'short_answer' && selectedAnswers[index] === q.correctAnswer) {
          correct++;
        }
      }
    });
    return {
      correct,
      total: segment.questions.length,
      percentage: Math.round((correct / segment.questions.length) * 100),
    };
  };

  if (quizComplete) {
    const score = calculateScore();
    return (
      <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg max-w-2xl w-full p-8 shadow-xl">
          <div className="text-center">
            <h2 className="text-3xl font-semibold text-gray-900 mb-2">
              Quiz Complete
            </h2>
            <p className="text-gray-500 mb-8">{segment.title}</p>

            <div className="bg-gray-900 text-white rounded-lg p-8 mb-8">
              <div className="text-6xl font-bold mb-2">
                {score.percentage}%
              </div>
              <p className="text-lg opacity-75">
                {score.correct} / {score.total} correct
              </p>
            </div>

            {score.percentage === 100 && (
              <div className="bg-green-50 rounded-lg p-4 mb-6 text-green-900">
                <p className="font-medium">Perfect score!</p>
              </div>
            )}

            {score.percentage >= 70 && score.percentage < 100 && (
              <div className="bg-gray-50 rounded-lg p-4 mb-6 text-gray-900">
                <p className="font-medium">Good work.</p>
              </div>
            )}

            {score.percentage < 70 && (
              <div className="bg-gray-50 rounded-lg p-4 mb-6 text-gray-900">
                <p className="font-medium">Consider reviewing this chapter.</p>
              </div>
            )}

            <div className="space-y-2 mb-8 text-left max-h-60 overflow-y-auto">
              {segment.questions.map((q, index) => {
                const isCorrect = questionResults[index] === true;
                return (
                  <div
                    key={q.id}
                    className={`p-3 rounded-lg text-sm ${isCorrect
                      ? 'bg-green-50 text-green-900'
                      : 'bg-red-50 text-red-900'
                      }`}
                  >
                    <div className="flex items-start gap-2">
                      {isCorrect ? (
                        <CheckCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
                      ) : (
                        <XCircle className="w-4 h-4 flex-shrink-0 mt-0.5" />
                      )}
                      <div className="flex-1">
                        <p className="font-medium mb-1">{q.question}</p>
                        {!isCorrect && q.type !== 'short_answer' && (
                          <p className="opacity-75">
                            Correct: {q.options?.[q.correctAnswer!]}
                          </p>
                        )}
                        {/* For short answer, we don't always show the *exact* correct text as it's semantic, but we could if available */}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            <button
              onClick={onClose}
              className="w-full px-5 py-3 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-all font-medium"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Determine correctness for current question (for UI feedback)
  const isCurrentCorrect = questionResults[currentQuestionIndex] === true;

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-2xl w-full p-8 max-h-[90vh] overflow-y-auto shadow-xl">
        <div className="flex items-start justify-between mb-6">
          <div>
            <h2 className="text-2xl font-semibold text-gray-900">Chapter Quiz</h2>
            <p className="text-sm text-gray-500">{segment.title}</p>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="mb-8">
          <div className="flex items-center justify-between text-sm text-gray-600 mb-2">
            <span>
              Question {currentQuestionIndex + 1} / {segment.questions.length}
              {currentQuestion.difficulty && (
                <span className={`ml-3 px-2 py-0.5 rounded text-xs font-medium uppercase tracking-wide ${currentQuestion.difficulty === 'hard'
                  ? 'bg-red-100 text-red-800'
                  : currentQuestion.difficulty === 'medium'
                    ? 'bg-yellow-100 text-yellow-800'
                    : 'bg-green-100 text-green-800'
                  }`}>
                  {currentQuestion.difficulty}
                </span>
              )}
            </span>
            <span>{Math.round(((currentQuestionIndex + 1) / segment.questions.length) * 100)}%</span>
          </div>
          <div className="w-full h-1 bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-gray-900 transition-all duration-300"
              style={{
                width: `${((currentQuestionIndex + 1) / segment.questions.length) * 100}%`,
              }}
            />
          </div>
        </div>

        {currentQuestion.diagram && (
          <div className="mb-6 rounded-lg overflow-hidden border border-gray-200">
            <img
              src={currentQuestion.diagram}
              alt="Question diagram"
              className="w-full max-h-64 object-contain bg-gray-50"
            />
          </div>
        )}

        <p className="text-lg font-medium text-gray-900 mb-6">{currentQuestion.question}</p>

        {currentQuestion.type === 'short_answer' ? (
          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-2">Your Answer</label>
            <textarea
              value={selectedAnswers[currentQuestionIndex] as string || ''}
              onChange={(e) => handleAnswerSelect(e.target.value)}
              disabled={submitted}
              rows={4}
              className={`w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${submitted
                ? isCurrentCorrect
                  ? 'bg-green-50 border-green-500 text-green-900'
                  : 'bg-red-50 border-red-500 text-red-900'
                : 'border-gray-300'
                }`}
              placeholder="Type your answer here..."
            />
            {submitted && (
              <div className="mt-2 text-sm">
                {feedback && (
                  <p className={`${isCurrentCorrect ? 'text-green-700' : 'text-red-700'} font-medium`}>
                    {feedback}
                  </p>
                )}
              </div>
            )}
          </div>
        ) : (
          <div className="space-y-2 mb-6">
            {currentQuestion.options && currentQuestion.options.map((option, index) => {
              const isSelected = selectedAnswers[currentQuestionIndex] === index;
              const isCorrectAnswer = index === currentQuestion.correctAnswer;
              // For MCQ, we rely on local correct answer for immediate feedback styling IF we haven't submitted yet?
              // No, logic was: if submitted, styling depends on correctness.
              // We can use isCurrentCorrect (from server) OR fall back to local check for styling consistency

              let buttonClass = 'w-full text-left p-4 rounded-lg transition-all ';

              if (!submitted) {
                buttonClass += isSelected
                  ? 'bg-gray-900 text-white'
                  : 'bg-gray-50 hover:bg-gray-100 text-gray-700';
              } else {
                // If submitted, show correct/incorrect
                // If we have server result, rely on that?
                // Actually for MCQ, displaying the *correct* option green is helpful regardless of what user picked.
                if (isCorrectAnswer) {
                  buttonClass += 'bg-green-50 border-2 border-green-500 text-green-900';
                } else if (isSelected && !isCurrentCorrect) {
                  // If user selected this and it's wrong
                  buttonClass += 'bg-red-50 border-2 border-red-500 text-red-900';
                } else {
                  buttonClass += 'bg-gray-50 text-gray-400';
                }
              }

              return (
                <button
                  key={index}
                  onClick={() => handleAnswerSelect(index)}
                  disabled={submitted}
                  className={buttonClass}
                >
                  <div className="flex items-center justify-between">
                    <span>{option}</span>
                    {submitted && isCorrectAnswer && (
                      <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0" />
                    )}
                    {submitted && isSelected && !isCurrentCorrect && (
                      <XCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
                    )}
                  </div>
                </button>
              );
            })}
          </div>
        )}

        {submitted && (
          <>
            <div
              className={`mb-6 p-4 rounded-lg text-sm ${isCurrentCorrect
                ? 'bg-green-50 text-green-900'
                : 'bg-red-50 text-red-900'
                }`}
            >
              <p className="font-medium mb-1">
                {isCurrentCorrect ? "Correct!" : "Incorrect"}
              </p>

              {/* MCQ: Show correct option text if wrong */}
              {!isCurrentCorrect &&
                currentQuestion.type !== 'short_answer' &&
                currentQuestion.correctAnswer !== undefined &&
                currentQuestion.options &&
                currentQuestion.options[currentQuestion.correctAnswer] !== undefined && (
                  <div className="mb-2">
                    <p className="font-semibold">Correct Answer:</p>
                    <p>{currentQuestion.options[currentQuestion.correctAnswer]}</p>
                  </div>
                )}

              {/* Show explanation from API if available */}
              {((explanation && typeof explanation === 'string') ||
                ((currentQuestion as any).explanation && typeof (currentQuestion as any).explanation === 'string')) && (
                  <div className="mt-2 text-gray-700 bg-white/50 p-2 rounded">
                    <p className="font-semibold text-gray-900">Explanation:</p>
                    <p>{explanation || (currentQuestion as any).explanation}</p>
                  </div>
                )}
            </div>
          </>
        )}

        <div className="flex gap-3 justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2.5 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-medium text-sm"
          >
            Exit
          </button>
          {!submitted ? (
            <button
              onClick={handleSubmit}
              disabled={selectedAnswers[currentQuestionIndex] === null || selectedAnswers[currentQuestionIndex] === '' || isValidating}
              className="px-5 py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed font-medium text-sm flex items-center justify-center min-w-[100px]"
            >
              {isValidating ? "Validating..." : "Submit"}
            </button>
          ) : (
            <button
              onClick={handleSubmitExplanation}
              className="px-5 py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors font-medium text-sm"
            >
              {isLastQuestion ? 'Finish' : 'Next'}
            </button>
          )}
        </div>
      </div>
    </div >
  );
}