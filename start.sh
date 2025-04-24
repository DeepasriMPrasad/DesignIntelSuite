
#!/bin/bash

# Start Spring Boot application in the background
echo "Starting Spring Boot application..."
mvn spring-boot:run &
SPRING_PID=$!

# Wait for Spring Boot to be ready
echo "Waiting for Spring Boot to start (max 60 seconds)..."
MAX_WAIT=60
COUNTER=0
while ! curl -s http://localhost:5000/quizmaster/api/quiz/health > /dev/null && [ $COUNTER -lt $MAX_WAIT ]; do
  echo "Waiting for Spring Boot... ($COUNTER/$MAX_WAIT)"
  sleep 1
  COUNTER=$((COUNTER + 1))
done

if [ $COUNTER -lt $MAX_WAIT ]; then
  echo "Spring Boot started successfully!"
else
  echo "Spring Boot failed to start within $MAX_WAIT seconds"
  exit 1
fi

# Start Streamlit
echo "Starting Streamlit application..."
streamlit run app.py --server.port 5001 --server.address 0.0.0.0

# Cleanup when script exits
trap "kill $SPRING_PID" EXIT
