import { useState } from 'react';
import { X, CheckCircle, XCircle } from 'lucide-react';
import type { Question } from '../App';

interface QuestionModalProps {
  question: Question;
  onClose: () => void;
  onContinue: () => void;
}

export function QuestionModal({ question, onClose, onContinue }: QuestionModalProps) {
  const [selectedAnswer, setSelectedAnswer] = useState<number | null>(null);
  const [submitted, setSubmitted] = useState(false);

  const handleSubmit = () => {
    if (selectedAnswer !== null) {
      setSubmitted(true);
    }
  };

  const isCorrect = selectedAnswer === question.correctAnswer;

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-2xl w-full p-8 shadow-xl">
        <div className="flex items-start justify-between mb-6">
          <h2 className="text-2xl font-semibold text-gray-900">Quick Check</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {question.diagram && (
          <div className="mb-6 rounded-lg overflow-hidden border border-gray-200">
            <img
              src={question.diagram}
              alt="Question diagram"
              className="w-full max-h-64 object-contain bg-gray-50"
            />
          </div>
        )}

        <p className="text-lg font-medium text-gray-900 mb-6">{question.question}</p>

        {question.options && (
          <div className="space-y-2 mb-6">
            {question.options.map((option, index) => {
              const isSelected = selectedAnswer === index;
              const isCorrectAnswer = index === question.correctAnswer;
              
              let buttonClass = 'w-full text-left p-4 rounded-lg transition-all ';
              
              if (!submitted) {
                buttonClass += isSelected
                  ? 'bg-gray-900 text-white'
                  : 'bg-gray-50 hover:bg-gray-100 text-gray-700';
              } else {
                if (isCorrectAnswer) {
                  buttonClass += 'bg-green-50 border-2 border-green-500 text-green-900';
                } else if (isSelected && !isCorrect) {
                  buttonClass += 'bg-red-50 border-2 border-red-500 text-red-900';
                } else {
                  buttonClass += 'bg-gray-50 text-gray-400';
                }
              }

              return (
                <button
                  key={index}
                  onClick={() => !submitted && setSelectedAnswer(index)}
                  disabled={submitted}
                  className={buttonClass}
                >
                  <div className="flex items-center justify-between">
                    <span>{option}</span>
                    {submitted && isCorrectAnswer && (
                      <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0" />
                    )}
                    {submitted && isSelected && !isCorrect && (
                      <XCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
                    )}
                  </div>
                </button>
              );
            })}
          </div>
        )}

        {submitted && (
          <div className={`mb-6 p-4 rounded-lg text-sm ${
            isCorrect 
              ? 'bg-green-50 text-green-900' 
              : 'bg-red-50 text-red-900'
          }`}>
            <p className="font-medium mb-1">
              {isCorrect ? "Correct!" : "Incorrect"}
            </p>
            <p>
              {isCorrect
                ? "Well done."
                : `Correct answer: ${question.options?.[question.correctAnswer!]}`}
            </p>
          </div>
        )}

        <div className="flex gap-3 justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2.5 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-medium text-sm"
          >
            Skip
          </button>
          {!submitted ? (
            <button
              onClick={handleSubmit}
              disabled={selectedAnswer === null}
              className="px-5 py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed font-medium text-sm"
            >
              Submit
            </button>
          ) : (
            <button
              onClick={onContinue}
              className="px-5 py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors font-medium text-sm"
            >
              Continue
            </button>
          )}
        </div>
      </div>
    </div>
  );
}