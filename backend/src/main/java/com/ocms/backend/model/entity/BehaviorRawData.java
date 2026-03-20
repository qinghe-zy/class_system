package com.ocms.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("behavior_raw_data")
public class BehaviorRawData extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long teacherId;
    private Long courseId;
    private Long classSessionId;
    private Integer onlineFlag;
    private LocalDateTime lastActiveTime;
    private LocalDateTime heartbeatTime;
    private Integer pageStayDurationSec;
    private Integer focusFlag;
    private Integer blurDurationSec;
    private Integer mouseClickCount;
    private Integer keyInputCount;
    private Integer signInFlag;
    private Integer quizJoinFlag;
    private Integer classStayDurationSec;
    private Integer baseActivityScore;
    private String rawDataJson;
}
