import { useEffect, useRef, forwardRef, useImperativeHandle } from 'react';

interface YouTubePlayerProps {
  videoId: string;
  onTimeUpdate?: (currentTime: number) => void;
  onDurationChange?: (duration: number) => void;
  onStateChange?: (isPlaying: boolean) => void;
}

export interface YouTubePlayerRef {
  play: () => void;
  pause: () => void;
  seekTo: (seconds: number) => void;
  getCurrentTime: () => number;
  getDuration: () => number;
  getPlayer: () => any;
}

export const YouTubePlayer = forwardRef<YouTubePlayerRef, YouTubePlayerProps>(
  ({ videoId, onTimeUpdate, onDurationChange, onStateChange }, ref) => {
    const containerRef = useRef<HTMLDivElement>(null);
    const playerRef = useRef<any>(null);
    const intervalRef = useRef<NodeJS.Timeout | null>(null);

    useImperativeHandle(ref, () => ({
      play: () => {
        if (playerRef.current) {
          playerRef.current.playVideo();
        }
      },
      pause: () => {
        if (playerRef.current) {
          playerRef.current.pauseVideo();
        }
      },
      seekTo: (seconds: number) => {
        if (playerRef.current) {
          playerRef.current.seekTo(seconds, true);
        }
      },
      getCurrentTime: () => {
        if (playerRef.current) {
          return playerRef.current.getCurrentTime() || 0;
        }
        return 0;
      },
      getDuration: () => {
        if (playerRef.current) {
          return playerRef.current.getDuration() || 0;
        }
        return 0;
      },
      getPlayer: () => playerRef.current,
    }));

    useEffect(() => {
      // Load YouTube IFrame API
      if (!(window as any).YT) {
        const tag = document.createElement('script');
        tag.src = 'https://www.youtube.com/iframe_api';
        const firstScriptTag = document.getElementsByTagName('script')[0];
        firstScriptTag.parentNode?.insertBefore(tag, firstScriptTag);
      }

      const onYouTubeIframeAPIReady = () => {
        if (!containerRef.current) return;

        playerRef.current = new (window as any).YT.Player(containerRef.current, {
          videoId: videoId,
          playerVars: {
            autoplay: 0,
            controls: 0,
            modestbranding: 1,
            rel: 0,
            showinfo: 0,
            fs: 1,
            playsinline: 1,
          },
          events: {
            onReady: (event: any) => {
              const duration = event.target.getDuration();
              if (onDurationChange) {
                onDurationChange(duration);
              }

              // Start time update interval
              intervalRef.current = setInterval(() => {
                if (playerRef.current && onTimeUpdate) {
                  const currentTime = playerRef.current.getCurrentTime();
                  onTimeUpdate(currentTime);
                }
              }, 250); // Update 4 times per second
            },
            onStateChange: (event: any) => {
              if (onStateChange) {
                // YT.PlayerState.PLAYING = 1, YT.PlayerState.PAUSED = 2
                const isPlaying = event.data === 1;
                onStateChange(isPlaying);
              }
            },
          },
        });
      };

      if ((window as any).YT && (window as any).YT.Player) {
        onYouTubeIframeAPIReady();
      } else {
        (window as any).onYouTubeIframeAPIReady = onYouTubeIframeAPIReady;
      }

      return () => {
        if (intervalRef.current) {
          clearInterval(intervalRef.current);
        }
        if (playerRef.current) {
          playerRef.current.destroy();
        }
      };
    }, [videoId]);

    return <div ref={containerRef} className="w-full h-full" />;
  }
);

YouTubePlayer.displayName = 'YouTubePlayer';
