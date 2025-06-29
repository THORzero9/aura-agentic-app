# Start from a lightweight official Python image.
FROM python:3.11-slim-buster

# Set the working directory inside the container.
WORKDIR /app

# Copy the requirements.txt file first.
COPY requirements.txt .

# Install the Python dependencies.
RUN pip install --no-cache-dir --upgrade pip setuptools && \
    pip install --no-cache-dir -r requirements.txt

# Copy the rest of your application code into the container.
COPY . .

# Expose port 8080 (Cloud Run's default, and where the $PORT env var points)
EXPOSE 8080

# === CORRECTED CMD LINE ===
# Use the shell form of CMD to ensure that the ${PORT} environment variable is expanded
# before Uvicorn is called. This is crucial for Uvicorn to receive a valid port number.
CMD uvicorn main:app --host 0.0.0.0 --port ${PORT}