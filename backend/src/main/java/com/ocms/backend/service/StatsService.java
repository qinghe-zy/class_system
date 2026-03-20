package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ocms.backend.mapper.*;
import com.ocms.backend.model.entity.*;
import com.ocms.backend.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final CourseMapper courseMapper;
    private final ClassSessionMapper classSessionMapper;
    private final BehaviorRecordMapper behaviorRecordMapper;
    private final SignInRecordMapper signInRecordMapper;
    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final QuizSubmissionMapper quizSubmissionMapper;

    public StatsService(SysUserMapper sysUserMapper,
                        SysUserRoleMapper sysUserRoleMapper,
                        SysRoleMapper sysRoleMapper,
                        CourseMapper courseMapper,
                        ClassSessionMapper classSessionMapper,
                        BehaviorRecordMapper behaviorRecordMapper,
                        SignInRecordMapper signInRecordMapper,
                        CourseEnrollmentMapper courseEnrollmentMapper,
                        QuizSubmissionMapper quizSubmissionMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.courseMapper = courseMapper;
        this.classSessionMapper = classSessionMapper;
        this.behaviorRecordMapper = behaviorRecordMapper;
        this.signInRecordMapper = signInRecordMapper;
        this.courseEnrollmentMapper = courseEnrollmentMapper;
        this.quizSubmissionMapper = quizSubmissionMapper;
    }

    public Map<String, Object> adminOverview() {
        Map<String, Object> data = new HashMap<>();
        long userTotal = sysUserMapper.selectCount(new LambdaQueryWrapper<>());
        long courseTotal = courseMapper.selectCount(new LambdaQueryWrapper<>());
        long sessionTotal = classSessionMapper.selectCount(new LambdaQueryWrapper<>());
        long detectTotal = behaviorRecordMapper.selectCount(new LambdaQueryWrapper<>());
        long abnormalTotal = behaviorRecordMapper.selectCount(new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getExceptionFlag, 1));

        Map<String, Long> roleCount = roleCountMap();
        data.put("userTotal", userTotal);
        data.put("teacherTotal", roleCount.getOrDefault("TEACHER", 0L));
        data.put("studentTotal", roleCount.getOrDefault("STUDENT", 0L));
        data.put("courseTotal", courseTotal);
        data.put("sessionTotal", sessionTotal);
        data.put("detectTotal", detectTotal);
        data.put("abnormalTotal", abnormalTotal);
        return data;
    }

    public List<Map<String, Object>> adminAbnormalByCourse() {
        QueryWrapper<BehaviorRecord> wrapper = new QueryWrapper<>();
        wrapper.select("course_id as courseId", "count(1) as abnormalCount")
                .eq("exception_flag", 1)
                .groupBy("course_id");
        List<Map<String, Object>> rows = behaviorRecordMapper.selectMaps(wrapper);
        return rows.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            Long courseId = ((Number) row.get("courseId")).longValue();
            Course course = courseMapper.selectById(courseId);
            item.put("courseId", courseId);
            item.put("courseName", course == null ? "未知课程" : course.getCourseName());
            item.put("abnormalCount", ((Number) row.get("abnormalCount")).longValue());
            return item;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> adminDetectTrend() {
        QueryWrapper<BehaviorRecord> wrapper = new QueryWrapper<>();
        wrapper.select("date(detect_time) as date", "count(1) as total")
                .groupBy("date(detect_time)")
                .orderByAsc("date(detect_time)");
        return behaviorRecordMapper.selectMaps(wrapper).stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("date", row.get("date"));
            item.put("total", ((Number) row.get("total")).longValue());
            return item;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> teacherOverview(Long courseId) {
        Long teacherId = AuthContext.userId();
        List<Long> courseIds;
        if (courseId != null) {
            courseIds = List.of(courseId);
        } else {
            courseIds = courseMapper.selectList(new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, teacherId)).stream().map(Course::getId).toList();
        }
        if (courseIds.isEmpty()) {
            return Map.of(
                    "signRate", 0,
                    "quizJoinRate", 0,
                    "quizAvgScore", 0,
                    "abnormalStudentCount", 0,
                    "behaviorDistribution", List.of()
            );
        }

        long enrollCount = courseEnrollmentMapper.selectCount(new LambdaQueryWrapper<CourseEnrollment>().in(CourseEnrollment::getCourseId, courseIds).eq(CourseEnrollment::getStatus, 1));
        long signCount = signInRecordMapper.selectCount(new LambdaQueryWrapper<SignInRecord>().in(SignInRecord::getCourseId, courseIds));
        long quizSubmitCount = quizSubmissionMapper.selectCount(new LambdaQueryWrapper<QuizSubmission>().in(QuizSubmission::getCourseId, courseIds));
        List<QuizSubmission> submissions = quizSubmissionMapper.selectList(new LambdaQueryWrapper<QuizSubmission>().in(QuizSubmission::getCourseId, courseIds));
        double avgScore = submissions.stream().mapToInt(s -> s.getScore() == null ? 0 : s.getScore()).average().orElse(0);

        List<BehaviorRecord> records = behaviorRecordMapper.selectList(new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getTeacherId, teacherId)
                .in(BehaviorRecord::getCourseId, courseIds));
        long abnormalStudentCount = records.stream().filter(r -> r.getExceptionFlag() != null && r.getExceptionFlag() == 1).map(BehaviorRecord::getStudentId).distinct().count();

        Map<String, Long> statusCount = records.stream().collect(Collectors.groupingBy(BehaviorRecord::getBehaviorStatus, Collectors.counting()));
        List<Map<String, Object>> dist = statusCount.entrySet().stream().map(e -> {
            Map<String, Object> item = new HashMap<>();
            item.put("status", e.getKey());
            item.put("count", e.getValue());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("signRate", enrollCount == 0 ? 0 : Math.round((signCount * 10000.0 / enrollCount)) / 100.0);
        data.put("quizJoinRate", enrollCount == 0 ? 0 : Math.round((quizSubmitCount * 10000.0 / enrollCount)) / 100.0);
        data.put("quizAvgScore", Math.round(avgScore * 100.0) / 100.0);
        data.put("abnormalStudentCount", abnormalStudentCount);
        data.put("behaviorDistribution", dist);
        return data;
    }

    public List<Map<String, Object>> teacherSessionTrend(Long sessionId) {
        QueryWrapper<BehaviorRecord> wrapper = new QueryWrapper<>();
        wrapper.select("date_format(detect_time,'%Y-%m-%d %H:00:00') as timePoint", "count(1) as total")
                .eq("teacher_id", AuthContext.userId())
                .eq(sessionId != null, "class_session_id", sessionId)
                .groupBy("date_format(detect_time,'%Y-%m-%d %H:00:00')")
                .orderByAsc("timePoint");
        return behaviorRecordMapper.selectMaps(wrapper).stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("time", row.get("timePoint"));
            item.put("count", ((Number) row.get("total")).longValue());
            return item;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> studentOverview() {
        Long studentId = AuthContext.userId();
        List<SignInRecord> signRecords = signInRecordMapper.selectList(new LambdaQueryWrapper<SignInRecord>().eq(SignInRecord::getStudentId, studentId));
        List<QuizSubmission> submissions = quizSubmissionMapper.selectList(new LambdaQueryWrapper<QuizSubmission>().eq(QuizSubmission::getStudentId, studentId));
        List<BehaviorRecord> records = behaviorRecordMapper.selectList(new LambdaQueryWrapper<BehaviorRecord>().eq(BehaviorRecord::getStudentId, studentId));

        double avgScore = submissions.stream().mapToInt(s -> s.getScore() == null ? 0 : s.getScore()).average().orElse(0);
        long abnormalCount = records.stream().filter(r -> r.getExceptionFlag() != null && r.getExceptionFlag() == 1).count();

        Map<String, Object> data = new HashMap<>();
        data.put("signCount", signRecords.size());
        data.put("quizCount", submissions.size());
        data.put("quizAvgScore", Math.round(avgScore * 100.0) / 100.0);
        data.put("behaviorCount", records.size());
        data.put("abnormalCount", abnormalCount);
        return data;
    }

    public List<Map<String, Object>> studentActivityTrend(Long courseId) {
        QueryWrapper<BehaviorRecord> wrapper = new QueryWrapper<>();
        wrapper.select("date_format(detect_time,'%Y-%m-%d %H:00:00') as timePoint", "avg(activity_score) as avgScore")
                .eq("student_id", AuthContext.userId())
                .eq(courseId != null, "course_id", courseId)
                .groupBy("date_format(detect_time,'%Y-%m-%d %H:00:00')")
                .orderByAsc("timePoint");
        return behaviorRecordMapper.selectMaps(wrapper).stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("time", row.get("timePoint"));
            item.put("avgScore", row.get("avgScore"));
            return item;
        }).collect(Collectors.toList());
    }

    private Map<String, Long> roleCountMap() {
        Map<Long, String> roleIdCode = sysRoleMapper.selectList(new LambdaQueryWrapper<>()).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleCode));
        Map<String, Long> result = new HashMap<>();
        for (SysUserRole userRole : sysUserRoleMapper.selectList(new LambdaQueryWrapper<>())) {
            String code = roleIdCode.get(userRole.getRoleId());
            if (code != null) {
                result.put(code, result.getOrDefault(code, 0L) + 1);
            }
        }
        return result;
    }
}
