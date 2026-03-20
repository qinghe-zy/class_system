package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseContentAuditRequest {
    @NotBlank
    private String auditStatus;
    private String auditRemark;
}
