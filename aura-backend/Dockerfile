# Start from a lightweight official Python image.
FROM python:3.11-slim-buster

# Set the working directory inside the container.
WORKDIR /app

# Copy the requirements.txt file first.
COPY requirements.txt .

# Install the Python dependencies.
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of your application code into the container.
COPY . .

# Expose port 8080 (this is Cloud Run's default, and where the $PORT env var points)
EXPOSE 8080

# Define the command that runs your FastAPI application using Uvicorn.
# Crucially, Uvicorn must listen on the port specified by the $PORT environment variable,
# which Cloud Run injects.
# The default is often 8080, but using $PORT makes it adaptable.
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "${PORT}"]