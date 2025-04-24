
#!/bin/bash

# Start Spring Boot application in the background
echo "Starting Spring Boot application..."
mvn spring-boot:run &

# Wait for Spring Boot to be ready
echo "Waiting for Spring Boot to start..."
while ! nc -z localhost 5000; do
  sleep 1
done
echo "Spring Boot started successfully!"

# Start Streamlit
echo "Starting Streamlit application..."
streamlit run app.py --server.port 5000 --server.address 0.0.0.0
