package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuditCourseRequest {
    @NotBlank
    private String auditStatus;
    private String remark;
}
