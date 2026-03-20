package com.ocms.backend.service;

import com.ocms.backend.model.dto.BehaviorReportRequest;
import com.ocms.backend.model.vo.BehaviorAnalyzeResult;
import org.springframework.stereotype.Service;

@Service
public class BehaviorAnalyzeService {

    private final BehaviorRuleEngine behaviorRuleEngine;

    public BehaviorAnalyzeService(BehaviorRuleEngine behaviorRuleEngine) {
        this.behaviorRuleEngine = behaviorRuleEngine;
    }

    public BehaviorAnalyzeResult analyze(BehaviorReportRequest request) {
        return behaviorRuleEngine.evaluate(request);
    }
}
