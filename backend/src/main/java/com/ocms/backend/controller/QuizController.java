package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.model.dto.QuizCreateRequest;
import com.ocms.backend.model.dto.QuizSubmitRequest;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/teacher/quizzes")
    @RequireRole("TEACHER")
    public ApiResponse<Long> create(@Valid @RequestBody QuizCreateRequest request) {
        return ApiResponse.success(quizService.create(request));
    }

    @PutMapping("/teacher/quizzes/{quizId}")
    @RequireRole("TEACHER")
    public ApiResponse<Void> update(@PathVariable Long quizId, @Valid @RequestBody QuizCreateRequest request) {
        quizService.update(quizId, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/teacher/quizzes")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> teacherList(@RequestParam(required = false) Long sessionId) {
        return ApiResponse.success(quizService.teacherList(sessionId));
    }

    @GetMapping("/teacher/quizzes/{quizId}/submissions")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> teacherSubmissions(@PathVariable Long quizId) {
        return ApiResponse.success(quizService.teacherSubmissions(quizId));
    }

    @GetMapping("/teacher/quizzes/{quizId}/questions")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> teacherQuestions(@PathVariable Long quizId) {
        return ApiResponse.success(quizService.teacherQuestions(quizId));
    }

    @GetMapping("/student/quizzes")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> studentList(@RequestParam(required = false) Long sessionId) {
        return ApiResponse.success(quizService.studentList(sessionId));
    }

    @GetMapping("/student/quizzes/{quizId}/questions")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> questions(@PathVariable Long quizId) {
        return ApiResponse.success(quizService.quizQuestions(quizId));
    }

    @PostMapping("/student/quizzes/{quizId}/submit")
    @RequireRole("STUDENT")
    public ApiResponse<Map<String, Object>> submit(@PathVariable Long quizId, @Valid @RequestBody QuizSubmitRequest request) {
        return ApiResponse.success(quizService.submit(quizId, request));
    }

    @GetMapping("/student/quizzes/submissions")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> studentSubmissions() {
        return ApiResponse.success(quizService.studentSubmissions());
    }
}
