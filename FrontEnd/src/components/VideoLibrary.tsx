import { Upload, Video as VideoIcon, Play } from 'lucide-react';
import type { Video } from '../App';

interface VideoLibraryProps {
  videos: Video[];
  onVideoSelect: (video: Video) => void;
  onUploadClick: () => void;
}

export function VideoLibrary({ videos, onVideoSelect, onUploadClick }: VideoLibraryProps) {
  return (
    <div className="min-h-screen bg-white flex items-center justify-center px-6 py-16">
      <div className="max-w-5xl w-full">
        {/* Hero Header */}
        <div className="text-center mb-16">
          <h1 className="text-6xl font-bold text-gray-900 mb-4 tracking-tight">
            Video Learning
          </h1>
          <p className="text-xl text-gray-500 mb-10 max-w-2xl mx-auto">
            AI-powered video segmentation with interactive quizzes
          </p>
          <button
            onClick={onUploadClick}
            className="inline-flex items-center gap-2 bg-gray-900 text-white px-8 py-4 rounded-lg hover:bg-gray-800 transition-colors font-medium"
          >
            <Upload className="w-5 h-5" />
            Upload Video
          </button>
        </div>

        {/* Video Grid */}
        {videos.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-4xl mx-auto">
            {videos.map((video) => (
              <div
                key={video.id}
                onClick={() => onVideoSelect(video)}
                className="group cursor-pointer"
              >
                <div className="relative aspect-video bg-gray-900 rounded-lg overflow-hidden mb-4 border border-gray-200">
                  <img
                    src={video.thumbnail}
                    alt={video.title}
                    className="w-full h-full object-cover group-hover:opacity-75 transition-opacity duration-300"
                  />
                  <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                    <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center shadow-xl">
                      <Play className="w-7 h-7 text-gray-900 ml-1" fill="currentColor" />
                    </div>
                  </div>
                </div>
                <div className="text-center">
                  <h3 className="font-semibold text-gray-900 mb-2 text-lg">{video.title}</h3>
                  <p className="text-sm text-gray-500">{video.topic}</p>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-24">
            <div className="w-20 h-20 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-8">
              <VideoIcon className="w-10 h-10 text-gray-400" />
            </div>
            <h3 className="text-2xl font-semibold text-gray-900 mb-3">No videos yet</h3>
            <p className="text-gray-500 mb-10 text-lg">Upload your first video to get started</p>
            <button
              onClick={onUploadClick}
              className="inline-flex items-center gap-2 bg-gray-900 text-white px-8 py-4 rounded-lg hover:bg-gray-800 transition-colors font-medium"
            >
              <Upload className="w-5 h-5" />
              Upload Video
            </button>
          </div>
        )}
      </div>
    </div>
  );
}