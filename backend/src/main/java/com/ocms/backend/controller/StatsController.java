package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/admin/stats/overview")
    @RequireRole("ADMIN")
    public ApiResponse<Map<String, Object>> adminOverview() {
        return ApiResponse.success(statsService.adminOverview());
    }

    @GetMapping("/admin/stats/abnormal-course")
    @RequireRole("ADMIN")
    public ApiResponse<List<Map<String, Object>>> adminAbnormalCourse() {
        return ApiResponse.success(statsService.adminAbnormalByCourse());
    }

    @GetMapping("/admin/stats/detection-trend")
    @RequireRole("ADMIN")
    public ApiResponse<List<Map<String, Object>>> adminDetectTrend() {
        return ApiResponse.success(statsService.adminDetectTrend());
    }

    @GetMapping("/teacher/stats/overview")
    @RequireRole("TEACHER")
    public ApiResponse<Map<String, Object>> teacherOverview(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(statsService.teacherOverview(courseId));
    }

    @GetMapping("/teacher/stats/session-trend")
    @RequireRole("TEACHER")
    public ApiResponse<List<Map<String, Object>>> teacherSessionTrend(@RequestParam(required = false) Long sessionId) {
        return ApiResponse.success(statsService.teacherSessionTrend(sessionId));
    }

    @GetMapping("/student/stats/overview")
    @RequireRole("STUDENT")
    public ApiResponse<Map<String, Object>> studentOverview() {
        return ApiResponse.success(statsService.studentOverview());
    }

    @GetMapping("/student/stats/activity-trend")
    @RequireRole("STUDENT")
    public ApiResponse<List<Map<String, Object>>> studentActivityTrend(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(statsService.studentActivityTrend(courseId));
    }
}
