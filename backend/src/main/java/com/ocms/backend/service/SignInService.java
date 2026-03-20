package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ocms.backend.common.BizException;
import com.ocms.backend.mapper.ClassSessionMapper;
import com.ocms.backend.mapper.CourseEnrollmentMapper;
import com.ocms.backend.mapper.SignInPenaltyMapper;
import com.ocms.backend.mapper.SignInRecordMapper;
import com.ocms.backend.mapper.SignInTaskMapper;
import com.ocms.backend.mapper.SysUserMapper;
import com.ocms.backend.model.dto.SignInTaskRequest;
import com.ocms.backend.model.entity.ClassSession;
import com.ocms.backend.model.entity.CourseEnrollment;
import com.ocms.backend.model.entity.SignInPenalty;
import com.ocms.backend.model.entity.SignInRecord;
import com.ocms.backend.model.entity.SignInTask;
import com.ocms.backend.model.entity.SysUser;
import com.ocms.backend.security.AuthContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SignInService {

    private final SignInTaskMapper signInTaskMapper;
    private final SignInRecordMapper signInRecordMapper;
    private final SignInPenaltyMapper signInPenaltyMapper;
    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final SysUserMapper sysUserMapper;
    private final ClassSessionMapper classSessionMapper;

    public SignInService(SignInTaskMapper signInTaskMapper,
                         SignInRecordMapper signInRecordMapper,
                         SignInPenaltyMapper signInPenaltyMapper,
                         CourseEnrollmentMapper courseEnrollmentMapper,
                         SysUserMapper sysUserMapper,
                         ClassSessionMapper classSessionMapper) {
        this.signInTaskMapper = signInTaskMapper;
        this.signInRecordMapper = signInRecordMapper;
        this.signInPenaltyMapper = signInPenaltyMapper;
        this.courseEnrollmentMapper = courseEnrollmentMapper;
        this.sysUserMapper = sysUserMapper;
        this.classSessionMapper = classSessionMapper;
    }

    public Long createTask(SignInTaskRequest request) {
        ClassSession session = classSessionMapper.selectById(request.getSessionId());
        if (session == null) {
            throw new BizException(404, "课堂会话不存在");
        }
        if (!session.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "仅可对本人课堂会话发布签到");
        }
        if (!session.getCourseId().equals(request.getCourseId())) {
            throw new BizException("课堂会话与课程不匹配");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BizException("签到开始时间必须早于结束时间");
        }
        SignInTask task = new SignInTask();
        task.setSessionId(request.getSessionId());
        task.setCourseId(request.getCourseId());
        task.setTeacherId(AuthContext.userId());
        task.setTaskTitle(request.getTaskTitle());
        task.setStartTime(request.getStartTime());
        task.setEndTime(request.getEndTime());
        task.setTaskStatus(calcTaskStatus(request.getStartTime(), request.getEndTime(), LocalDateTime.now()));
        task.setPenaltyScore(request.getPenaltyScore() == null ? 5 : request.getPenaltyScore());
        signInTaskMapper.insert(task);
        return task.getId();
    }

    public List<Map<String, Object>> teacherTaskList(Long sessionId) {
        List<SignInTask> tasks = signInTaskMapper.selectList(new LambdaQueryWrapper<SignInTask>()
                .eq(SignInTask::getTeacherId, AuthContext.userId())
                .eq(sessionId != null, SignInTask::getSessionId, sessionId)
                .orderByDesc(SignInTask::getId));
        applyExpiredTaskPenalties(tasks.stream().map(SignInTask::getCourseId).collect(Collectors.toSet()));
        return tasks.stream().map(this::taskMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> studentAvailableTasks(Long courseId) {
        Long studentId = AuthContext.userId();
        Set<Long> courseIds = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                        .eq(CourseEnrollment::getStudentId, studentId)
                        .eq(CourseEnrollment::getStatus, 1))
                .stream().map(CourseEnrollment::getCourseId).collect(Collectors.toSet());
        if (courseIds.isEmpty()) {
            return List.of();
        }
        applyExpiredTaskPenalties(courseIds);

        List<SignInTask> tasks = signInTaskMapper.selectList(new LambdaQueryWrapper<SignInTask>()
                .in(SignInTask::getCourseId, courseIds)
                .eq(courseId != null, SignInTask::getCourseId, courseId)
                .orderByDesc(SignInTask::getId));
        return tasks.stream().map(task -> {
            Map<String, Object> map = taskMap(task);
            SignInRecord record = signInRecordMapper.selectOne(new LambdaQueryWrapper<SignInRecord>()
                    .eq(SignInRecord::getTaskId, task.getId())
                    .eq(SignInRecord::getStudentId, studentId)
                    .last("limit 1"));
            SignInPenalty penalty = signInPenaltyMapper.selectOne(new LambdaQueryWrapper<SignInPenalty>()
                    .eq(SignInPenalty::getTaskId, task.getId())
                    .eq(SignInPenalty::getStudentId, studentId)
                    .last("limit 1"));
            map.put("signed", record != null);
            map.put("signInStatus", record == null ? "UNSIGNED" : record.getSignInStatus());
            map.put("signInTime", record == null ? null : record.getSignInTime());
            map.put("expired", LocalDateTime.now().isAfter(task.getEndTime()));
            map.put("penaltyScore", penalty == null ? 0 : penalty.getPenaltyScore());
            map.put("penaltyReason", penalty == null ? null : penalty.getPenaltyReason());
            return map;
        }).collect(Collectors.toList());
    }

    public void studentSignIn(Long taskId) {
        SignInTask task = signInTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException(404, "签到任务不存在");
        }
        Long studentId = AuthContext.userId();
        CourseEnrollment enrollment = courseEnrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getCourseId, task.getCourseId())
                .eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getStatus, 1));
        if (enrollment == null) {
            throw new BizException(403, "未选修该课程");
        }

        if (LocalDateTime.now().isBefore(task.getStartTime())) {
            throw new BizException("签到尚未开始");
        }

        SignInRecord exists = signInRecordMapper.selectOne(new LambdaQueryWrapper<SignInRecord>()
                .eq(SignInRecord::getTaskId, taskId)
                .eq(SignInRecord::getStudentId, studentId)
                .last("limit 1"));
        if (exists != null) {
            return;
        }

        if (LocalDateTime.now().isAfter(task.getEndTime())) {
            createPenaltyIfAbsent(task, studentId, "签到过期未完成，按缺勤处理");
            throw new BizException("签到已过期，不允许签到，已记录惩罚");
        }

        SignInRecord record = new SignInRecord();
        record.setTaskId(taskId);
        record.setSessionId(task.getSessionId());
        record.setCourseId(task.getCourseId());
        record.setStudentId(studentId);
        record.setSignInTime(LocalDateTime.now());
        record.setSignInStatus("SIGNED");
        signInRecordMapper.insert(record);
    }

    public List<Map<String, Object>> teacherRecords(Long taskId) {
        SignInTask task = signInTaskMapper.selectById(taskId);
        if (task == null || !task.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "无权查看签到记录");
        }
        applyExpiredTaskPenalties(Set.of(task.getCourseId()));

        return signInRecordMapper.selectList(new LambdaQueryWrapper<SignInRecord>()
                        .eq(SignInRecord::getTaskId, taskId)
                        .orderByDesc(SignInRecord::getId))
                .stream().map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", record.getId());
                    map.put("taskId", record.getTaskId());
                    map.put("sessionId", record.getSessionId());
                    map.put("courseId", record.getCourseId());
                    map.put("studentId", record.getStudentId());
                    SysUser user = sysUserMapper.selectById(record.getStudentId());
                    map.put("studentName", user == null ? "" : user.getRealName());
                    map.put("signInTime", record.getSignInTime());
                    map.put("signInStatus", record.getSignInStatus());
                    return map;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> studentRecords() {
        return signInRecordMapper.selectList(new LambdaQueryWrapper<SignInRecord>()
                        .eq(SignInRecord::getStudentId, AuthContext.userId())
                        .orderByDesc(SignInRecord::getId))
                .stream().map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", record.getId());
                    map.put("taskId", record.getTaskId());
                    map.put("sessionId", record.getSessionId());
                    map.put("courseId", record.getCourseId());
                    map.put("signInTime", record.getSignInTime());
                    map.put("signInStatus", record.getSignInStatus());
                    return map;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> adminOverview() {
        applyExpiredTaskPenalties(null);
        return signInRecordMapper.selectList(new LambdaQueryWrapper<SignInRecord>().orderByDesc(SignInRecord::getId))
                .stream().map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", record.getId());
                    map.put("taskId", record.getTaskId());
                    map.put("sessionId", record.getSessionId());
                    map.put("courseId", record.getCourseId());
                    map.put("studentId", record.getStudentId());
                    map.put("signInTime", record.getSignInTime());
                    map.put("signInStatus", record.getSignInStatus());
                    return map;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> teacherPenalties(Long courseId) {
        applyExpiredTaskPenalties(courseId == null ? null : Set.of(courseId));
        return signInPenaltyMapper.selectList(new LambdaQueryWrapper<SignInPenalty>()
                        .inSql(SignInPenalty::getCourseId, "select id from course where teacher_id=" + AuthContext.userId())
                        .eq(courseId != null, SignInPenalty::getCourseId, courseId)
                        .orderByDesc(SignInPenalty::getId))
                .stream().map(this::penaltyMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> studentPenalties() {
        applyExpiredTaskPenalties(null);
        return signInPenaltyMapper.selectList(new LambdaQueryWrapper<SignInPenalty>()
                        .eq(SignInPenalty::getStudentId, AuthContext.userId())
                        .orderByDesc(SignInPenalty::getId))
                .stream().map(this::penaltyMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> adminPenalties() {
        applyExpiredTaskPenalties(null);
        return signInPenaltyMapper.selectList(new LambdaQueryWrapper<SignInPenalty>().orderByDesc(SignInPenalty::getId))
                .stream().map(this::penaltyMap).collect(Collectors.toList());
    }

    private void applyExpiredTaskPenalties(Set<Long> courseIds) {
        List<SignInTask> expiredTasks = signInTaskMapper.selectList(new LambdaQueryWrapper<SignInTask>()
                .lt(SignInTask::getEndTime, LocalDateTime.now())
                .in(courseIds != null && !courseIds.isEmpty(), SignInTask::getCourseId, courseIds)
                .orderByDesc(SignInTask::getId));
        for (SignInTask task : expiredTasks) {
            if (!"CLOSED".equalsIgnoreCase(task.getTaskStatus())) {
                task.setTaskStatus("CLOSED");
                signInTaskMapper.updateById(task);
            }
            List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                    .eq(CourseEnrollment::getCourseId, task.getCourseId())
                    .eq(CourseEnrollment::getStatus, 1));
            for (CourseEnrollment e : enrollments) {
                SignInRecord signed = signInRecordMapper.selectOne(new LambdaQueryWrapper<SignInRecord>()
                        .eq(SignInRecord::getTaskId, task.getId())
                        .eq(SignInRecord::getStudentId, e.getStudentId())
                        .last("limit 1"));
                if (signed == null) {
                    createPenaltyIfAbsent(task, e.getStudentId(), "任务截止未签到，自动记惩罚");
                }
            }
        }
    }

    private void createPenaltyIfAbsent(SignInTask task, Long studentId, String reason) {
        SignInPenalty exists = signInPenaltyMapper.selectOne(new LambdaQueryWrapper<SignInPenalty>()
                .eq(SignInPenalty::getTaskId, task.getId())
                .eq(SignInPenalty::getStudentId, studentId)
                .last("limit 1"));
        if (exists != null) {
            return;
        }
        SignInPenalty penalty = new SignInPenalty();
        penalty.setTaskId(task.getId());
        penalty.setCourseId(task.getCourseId());
        penalty.setStudentId(studentId);
        penalty.setPenaltyType("MISSED_SIGNIN");
        penalty.setPenaltyScore(task.getPenaltyScore() == null ? 5 : task.getPenaltyScore());
        penalty.setPenaltyReason(reason);
        penalty.setCreatedTime(LocalDateTime.now());
        signInPenaltyMapper.insert(penalty);
    }

    private Map<String, Object> taskMap(SignInTask task) {
        String realStatus = calcTaskStatus(task.getStartTime(), task.getEndTime(), LocalDateTime.now());
        if (!realStatus.equalsIgnoreCase(task.getTaskStatus())) {
            task.setTaskStatus(realStatus);
            signInTaskMapper.updateById(task);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("sessionId", task.getSessionId());
        map.put("courseId", task.getCourseId());
        map.put("teacherId", task.getTeacherId());
        map.put("taskTitle", task.getTaskTitle());
        map.put("startTime", task.getStartTime());
        map.put("endTime", task.getEndTime());
        map.put("taskStatus", realStatus);
        map.put("penaltyScore", task.getPenaltyScore());
        return map;
    }

    private Map<String, Object> penaltyMap(SignInPenalty p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("taskId", p.getTaskId());
        map.put("courseId", p.getCourseId());
        map.put("studentId", p.getStudentId());
        SysUser user = sysUserMapper.selectById(p.getStudentId());
        map.put("studentName", user == null ? "" : user.getRealName());
        map.put("penaltyType", p.getPenaltyType());
        map.put("penaltyScore", p.getPenaltyScore());
        map.put("penaltyReason", p.getPenaltyReason());
        map.put("createdTime", p.getCreatedTime());
        return map;
    }

    private String calcTaskStatus(LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        if (now.isBefore(start)) {
            return "NOT_STARTED";
        }
        if (now.isAfter(end)) {
            return "CLOSED";
        }
        return "OPEN";
    }
}
