package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizQuestionRequest {
    @NotBlank
    private String questionTitle;
    @NotBlank
    private String questionType;
    private String optionsJson;
    @NotBlank
    private String correctAnswer;
    @NotNull
    private Integer score;
    @NotNull
    private Integer sortNo;
}
