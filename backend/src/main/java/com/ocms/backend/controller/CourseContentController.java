package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.model.dto.CourseContentAuditRequest;
import com.ocms.backend.model.dto.CourseContentUpsertRequest;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.CourseContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CourseContentController {

    private final CourseContentService courseContentService;

    public CourseContentController(CourseContentService courseContentService) {
        this.courseContentService = courseContentService;
    }

    @PostMapping("/teacher/course-contents")
    @RequireRole("TEACHER")
    public ApiResponse<Long> create(@Valid @RequestBody CourseContentUpsertRequest request) {
        return ApiResponse.success(courseContentService.teacherCreate(request));
    }

    @PutMapping("/teacher/course-contents/{id}")
    @RequireRole("TEACHER")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CourseContentUpsertRequest request) {
        courseContentService.teacherUpdate(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/teacher/course-contents")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> teacherList(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(courseContentService.teacherList(courseId));
    }

    @GetMapping("/admin/course-contents")
    @RequireRole("ADMIN")
    public ApiResponse<List<Map<String, Object>>> adminList(@RequestParam(required = false) String auditStatus) {
        return ApiResponse.success(courseContentService.adminList(auditStatus));
    }

    @PostMapping("/admin/course-contents/{id}/audit")
    @RequireRole("ADMIN")
    public ApiResponse<Void> audit(@PathVariable Long id, @Valid @RequestBody CourseContentAuditRequest request) {
        courseContentService.adminAudit(id, request);
        return ApiResponse.success(null);
    }

    @GetMapping("/student/course-contents")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> studentList(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(courseContentService.studentList(courseId));
    }
}
