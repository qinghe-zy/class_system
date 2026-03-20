package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ocms.backend.common.BizException;
import com.ocms.backend.mapper.CourseContentMapper;
import com.ocms.backend.mapper.CourseEnrollmentMapper;
import com.ocms.backend.mapper.CourseMapper;
import com.ocms.backend.mapper.SysUserMapper;
import com.ocms.backend.model.dto.CourseContentAuditRequest;
import com.ocms.backend.model.dto.CourseContentUpsertRequest;
import com.ocms.backend.model.entity.Course;
import com.ocms.backend.model.entity.CourseContent;
import com.ocms.backend.model.entity.CourseEnrollment;
import com.ocms.backend.model.entity.SysUser;
import com.ocms.backend.security.AuthContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseContentService {

    private final CourseContentMapper courseContentMapper;
    private final CourseMapper courseMapper;
    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final SysUserMapper sysUserMapper;

    public CourseContentService(CourseContentMapper courseContentMapper, CourseMapper courseMapper, CourseEnrollmentMapper courseEnrollmentMapper, SysUserMapper sysUserMapper) {
        this.courseContentMapper = courseContentMapper;
        this.courseMapper = courseMapper;
        this.courseEnrollmentMapper = courseEnrollmentMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public Long teacherCreate(CourseContentUpsertRequest request) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        if (!course.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "仅可发布本人课程内容");
        }
        CourseContent content = new CourseContent();
        content.setCourseId(request.getCourseId());
        content.setTeacherId(AuthContext.userId());
        content.setContentTitle(request.getContentTitle());
        content.setContentBody(request.getContentBody());
        content.setAttachmentName(request.getAttachmentName());
        content.setAttachmentUrl(request.getAttachmentUrl());
        content.setAttachmentType(request.getAttachmentType());
        content.setAuditStatus("PENDING");
        content.setPublishStatus(request.getPublishStatus());
        courseContentMapper.insert(content);
        return content.getId();
    }

    public void teacherUpdate(Long id, CourseContentUpsertRequest request) {
        CourseContent content = courseContentMapper.selectById(id);
        if (content == null) {
            throw new BizException(404, "课堂内容不存在");
        }
        if (!content.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "仅可编辑本人课堂内容");
        }
        content.setContentTitle(request.getContentTitle());
        content.setContentBody(request.getContentBody());
        content.setAttachmentName(request.getAttachmentName());
        content.setAttachmentUrl(request.getAttachmentUrl());
        content.setAttachmentType(request.getAttachmentType());
        content.setPublishStatus(request.getPublishStatus());
        content.setAuditStatus("PENDING");
        content.setAuditorId(null);
        content.setAuditRemark(null);
        courseContentMapper.updateById(content);
    }

    public List<Map<String, Object>> teacherList(Long courseId) {
        return courseContentMapper.selectList(new LambdaQueryWrapper<CourseContent>()
                        .eq(CourseContent::getTeacherId, AuthContext.userId())
                        .eq(courseId != null, CourseContent::getCourseId, courseId)
                        .orderByDesc(CourseContent::getId))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> adminList(String auditStatus) {
        return courseContentMapper.selectList(new LambdaQueryWrapper<CourseContent>()
                        .eq(auditStatus != null && !auditStatus.isBlank(), CourseContent::getAuditStatus, auditStatus)
                        .orderByDesc(CourseContent::getId))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    public void adminAudit(Long id, CourseContentAuditRequest request) {
        if (!Set.of("APPROVED", "REJECTED", "PENDING").contains(request.getAuditStatus())) {
            throw new BizException("审核状态非法");
        }
        CourseContent content = courseContentMapper.selectById(id);
        if (content == null) {
            throw new BizException(404, "课堂内容不存在");
        }
        content.setAuditStatus(request.getAuditStatus());
        content.setAuditorId(AuthContext.userId());
        content.setAuditRemark(request.getAuditRemark());
        courseContentMapper.updateById(content);
    }

    public List<Map<String, Object>> studentList(Long courseId) {
        Long studentId = AuthContext.userId();
        List<Long> courseIds = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                        .eq(CourseEnrollment::getStudentId, studentId)
                        .eq(CourseEnrollment::getStatus, 1))
                .stream().map(CourseEnrollment::getCourseId).toList();
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return courseContentMapper.selectList(new LambdaQueryWrapper<CourseContent>()
                        .in(CourseContent::getCourseId, courseIds)
                        .eq(courseId != null, CourseContent::getCourseId, courseId)
                        .eq(CourseContent::getAuditStatus, "APPROVED")
                        .eq(CourseContent::getPublishStatus, 1)
                        .orderByDesc(CourseContent::getId))
                .stream().map(this::toMap).collect(Collectors.toList());
    }

    private Map<String, Object> toMap(CourseContent c) {
        Map<String, Object> map = new HashMap<>();
        Course course = courseMapper.selectById(c.getCourseId());
        SysUser teacher = c.getTeacherId() == null ? null : sysUserMapper.selectById(c.getTeacherId());
        map.put("id", c.getId());
        map.put("courseId", c.getCourseId());
        map.put("teacherId", c.getTeacherId());
        map.put("courseName", course == null ? null : course.getCourseName());
        map.put("teacherName", teacher == null ? null : teacher.getRealName());
        map.put("contentTitle", c.getContentTitle());
        map.put("contentBody", c.getContentBody());
        map.put("attachmentName", c.getAttachmentName());
        map.put("attachmentUrl", c.getAttachmentUrl());
        map.put("attachmentType", c.getAttachmentType());
        map.put("auditStatus", c.getAuditStatus());
        map.put("auditorId", c.getAuditorId());
        map.put("auditRemark", c.getAuditRemark());
        map.put("publishStatus", c.getPublishStatus());
        map.put("createdTime", c.getCreatedTime());
        map.put("updatedTime", c.getUpdatedTime());
        return map;
    }
}
