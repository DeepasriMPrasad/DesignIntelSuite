
#!/bin/bash

# Ensure Java is in PATH
export PATH=$PATH:/nix/var/nix/profiles/default/bin:/nix/store/*/bin

# Build the WAR file
echo "Building the JAR file..."
mvn clean package -DskipTests

# Start the Spring Boot application
echo "Starting Spring Boot application..."
java -jar target/quizmaster.jar --server.port=8080 --server.address=0.0.0.0 

