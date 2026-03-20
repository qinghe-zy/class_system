package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.common.PageResult;
import com.ocms.backend.model.dto.BehaviorReportRequest;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.BehaviorCollectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class BehaviorController {

    private final BehaviorCollectService behaviorCollectService;

    public BehaviorController(BehaviorCollectService behaviorCollectService) {
        this.behaviorCollectService = behaviorCollectService;
    }

    @PostMapping("/student/behavior/report")
    @RequireRole("STUDENT")
    public ApiResponse<Map<String, Object>> report(@Valid @RequestBody BehaviorReportRequest request) {
        return ApiResponse.success(behaviorCollectService.report(request));
    }

    @GetMapping("/admin/behavior/records")
    @RequireRole("ADMIN")
    public ApiResponse<PageResult<Map<String, Object>>> adminRecords(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                      @RequestParam(defaultValue = "10") Integer pageSize,
                                                                      @RequestParam(required = false) Long courseId,
                                                                      @RequestParam(required = false) Long sessionId) {
        return ApiResponse.success(behaviorCollectService.adminRecords(pageNum, pageSize, courseId, sessionId));
    }

    @GetMapping("/teacher/behavior/records")
    @RequireRole("TEACHER")
    public ApiResponse<PageResult<Map<String, Object>>> teacherRecords(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                                                        @RequestParam(required = false) Long courseId,
                                                                        @RequestParam(required = false) Long sessionId) {
        return ApiResponse.success(behaviorCollectService.teacherRecords(pageNum, pageSize, courseId, sessionId));
    }

    @GetMapping("/student/behavior/records")
    @RequireRole("STUDENT")
    public ApiResponse<PageResult<Map<String, Object>>> studentRecords(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                                                        @RequestParam(required = false) Long sessionId) {
        return ApiResponse.success(behaviorCollectService.studentRecords(pageNum, pageSize, sessionId));
    }
}
