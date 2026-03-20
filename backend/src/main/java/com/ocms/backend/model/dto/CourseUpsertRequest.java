package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseUpsertRequest {
    @NotBlank
    private String courseName;
    @NotBlank
    private String courseCode;
    private String courseIntro;
    private String courseCover;
    private String contentSummary;
    private Integer publishStatus;
}
