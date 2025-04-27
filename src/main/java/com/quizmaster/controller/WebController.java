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
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for web views
 */
@Controller
@Slf4j
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
        // Get top 15 results
        List<QuizResult> topResults = quizRankingService.getTopResults(15);
        model.addAttribute("rankings", topResults);
        
        // Get the total number of participants
        List<QuizResult> allResults = quizRankingService.getAllResults();
        model.addAttribute("totalParticipants", allResults.size());

        // Add the user's ranking if provided
        if (userName != null && !userName.isEmpty()) {
            model.addAttribute("userRanking", quizRankingService.getUserRanking(userName));
            model.addAttribute("userName", userName);
        }

        return "leaderboard";
    }
    
    /**
     * Admin Database Management page
     * Requires admin password parameter for access
     * Supports pagination for result entries
     */
    @GetMapping("/admin")
    public String adminDashboard(
            @RequestParam(required = true) String password,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        log.info("Accessing admin dashboard with page: {} and size: {}", page, size);
        
        // Check password (hardcoded for simplicity - same as in the UI)
        if (!"Donotdelete1#".equals(password)) {
            log.warn("Invalid admin password attempt");
            return "redirect:/leaderboard";
        }
        
        // Get all results for management
        List<QuizResult> allResults = quizRankingService.getAllResults();
        
        // Calculate total pages
        int totalItems = allResults.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        
        // Validate page range
        if (page < 0) {
            page = 0;
        } else if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
        }
        
        // Calculate start and end indices for the current page
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);
        
        // Get the sublist for the current page
        List<QuizResult> paginatedResults = 
            allResults.isEmpty() ? allResults : allResults.subList(startIndex, endIndex);
        
        // Add pagination information to the model
        model.addAttribute("allResults", paginatedResults);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", size);
        
        return "admin";
    }
}
