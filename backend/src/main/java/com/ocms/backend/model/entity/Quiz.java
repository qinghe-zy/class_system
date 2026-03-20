package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("quiz")
public class Quiz extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long courseId;
    private Long teacherId;
    private String quizTitle;
    private String quizDesc;
    private Integer totalScore;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String quizStatus;
}
