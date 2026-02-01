// Imports cleaned up
import { useState, useRef, useEffect, useCallback } from 'react';
import { ArrowLeft, Download, FileText, ChevronDown, ChevronUp, Brain, Sparkles, Clock, Lock } from 'lucide-react';
import type { Video, Segment, Question } from '../App';
import SidebarQuiz from './SidebarQuiz';
import { QuestionModal } from './QuestionModal';
import { QuizModal } from './QuizModal';
import { YouTubePlayer, type YouTubePlayerRef } from './YouTubePlayer';

interface VideoPlayerProps {
  video: Video;
  onBack: () => void;
}

interface PanoptoData {
  sessionId: string;
  baseDomain: string;
  title: string;
  author: string;
  srtUrl?: string;
  rssUrl?: string;
  jsonUrl?: string;
  embedUrl: string;
  metadata: any;
}

// HARDCODED PANOPTO URL
const PANOPTO_URL = 'https://imperial.cloud.panopto.eu/Panopto/Pages/Viewer.aspx?id=4da6e998-322b-4a2f-bb97-b3e4001c96db';
const PANOPTO_BASE_DOMAIN = 'https://imperial.cloud.panopto.eu';
const PANOPTO_SESSION_ID = '4da6e998-322b-4a2f-bb97-b3e4001c96db';

interface Segment {
  id: string;
  videoId: string;
  startTime: number;
  endTime: number;
  title: string;
  transcript: string;
  questions: Question[];
}

interface Question {
  id: string;
  segmentId: string;
  question: string;
  timestamp: number;
  options: string[];
  correctAnswer: number;
  type?: 'mcq' | 'short_answer';
  diagram?: string;
  explanation?: string;
}

export function VideoPlayer({ video, onBack }: VideoPlayerProps) {
  const videoRef = useRef<YouTubePlayerRef>(null);
  const panoptoVideoRef = useRef<HTMLVideoElement>(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(3600); // Default 1hr
  const [activePlayer, setActivePlayer] = useState<'youtube' | 'panopto'>('panopto');
  const [showQuestionModal, setShowQuestionModal] = useState(false);
  const [currentQuestion, setCurrentQuestion] = useState<Question | null>(null);

  const [showQuizModal, setShowQuizModal] = useState(false);
  const [currentQuizSegment, setCurrentQuizSegment] = useState<Segment | null>(null);
  // Track quiz progress: segmentId -> { correct, total, completed }
  const [quizProgress, setQuizProgress] = useState<Record<string, { correct: number; total: number; completed: boolean }>>({});
  const [unlockedQuestions, setUnlockedQuestions] = useState<Set<string>>(new Set());
  const [panoptoData, setPanoptoData] = useState<PanoptoData | null>(null);
  const [videoFilename, setVideoFilename] = useState<string | null>(video.videoFilename || null);
  const [activeSegmentId, setActiveSegmentId] = useState<string | null>(null);

  // NEW STATE
  const [isQuizUnlocked, setIsQuizUnlocked] = useState(false);
  const [preemptiveTriggered, setPreemptiveTriggered] = useState<Set<string>>(new Set());

  // REFS (Restored)

  const hasShownQuestionRef = useRef<Set<string>>(new Set());
  const pausedSegmentsRef = useRef<Set<string>>(new Set());
  const lastPanoptoActivityRef = useRef<number>(0);
  const wasPlayingRef = useRef(false);
  const previousTimeRef = useRef<number>(0);

  const [segments, setSegments] = useState<Segment[]>([]);

  // Fetch segments
  useEffect(() => {
    const fetchSegments = async () => {
      try {
        const response = await fetch(`http://localhost:8000/video/${video.id}/segments`);
        if (response.ok) {
          const data = await response.json();
          const mappedSegments: Segment[] = data.segments.map((s: any) => ({
            id: s.segment_id,
            videoId: video.id,
            startTime: s.segment_start_timestamp,
            endTime: s.segment_end_timestamp,
            title: s.segment_title,
            transcript: '', // Transcript not in segment summary
            questions: [] // Questions loaded separately
          }));
          setSegments(mappedSegments);
        }
      } catch (error) {
        console.error('Failed to fetch segments:', error);
      }
    };

    if (video.id) {
      fetchSegments();
    }
  }, [video.id]);

  // Initialize Panopto data on mount
  useEffect(() => {
    const initPanopto = async () => {
      const newPanoptoData: PanoptoData = {
        sessionId: PANOPTO_SESSION_ID,
        baseDomain: PANOPTO_BASE_DOMAIN,
        title: 'Imperial College Lecture',
        author: 'By: Imperial Panopto',
        srtUrl: `${PANOPTO_BASE_DOMAIN}/Panopto/Pages/Transcription/GenerateSRT.ashx?id=${PANOPTO_SESSION_ID}`,
        rssUrl: `${PANOPTO_BASE_DOMAIN}/Panopto/Podcast/Podcast.ashx?id=${PANOPTO_SESSION_ID}`,
        jsonUrl: `${PANOPTO_BASE_DOMAIN}/Panopto/Pages/Viewer/DeliveryInfo.aspx?deliveryId=${PANOPTO_SESSION_ID}&responseType=json`,
        embedUrl: `${PANOPTO_BASE_DOMAIN}/Panopto/Pages/Embed.aspx?id=${PANOPTO_SESSION_ID}&autoplay=false&offerviewer=true&showtitle=true&showbrand=false&interactivity=all`,
        metadata: {}
      };

      try {
        const oEmbedUrl = `${PANOPTO_BASE_DOMAIN}/Panopto/oEmbed.aspx?url=${encodeURIComponent(PANOPTO_URL)}`;
        const response = await fetch(oEmbedUrl);
        if (response.ok) {
          const data = await response.json();
          newPanoptoData.title = data.title || 'Imperial College Lecture';
          newPanoptoData.author = `By: ${data.author_name || 'Imperial Panopto'}`;
        }
      } catch (error) {
        console.log("oEmbed fetch failed (CORS):", error);
      }

      setPanoptoData(newPanoptoData);
      setActivePlayer('panopto');
    };

    initPanopto();
  }, []);

  // Fetch video metadata including filename
  useEffect(() => {
    const fetchVideoMetadata = async () => {
      try {
        const response = await fetch(`http://localhost:8000/video/${video.id}/metadata`);
        if (response.ok) {
          const data = await response.json();
          if (data.video_filename) {
            setVideoFilename(data.video_filename);
          }
        }
      } catch (error) {
        console.log('Failed to fetch video metadata:', error);
      }
    };

    fetchVideoMetadata();
  }, [video.id]);

  // Fetch progress
  useEffect(() => {
    if (video.id) {
      fetch(`http://localhost:8000/video/${video.id}/progress`)
        .then(res => res.json())
        .then(data => {
          if (data.progress) {
            setQuizProgress(data.progress);
          }
        })
        .catch(err => console.error("Failed to load progress:", err));
    }
  }, [video.id]);

  // Auto-scroll to active segment
  // Auto-scroll removed as per user request to pause instead
  // Reset paused segments when video changes
  useEffect(() => {
    pausedSegmentsRef.current.clear();
  }, [video.id]);

  // 🔥 PANOPTO SKIP DETECTION + QUIZ TRIGGERS (HTML5 Video)
  const handleVideoSeeked = () => {
    if (!panoptoVideoRef.current || activePlayer !== 'panopto') return;

    const newTime = Math.floor(panoptoVideoRef.current.currentTime);
    const previousTime = previousTimeRef.current;
    const timeDiff = newTime - previousTime;

    console.log(`⏱️ Video seeked: ${formatTime(previousTime)} → ${formatTime(newTime)} (diff: ${timeDiff}s)`);

    // Detect skips (time jump > 2 seconds)
    if (Math.abs(timeDiff) > 2 && previousTime > 0) {
      console.log(`⚡ SKIP DETECTED: jumped ${timeDiff} seconds`);

      // Check if we skipped over any quiz questions
      const skippedQuestions: Question[] = [];
      segments.forEach(segment => {
        segment.questions.forEach(q => {
          const quizTime = segment.startTime + q.timestamp;
          // If we skipped over this question
          if (previousTime < quizTime && newTime >= quizTime && !hasShownQuestionRef.current.has(q.id)) {
            skippedQuestions.push(q);
          }
        });
      });

      // Trigger the first skipped question
      if (skippedQuestions.length > 0) {
        const firstQuestion = skippedQuestions[0];
        console.log(`📝 QUIZ TRIGGERED (skipped over): ${firstQuestion.question}`);
        hasShownQuestionRef.current.add(firstQuestion.id);
        setUnlockedQuestions((prev: Set<string>) => new Set([...prev, firstQuestion.id]));
        setCurrentQuestion(firstQuestion);
        setShowQuestionModal(true);
        setIsPlaying(false);
        wasPlayingRef.current = false;

        // Pause the video
        if (panoptoVideoRef.current) {
          panoptoVideoRef.current.pause();
        }
      }
    }

    previousTimeRef.current = newTime;
  };

  // Check for quiz triggers during normal playback
  useEffect(() => {
    if (activePlayer !== 'panopto' || !isPlaying) return;

    segments.forEach(segment => {
      segment.questions.forEach(q => {
        const quizTime = segment.startTime + q.timestamp;
        if (Math.abs(quizTime - currentTime) < 2 &&
          !unlockedQuestions.has(q.id) &&
          !hasShownQuestionRef.current.has(q.id)) {
          console.log(`📝 QUIZ TRIGGERED at ${formatTime(quizTime)}: ${q.question}`);
          hasShownQuestionRef.current.add(q.id);
          setUnlockedQuestions((prev: Set<string>) => new Set([...prev, q.id]));
          setCurrentQuestion(q);
          setShowQuestionModal(true);
          setIsPlaying(false);
          wasPlayingRef.current = false;

          // Pause the video
          if (panoptoVideoRef.current) {
            panoptoVideoRef.current.pause();
          }
        }
      });
    });
  }, [activePlayer, currentTime, isPlaying, segments, unlockedQuestions]);

  // Note: Time progression for Panopto is now handled via postMessage polling above
  // This effect is no longer needed but kept as a fallback for YouTube
  useEffect(() => {
    let interval: NodeJS.Timeout;
    if (activePlayer === 'youtube' && isPlaying) {
      // YouTube time is handled by YouTubePlayer component callbacks
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [activePlayer, isPlaying]);

  const handleChapterClick = useCallback((startTime: number) => {
    console.log(`⏩ SKIPPED TO: ${formatTime(startTime)}`);
    if (activePlayer === 'panopto' && panoptoVideoRef.current) {
      panoptoVideoRef.current.currentTime = startTime;
      panoptoVideoRef.current.play();
      setIsPlaying(true);
    } else if (activePlayer === 'youtube' && videoRef.current) {
      videoRef.current.seekTo(startTime);
      videoRef.current.play();
      setIsPlaying(true);
    }
  }, [activePlayer]);

  const handleSeek = useCallback((time: number) => {
    console.log(`🎛️ SEEK to: ${formatTime(time)}`);
    setCurrentTime(time);
    if (activePlayer === 'youtube' && videoRef.current) {
      videoRef.current.seekTo(time);
    } else if (activePlayer === 'panopto' && panoptoVideoRef.current) {
      panoptoVideoRef.current.currentTime = time;
    }
  }, [activePlayer]);

  const togglePlayPause = useCallback(() => {
    const newState = !isPlaying;
    console.log(`🎮 BUTTON ${newState ? 'PLAY' : 'PAUSE'} clicked`);

    if (activePlayer === 'youtube' && videoRef.current) {
      if (newState) {
        videoRef.current.play();
      } else {
        videoRef.current.pause();
      }
    }
    // Panopto: visual feedback only
    setIsPlaying(newState);
    wasPlayingRef.current = newState;
  }, [activePlayer, isPlaying]);

  const playSegment = useCallback((segment: Segment) => {
    console.log('▶️ Playing segment:', segment.id);
    handleChapterClick(segment.startTime);
  }, [handleChapterClick]);

  const handleQuestionClose = () => {
    console.log('❌ Question modal closed');
    setShowQuestionModal(false);
    setCurrentQuestion(null);
  };

  const handleQuestionContinue = () => {
    console.log('✅ Question answered - resuming video');
    setShowQuestionModal(false);
    setCurrentQuestion(null);
    setIsPlaying(true);
    wasPlayingRef.current = true;
  };

  const fetchQuizForSegment = async (segment: Segment) => {
    try {
      console.log(`🔍 Fetching quiz for segment: ${segment.id}`);
      // Use POST endpoint as requested by user
      const response = await fetch(`http://localhost:8000/video/segment-questions`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          video_id: video.id,
          // Extract numeric segment ID if possible, otherwise use original
          segment_id: parseInt(segment.id) || segment.id,
          num_questions: 3
        })
      });

      if (response.ok) {
        const result = await response.json();
        console.log('✅ Quiz data found:', result);

        // Response format: { video_id, segment_id, quiz_id, quiz_data: { questions: [] }, ... }
        const questionsData = result.quiz_data?.questions || result.questions || [];

        // Transform questions to match frontend format
        const transformedQuestions: Question[] = questionsData.map((q: any, index: number) => {
          // Map options from {id, text} to string[] and find correct answer index
          let options: string[] = [];
          let correctAnswer = 0;

          if (q.options && Array.isArray(q.options)) {
            options = q.options.map((opt: any) => opt.text);
            const correctOptIndex = q.options.findIndex((opt: any) => opt.id === q.answer);
            correctAnswer = correctOptIndex !== -1 ? correctOptIndex : 0;
          }

          return {
            id: q.id || `q-${index}`,
            segmentId: segment.id,
            question: q.question,
            timestamp: 0,
            options: options,
            correctAnswer: correctAnswer,
            type: q.type || (options.length > 0 ? 'mcq' : 'short_answer'),
            diagram: undefined,
            explanation: q.explanation,
            difficulty: q.difficulty ? q.difficulty.toLowerCase() : undefined
          };
        });

        if (transformedQuestions.length > 0) {
          // Create a segment copy with the fetched questions
          const quizSegment = {
            ...segment,
            quizId: result.quiz_id,
            videoId: video.id,
            questions: transformedQuestions
          };

          setCurrentQuizSegment(quizSegment);
          // setShowQuizModal(true); // REMOVED: Don't auto-open modal. Sidebar will react to currentQuizSegment.
        } else {
          console.log('ℹ️ No questions returned in quiz data');
        }
      } else {
        console.log('ℹ️ Failed to fetch/generate quiz for this segment');
      }
    } catch (error) {
      console.error('❌ Error fetching quiz:', error);
    }
  };

  const openQuiz = (segment: Segment) => {
    console.log('📚 Opening quiz for:', segment.title);

    // If questions are already loaded, set it as current
    if (segment.questions.length > 0) {
      // Ideally we should make sure currentQuizSegment is set to this segment
      // This logic might be a bit circular if questions are in segment but not in currentQuizSegment
      // For now, let's rely on fetch updating it or setting it if we have it.
      setCurrentQuizSegment(segment);
    }

    if (segment.questions.length === 0) {
      fetchQuizForSegment(segment);
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = Math.floor(seconds % 60);
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="min-h-screen bg-white flex flex-col">
      {/* Header with play state */}
      <div className="border-b border-gray-200 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-center">
          <button
            onClick={onBack}
            className="absolute left-6 flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors text-sm font-medium"
          >
            <ArrowLeft className="w-4 h-4" />
            Back
          </button>
          <div className="text-center">
            <h1 className="font-semibold text-gray-900 flex items-center justify-center gap-2 flex-wrap">
              {panoptoData?.title || video.title}
              <span className={`px-2 py-1 rounded-full text-xs font-medium ${isPlaying
                ? 'bg-green-100 text-green-800 animate-pulse'
                : 'bg-red-100 text-red-800'
                }`}>
                {isPlaying ? '▶️ LIVE' : '⏸️ PAUSED'}
              </span>
            </h1>
            <p className="text-sm text-gray-500">{video.topic}</p>
          </div>
        </div>
      </div>

      <div className="flex-1 flex items-start justify-center px-6 py-8">
        <div className="w-full max-w-7xl">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Video Player */}
            <div className="lg:col-span-2 space-y-6">
              {/* Video Container */}
              <div className="bg-gray-900 rounded-lg overflow-hidden border border-gray-200">
                <div className="aspect-video relative">
                  {activePlayer === 'youtube' ? (
                    <YouTubePlayer
                      ref={videoRef}
                      videoId="yLOM8R6lbzg"
                      onTimeUpdate={setCurrentTime}
                      onDurationChange={setDuration}
                      onStateChange={setIsPlaying}
                    />
                  ) : (
                    <video
                      ref={panoptoVideoRef}
                      src={videoFilename ? `/videos/${videoFilename}` : '/videos/lecture.mp4'}
                      controls
                      className="w-full h-full bg-black"
                      onTimeUpdate={() => {
                        if (!panoptoVideoRef.current) return;
                        const newTime = Math.floor(panoptoVideoRef.current.currentTime);
                        const exactTime = panoptoVideoRef.current.currentTime;
                        setCurrentTime(newTime);

                        // Update active segment
                        const currentSeg = segments.find(s => newTime >= s.startTime && newTime <= s.endTime);
                        if (currentSeg) {
                          if (currentSeg.id !== activeSegmentId) {
                            setActiveSegmentId(currentSeg.id);
                            setIsQuizUnlocked(false); // Lock quiz when segment changes
                            // If we already have this segment in our "currentQuizSegment" state?
                            // We might need to rely on the fetch logic to update currentQuizSegment.
                            // Ideally we cache quizzes. For now, we will re-fetch or check cache.
                          }

                          // PREEMPTIVE GENERATION (-30s)
                          // Trigger if we are within 35 seconds of end, and haven't triggered yet
                          if (currentSeg.endTime - newTime <= 35 && !preemptiveTriggered.has(currentSeg.id)) {
                            console.log(`⚡ Preemptive trigger for segment ${currentSeg.id}`);
                            setPreemptiveTriggered(prev => new Set(prev).add(currentSeg.id));
                            fetchQuizForSegment(currentSeg);
                          }

                          // UNLOCK QUIZ at end
                          if (newTime >= currentSeg.endTime - 2) { // Unlock slightly before actual pause
                            if (!isQuizUnlocked) setIsQuizUnlocked(true);
                          }

                          // Check if we reached the end of the segment (within 1s threshold)
                          // AND we haven't already paused for this segment end
                          if (exactTime >= currentSeg.endTime - 0.5 &&
                            !pausedSegmentsRef.current.has(currentSeg.id) &&
                            isPlaying) {

                            console.log(`⏸️ PAUSING at end of segment: ${currentSeg.title}`);
                            panoptoVideoRef.current.pause();
                            setIsPlaying(false);
                            pausedSegmentsRef.current.add(currentSeg.id);

                            setIsQuizUnlocked(true);
                          }
                        }
                      }}

                      onLoadedMetadata={() => {
                        if (!panoptoVideoRef.current) return;
                        setDuration(Math.floor(panoptoVideoRef.current.duration));
                      }}
                      onPlay={() => setIsPlaying(true)}
                      onPause={() => setIsPlaying(false)}
                      onSeeked={handleVideoSeeked}
                    />
                  )}
                  {panoptoData && (
                    <div className="absolute top-4 right-4 bg-black/80 backdrop-blur-sm text-white px-3 py-1 rounded text-xs opacity-75 z-10">
                      PANOPTO ACTIVE
                    </div>
                  )}
                </div>
              </div>



              {/* Segments List */}
              <div>
                <h3 className="font-semibold text-gray-900 mb-4 text-center">Segments (Double-click to jump)</h3>
                <div className="space-y-3">
                  {segments.map((segment, index) => {
                    const isActive = activeSegmentId === segment.id;
                    return (
                      <div
                        key={segment.id}
                        id={isActive ? "active-segment" : undefined}
                        className={`bg-gray-50 rounded-lg p-4 hover:bg-gray-100 transition-colors cursor-pointer border-l-4 ${isActive
                          ? 'border-green-500 ring-2 ring-green-100'
                          : quizProgress[segment.id]?.completed
                            ? 'border-green-400 bg-green-50/50'
                            : 'border-blue-500'
                          }`}
                        style={isActive ? { backgroundColor: '#f0fdf4' } : {}}
                        onDoubleClick={() => handleChapterClick(segment.startTime)}
                      >
                        <div className="flex items-start justify-between gap-4">
                          <div className="flex-1 min-w-0">
                            <div className="flex items-center gap-3 mb-2">
                              {quizProgress[segment.id]?.completed ? (
                                <span className="flex items-center justify-center w-6 h-6 bg-green-100 text-green-700 rounded-full text-xs font-bold">
                                  ✓
                                </span>
                              ) : (
                                <span className="text-xs font-medium text-gray-400">{index + 1}</span>
                              )}
                              <h4 className="font-medium text-gray-900">{segment.title}</h4>
                              {segment.questions.length > 0 && (
                                <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded-full">
                                  {segment.questions.length} Quiz
                                </span>
                              )}
                              {quizProgress[segment.id] && !quizProgress[segment.id].completed && (
                                <span className="text-xs text-gray-500 font-medium">
                                  ({quizProgress[segment.id].correct}/{quizProgress[segment.id].total} Correct)
                                </span>
                              )}
                            </div>
                            <p className="text-sm text-gray-500 mb-2">
                              {formatTime(segment.startTime)} - {formatTime(segment.endTime)}
                            </p>
                            <p className="text-sm text-gray-600 line-clamp-2">{segment.transcript}</p>
                          </div>

                          <div className="flex gap-2 flex-shrink-0">
                            <button
                              onClick={() => playSegment({
                                segmentId: segment.id,
                                startTime: segment.startTime,
                                endTime: segment.endTime,
                              })}
                              className="px-3 py-1.5 bg-gray-900 hover:bg-gray-800 text-white rounded text-xs font-medium transition-colors"
                            >
                              Play
                            </button>
                            <button
                              onClick={() => openQuiz(segment)}
                              className="px-3 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded text-xs font-medium transition-colors"
                            >
                              Quiz
                            </button>

                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>

            {/* Sidebar */}
            <div className="lg:col-span-1 flex flex-col h-[calc(100vh-140px)] sticky top-6 border border-gray-200 rounded-lg overflow-hidden bg-white shadow-sm">
              <div className="flex-1 overflow-y-auto relative bg-gray-50">
                {activeSegmentId ? (
                  <div className="h-full flex flex-col">
                    {/* If we have a quiz segment loaded for the active segment, show it */}
                    {currentQuizSegment && currentQuizSegment.id === activeSegmentId ? (
                      <SidebarQuiz
                        segment={currentQuizSegment}
                        isLocked={!isQuizUnlocked}
                        videoTime={currentTime}
                        onComplete={(results) => {
                          console.log("Quiz completed, results:", results);
                          const progressData = {
                            correct: results.correct,
                            total: results.total,
                            completed: true
                          };

                          // Update local state
                          setQuizProgress(prev => ({
                            ...prev,
                            [activeSegmentId]: progressData
                          }));

                          // Save to backend
                          fetch('http://localhost:8000/video/progress', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                              video_id: video.id,
                              segment_id: activeSegmentId,
                              ...progressData
                            })
                          }).catch(err => console.error("Failed to save progress:", err));
                        }}
                      />
                    ) : (
                      // Loading state or "No Quiz" state
                      <div className="flex flex-col items-center justify-start pt-12 h-full text-gray-400 p-8 text-center space-y-4">
                        <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center mb-2 shadow-sm">
                          <Brain className="w-8 h-8 text-indigo-300" />
                        </div>
                        {isPlaying ? (
                          <>
                            <h3 className="font-semibold text-gray-700">Generating Quiz...</h3>
                            <p className="text-sm text-gray-500 max-w-[200px]">
                              We're analyzing the lecture content to create questions for you.
                            </p>
                            <div className="flex items-center gap-2 text-xs font-medium text-blue-600 bg-blue-50 px-3 py-1.5 rounded-full mt-2 animate-pulse">
                              <Sparkles className="w-3 h-3" />
                              <span>Generating automatically</span>
                            </div>
                          </>
                        ) : (
                          <>
                            <h3 className="font-semibold text-gray-700">No Quiz Available</h3>
                            <p className="text-sm text-gray-500 max-w-[200px]">
                              Select a segment from the chapters list to view or generate a quiz.
                            </p>
                          </>
                        )}
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="flex items-center justify-center h-full text-gray-500">
                    Select a segment to start
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div >

      {/* Question Modal */}
      {
        showQuestionModal && currentQuestion && (
          <QuestionModal
            question={currentQuestion}
            onClose={handleQuestionClose}
            onContinue={handleQuestionContinue}
          />
        )
      }

      {/* Quiz Modal */}
      {
        showQuizModal && currentQuizSegment && (
          <QuizModal
            segment={currentQuizSegment}
            onClose={() => {
              setShowQuizModal(false);
              setCurrentQuizSegment(null);
            }}
            onComplete={(results) => {
              if (currentQuizSegment) {
                const progressData = {
                  correct: results.correct,
                  total: results.total,
                  completed: results.correct === results.total
                };

                setQuizProgress(prev => ({
                  ...prev,
                  [currentQuizSegment.id]: progressData
                }));

                // Save to backend
                fetch('http://localhost:8000/video/progress', {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/json' },
                  body: JSON.stringify({
                    video_id: video.id,
                    segment_id: currentQuizSegment.id,
                    ...progressData
                  })
                }).catch(err => console.error("Failed to save progress:", err));
              }
            }}
          />
        )
      }
    </div >
  );
}
