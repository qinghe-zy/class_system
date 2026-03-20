package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.common.PageResult;
import com.ocms.backend.model.dto.CourseUpsertRequest;
import com.ocms.backend.security.RequireRole;
import com.ocms.backend.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/courses")
@RequireRole("TEACHER")
public class TeacherCourseController {

    private final CourseService courseService;

    public TeacherCourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                                              @RequestParam(required = false) String keyword) {
        return ApiResponse.success(courseService.teacherCoursePage(pageNum, pageSize, keyword));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CourseUpsertRequest request) {
        return ApiResponse.success(courseService.createTeacherCourse(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @Valid @RequestBody CourseUpsertRequest request) {
        courseService.updateTeacherCourse(id, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.deleteTeacherCourse(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/{id}/students")
    public ApiResponse<List<Map<String, Object>>> students(@PathVariable Long id) {
        return ApiResponse.success(courseService.teacherStudents(id));
    }
}
