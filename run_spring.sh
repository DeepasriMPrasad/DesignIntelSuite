#!/bin/bash

# Cleanup any existing processes
echo "Cleaning up any existing processes..."
pkill -f java || echo "No Java processes to clean up"
pkill -f mvn || echo "No Maven processes to clean up"
sleep 2

# Remove any lock files or temporary files that might cause issues
rm -f ./*.lock 2>/dev/null
rm -f ./quizmaster.log 2>/dev/null

# Set strict but reasonable memory limits
echo "Starting Spring Boot application with optimized settings..."
export MAVEN_OPTS="-Xms256m -Xmx1024m"
export _JAVA_OPTIONS="-Xms256m -Xmx1024m -Dspring.config.location=classpath:/application.properties"

# Use production mode with better compatibility
export SPRING_PROFILES_ACTIVE=production

# Fix any potential database locks by making sure the database is properly closed
if [ -f "./data/quizmaster.mv.db" ]; then
  echo "Ensuring database is properly closed..."
  java -cp ./target/quizmaster.jar org.h2.tools.Shell -url jdbc:h2:file:./data/quizmaster -user sa -password password -sql "SHUTDOWN COMPACT" > /dev/null 2>&1 || echo "No need to shut down database"
  echo "Database prepared."
fi

# Run the application directly using java instead of Maven
echo "Starting Spring Boot application..."
java -jar target/quizmaster.jar --server.port=5000 &

# Store the PID
SPRING_PID=$!

# Wait for the application to start with a reliable health check
echo "Waiting for Spring Boot to start..."
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
  fi
  
  # Check if Spring Boot is still running
  if ! ps -p $SPRING_PID > /dev/null; then
    echo "❌ Spring Boot process died during startup"
    cat quizmaster.log | tail -n 20
    exit 1
  fi
  
  echo "Waiting for Spring Boot... ($COUNTER/$MAX_WAIT)"
  sleep 1
  COUNTER=$((COUNTER + 1))
done

# If we get here, startup failed
echo "❌ Spring Boot failed to start within $MAX_WAIT seconds"
echo "Showing last 20 lines of the log:"
cat quizmaster.log | tail -n 20
kill $SPRING_PID 2>/dev/null
exit 1