package com.ocms.backend.service;

import com.ocms.backend.mapper.BehaviorRecordMapper;
import com.ocms.backend.model.entity.BehaviorRecord;
import com.ocms.backend.model.vo.BehaviorAnalyzeResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BehaviorRecordService {

    private final BehaviorRecordMapper behaviorRecordMapper;

    public BehaviorRecordService(BehaviorRecordMapper behaviorRecordMapper) {
        this.behaviorRecordMapper = behaviorRecordMapper;
    }

    public Long saveRecord(Long studentId, Long teacherId, Long courseId, Long sessionId, BehaviorAnalyzeResult result) {
        BehaviorRecord record = new BehaviorRecord();
        record.setStudentId(studentId);
        record.setTeacherId(teacherId);
        record.setCourseId(courseId);
        record.setClassSessionId(sessionId);
        record.setDetectTime(LocalDateTime.now());
        record.setBehaviorStatus(result.getBehaviorStatus());
        record.setBehaviorType(result.getBehaviorType());
        record.setStatusDescription(result.getStatusDescription());
        record.setActivityScore(result.getActivityScore());
        record.setFocusFlag(result.getFocusFlag());
        record.setExceptionFlag(result.getExceptionFlag());
        record.setDataSource("FRONTEND_HEARTBEAT");
        record.setRawDataSummary(result.getRawDataSummary());
        behaviorRecordMapper.insert(record);
        return record.getId();
    }
}
