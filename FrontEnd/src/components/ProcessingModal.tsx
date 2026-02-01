import { useEffect, useState, useRef } from 'react';
import { Loader2, Check, X, ChevronDown, ChevronUp } from 'lucide-react';
import type { Video } from '../App';

interface ProcessingModalProps {
  metadata: { url: string; title: string; topic: string };
  onComplete: (video: Video) => void;
  onCancel: () => void;
}

type ProcessingStatus = 'pending' | 'processing' | 'completed' | 'failed';

const API_BASE_URL = 'http://localhost:8000';

// Submit lecture to backend
async function submitLecture(lectureUrl: string, lectureTitle: string, lectureTopic: string, videoFilename?: string): Promise<string> {
  const response = await fetch(`${API_BASE_URL}/submit-lecture`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      lecture_url: lectureUrl,
      lecture_title: lectureTitle,
      lecture_topic: lectureTopic,
      video_filename: videoFilename,
    }),
  });

  if (!response.ok) {
    throw new Error(`Failed to submit lecture: ${response.status}`);
  }

  const data = await response.json();
  return data.job_id;
}

// Poll job status
async function checkJobStatus(jobId: string): Promise<{ status: ProcessingStatus; error?: string; video_filename?: string }> {
  const response = await fetch(`${API_BASE_URL}/job-status/${jobId}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  });

  if (!response.ok) {
    throw new Error(`Failed to check job status: ${response.status}`);
  }

  const data = await response.json();
  return {
    status: data.status,
    error: data.error,
    video_filename: data.video_filename,
  };
}

// Download video from Panopto URL
async function downloadVideo(url: string, onProgress?: (progress: number) => void): Promise<Blob> {
  const response = await fetch(url);

  if (!response.ok) {
    throw new Error(`Failed to download video: ${response.status}`);
  }

  const contentLength = response.headers.get('content-length');
  const total = contentLength ? parseInt(contentLength, 10) : 0;

  const reader = response.body?.getReader();
  if (!reader) {
    throw new Error('Failed to get response reader');
  }

  const chunks: BlobPart[] = [];
  let receivedLength = 0;

  while (true) {
    const { done, value } = await reader.read();

    if (done) break;

    chunks.push(value);
    receivedLength += value.length;

    if (total && onProgress) {
      onProgress((receivedLength / total) * 100);
    }
  }

  return new Blob(chunks);
}

// Upload video to backend
async function uploadVideo(videoBlob: Blob, filename: string, onProgress?: (progress: number) => void): Promise<string> {
  const formData = new FormData();
  formData.append('file', videoBlob, filename);

  const xhr = new XMLHttpRequest();

  return new Promise((resolve, reject) => {
    xhr.upload.addEventListener('progress', (e) => {
      if (e.lengthComputable && onProgress) {
        onProgress((e.loaded / e.total) * 100);
      }
    });

    xhr.addEventListener('load', () => {
      if (xhr.status === 200) {
        const data = JSON.parse(xhr.responseText);
        resolve(data.filename);
      } else {
        reject(new Error(`Upload failed: ${xhr.status}`));
      }
    });

    xhr.addEventListener('error', () => {
      reject(new Error('Upload failed'));
    });

    xhr.open('POST', `${API_BASE_URL}/upload-video`);
    xhr.send(formData);
  });
}

export function ProcessingModal({ metadata, onComplete, onCancel }: ProcessingModalProps) {
  const [status, setStatus] = useState<ProcessingStatus>('pending');
  const [isExpanded, setIsExpanded] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [currentStep, setCurrentStep] = useState<'processing'>('processing');

  const timeoutRef = useRef<NodeJS.Timeout | null>(null);
  const onCompleteRef = useRef(onComplete);
  const metadataRef = useRef(metadata);
  const jobIdRef = useRef<string | null>(null);

  // Update refs when props change without triggering effect
  useEffect(() => {
    onCompleteRef.current = onComplete;
    metadataRef.current = metadata;
  });

  useEffect(() => {
    let isMounted = true;

    const pollJobStatus = async (jobId: string) => {
      try {
        const result = await checkJobStatus(jobId);

        if (!isMounted) return;

        setStatus(result.status);

        // If processing is complete, create the video and call onComplete
        if (result.status === 'completed') {
          const completeTimeout = setTimeout(() => {
            if (!isMounted) return;

            const video: Video = {
              id: jobId,
              title: metadataRef.current.title,
              url: metadataRef.current.url,
              thumbnail: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800',
              topic: metadataRef.current.topic,
              uploadedAt: new Date(),
              videoFilename: result.video_filename,
            };
            onCompleteRef.current(video);
          }, 2000);

          timeoutRef.current = completeTimeout;
          return;
        }

        // If failed, show error
        if (result.status === 'failed') {
          setErrorMessage(result.error || 'Processing failed');
          return;
        }

        // If still processing or pending, continue polling
        if ((result.status === 'processing' || result.status === 'pending') && isMounted) {
          const pollInterval = setTimeout(() => pollJobStatus(jobId), 3000); // Poll every 3 seconds
          timeoutRef.current = pollInterval;
        }
      } catch (error) {
        console.error('Polling error:', error);
        if (isMounted) {
          // Continue polling even on error
          const pollInterval = setTimeout(() => pollJobStatus(jobId), 5000); // Wait longer on error
          timeoutRef.current = pollInterval;
        }
      }
    };

    const initializeProcessing = async () => {
      try {
        // Submit the lecture (backend will download video)
        setCurrentStep('processing');
        const jobId = await submitLecture(
          metadataRef.current.url,
          metadataRef.current.title,
          metadataRef.current.topic
        );

        if (!isMounted) return;

        jobIdRef.current = jobId;
        console.log('Job submitted with ID:', jobId);

        // Start polling
        pollJobStatus(jobId);
      } catch (error) {
        console.error('Failed to process lecture:', error);
        if (isMounted) {
          setStatus('failed');
          setErrorMessage(error instanceof Error ? error.message : 'Failed to process lecture');
        }
      }
    };

    // Start the process
    initializeProcessing();

    return () => {
      isMounted = false;
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
    };
  }, []);

  return (
    <div className="fixed bottom-6 right-6 z-50">
      <div className="bg-white rounded-lg shadow-2xl border border-gray-200 w-80">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-gray-200">
          <div className="flex items-center gap-3">
            {status === 'completed' ? (
              <div className="w-8 h-8 rounded-full bg-green-500 flex items-center justify-center">
                <Check className="w-5 h-5 text-white" />
              </div>
            ) : status === 'failed' ? (
              <div className="w-8 h-8 rounded-full bg-red-500 flex items-center justify-center">
                <X className="w-5 h-5 text-white" />
              </div>
            ) : (
              <Loader2 className="w-5 h-5 text-gray-900 animate-spin" />
            )}
            <div>
              <h3 className="font-semibold text-gray-900 text-sm">
                {status === 'completed' ? 'Processing Complete' : status === 'failed' ? 'Processing Failed' : 'Processing Video'}
              </h3>
              <p className="text-xs text-gray-500">{metadata.title}</p>
            </div>
          </div>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setIsExpanded(!isExpanded)}
              className="p-1 hover:bg-gray-100 rounded transition-colors"
            >
              {isExpanded ? (
                <ChevronDown className="w-4 h-4 text-gray-600" />
              ) : (
                <ChevronUp className="w-4 h-4 text-gray-600" />
              )}
            </button>
            {(status === 'processing' || status === 'pending') && (
              <button
                onClick={onCancel}
                className="p-1 hover:bg-gray-100 rounded transition-colors"
              >
                <X className="w-4 h-4 text-gray-600" />
              </button>
            )}
          </div>
        </div>

        {/* Expandable Content */}
        {isExpanded && (
          <div className="p-4">
            {status === 'completed' ? (
              <div className="text-center py-6">
                <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                  <Check className="w-6 h-6 text-green-600" />
                </div>
                <p className="text-sm font-medium text-gray-900 mb-1">Video processed successfully</p>
                <p className="text-xs text-gray-500">Your video is ready to watch</p>
              </div>
            ) : status === 'failed' ? (
              <div className="text-center py-6">
                <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-3">
                  <X className="w-6 h-6 text-red-600" />
                </div>
                <p className="text-sm font-medium text-gray-900 mb-1">Processing failed</p>
                <p className="text-xs text-gray-500">{errorMessage || 'Please try again later'}</p>
              </div>
            ) : (
              <div className="text-center py-8">
                <div className="relative inline-block mb-6">
                  <svg className="w-20 h-20 animate-spin" viewBox="0 0 100 100">
                    <circle
                      cx="50"
                      cy="50"
                      r="40"
                      fill="none"
                      stroke="#e5e7eb"
                      strokeWidth="8"
                    />
                    <circle
                      cx="50"
                      cy="50"
                      r="40"
                      fill="none"
                      stroke="#111827"
                      strokeWidth="8"
                      strokeDasharray="251.2"
                      strokeDashoffset="62.8"
                      strokeLinecap="round"
                      className="origin-center"
                    />
                  </svg>
                </div>
                <p className="text-sm font-medium text-gray-900 mb-2">Processing your video...</p>



                <p className="text-xs text-gray-500 mb-4">
                  This may take a few minutes. You can navigate away and processing will continue in the background.
                </p>
                <div className="bg-gray-50 rounded-lg p-3 text-xs text-gray-600">
                  <p className="mb-1">Current tasks:</p>
                  <ul className="space-y-1 text-left">
                    <li className="flex items-center gap-2">
                      <div className="w-1 h-1 bg-gray-900 rounded-full animate-pulse" />
                      Downloading and analyzing video
                    </li>
                    <li className="flex items-center gap-2">
                      <div className="w-1 h-1 bg-gray-900 rounded-full animate-pulse" style={{ animationDelay: '0.2s' }} />
                      Generating segments
                    </li>
                    <li className="flex items-center gap-2">
                      <div className="w-1 h-1 bg-gray-900 rounded-full animate-pulse" style={{ animationDelay: '0.4s' }} />
                      Creating quiz questions
                    </li>
                  </ul>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}