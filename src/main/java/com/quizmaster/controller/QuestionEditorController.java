package com.quizmaster.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.quizmaster.model.Question;
import com.quizmaster.model.dto.AdminPasswordRequest;
import com.quizmaster.service.QuestionEditorService;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/admin")
@Slf4j
public class QuestionEditorController {

    private static final String ADMIN_PASSWORD = "Donotdelete1#";
    
    @Autowired
    private QuestionEditorService questionEditorService;
    
    @GetMapping("/questions")
    public String getQuestionsEditor(@RequestParam(required = false) String password, Model model) {
        if (password == null || !password.equals(ADMIN_PASSWORD)) {
            model.addAttribute("needsPassword", true);
            return "admin/password";
        }
        
        List<Question> questions = questionEditorService.getAllQuestions();
        model.addAttribute("questions", questions);
        model.addAttribute("password", password);
        return "admin/questions";
    }
    
    @GetMapping("/questions/edit/{id}")
    public String editQuestion(@PathVariable String id, @RequestParam String password, Model model) {
        if (!password.equals(ADMIN_PASSWORD)) {
            model.addAttribute("needsPassword", true);
            return "admin/password";
        }
        
        Question question = questionEditorService.getQuestionById(id);
        model.addAttribute("question", question);
        model.addAttribute("password", password);
        return "admin/edit_question";
    }
    
    @PostMapping("/questions/update")
    public String updateQuestion(@ModelAttribute Question question, @RequestParam String password, Model model) {
        if (!password.equals(ADMIN_PASSWORD)) {
            model.addAttribute("needsPassword", true);
            return "admin/password";
        }
        
        try {
            questionEditorService.updateQuestion(question);
            model.addAttribute("successMessage", "Question updated successfully!");
        } catch (Exception e) {
            log.error("Error updating question", e);
            model.addAttribute("errorMessage", "Error updating question: " + e.getMessage());
        }
        
        List<Question> questions = questionEditorService.getAllQuestions();
        model.addAttribute("questions", questions);
        model.addAttribute("password", password);
        return "admin/questions";
    }
    
    @PostMapping("/questions/add")
    public String addQuestion(@ModelAttribute Question question, @RequestParam String password, Model model) {
        if (!password.equals(ADMIN_PASSWORD)) {
            model.addAttribute("needsPassword", true);
            return "admin/password";
        }
        
        try {
            questionEditorService.addQuestion(question);
            model.addAttribute("successMessage", "Question added successfully!");
        } catch (Exception e) {
            log.error("Error adding question", e);
            model.addAttribute("errorMessage", "Error adding question: " + e.getMessage());
        }
        
        List<Question> questions = questionEditorService.getAllQuestions();
        model.addAttribute("questions", questions);
        model.addAttribute("password", password);
        return "admin/questions";
    }
    
    @GetMapping("/questions/new")
    public String newQuestion(@RequestParam String password, Model model) {
        if (!password.equals(ADMIN_PASSWORD)) {
            model.addAttribute("needsPassword", true);
            return "admin/password";
        }
        
        model.addAttribute("question", new Question());
        model.addAttribute("password", password);
        return "admin/new_question";
    }
    
    @PostMapping("/questions/delete/{id}")
    public String deleteQuestion(@PathVariable String id, @RequestParam String password, Model model) {
        if (!password.equals(ADMIN_PASSWORD)) {
            model.addAttribute("needsPassword", true);
            return "admin/password";
        }
        
        try {
            questionEditorService.deleteQuestion(id);
            model.addAttribute("successMessage", "Question deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting question", e);
            model.addAttribute("errorMessage", "Error deleting question: " + e.getMessage());
        }
        
        List<Question> questions = questionEditorService.getAllQuestions();
        model.addAttribute("questions", questions);
        model.addAttribute("password", password);
        return "admin/questions";
    }
    
    @PostMapping("/questions/saveToExcel")
    public String saveToExcel(@RequestParam String password, Model model) {
        if (!password.equals(ADMIN_PASSWORD)) {
            model.addAttribute("needsPassword", true);
            return "admin/password";
        }
        
        try {
            questionEditorService.saveQuestionsToExcel();
            model.addAttribute("successMessage", "Questions saved to Excel successfully!");
        } catch (IOException e) {
            log.error("Error saving questions to Excel", e);
            model.addAttribute("errorMessage", "Error saving to Excel: " + e.getMessage());
        }
        
        List<Question> questions = questionEditorService.getAllQuestions();
        model.addAttribute("questions", questions);
        model.addAttribute("password", password);
        return "admin/questions";
    }
    
    @PostMapping("/validate-password")
    @ResponseBody
    public ResponseEntity<?> validatePassword(@ModelAttribute AdminPasswordRequest passwordRequest) {
        if (passwordRequest.getPassword().equals(ADMIN_PASSWORD)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
        }
    }
}