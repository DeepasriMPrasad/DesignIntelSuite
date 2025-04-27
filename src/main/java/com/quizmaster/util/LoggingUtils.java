package com.quizmaster.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for standardized logging across the application.
 * Provides methods for different log levels with appropriate formatting.
 */
@Component
public class LoggingUtils {

    /**
     * Get a logger for the specified class.
     *
     * @param clazz The class to get the logger for
     * @return Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    /**
     * Log application startup information.
     * 
     * @param logger The logger to use
     * @param appName Application name
     * @param version Application version
     */
    public static void logApplicationStartup(Logger logger, String appName, String version) {
        logger.info("=========================================");
        logger.info("  Application: {} - Version: {}", appName, version);
        logger.info("  Spring Boot QuizMaster Application Started");
        logger.info("=========================================");
    }

    /**
     * Log API request information.
     * 
     * @param logger The logger to use
     * @param method HTTP method
     * @param endpoint API endpoint
     * @param params Request parameters (optional)
     */
    public static void logApiRequest(Logger logger, String method, String endpoint, Object... params) {
        if (params != null && params.length > 0) {
            logger.debug("API Request: {} {} - Params: {}", method, endpoint, params);
        } else {
            logger.debug("API Request: {} {}", method, endpoint);
        }
    }

    /**
     * Log database operations.
     * 
     * @param logger The logger to use
     * @param operation Type of operation (SELECT, INSERT, etc.)
     * @param entity Entity being operated on
     * @param details Additional details (optional)
     */
    public static void logDatabaseOperation(Logger logger, String operation, String entity, String details) {
        if (details != null && !details.isEmpty()) {
            logger.debug("Database: {} on {} - {}", operation, entity, details);
        } else {
            logger.debug("Database: {} on {}", operation, entity);
        }
    }

    /**
     * Log errors with standardized format.
     * 
     * @param logger The logger to use
     * @param message Error message
     * @param e Exception that occurred
     */
    public static void logError(Logger logger, String message, Throwable e) {
        logger.error("ERROR: {} - Exception: {} - Message: {}", 
                message, 
                e.getClass().getSimpleName(), 
                e.getMessage());
    }

    /**
     * Log application events.
     * 
     * @param logger The logger to use
     * @param event Event name/type
     * @param details Event details
     */
    public static void logEvent(Logger logger, String event, String details) {
        logger.info("EVENT: {} - {}", event, details);
    }
}