package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank
    private String realName;
    private String gender;
    @NotBlank
    private String phone;
    private String email;
    @NotBlank
    private String departmentOrClass;
}
