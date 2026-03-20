package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course")
public class Course extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String courseName;
    private String courseCode;
    private String courseIntro;
    private String courseCover;
    private String contentSummary;
    private Long teacherId;
    private String auditStatus;
    private Integer publishStatus;
}
