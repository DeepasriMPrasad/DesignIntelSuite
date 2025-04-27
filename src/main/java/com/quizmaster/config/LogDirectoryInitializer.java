package com.quizmaster.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Component that ensures the log directory structure exists on application startup.
 * This is needed for package-specific log files.
 */
@Component
public class LogDirectoryInitializer {

    private static final Logger logger = LoggerFactory.getLogger(LogDirectoryInitializer.class);
    private static final String[] LOG_DIRECTORIES = {
        "./logs"
    };

    /**
     * Initialize the log directories when the application starts
     */
    @PostConstruct
    public void init() {
        for (String directory : LOG_DIRECTORIES) {
            createDirectory(directory);
        }
    }

    /**
     * Create a directory if it doesn't exist
     * 
     * @param directoryPath The directory path to create
     */
    private void createDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created log directory: {}", path.toAbsolutePath());
            } else {
                logger.debug("Log directory already exists: {}", path.toAbsolutePath());
            }
            
            // Verify the directory is writeable
            File dir = path.toFile();
            if (!dir.canWrite()) {
                logger.warn("Log directory is not writable: {}", path.toAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Failed to create log directory: " + directoryPath, e);
        }
    }
}