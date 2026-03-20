package com.ocms.backend.model.vo;

import lombok.Data;

@Data
public class BehaviorAnalyzeResult {
    private String behaviorStatus;
    private String behaviorType;
    private String statusDescription;
    private Integer activityScore;
    private Integer focusFlag;
    private Integer exceptionFlag;
    private String rawDataSummary;
}
