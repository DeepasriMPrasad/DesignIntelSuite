package com.quizmaster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for development environment.
 * Only activated when the 'dev' or default profile is active.
 */
@Configuration
@EnableWebSecurity
@Profile({"dev", "default"})
public class DevSecurityConfig {

    /**
     * Configures security for the application in development mode.
     * This configuration allows unrestricted access to all endpoints.
     * 
     * @param http the HttpSecurity to modify
     * @return the SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll()
            );
                
        return http.build();
    }
}