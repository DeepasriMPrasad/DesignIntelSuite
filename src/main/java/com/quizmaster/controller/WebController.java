package com.quizmaster.controller;

import com.quizmaster.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for web views
 */
@Controller
public class WebController {

    private final QuizService quizService;

    @Autowired
    public WebController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * Home page (root context path)
     */
    @GetMapping(value = {"", "/", "/index", "/home"})
    public String home() {
        return "index";
    }

    /**
     * Quiz page
     */
    @GetMapping("/quiz")
    public String quiz(@RequestParam(required = false) String sessionId, Model model) {
        model.addAttribute("sessionId", sessionId != null ? sessionId : "");
        return "quiz";
    }
}