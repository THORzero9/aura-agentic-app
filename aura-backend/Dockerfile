FROM python:3.11-slim-buster

WORKDIR /app

COPY requirements.txt .

RUN pip install --no-cache-dir --upgrade pip setuptools && \
    pip install --no-cache-dir -r requirements.txt

COPY . .

# Expose port 8080, which is Cloud Run's default.
EXPOSE 8080

# Use the shell form of CMD to ensure the ${PORT} environment variable is expanded.
CMD uvicorn main:app --host 0.0.0.0 --port ${PORT}