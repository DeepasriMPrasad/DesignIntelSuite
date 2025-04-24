package com.quizmaster.controller;

import com.quizmaster.model.QuizResult;
import com.quizmaster.service.QuizRankingService;
import com.quizmaster.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for web views
 */
@Controller
public class WebController {

    private final QuizService quizService;
    private final QuizRankingService quizRankingService;

    @Autowired
    public WebController(QuizService quizService, 
                        @Qualifier("jpaQuizRankingService") QuizRankingService quizRankingService) {
        this.quizService = quizService;
        this.quizRankingService = quizRankingService;
    }

    /**
     * Home page (root context path)
     */
    @GetMapping(value = {"", "/", "/index", "/home"})
    public String home() {
        return "index";
    }

    /**
     * Quiz page - supports both /quiz and /quiz_new endpoints
     */
    @GetMapping({"/quiz", "/quiz_new"})
    public String quiz(@RequestParam(required = false) String sessionId, Model model) {
        model.addAttribute("sessionId", sessionId != null ? sessionId : "");
        return "quiz_new";
    }

    /**
     * Leaderboard page
     */
    @GetMapping("/leaderboard")
    public String leaderboard(@RequestParam(required = false) String userName, Model model) {
        // Get top 10 results
        List<QuizResult> topResults = quizRankingService.getTopResults(10);
        model.addAttribute("rankings", topResults);

        // Add the user's ranking if provided
        if (userName != null && !userName.isEmpty()) {
            model.addAttribute("userRanking", quizRankingService.getUserRanking(userName));
            model.addAttribute("userName", userName);
        }

        return "leaderboard";
    }
}
