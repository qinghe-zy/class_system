package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocms.backend.common.BizException;
import com.ocms.backend.common.PageResult;
import com.ocms.backend.mapper.BehaviorRawDataMapper;
import com.ocms.backend.mapper.BehaviorRecordMapper;
import com.ocms.backend.mapper.ClassSessionMapper;
import com.ocms.backend.model.dto.BehaviorReportRequest;
import com.ocms.backend.model.entity.BehaviorRawData;
import com.ocms.backend.model.entity.BehaviorRecord;
import com.ocms.backend.model.entity.ClassSession;
import com.ocms.backend.model.vo.BehaviorAnalyzeResult;
import com.ocms.backend.security.AuthContext;
import com.ocms.backend.util.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BehaviorCollectService {

    private final BehaviorRawDataMapper behaviorRawDataMapper;
    private final BehaviorRecordMapper behaviorRecordMapper;
    private final ClassSessionMapper classSessionMapper;
    private final BehaviorAnalyzeService behaviorAnalyzeService;
    private final BehaviorRecordService behaviorRecordService;
    private final JsonUtil jsonUtil;

    public BehaviorCollectService(BehaviorRawDataMapper behaviorRawDataMapper,
                                  BehaviorRecordMapper behaviorRecordMapper,
                                  ClassSessionMapper classSessionMapper,
                                  BehaviorAnalyzeService behaviorAnalyzeService,
                                  BehaviorRecordService behaviorRecordService,
                                  JsonUtil jsonUtil) {
        this.behaviorRawDataMapper = behaviorRawDataMapper;
        this.behaviorRecordMapper = behaviorRecordMapper;
        this.classSessionMapper = classSessionMapper;
        this.behaviorAnalyzeService = behaviorAnalyzeService;
        this.behaviorRecordService = behaviorRecordService;
        this.jsonUtil = jsonUtil;
    }

    public Map<String, Object> report(BehaviorReportRequest request) {
        ClassSession session = classSessionMapper.selectById(request.getClassSessionId());
        if (session == null) {
            throw new BizException(404, "课堂会话不存在");
        }
        Long studentId = AuthContext.userId();

        BehaviorRawData rawData = new BehaviorRawData();
        rawData.setStudentId(studentId);
        rawData.setTeacherId(session.getTeacherId());
        rawData.setCourseId(request.getCourseId());
        rawData.setClassSessionId(request.getClassSessionId());
        rawData.setOnlineFlag(request.getOnlineFlag());
        rawData.setLastActiveTime(request.getLastActiveTime());
        rawData.setHeartbeatTime(request.getHeartbeatTime());
        rawData.setPageStayDurationSec(request.getPageStayDurationSec());
        rawData.setFocusFlag(request.getFocusFlag());
        rawData.setBlurDurationSec(request.getBlurDurationSec());
        rawData.setMouseClickCount(request.getMouseClickCount());
        rawData.setKeyInputCount(request.getKeyInputCount());
        rawData.setSignInFlag(request.getSignInFlag());
        rawData.setQuizJoinFlag(request.getQuizJoinFlag());
        rawData.setClassStayDurationSec(request.getClassStayDurationSec());
        rawData.setBaseActivityScore(request.getBaseActivityScore());
        rawData.setRawDataJson(jsonUtil.toJson(request));
        behaviorRawDataMapper.insert(rawData);

        BehaviorAnalyzeResult result = behaviorAnalyzeService.analyze(request);
        Long recordId = behaviorRecordService.saveRecord(studentId, session.getTeacherId(), request.getCourseId(), request.getClassSessionId(), result);

        Map<String, Object> response = new HashMap<>();
        response.put("rawId", rawData.getId());
        response.put("recordId", recordId);
        response.put("behaviorStatus", result.getBehaviorStatus());
        response.put("activityScore", result.getActivityScore());
        response.put("exceptionFlag", result.getExceptionFlag());
        return response;
    }

    public PageResult<Map<String, Object>> adminRecords(Integer pageNum, Integer pageSize, Long courseId, Long sessionId) {
        Page<BehaviorRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BehaviorRecord> wrapper = new LambdaQueryWrapper<BehaviorRecord>()
                .eq(courseId != null, BehaviorRecord::getCourseId, courseId)
                .eq(sessionId != null, BehaviorRecord::getClassSessionId, sessionId)
                .orderByDesc(BehaviorRecord::getId);
        Page<BehaviorRecord> result = behaviorRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords().stream().map(this::toMap).collect(Collectors.toList()));
    }

    public PageResult<Map<String, Object>> teacherRecords(Integer pageNum, Integer pageSize, Long courseId, Long sessionId) {
        Page<BehaviorRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BehaviorRecord> wrapper = new LambdaQueryWrapper<BehaviorRecord>()
                .eq(BehaviorRecord::getTeacherId, AuthContext.userId())
                .eq(courseId != null, BehaviorRecord::getCourseId, courseId)
                .eq(sessionId != null, BehaviorRecord::getClassSessionId, sessionId)
                .orderByDesc(BehaviorRecord::getId);
        Page<BehaviorRecord> result = behaviorRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords().stream().map(this::toMap).collect(Collectors.toList()));
    }

    public PageResult<Map<String, Object>> studentRecords(Integer pageNum, Integer pageSize, Long sessionId) {
        Page<BehaviorRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BehaviorRecord> wrapper = new LambdaQueryWrapper<BehaviorRecord>()
                .eq(BehaviorRecord::getStudentId, AuthContext.userId())
                .eq(sessionId != null, BehaviorRecord::getClassSessionId, sessionId)
                .orderByDesc(BehaviorRecord::getId);
        Page<BehaviorRecord> result = behaviorRecordMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getTotal(), result.getRecords().stream().map(this::toMap).collect(Collectors.toList()));
    }

    private Map<String, Object> toMap(BehaviorRecord r) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", r.getId());
        map.put("studentId", r.getStudentId());
        map.put("teacherId", r.getTeacherId());
        map.put("courseId", r.getCourseId());
        map.put("classSessionId", r.getClassSessionId());
        map.put("detectTime", r.getDetectTime());
        map.put("behaviorStatus", r.getBehaviorStatus());
        map.put("behaviorType", r.getBehaviorType());
        map.put("statusDescription", r.getStatusDescription());
        map.put("activityScore", r.getActivityScore());
        map.put("focusFlag", r.getFocusFlag());
        map.put("exceptionFlag", r.getExceptionFlag());
        map.put("dataSource", r.getDataSource());
        map.put("rawDataSummary", r.getRawDataSummary());
        map.put("createdTime", r.getCreatedTime());
        map.put("updatedTime", r.getUpdatedTime());
        return map;
    }
}
