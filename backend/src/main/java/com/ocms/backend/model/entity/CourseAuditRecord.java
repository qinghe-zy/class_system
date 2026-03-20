package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("course_audit_record")
public class CourseAuditRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long auditorId;
    private String auditStatus;
    private String remark;
    private LocalDateTime auditTime;
}
