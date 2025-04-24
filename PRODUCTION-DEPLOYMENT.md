# CXS Quiz Master - Production Deployment Guide

This guide explains how to deploy only the Spring Boot application component of the CXS Quiz Master to production.

## Prerequisites

- Java 17 or higher
- Maven
- Sufficient disk space for database storage
- Port 5000 available on the host

## Deployment Steps

1. **Clone or download the repository**

   Make sure you have the latest version of the code.

2. **Option 1: Manual Deployment**

   Run the deployment script:

   ```bash
   ./deploy_spring.sh
   ```

   This script will:
   - Build the Spring Boot application
   - Create the necessary data directory for the H2 database
   - Start the application with the production profile

3. **Option 2: Deploy as a System Service (Recommended for Production)**

   a. Edit the systemd service file to set your user and directory:

   ```bash
   vi quizmaster.service
   # Update the User and WorkingDirectory fields
   ```

   b. Copy the service file to the systemd directory:

   ```bash
   sudo cp quizmaster.service /etc/systemd/system/
   ```

   c. Build the application:

   ```bash
   mvn clean package -DskipTests
   ```

   d. Enable and start the service:

   ```bash
   sudo systemctl enable quizmaster
   sudo systemctl start quizmaster
   ```

   e. Check the service status:

   ```bash
   sudo systemctl status quizmaster
   ```

4. **Verify the deployment**

   After a successful deployment, the application will be running at:
   
   ```
   http://[your-server-ip]:5000/quizmaster/
   ```

## Production Configuration

The application uses the following production settings:

- **Port**: 5000
- **Context Path**: /quizmaster
- **Database**: File-based H2 database stored in ./data/quizmaster
- **API Documentation**: Available at /quizmaster/swagger-ui.html

## Admin Access

To access the admin features of the leaderboard:

1. Go to the leaderboard page at /quizmaster/leaderboard
2. Click on "Admin Login"
3. Enter the admin password: "Donotdelete1#"

## Database Access

The H2 database console is available at:

```
http://[your-server-ip]:5000/quizmaster/h2-console
```

Connection details:
- JDBC URL: jdbc:h2:file:./data/quizmaster
- Username: sa
- Password: password

**Note**: For security reasons, the H2 console is only accessible from the server where the application is running.

## Monitoring and Maintenance

### Logs

- If running manually: Application logs are available in the standard output
- If running as a service: Logs are available via systemd
  ```bash
  sudo journalctl -u quizmaster -f   # Follow logs in real-time
  sudo journalctl -u quizmaster -e   # Show the end of the log file
  ```

### Service Management

- Start the service: `sudo systemctl start quizmaster`
- Stop the service: `sudo systemctl stop quizmaster`
- Restart the service: `sudo systemctl restart quizmaster`
- Check status: `sudo systemctl status quizmaster`

### Data Management

- Database files are stored in the ./data directory
- No manual database migrations are needed (Hibernate will handle schema updates)
- To reset the database, stop the service and delete the ./data/quizmaster.* files

## Backup Strategy

It's recommended to periodically back up the ./data directory to preserve quiz results.

## Troubleshooting

If the application fails to start:

1. Check if port 5000 is already in use
2. Ensure Java 17+ is installed and configured
3. Verify that the ./data directory is writable
4. Check the application logs for specific error messages