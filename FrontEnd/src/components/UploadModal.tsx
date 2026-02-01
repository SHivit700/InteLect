import { useState } from 'react';
import { X, Link } from 'lucide-react';

interface UploadModalProps {
  onClose: () => void;
  onUpload: (metadata: { url: string; title: string; topic: string }) => void;
}

export function UploadModal({ onClose, onUpload }: UploadModalProps) {
  const [url, setUrl] = useState('');
  const [title, setTitle] = useState('');
  const [topic, setTopic] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (url && title && topic) {
      onUpload({ url, title, topic });
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-lg max-w-xl w-full p-8 shadow-xl">
        <div className="flex items-center justify-between mb-8">
          <h2 className="text-2xl font-semibold text-gray-900">Add Video</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="space-y-5 mb-6">
            <div>
              <label htmlFor="url" className="block text-sm font-medium text-gray-900 mb-2">
                Video URL
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Link className="w-4 h-4 text-gray-400" />
                </div>
                <input
                  id="url"
                  type="url"
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                  placeholder="https://example.com/video.mp4"
                  required
                />
              </div>
            </div>

            <div>
              <label htmlFor="title" className="block text-sm font-medium text-gray-900 mb-2">
                Title
              </label>
              <input
                id="title"
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                placeholder="Introduction to Machine Learning"
                required
              />
            </div>

            <div>
              <label htmlFor="topic" className="block text-sm font-medium text-gray-900 mb-2">
                Topic
              </label>
              <input
                id="topic"
                type="text"
                value={topic}
                onChange={(e) => setTopic(e.target.value)}
                className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-gray-900 focus:border-transparent"
                placeholder="Computer Science"
                required
              />
            </div>
          </div>

          <div className="flex gap-3 justify-end">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors font-medium text-sm"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!url || !title || !topic}
              className="px-5 py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed font-medium text-sm"
            >
              Process Video
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}