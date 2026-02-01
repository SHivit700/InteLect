import { useState, useEffect } from 'react';
import { Lock } from 'lucide-react';
import type { Segment } from '../App';

interface QuizSidebarProps {
  segments: Segment[];
  currentSegment?: Segment;
  unlockedQuestions: Set<string>;
  onChapterClick?: (startTime: number) => void;
  onSegmentSelect?: (segment: Segment) => void;
  quizProgress?: Record<string, { correct: number; total: number; completed: boolean }>;
}

export function QuizSidebar({ segments, currentSegment, unlockedQuestions, onChapterClick, onSegmentSelect, quizProgress }: QuizSidebarProps) {
  const [activeSegmentId, setActiveSegmentId] = useState<string | null>(currentSegment?.id || null);

  // Sync internal state with prop changes (e.g. video progresses automatically)
  useEffect(() => {
    if (currentSegment) {
      setActiveSegmentId(currentSegment.id);
    }
  }, [currentSegment]);

  /* 
    Single click: Selects the segment in the UI (shows questions if any)
    Double click: Seeks video to that segment
  */
  const handleSegmentSelect = (segment: Segment, shouldSeek: boolean = false) => {
    setActiveSegmentId(segment.id); // Keep local state update for list highlighting

    if (shouldSeek && onChapterClick) {
      onChapterClick(segment.startTime);
    } else if (!shouldSeek && onSegmentSelect) {
      onSegmentSelect(segment);
    }
  };

  return (
    <div className="bg-gray-50 rounded-lg p-4">
      <div className="space-y-2">
        {segments.map((segment, index) => {
          const segmentUnlocked = segment.questions.some(q => unlockedQuestions.has(q.id));
          const progress = quizProgress?.[segment.id];

          // Default to 3 questions if we don't have progress or actual length is 0 (which shouldn't happen for generated quizzes but good fallback)
          // If we have progress, use that.
          // Requirement is to show "0/3" initially.
          const totalQuestions = segment.questions.length > 0 ? segment.questions.length : 3;
          const correctCount = progress ? progress.correct : 0;
          const isCompleted = progress?.completed;

          return (
            <button
              key={segment.id}
              onClick={() => handleSegmentSelect(segment, false)}
              onDoubleClick={() => handleSegmentSelect(segment, true)}
              className={`w-full text-center p-3 rounded-lg transition-all text-sm ${activeSegmentId === segment.id
                ? 'bg-gray-900 text-white'
                : 'bg-white hover:bg-gray-100 text-gray-700 border border-gray-200'
                }`}
            >
              <div className="flex items-center justify-center gap-3">
                <span className="font-medium">{index + 1}</span>
                <div className="flex-1 min-w-0">
                  <p className="font-medium truncate">{segment.title}</p>
                  <div className="flex flex-col items-center justify-center gap-0.5">
                    <p className={`text-xs font-medium ${isCompleted ? 'text-green-500' : 'opacity-75'}`}>
                      {isCompleted ? '✓ Complete' : `${correctCount}/${totalQuestions} Correct`}
                    </p>
                  </div>
                </div>
                {!segmentUnlocked && <Lock className="w-4 h-4 opacity-50" />}
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
}