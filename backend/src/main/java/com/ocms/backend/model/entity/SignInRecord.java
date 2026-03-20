package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sign_in_record")
public class SignInRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long sessionId;
    private Long courseId;
    private Long studentId;
    private LocalDateTime signInTime;
    private String signInStatus;
}
