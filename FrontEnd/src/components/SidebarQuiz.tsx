
import React, { useState, useEffect } from 'react';
import { CheckCircle2, XCircle, AlertCircle, RefreshCw } from 'lucide-react';
import { Segment, Question } from '../App';

interface SidebarQuizProps {
    segment: Segment;
    isLocked: boolean;
    onComplete: (results: { correct: number; total: number }) => void;
    videoTime: number; // To potentially use for tracking
}

const SidebarQuiz: React.FC<SidebarQuizProps> = ({ segment, isLocked, onComplete }) => {
    // ... state ...
    const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
    const [selectedAnswers, setSelectedAnswers] = useState<(number | string | null)[]>([]);
    const [submitted, setSubmitted] = useState(false);
    const [questionResults, setQuestionResults] = useState<(boolean | null)[]>([]);
    const [explanation, setExplanation] = useState<string | null>(null);
    const [feedback, setFeedback] = useState('');
    const [isValidating, setIsValidating] = useState(false);

    useEffect(() => {
        if (segment) {
            // Reset state when segment changes
            setCurrentQuestionIndex(0);
            setSelectedAnswers(new Array(segment.questions.length).fill(null));
            setSubmitted(false);
            setQuestionResults(new Array(segment.questions.length).fill(null));
            setExplanation(null);
            setFeedback('');
            setIsCompleted(false);
        }
    }, [segment]);

    if (!segment || !segment.questions || segment.questions.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center p-8 text-center text-gray-500 h-full">
                <p>No quiz available for this segment.</p>
            </div>
        );
    }

    const currentQuestion = segment.questions[currentQuestionIndex];
    const isLastQuestion = currentQuestionIndex === segment.questions.length - 1;

    const handleAnswerSelect = (answer: number | string) => {
        if (!submitted && !isValidating && !isLocked) {
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
                            video_id: (segment as any).videoId || "3", // Fallback ID needs to be handled better in parent
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

    const [isCompleted, setIsCompleted] = useState(false);

    const handleNext = () => {
        if (isLastQuestion) {
            const correctCount = questionResults.filter(r => r === true).length;
            setIsCompleted(true); // Show local completion state
            onComplete({
                correct: correctCount,
                total: segment.questions.length
            });
        } else {
            setCurrentQuestionIndex(prev => prev + 1);
            setSubmitted(false);
            setExplanation(null);
            setFeedback('');
        }
    };

    if (isCompleted) {
        const correctCount = questionResults.filter(r => r === true).length;
        const total = segment.questions.length;
        const percentage = Math.round((correctCount / total) * 100);

        return (
            <div className="flex flex-col items-center justify-start pt-12 p-8 text-center h-full animate-in fade-in duration-300">
                <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mb-6">
                    <CheckCircle2 className="w-8 h-8 text-green-600" />
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-2">Quiz Complete!</h3>
                <p className="text-gray-500 mb-6">You scored {correctCount} out of {total}</p>

                <div className="flex gap-3 w-full max-w-xs">
                    {/* Add buttons here if needed, e.g. "Review" or "Back to Chapters" */}
                </div>
            </div>
        );
    }

    return (
        <div className={`relative ${isLocked ? 'opacity-50 pointer-events-none filter grayscale' : ''}`}>

            {isLocked && (
                <div className="absolute inset-0 z-10 flex items-center justify-center bg-gray-50/50 backdrop-blur-[1px] h-full">
                    <div className="bg-white px-4 py-3 rounded-lg shadow-sm border border-gray-200 text-sm font-medium text-gray-600 flex items-center gap-2">
                        <AlertCircle className="w-4 h-4" />
                        Complete segment to unlock quiz
                    </div>
                </div>
            )}

            <div className="p-4 border-b border-gray-100 flex justify-between items-center bg-white sticky top-0 z-20">
                <div>
                    {/* Dynamic Title */}
                    <h3 className="font-semibold text-gray-800 line-clamp-1" title={segment.title || "Quiz"}>
                        {segment.title ? `Quiz: ${segment.title}` : "Section Quiz"}
                    </h3>
                    <div className="flex items-center gap-2 text-xs text-gray-500 mt-0.5">
                        <span>Question {currentQuestionIndex + 1} of {segment.questions.length}</span>
                        {currentQuestion.difficulty && (
                            <span className={`px-1.5 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider ${currentQuestion.difficulty === 'hard'
                                ? 'bg-red-100 text-red-700'
                                : currentQuestion.difficulty === 'medium'
                                    ? 'bg-yellow-100 text-yellow-700'
                                    : 'bg-green-100 text-green-700'
                                }`}>
                                {currentQuestion.difficulty}
                            </span>
                        )}
                    </div>
                </div>
            </div>

            <div className="p-4">
                {/* Scrollable content logic is handled by parent VideoPlayer container now.
                    We just render the content naturally. */}
                <div className="mb-6">
                    <p className="text-gray-900 font-medium text-sm leading-relaxed mb-4">
                        {currentQuestion.question}
                    </p>

                    {currentQuestion.type === 'short_answer' ? (
                        <textarea
                            value={(selectedAnswers[currentQuestionIndex] as string) || ''}
                            onChange={(e) => handleAnswerSelect(e.target.value)}
                            placeholder="Type your answer here..."
                            disabled={submitted || isLocked}
                            className={`w-full p-3 text-sm border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none
                ${submitted
                                    ? questionResults[currentQuestionIndex]
                                        ? 'bg-green-50 border-green-200 text-green-900'
                                        : 'bg-red-50 border-red-200 text-red-900'
                                    : 'bg-white border-gray-200 text-gray-900'
                                }
              `}
                            rows={4}
                        />
                    ) : (
                        <div className="space-y-2">
                            {currentQuestion.options?.map((option, index) => {
                                // Handle string options or object options
                                const optionText = typeof option === 'string' ? option : option;
                                const isSelected = selectedAnswers[currentQuestionIndex] === index;
                                const isCorrect = index === currentQuestion.correctAnswer;

                                let itemClass = "w-full text-left p-3 rounded-lg border text-sm transition-all ";

                                if (submitted) {
                                    if (isCorrect) {
                                        itemClass += "bg-green-50 border-green-200 text-green-800 ring-1 ring-green-200";
                                    } else if (isSelected && !isCorrect) {
                                        itemClass += "bg-red-50 border-red-200 text-red-800 ring-1 ring-red-200";
                                    } else {
                                        itemClass += "bg-white border-gray-100 text-gray-400 opacity-60";
                                    }
                                } else {
                                    if (isSelected) {
                                        itemClass += "bg-gray-900 text-white border-gray-900 shadow-sm";
                                    } else {
                                        itemClass += "bg-white border-gray-200 text-gray-700 hover:border-gray-300 hover:bg-gray-50";
                                    }
                                }

                                return (
                                    <button
                                        key={index}
                                        onClick={() => handleAnswerSelect(index)}
                                        disabled={submitted || isLocked}
                                        className={itemClass}
                                    >
                                        <div className="flex items-start gap-4">
                                            <div className={`w-5 h-5 rounded-full border flex items-center justify-center flex-shrink-0 mt-0.5 text-xs font-medium transition-colors
                           ${submitted
                                                    ? isCorrect ? 'border-green-500 bg-green-500 text-white' : isSelected ? 'border-red-500 bg-red-500 text-white' : 'border-gray-200 text-gray-400'
                                                    : isSelected ? 'border-white text-gray-900 bg-white' : 'border-gray-300 text-gray-500'
                                                }
                        `}>
                                                {String.fromCharCode(65 + index)}
                                            </div>
                                            <span className="leading-tight">{optionText}</span>
                                        </div>
                                    </button>
                                );
                            })}
                        </div>
                    )}
                </div>

                {submitted && (
                    <div className={`mt-4 p-3 rounded-lg text-sm border ${questionResults[currentQuestionIndex]
                        ? 'bg-green-50 border-green-100 text-green-800'
                        : 'bg-red-50 border-red-100 text-red-800'
                        }`}>
                        <div className="flex items-center gap-2 font-semibold mb-1">
                            {questionResults[currentQuestionIndex]
                                ? <><CheckCircle2 className="w-4 h-4" /> Correct</>
                                : <><XCircle className="w-4 h-4" /> Incorrect</>
                            }
                        </div>
                        {feedback && <p className="mb-2">{feedback}</p>}

                        {explanation && (
                            <div className="mt-2 text-xs opacity-90 border-t border-black/5 pt-2">
                                <span className="font-medium">Explanation: </span>
                                {explanation}
                            </div>
                        )}
                    </div>
                )}
            </div>

            <div className="p-4 border-t border-gray-100 bg-white">
                {!submitted ? (
                    <button
                        onClick={handleSubmit}
                        disabled={selectedAnswers[currentQuestionIndex] === null || selectedAnswers[currentQuestionIndex] === '' || isValidating || isLocked}
                        className="w-full py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 disabled:bg-gray-200 disabled:text-gray-400 disabled:cursor-not-allowed transition-colors text-sm font-medium flex items-center justify-center gap-2"
                    >
                        {isValidating ? (
                            <>
                                <RefreshCw className="w-4 h-4 animate-spin" />
                                Validating...
                            </>
                        ) : "Submit Answer"}
                    </button>
                ) : (
                    <button
                        onClick={handleNext}
                        className="w-full py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors text-sm font-medium"
                    >
                        {isLastQuestion ? 'Complete Quiz' : 'Next Question'}
                    </button>
                )}
            </div>
        </div>
    );
};

export default SidebarQuiz;
