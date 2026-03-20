package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.model.dto.SessionUpsertRequest;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/teacher/sessions")
    @RequireRole("TEACHER")
    public ApiResponse<Long> create(@Valid @RequestBody SessionUpsertRequest request) {
        return ApiResponse.success(sessionService.create(request));
    }

    @PutMapping("/teacher/sessions/{id}")
    @RequireRole("TEACHER")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody SessionUpsertRequest request) {
        sessionService.update(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/teacher/sessions")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> teacherList(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(sessionService.teacherList(courseId));
    }

    @GetMapping("/admin/sessions")
    @RequireRole("ADMIN")
    public ApiResponse<List<Map<String, Object>>> adminList() {
        return ApiResponse.success(sessionService.adminList());
    }

    @GetMapping("/student/sessions")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> studentList(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(sessionService.studentList(courseId));
    }
}
