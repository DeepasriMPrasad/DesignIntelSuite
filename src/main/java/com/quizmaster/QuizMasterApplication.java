package com.quizmaster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.quizmaster.util.LoggingUtils;

@SpringBootApplication
@ComponentScan(basePackages = "com.quizmaster")
@EnableScheduling
public class QuizMasterApplication extends SpringBootServletInitializer {

    private static final Logger logger = LoggingUtils.getLogger(QuizMasterApplication.class);
    private static final String APPLICATION_NAME = "CXS Quiz Master";
    private static final String APPLICATION_VERSION = "1.0.0";

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(QuizMasterApplication.class);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(QuizMasterApplication.class, args);
        
        // Log application startup information
        LoggingUtils.logApplicationStartup(logger, APPLICATION_NAME, APPLICATION_VERSION);
        
        // Log active profiles
        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        if (activeProfiles.length > 0) {
            logger.info("Active profiles: {}", String.join(", ", activeProfiles));
        } else {
            logger.info("No active profiles, using default configuration");
        }
        
        // Log important file paths
        logger.info("Log file location: ./quizmaster.log");
        logger.info("Package logs location: ./logs/");
        logger.info("Database location: ./data/quizmaster");
        logger.info("Questions file: ./questions.xlsx");
        logger.info("Results file: ./results.xlsx");
        
        // Add log examples for different log files
        Logger controllerLogger = LoggerFactory.getLogger("com.quizmaster.controller");
        controllerLogger.info("Controller logging initialized - This will go to ./logs/controller.log");
        
        Logger serviceLogger = LoggerFactory.getLogger("com.quizmaster.service");
        serviceLogger.info("Service logging initialized - This will go to ./logs/service.log");
        
        Logger repositoryLogger = LoggerFactory.getLogger("com.quizmaster.repository");
        repositoryLogger.info("Repository logging initialized - This will go to ./logs/repository.log");
    }
}
