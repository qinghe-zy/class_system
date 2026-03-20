package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.security.RequireRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/permissions")
@RequireRole("ADMIN")
public class PermissionController {

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.success(List.of(
                Map.of("role", "ADMIN", "desc", "全局管理权限"),
                Map.of("role", "TEACHER", "desc", "课程与课堂教学管理权限"),
                Map.of("role", "STUDENT", "desc", "学习与课堂参与权限")
        ));
    }
}
