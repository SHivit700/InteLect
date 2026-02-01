import os
import yt_dlp
import mlx_whisper

# --- CONFIGURATION ---
video_url = "https://imperial.cloud.panopto.eu/Panopto/Pages/Viewer.aspx?id=906c7b79-4228-44db-8218-b34b00a5b3eb"
output_audio = "lecture_audio.mp3"

# Options: 'tiny', 'base', 'small', 'medium', 'large-v3'
model_size = "base"

# Timestamp modes
WORD_TIMESTAMPS = False  # True = word-level timestamps (slower, more detailed)


def format_timestamp(seconds: float) -> str:
    """Format seconds -> HH:MM:SS.mmm"""
    if seconds is None:
        seconds = 0.0
    hrs = int(seconds // 3600)
    mins = int((seconds % 3600) // 60)
    secs = seconds % 60
    return f"{hrs:02d}:{mins:02d}:{secs:06.3f}"


def download_panopto_audio(url: str, output_path: str) -> None:
    print("Step 1: Downloading audio from Panopto...")

    base_no_ext = os.path.splitext(output_path)[0]

    ydl_opts = {
        "format": "bestaudio/best",
        # Handles Imperial SSO by grabbing your active browser session
        "cookiesfrombrowser": ("chrome",),  # or ('firefox',) / ('edge',)
        "outtmpl": base_no_ext,  # yt-dlp will add extension(s)
        "postprocessors": [
            {
                "key": "FFmpegExtractAudio",
                "preferredcodec": "mp3",
                "preferredquality": "192",
            }
        ],
        # Optional: more robust in some cases
        "noplaylist": True,
        "quiet": False,
    }

    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        ydl.download([url])

    if not os.path.exists(output_path):
        # yt-dlp sometimes outputs slightly different names; you can add logic here if needed.
        raise FileNotFoundError(f"Expected output not found: {output_path}")

    print(f"Download complete: {output_path}")


def write_segment_transcript(segments, out_path: str) -> None:
    """Write segment-level timestamps + text."""
    with open(out_path, "w", encoding="utf-8") as f:
        for seg in segments:
            start = format_timestamp(seg.get("start", 0.0))
            end = format_timestamp(seg.get("end", 0.0))
            text = (seg.get("text") or "").strip()
            if text:
                f.write(f"[{start} → {end}] {text}\n")


def write_word_transcript(segments, out_path: str) -> None:
    """
    Write word-level timestamps.
    Keeps segment boundaries, but prints each word with timing.
    """
    with open(out_path, "w", encoding="utf-8") as f:
        for seg in segments:
            seg_start = format_timestamp(seg.get("start", 0.0))
            seg_end = format_timestamp(seg.get("end", 0.0))
            seg_text = (seg.get("text") or "").strip()

            f.write(f"\n=== Segment [{seg_start} → {seg_end}] ===\n")
            if seg_text:
                f.write(seg_text + "\n")

            words = seg.get("words", [])
            if not words:
                f.write("(No word timestamps returned — try WORD_TIMESTAMPS=True)\n")
                continue

            for w in words:
                w_text = (w.get("word") or "").strip()
                w_start = format_timestamp(w.get("start", 0.0))
                w_end = format_timestamp(w.get("end", 0.0))
                if w_text:
                    f.write(f"  [{w_start} → {w_end}] {w_text}\n")


def transcribe_audio(audio_path: str, output_base_name: str = "full_transcript"):
    print(f"Step 2: Transcribing with MLX Whisper ({model_size})...")
    print(f"Word timestamps: {WORD_TIMESTAMPS}")

    result = mlx_whisper.transcribe(
        audio_path,
        path_or_hf_repo=f"mlx-community/whisper-{model_size}-mlx",
        word_timestamps=WORD_TIMESTAMPS,
        verbose=False,
    )

    # Full plain text
    full_text = (result.get("text") or "").strip()
    with open(f"{output_base_name}.txt", "w", encoding="utf-8") as f:
        f.write(full_text + "\n")

    # Timestamped output
    segments = result.get("segments", [])
    if not segments:
        raise RuntimeError("No segments returned by transcription (unexpected).")

    write_segment_transcript(segments, f"{output_base_name}_with_timestamps.txt")

    if WORD_TIMESTAMPS:
        write_word_transcript(segments, f"{output_base_name}_word_timestamps.txt")

    print("Success!")
    print(f" - {output_base_name}.txt")
    print(f" - {output_base_name}_with_timestamps.txt")
    if WORD_TIMESTAMPS:
        print(f" - {output_base_name}_word_timestamps.txt")

    return result


if __name__ == "__main__":
    try:
        if not os.path.exists(output_audio):
            download_panopto_audio(video_url, output_audio)
        else:
            print(f"Audio file {output_audio} already exists, skipping download.")

        transcribe_audio(output_audio)

    except Exception as e:
        print(f"Error: {e}")