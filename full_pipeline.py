import os
import sys

# Add current directory to path so we can import local modules
current_dir = os.path.dirname(os.path.abspath(__file__))
if current_dir not in sys.path:
    sys.path.append(current_dir)

transcription_dir = os.path.join(current_dir, 'transcription')
if transcription_dir not in sys.path:
    sys.path.append(transcription_dir)

try:
    from transcription_pipeline import download_panopto_audio, transcribe_audio
    from process_transcript import process_transcript_file
except ImportError as e:
    print(f"Error importing modules: {e}")
    print("Ensure transcription_pipeline.py and process_transcript.py are in the same directory.")
    sys.exit(1)

def get_data(video_url: str, output_json_path: str = "chapters.json"):
    """
    Full pipeline: Video URL -> Audio -> Transcript -> Chapters JSON
    """
    print(f"--- Starting Pipeline for: {video_url} ---")
    
    # Define file paths
    # We use a fixed name for simplicity, or we could derive from video title if possible
    # For now, let's keep it consistent with the existing scripts
    audio_output = "lecture_audio.mp3"
    transcript_base = "full_transcript"
    transcript_file_with_ts = f"{transcript_base}_with_timestamps.txt"
    chapters_md = "chapters.md"
    chapters_json = output_json_path

    # Step 1: Download Audio
    if not os.path.exists(audio_output):
        print(f"Downloading audio to {audio_output}...")
        try:
            download_panopto_audio(video_url, audio_output)
        except Exception as e:
            print(f"Failed to download audio: {e}")
            raise
    else:
        print(f"Audio file {audio_output} already exists. Skipping download.")

    # Step 2: Transcribe
    if not os.path.exists(transcript_file_with_ts):
        print(f"Transcribing audio to {transcript_file_with_ts}...")
        try:
            transcribe_audio(audio_output, output_base_name=transcript_base)
        except Exception as e:
             print(f"Failed to transcribe: {e}")
             raise
    else:
        print(f"Transcript file {transcript_file_with_ts} already exists. Skipping transcription.")

    # Step 3: Process Transcript into Chapters
    print(f"Processing transcript to generate chapters...")
    try:
        chapters_data = process_transcript_file(transcript_file_with_ts, chapters_md, chapters_json)
    except Exception as e:
        print(f"Failed to process transcript: {e}")
        raise

    print(f"Pipeline Complete! Output in {output_json_path}")
    return chapters_data

if __name__ == "__main__":
    # Example URL
    test_url = "https://imperial.cloud.panopto.eu/Panopto/Pages/Viewer.aspx?id=ba149b5d-e639-4525-8a58-b3d40083a406"
    get_data(test_url)