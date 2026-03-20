package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.model.dto.SignInTaskRequest;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.SignInService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SignInController {

    private final SignInService signInService;

    public SignInController(SignInService signInService) {
        this.signInService = signInService;
    }

    @PostMapping("/teacher/signin/tasks")
    @RequireRole("TEACHER")
    public ApiResponse<Long> createTask(@Valid @RequestBody SignInTaskRequest request) {
        return ApiResponse.success(signInService.createTask(request));
    }

    @GetMapping("/teacher/signin/tasks")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> taskList(@RequestParam(required = false) Long sessionId) {
        return ApiResponse.success(signInService.teacherTaskList(sessionId));
    }

    @GetMapping("/teacher/signin/records")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> teacherRecords(@RequestParam Long taskId) {
        return ApiResponse.success(signInService.teacherRecords(taskId));
    }

    @PostMapping("/student/signin/{taskId}")
    @RequireRole("STUDENT")
    public ApiResponse<Void> studentSignIn(@PathVariable Long taskId) {
        signInService.studentSignIn(taskId);
        return ApiResponse.success(null);
    }

    @GetMapping("/student/signin/records")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> studentRecords() {
        return ApiResponse.success(signInService.studentRecords());
    }

    @GetMapping("/student/signin/tasks")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> studentTasks(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(signInService.studentAvailableTasks(courseId));
    }

    @GetMapping("/admin/signin/overview")
    @RequireRole("ADMIN")
    public ApiResponse<List<Map<String, Object>>> adminOverview() {
        return ApiResponse.success(signInService.adminOverview());
    }

    @GetMapping("/teacher/signin/penalties")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> teacherPenalties(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(signInService.teacherPenalties(courseId));
    }

    @GetMapping("/student/signin/penalties")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> studentPenalties() {
        return ApiResponse.success(signInService.studentPenalties());
    }

    @GetMapping("/admin/signin/penalties")
    @RequireRole("ADMIN")
    public ApiResponse<List<Map<String, Object>>> adminPenalties() {
        return ApiResponse.success(signInService.adminPenalties());
    }
}
