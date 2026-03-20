package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sign_in_task")
public class SignInTask extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long courseId;
    private Long teacherId;
    private String taskTitle;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String taskStatus;
    private Integer penaltyScore;
}
