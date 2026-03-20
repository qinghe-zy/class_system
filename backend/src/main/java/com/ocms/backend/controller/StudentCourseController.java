package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.common.PageResult;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/courses")
@RequireRole("STUDENT")
public class StudentCourseController {

    private final CourseService courseService;

    public StudentCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/available")
    public ApiResponse<PageResult<Map<String, Object>>> available(@RequestParam(defaultValue = "1") Integer pageNum,
                                                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                                                   @RequestParam(required = false) String keyword) {
        return ApiResponse.success(courseService.studentAvailableCourses(pageNum, pageSize, keyword));
    }

    @PostMapping("/{id}/enroll")
    public ApiResponse<Void> enroll(@PathVariable Long id) {
        courseService.enrollCourse(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/mine")
    public ApiResponse<List<Map<String, Object>>> mine() {
        return ApiResponse.success(courseService.studentMyCourses());
    }
}
