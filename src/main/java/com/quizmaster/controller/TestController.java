package com.quizmaster.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test-endpoint")
    public String test() {
        return "Application is running correctly!";
    }
}