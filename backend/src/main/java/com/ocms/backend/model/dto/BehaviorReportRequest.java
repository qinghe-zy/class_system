package com.ocms.backend.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BehaviorReportRequest {
    @NotNull
    private Long classSessionId;
    @NotNull
    private Long courseId;
    @NotNull
    private Integer onlineFlag;
    @NotNull
    private LocalDateTime lastActiveTime;
    @NotNull
    private LocalDateTime heartbeatTime;
    @NotNull
    private Integer pageStayDurationSec;
    @NotNull
    private Integer focusFlag;
    @NotNull
    private Integer blurDurationSec;
    @NotNull
    private Integer mouseClickCount;
    @NotNull
    private Integer keyInputCount;
    @NotNull
    private Integer signInFlag;
    @NotNull
    private Integer quizJoinFlag;
    @NotNull
    private Integer classStayDurationSec;
    @NotNull
    private Integer baseActivityScore;
}
