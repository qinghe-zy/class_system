package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("behavior_record")
public class BehaviorRecord extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long teacherId;
    private Long courseId;
    private Long classSessionId;
    private LocalDateTime detectTime;
    private String behaviorStatus;
    private String behaviorType;
    private String statusDescription;
    private Integer activityScore;
    private Integer focusFlag;
    private Integer exceptionFlag;
    private String dataSource;
    private String rawDataSummary;
}
