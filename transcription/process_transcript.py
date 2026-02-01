import anthropic
import os
import re
import math
import json
from collections import Counter

# --- CONFIGURATION ---
input_file = "full_transcript_with_timestamps.txt"
output_file = "chapters.md"

def is_garbage(text):
    """
    Determines if a line of text is 'garbage' based on repetition and known patterns.
    Returns: True if garbage, False if valid.
    """
    text = text.strip()
    if not text:
        return True

    # 1. Known garbage substrings (case-insensitive)
    garbage_patterns = []
    text_lower = text.lower()
    
    # Check if the line IS just one of the garbage words (or very short garbage)
    if text.strip() in garbage_patterns:
        return True
        
    for pat in garbage_patterns:
        if pat.lower() in text_lower:
            # If the pattern appears significantly
            if text_lower.count(pat.lower()) >= 2: 
                return True
            # If line is short and contains garbage pattern
            if len(text) < 20: 
                return True

    # 2. Punctuation Spam (e.g. ''''''''')
    # Remove whitespace
    clean_chars = text.replace(" ", "")
    if len(clean_chars) > 10:
        unique_chars = set(clean_chars)
        # If very few unique characters compared to length (e.g. all quotes)
        if len(unique_chars) < 5 and len(clean_chars) > 20:
             return True

    # 3. Repetition Ratio
    # Split into words
    words = text.split()
    if len(words) == 0: return True
    
    unique_words = set(words)
    ratio = len(unique_words) / len(words)
    
    # Garbage often has very low ratio (e.g. 50 words, 3 unique)
    # Valid English sentence (e.g. 50 words) usually has > 0.5 ratio
    if len(words) > 6 and ratio < 0.4:
        return True
    
    # Extra check for single word lines that might be noise?
    if len(words) == 1 and words[0].lower() in ["blues", "gats"]:
        return True
        
    return False

def timestamp_to_seconds(ts_str):
    """
    Converts HH:MM:SS.mmm to seconds (float).
    """
    if not ts_str:
        return 0.0
    try:
        parts = ts_str.split(':')
        hours = float(parts[0])
        minutes = float(parts[1])
        seconds = float(parts[2])
        return hours * 3600 + minutes * 60 + seconds
    except ValueError:
        return 0.0

def parse_lines(file_path):
    """
    Reads file and returns only VALID lines.
    Returns list of dicts: {'start': 'HH:MM:SS.mmm', 'end': 'HH:MM:SS.mmm', 'text': 'Full Line Text', 'start_seconds': float, 'end_seconds': float}
    """
    valid_lines = []
    
    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            # Format: [Start -> End] Text
            # We want to identify the text part to check for garbage
            match = re.match(r'^\[(\d{2}:\d{2}:\d{2}\.\d{3})\s*(?:->|→)\s*(\d{2}:\d{2}:\d{2}\.\d{3})\](.*)', line)
            if match:
                start_ts = match.group(1)
                end_ts = match.group(2)
                text_content = match.group(3)
                
                if not is_garbage(text_content):
                    valid_lines.append({
                        "start": start_ts,
                        "end": end_ts,
                        "text": text_content.strip(),
                        "full_line": line.strip(),
                        "start_seconds": timestamp_to_seconds(start_ts),
                        "end_seconds": timestamp_to_seconds(end_ts)
                    })
            else:
                # Handle lines without timestamps (headers etc)?
                # For now, skip or include? 
                # If it's valid text, likely part of previous line?
                # The user said strict format. Let's ignore non-timestamped lines to be safe against metadata.
                pass
                
    return valid_lines

def process_transcript_file(input_file_path, output_md_path, output_json_path):
    if not os.path.exists(input_file_path):
        print(f"Error: {input_file_path} not found.")
        return []

    print("Step 1: Filtering garbage lines...")
    valid_lines = parse_lines(input_file_path)
    print(f"Found {len(valid_lines)} valid lines.")
    
    # Prepare text for Claude to segment
    transcript_for_ai = "\n".join([f"[{l['start']}] {l['text']}" for l in valid_lines])
    
    # DEBUG: Save the input we are sending to Claude
    with open("debug_cleaned_input.txt", "w", encoding="utf-8") as f:
        f.write(transcript_for_ai)
    
    system_prompt = (
        '''You are an expert editor. Your goal is to split a lecture transcript into logical chapters with descriptive titles. 
        Each chapter should be defined by its Start and End Timestamps.
        
        Here is a lecture transcript with timestamps.

YOUR TASK:
1. Filter out any garbage lines (e.g. 'yw'n', 'gats', 'ag ag', etc) that are non valid english text.
2. Identify logical topic content (Chapters).
3. For each chapter, provide:
   - **title**: A descriptive title (e.g. "Introduction to Neural Networks").
   - **start_timestamp**: The exact timestamp where this chapter begins.
   - **end_timestamp**: The exact timestamp where this chapter ends.

INSTRUCTIONS:
- Use the timestamps provided in the text `[HH:MM:SS.mmm]`.
- Start the first chapter at the first valid timestamp.
- End the last chapter at the last valid timestamp.
- Ensure no gaps between chapters if the content is continuous.

OUTPUT FORMAT:
Use the `submit_chapters` tool.

TRANSCRIPT:
{transcript_for_ai}

        '''
    )
    


    tool_schema = {
        "name": "submit_chapters",
        "description": "Submit identified chapters with time ranges.",
        "input_schema": {
            "type": "object",
            "properties": {
                "summary": {"type": "string", "description": "Concise summary of the whole lecture."},
                "chapters": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "title": {"type": "string"},
                            "start_timestamp": {"type": "string", "description": "Exact format HH:MM:SS.mmm"},
                            "end_timestamp": {"type": "string", "description": "Exact format HH:MM:SS.mmm"}
                        },
                        "required": ["title", "start_timestamp", "end_timestamp"]
                    }
                }
            },
            "required": ["summary", "chapters"]
        }
    }

    print("Step 2: Asking Claude to segment chapters (in chunks)...")
    
    # Chunking lines
    CHUNK_SIZE = 250
    chunks = [valid_lines[i:i + CHUNK_SIZE] for i in range(0, len(valid_lines), CHUNK_SIZE)]
    
    all_chapters = []
    
    for i, chunk in enumerate(chunks):
        print(f"Processing chunk {i+1}/{len(chunks)} ({len(chunk)} lines)...")
        chunk_text = "\n".join([f"[{l['start']}] {l['text']}" for l in chunk])
        
        chunk_prompt = f"""
Here is PART {i+1} of a lecture transcript.

YOUR TASK:
Identify the logical chapters within THIS SPECIFIC CHUNK.
Return the list of chapters found in this text.
If a chapter seems to start before this chunk or end after it, just give the start/end timestamp that appears IN THIS TEXT.

TRANSCRIPT CHUNK:
{chunk_text}
"""
        try:
            message = client.messages.create(
                model="claude-3-haiku-20240307",
                max_tokens=4096,
                temperature=0,
                system=system_prompt,
                tools=[tool_schema],
                tool_choice={"type": "tool", "name": "submit_chapters"},
                messages=[{"role": "user", "content": chunk_prompt}]
            )
            
            tool_input = None
            for block in message.content:
                if block.type == "tool_use" and block.name == "submit_chapters":
                    tool_input = block.input
                    break
            
            if tool_input:
                chunk_chapters = tool_input.get("chapters", [])
                all_chapters.extend(chunk_chapters)
            
        except Exception as e:
            print(f"Error processing chunk {i+1}: {e}")

    print("Step 3: Reconstructing final transcript...")
    
    final_md = "# Summary\n\n(Generated from segmented processing)\n\n"
    chapters_json_data = []
    
    # Remove duplicates or overlaps?
    # Simple approach: Trust the timestamps. Sort by start time.
    # If Claude returns chapters nicely, we just list them.
    # Note: If a chapter spans across chunks, we might get "Part 1" and "Part 2".
    # We can just concatenate them.
    
    # Sort chapters by start timestamp
    all_chapters.sort(key=lambda x: x['start_timestamp'])
    
    for idx, ch in enumerate(all_chapters, start=1):
        title = ch['title']
        t_start = ch['start_timestamp']
        t_end = ch['end_timestamp']
        
        final_md += f"## {title}\n\n"
        
        # Data structure for this chapter
        chapter_data = {
            "segment_number": idx,
            "segment_title": title,
            "segment_start_timestamp": timestamp_to_seconds(t_start),
            "segment_end_timestamp": timestamp_to_seconds(t_end),
            "transcript": []
        }
        
        chapter_text = []
        for line in valid_lines:
             # Check for overlap. We use string comparison for HH:MM:SS.mmm which works effectively
             # providing the format is consistent.
             if t_start <= line['start'] <= t_end:
                 chapter_text.append(line['full_line'])
                 
                 chapter_data["transcript"].append({
                     "start_timestamp": line.get('start_seconds', timestamp_to_seconds(line['start'])),
                     "end_timestamp": line.get('end_seconds', timestamp_to_seconds(line.get('end', ''))),
                     "text": line['text']
                 })
        
        chapters_json_data.append(chapter_data)
        
        if chapter_text:
             final_md += "\n".join(chapter_text) + "\n\n"
             
    with open(output_md_path, "w", encoding="utf-8") as f:
        f.write(final_md)
        
    # Save JSON output
    with open(output_json_path, "w", encoding="utf-8") as f:
        json.dump(chapters_json_data, f, indent=4)
        
    print(f"Success! Saved to {output_md_path} and {output_json_path}")
    return chapters_json_data

def main():
    if not os.path.exists(input_file):
        print(f"Error: {input_file} not found.")
        return
    process_transcript_file(input_file, output_file, "chapters.json")

if __name__ == "__main__":
    main()