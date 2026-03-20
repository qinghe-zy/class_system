package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuizSubmitRequest {
    @NotNull
    private Long sessionId;
    @NotNull
    private Long courseId;
    @NotNull
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull
        private Long questionId;
        @NotBlank
        private String answer;
    }
}
