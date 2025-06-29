FROM python:3.11-slim-buster

# Set the working directory inside the container.
# All subsequent commands (COPY, RUN, CMD) will be executed relative to this directory.
# This means your application files will end up in /app within the container.
WORKDIR /app

# Copy the requirements.txt file into the container at /app.
# This step is done early and separately to leverage Docker's build cache.
# If only your application code changes, but dependencies remain the same,
# Docker won't re-run the 'pip install' step.
COPY requirements.txt .

# Install the Python dependencies specified in requirements.txt.
# '--no-cache-dir' is a best practice to prevent pip from storing downloaded packages,
# which helps in keeping the final Docker image size down.
# '-r requirements.txt' tells pip to install packages listed in the file.
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of your application code into the container at /app.
# This includes your 'main.py' file, any other Python files it imports,
# and any other assets needed by your application.
COPY . .

# Declare the port on which your application will listen.
# This informs Docker and container orchestration platforms (like Google Cloud Run)
# that the container expects incoming connections on this specific port.
EXPOSE 8000

# Define the command that runs your application when the container starts.
# This is the entry point for your application within the container.
# 'uvicorn' is the ASGI server that runs FastAPI applications.
# 'main:app' specifies that Uvicorn should run the 'app' object
# (which is your FastAPI instance) found within the 'main.py' file.
# '--host 0.0.0.0' makes the server accessible from any network interface inside the container,
# which is necessary for Docker containers to be reachable.
# '--port 8000' ensures Uvicorn listens on the port we exposed earlier.
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]