package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sign_in_penalty")
public class SignInPenalty {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long courseId;
    private Long studentId;
    private String penaltyType;
    private Integer penaltyScore;
    private String penaltyReason;
    private LocalDateTime createdTime;
}
