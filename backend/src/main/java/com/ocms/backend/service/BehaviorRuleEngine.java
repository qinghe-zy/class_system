package com.ocms.backend.service;

import com.ocms.backend.model.dto.BehaviorReportRequest;
import com.ocms.backend.model.vo.BehaviorAnalyzeResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BehaviorRuleEngine {

    public BehaviorAnalyzeResult evaluate(BehaviorReportRequest request) {
        int abnormalCount = 0;
        int activityScore = Math.max(0, Math.min(100,
                request.getBaseActivityScore()
                        + request.getMouseClickCount() / 2
                        + request.getKeyInputCount() / 2
                        - request.getBlurDurationSec() / 10
                        - (request.getOnlineFlag() == 1 ? 0 : 30)));

        List<String> tags = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();

        if (request.getSignInFlag() == 0) {
            abnormalCount++;
            tags.add("未签到");
            descriptions.add("当前课堂未完成签到");
        }

        if (request.getFocusFlag() == 0 && request.getBlurDurationSec() >= 180) {
            abnormalCount++;
            tags.add("轻度分心");
            descriptions.add("页面长时间失焦");
        }

        if (request.getMouseClickCount() + request.getKeyInputCount() == 0 && request.getPageStayDurationSec() >= 300) {
            abnormalCount++;
            tags.add("长时间无操作");
            descriptions.add("已持续较长时间无交互");
        }

        if (request.getOnlineFlag() == 0 || request.getClassStayDurationSec() > 600 && request.getMouseClickCount() + request.getKeyInputCount() < 3) {
            abnormalCount++;
            tags.add("疑似挂机");
            descriptions.add("在线/交互指标显示可能挂机");
        }

        if (request.getQuizJoinFlag() == 0 && activityScore < 40) {
            abnormalCount++;
            tags.add("互动不足");
            descriptions.add("未参与测验且活跃度较低");
        }

        BehaviorAnalyzeResult result = new BehaviorAnalyzeResult();
        result.setActivityScore(activityScore);
        result.setFocusFlag(request.getFocusFlag());
        result.setExceptionFlag(abnormalCount > 0 ? 1 : 0);

        if (abnormalCount == 0) {
            result.setBehaviorStatus("正常学习");
            result.setBehaviorType("NORMAL");
            result.setStatusDescription("已签到，心跳正常，页面活跃");
        } else if (abnormalCount >= 3) {
            result.setBehaviorStatus("学习状态异常");
            result.setBehaviorType("HIGH_RISK");
            result.setStatusDescription(String.join("；", descriptions));
        } else {
            result.setBehaviorStatus(tags.get(0));
            result.setBehaviorType("WARNING");
            result.setStatusDescription(String.join("；", descriptions));
        }
        result.setRawDataSummary(String.format("online=%d,focus=%d,mouse=%d,key=%d,score=%d",
                request.getOnlineFlag(), request.getFocusFlag(), request.getMouseClickCount(), request.getKeyInputCount(), activityScore));
        return result;
    }
}
