package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionUpsertRequest {
    @NotNull
    private Long courseId;
    @NotBlank
    private String sessionTitle;
    @NotNull
    private LocalDateTime sessionStartTime;
    @NotNull
    private LocalDateTime sessionEndTime;
    @NotBlank
    private String sessionStatus;
}
