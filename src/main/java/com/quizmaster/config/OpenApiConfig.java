package com.quizmaster.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI quizMasterOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Quiz Master API")
                        .description("Spring Boot Quiz Master application with RESTful APIs")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Your Name")
                                .email("deepasri.m.prasad@sap.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}