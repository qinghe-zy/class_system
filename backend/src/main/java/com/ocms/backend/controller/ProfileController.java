package com.ocms.backend.controller;

import com.ocms.backend.common.ApiResponse;
import com.ocms.backend.model.dto.ProfileUpdateRequest;
import com.ocms.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping
    public ApiResponse<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        userService.updateProfile(request);
        return ApiResponse.success(null);
    }
}
