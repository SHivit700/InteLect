import { useState, useCallback, useEffect } from 'react';
import { VideoLibrary } from './components/VideoLibrary';
import { VideoPlayer } from './components/VideoPlayer';
import { UploadModal } from './components/UploadModal';
import { ProcessingModal } from './components/ProcessingModal';

export interface Video {
  id: string;
  title: string;
  url: string;
  thumbnail: string;
  topic: string;
  uploadedAt: Date;
  videoFilename?: string;
}

export interface Segment {
  id: string;
  videoId: string;
  startTime: number;
  endTime: number;
  transcript: string;
  title: string;
  questions: Question[];
}

export interface Question {
  id: string;
  segmentId: string;
  question: string;
  diagram?: string;
  timestamp: number;
  options?: string[];
  correctAnswer?: number;
  type?: 'mcq' | 'short_answer';
  explanation?: string;
  difficulty?: 'easy' | 'medium' | 'hard';
}

export interface SegmentQueueItem {
  segmentId: string;
  startTime: number;
  endTime: number;
  playFrom?: number;
}

export default function App() {
  const [videos, setVideos] = useState<Video[]>([]);
  const [selectedVideo, setSelectedVideo] = useState<Video | null>(null);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [processingVideo, setProcessingVideo] = useState<{ url: string; title: string; topic: string } | null>(null);

  useEffect(() => {
    const fetchVideos = async () => {
      try {
        const response = await fetch('http://localhost:8000/video/feed');
        if (response.ok) {
          const data = await response.json();
          const mappedVideos: Video[] = data.videos.map((v: any) => ({
            id: v.video_id,
            title: v.lecture_title,
            url: v.lecture_url || '',
            thumbnail: 'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800', // Placeholder
            topic: v.lecture_topic,
            uploadedAt: new Date(v.created_at),
            videoFilename: v.video_filename
          }));
          setVideos(mappedVideos);
        }
      } catch (error) {
        console.error('Failed to fetch videos:', error);
      }
    };

    fetchVideos();
  }, []);


  const handleUpload = (metadata: { url: string; title: string; topic: string }) => {
    setShowUploadModal(false);
    setProcessingVideo(metadata);
  };

  const handleProcessingComplete = useCallback((video: Video) => {
    setVideos(prev => [video, ...prev]);
    setProcessingVideo(null);
  }, []);

  const handleVideoSelect = (video: Video) => {
    setSelectedVideo(video);
  };

  const handleBackToLibrary = () => {
    setSelectedVideo(null);
  };

  const handleCancelProcessing = useCallback(() => {
    setProcessingVideo(null);
  }, []);

  return (
    <div className="min-h-screen bg-white">
      {selectedVideo ? (
        <VideoPlayer video={selectedVideo} onBack={handleBackToLibrary} />
      ) : (
        <VideoLibrary
          videos={videos}
          onVideoSelect={handleVideoSelect}
          onUploadClick={() => setShowUploadModal(true)}
        />
      )}

      {showUploadModal && (
        <UploadModal
          onClose={() => setShowUploadModal(false)}
          onUpload={handleUpload}
        />
      )}

      {processingVideo && (
        <ProcessingModal
          metadata={processingVideo}
          onComplete={handleProcessingComplete}
          onCancel={handleCancelProcessing}
        />
      )}
    </div>
  );
}