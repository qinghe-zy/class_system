package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpsertRequest {
    @NotBlank
    private String username;
    private String password;
    @NotBlank
    private String realName;
    private String gender;
    @NotBlank
    private String phone;
    private String email;
    @NotBlank
    private String departmentOrClass;
    @NotBlank
    private String role;
    @NotNull
    private Integer status;
}
