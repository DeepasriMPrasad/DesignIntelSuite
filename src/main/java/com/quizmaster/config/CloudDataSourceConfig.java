package com.quizmaster.config;

import javax.sql.DataSource;

import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class to handle Cloud Foundry database services.
 * Only activated when the 'prod' profile is active.
 */
@Configuration
@Profile("prod")
public class CloudDataSourceConfig extends AbstractCloudConfig {

    /**
     * Creates a DataSource bean that connects to a database service
     * bound to the application in Cloud Foundry.
     * 
     * @return DataSource configured from bound service
     */
    @Bean
    public DataSource dataSource() {
        // Automatically connect to a service named "quizmaster-db"
        return connectionFactory().dataSource("quizmaster-db");
    }
}