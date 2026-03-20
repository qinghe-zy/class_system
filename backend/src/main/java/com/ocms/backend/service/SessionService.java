package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ocms.backend.common.BizException;
import com.ocms.backend.mapper.ClassSessionMapper;
import com.ocms.backend.mapper.CourseEnrollmentMapper;
import com.ocms.backend.mapper.CourseMapper;
import com.ocms.backend.model.dto.SessionUpsertRequest;
import com.ocms.backend.model.entity.ClassSession;
import com.ocms.backend.model.entity.Course;
import com.ocms.backend.model.entity.CourseEnrollment;
import com.ocms.backend.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final ClassSessionMapper classSessionMapper;
    private final CourseMapper courseMapper;
    private final CourseEnrollmentMapper courseEnrollmentMapper;

    public SessionService(ClassSessionMapper classSessionMapper, CourseMapper courseMapper, CourseEnrollmentMapper courseEnrollmentMapper) {
        this.classSessionMapper = classSessionMapper;
        this.courseMapper = courseMapper;
        this.courseEnrollmentMapper = courseEnrollmentMapper;
    }

    public Long create(SessionUpsertRequest request) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        if (!course.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "仅可创建本人课程会话");
        }
        ClassSession session = new ClassSession();
        session.setCourseId(request.getCourseId());
        session.setTeacherId(AuthContext.userId());
        session.setSessionTitle(request.getSessionTitle());
        session.setSessionStartTime(request.getSessionStartTime());
        session.setSessionEndTime(request.getSessionEndTime());
        session.setSessionStatus(request.getSessionStatus());
        classSessionMapper.insert(session);
        return session.getId();
    }

    public void update(Long id, SessionUpsertRequest request) {
        ClassSession session = classSessionMapper.selectById(id);
        if (session == null) {
            throw new BizException(404, "课堂会话不存在");
        }
        if (!session.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "仅可修改本人会话");
        }
        session.setSessionTitle(request.getSessionTitle());
        session.setSessionStartTime(request.getSessionStartTime());
        session.setSessionEndTime(request.getSessionEndTime());
        session.setSessionStatus(request.getSessionStatus());
        classSessionMapper.updateById(session);
    }

    public List<Map<String, Object>> teacherList(Long courseId) {
        Long teacherId = AuthContext.userId();
        return classSessionMapper.selectList(new LambdaQueryWrapper<ClassSession>()
                        .eq(courseId != null, ClassSession::getCourseId, courseId)
                        .eq(ClassSession::getTeacherId, teacherId)
                        .orderByDesc(ClassSession::getId))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> adminList() {
        return classSessionMapper.selectList(new LambdaQueryWrapper<ClassSession>().orderByDesc(ClassSession::getId))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> studentList(Long courseId) {
        Long studentId = AuthContext.userId();
        List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getStatus, 1));
        Set<Long> courseIds = enrollments.stream().map(CourseEnrollment::getCourseId).collect(Collectors.toSet());
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return classSessionMapper.selectList(new LambdaQueryWrapper<ClassSession>()
                        .in(ClassSession::getCourseId, courseIds)
                        .eq(courseId != null, ClassSession::getCourseId, courseId)
                        .orderByDesc(ClassSession::getId))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    private Map<String, Object> toMap(ClassSession session) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", session.getId());
        map.put("courseId", session.getCourseId());
        map.put("teacherId", session.getTeacherId());
        map.put("sessionTitle", session.getSessionTitle());
        map.put("sessionStartTime", session.getSessionStartTime());
        map.put("sessionEndTime", session.getSessionEndTime());
        map.put("sessionStatus", session.getSessionStatus());
        map.put("createdTime", session.getCreatedTime());
        return map;
    }
}
