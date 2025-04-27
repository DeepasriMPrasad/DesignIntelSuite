package com.quizmaster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Security configuration for SAP BTP deployment.
 * Only activated when the 'prod' or 'production' profile is active.
 * 
 * NOTE: Uncomment and configure properly if SAP XSUAA authentication is needed.
 * This is a template that you would need to customize based on your actual
 * security requirements in SAP BTP.
 */
@Configuration
@EnableWebSecurity
@Profile({"prod", "production"})
public class SapSecurityConfig {

    /**
     * Configures security for the application.
     * This configuration is applicable when deployed to SAP BTP.
     * 
     * @param http the HttpSecurity to modify
     * @return the SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // This is a basic configuration - customize based on your security requirements
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/quizmaster/", "/quizmaster/static/**", 
                                 "/quizmaster/api/quiz/health").permitAll()
                // Allow admin API endpoints without authentication for development/testing
                .requestMatchers("/quizmaster/api/quiz/admin/**").permitAll()
                // Admin web endpoints still require authentication
                .requestMatchers("/quizmaster/admin/**").authenticated()
                // API endpoints can be configured based on your needs
                .requestMatchers("/quizmaster/api/**").permitAll()
                // Default deny
                .anyRequest().authenticated()
            );
        
        /*
         * For XSUAA authentication, you would typically add:
         * 
         * http.oauth2ResourceServer()
         *     .jwt()
         *     .jwtAuthenticationConverter(getJwtAuthenticationConverter());
         * 
         * And then implement the getJwtAuthenticationConverter() method.
         */
        
        return http.build();
    }
    
    /*
     * Example JWT converter method for XSUAA:
     * 
     * private Converter<Jwt, AbstractAuthenticationToken> getJwtAuthenticationConverter() {
     *     XsuaaJwtAuthenticationConverter converter = new XsuaaJwtAuthenticationConverter();
     *     return converter;
     * }
     */
}