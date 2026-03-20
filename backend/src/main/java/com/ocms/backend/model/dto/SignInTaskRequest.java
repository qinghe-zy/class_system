package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignInTaskRequest {
    @NotNull
    private Long sessionId;
    @NotNull
    private Long courseId;
    @NotBlank
    private String taskTitle;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
    private Integer penaltyScore;
}
