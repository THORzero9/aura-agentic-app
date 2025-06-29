# Start from a lightweight official Python image.
FROM python:3.11-slim-buster

# Set the working directory inside the container.
WORKDIR /app

# Copy the requirements.txt file first.
COPY requirements.txt .

# Install the Python dependencies.
# We'll try explicitly upgrading pip and setuptools first, sometimes this helps with package resolution
RUN pip install --no-cache-dir --upgrade pip setuptools && \
    pip install --no-cache-dir -r requirements.txt

# Copy the rest of your application code into the container.
COPY . .

# Expose port 8080 (Cloud Run's default)
EXPOSE 8080

# === DEBUGGING STEPS: ===
# To diagnose if uvicorn is installed and in PATH, change the CMD temporarily.
# You can uncomment ONE of the following CMD lines for testing.
# REMEMBER TO REVERT TO THE ORIGINAL CMD AFTER DEBUGGING!

# CMD ["/bin/bash", "-c", "which uvicorn && uvicorn main:app --host 0.0.0.0 --port ${PORT}"]
# This tries to find uvicorn and then run it. If 'which uvicorn' fails, it will stop.

# CMD ["/bin/bash", "-c", "ls -l /usr/local/bin/uvicorn && uvicorn main:app --host 0.0.0.0 --port ${PORT}"]
# This checks if the uvicorn executable exists where pip usually puts it, then runs.

# CMD ["/bin/bash", "-c", "python -m pip show uvicorn && uvicorn main:app --host 0.0.0.0 --port ${PORT}"]
# This tries to show uvicorn's installation details, then runs.

# CMD ["/bin/bash", "-c", "echo 'Starting application...' && python -c 'import os; print(f\"PORT is {os.getenv(\"PORT\")}\"); from main import app; print(\"App loaded\");'"]
# This is a basic Python startup test, without uvicorn, to see if core imports work.
# If this works, the issue is likely with uvicorn's execution.

# === ORIGINAL CMD (UNCOMMENT THIS AFTER DEBUGGING) ===
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "${PORT}"]