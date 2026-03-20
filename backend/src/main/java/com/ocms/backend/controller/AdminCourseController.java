package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.common.PageResult;
import com.ocms.backend.model.dto.AuditCourseRequest;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/courses")
@RequireRole("ADMIN")
public class AdminCourseController {

    private final CourseService courseService;

    public AdminCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                                              @RequestParam(required = false) String keyword,
                                                              @RequestParam(required = false) String auditStatus) {
        return ApiResponse.success(courseService.adminCoursePage(pageNum, pageSize, keyword, auditStatus));
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable Long id, @Valid @RequestBody AuditCourseRequest request) {
        courseService.auditCourse(id, request);
        return ApiResponse.success(null);
    }
}
