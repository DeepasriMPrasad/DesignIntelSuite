#!/bin/bash

# Cleanup any existing processes
echo "Cleaning up any existing processes..."
pkill -f java || echo "No Java processes to clean up"
pkill -f mvn || echo "No Maven processes to clean up"
sleep 2

echo "Starting Spring Boot application with memory optimization..."
# Set strict memory limits
export MAVEN_OPTS="-Xms128m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+UseStringDeduplication"
export _JAVA_OPTIONS="-Xms128m -Xmx512m -XX:+UseG1GC"

# Run the application with low memory profile
echo "Running with explicit memory constraints..."
mvn spring-boot:run &

# Store the PID
SPRING_PID=$!

# Wait for the application to start
echo "Waiting for Spring Boot to start (max 60 seconds)..."
MAX_WAIT=60
COUNTER=0
while [ $COUNTER -lt $MAX_WAIT ]; do
  if curl -s http://localhost:5000/quizmaster/api/quiz/health > /dev/null 2>&1; then
    echo "✅ Spring Boot started successfully on port 5000!"
    # Stay alive to keep the process running
    while true; do
      sleep 10
      if ! ps -p $SPRING_PID > /dev/null; then
        echo "❌ Spring Boot process has died, exiting"
        exit 1
      fi
      echo "🟢 Spring Boot is running... (health check)"
    done
    exit 0
  fi
  
  echo "Waiting for Spring Boot... ($COUNTER/$MAX_WAIT)"
  sleep 1
  COUNTER=$((COUNTER + 1))
done

# If we get here, startup failed
echo "❌ Spring Boot failed to start within $MAX_WAIT seconds"
kill $SPRING_PID 2>/dev/null
exit 1