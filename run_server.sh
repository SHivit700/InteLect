#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Set up virtual environment
VENV_DIR="venv"
if [ ! -d "$VENV_DIR" ]; then
    echo "Creating virtual environment..."
    python3 -m venv "$VENV_DIR"
fi

# Activate virtual environment
source "$VENV_DIR/bin/activate"

# Install dependencies
echo "Installing dependencies..."
pip install -r requirements_local.txt

# Set PYTHONPATH to include the root directory and transcription directory
export PYTHONPATH="$SCRIPT_DIR:$SCRIPT_DIR/transcription"

# Run the server
echo "Starting FastAPI server..."
python api-server/main.py
