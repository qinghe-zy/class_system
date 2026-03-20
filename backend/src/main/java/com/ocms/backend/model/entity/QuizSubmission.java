package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quiz_submission")
public class QuizSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long quizId;
    private Long sessionId;
    private Long courseId;
    private Long studentId;
    private String answerJson;
    private Integer score;
    private String submitStatus;
    private LocalDateTime submitTime;
}
