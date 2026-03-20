package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseContentUpsertRequest {
    @NotNull
    private Long courseId;
    @NotBlank
    private String contentTitle;
    @NotBlank
    private String contentBody;
    private String attachmentName;
    private String attachmentUrl;
    private String attachmentType;
    @NotNull
    private Integer publishStatus;
}
