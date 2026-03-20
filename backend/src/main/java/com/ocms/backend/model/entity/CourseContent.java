package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("course_content")
public class CourseContent extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long teacherId;
    private String contentTitle;
    private String contentBody;
    private String attachmentName;
    private String attachmentUrl;
    private String attachmentType;
    private String auditStatus;
    private Long auditorId;
    private String auditRemark;
    private Integer publishStatus;
}
