package com.ocms.backend.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuizCreateRequest {
    @NotNull
    private Long sessionId;
    @NotNull
    private Long courseId;
    @NotBlank
    private String quizTitle;
    private String quizDesc;
    @NotNull
    private Integer totalScore;
    @NotNull
    private LocalDateTime startTime;
    @NotNull
    private LocalDateTime endTime;
    @Valid
    private List<QuizQuestionRequest> questions;
}
