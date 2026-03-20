package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ocms.backend.common.BizException;
import com.ocms.backend.common.PageResult;
import com.ocms.backend.constant.RoleConst;
import com.ocms.backend.mapper.CourseAuditRecordMapper;
import com.ocms.backend.mapper.CourseEnrollmentMapper;
import com.ocms.backend.mapper.CourseMapper;
import com.ocms.backend.mapper.SysUserMapper;
import com.ocms.backend.model.dto.AuditCourseRequest;
import com.ocms.backend.model.dto.CourseUpsertRequest;
import com.ocms.backend.model.entity.Course;
import com.ocms.backend.model.entity.CourseAuditRecord;
import com.ocms.backend.model.entity.CourseEnrollment;
import com.ocms.backend.model.entity.SysUser;
import com.ocms.backend.security.AuthContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseMapper courseMapper;
    private final CourseAuditRecordMapper courseAuditRecordMapper;
    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final SysUserMapper sysUserMapper;
    private final UserRoleService userRoleService;

    public CourseService(CourseMapper courseMapper,
                         CourseAuditRecordMapper courseAuditRecordMapper,
                         CourseEnrollmentMapper courseEnrollmentMapper,
                         SysUserMapper sysUserMapper,
                         UserRoleService userRoleService) {
        this.courseMapper = courseMapper;
        this.courseAuditRecordMapper = courseAuditRecordMapper;
        this.courseEnrollmentMapper = courseEnrollmentMapper;
        this.sysUserMapper = sysUserMapper;
        this.userRoleService = userRoleService;
    }

    public PageResult<Map<String, Object>> teacherCoursePage(Integer pageNum, Integer pageSize, String keyword) {
        Long teacherId = AuthContext.userId();
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId)
                .orderByDesc(Course::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getCourseName, keyword).or().like(Course::getCourseCode, keyword));
        }
        Page<Course> result = courseMapper.selectPage(page, wrapper);
        List<Map<String, Object>> list = result.getRecords().stream().map(this::toMap).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), list);
    }

    public Long createTeacherCourse(CourseUpsertRequest request) {
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setCourseCode(request.getCourseCode());
        course.setCourseIntro(request.getCourseIntro());
        course.setCourseCover(request.getCourseCover());
        course.setContentSummary(request.getContentSummary());
        course.setTeacherId(AuthContext.userId());
        course.setAuditStatus("PENDING");
        course.setPublishStatus(request.getPublishStatus() == null ? 1 : request.getPublishStatus());
        courseMapper.insert(course);
        return course.getId();
    }

    public void updateTeacherCourse(Long id, CourseUpsertRequest request) {
        Course course = courseMapper.selectById(id);
        assertTeacherOwnsCourse(course);
        course.setCourseName(request.getCourseName());
        course.setCourseCode(request.getCourseCode());
        course.setCourseIntro(request.getCourseIntro());
        course.setCourseCover(request.getCourseCover());
        course.setContentSummary(request.getContentSummary());
        course.setPublishStatus(request.getPublishStatus() == null ? 1 : request.getPublishStatus());
        course.setAuditStatus("PENDING");
        courseMapper.updateById(course);
    }

    public void deleteTeacherCourse(Long id) {
        Course course = courseMapper.selectById(id);
        assertTeacherOwnsCourse(course);
        courseMapper.deleteById(id);
    }

    public PageResult<Map<String, Object>> adminCoursePage(Integer pageNum, Integer pageSize, String keyword, String auditStatus) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>().orderByDesc(Course::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getCourseName, keyword).or().like(Course::getCourseCode, keyword));
        }
        if (StringUtils.hasText(auditStatus)) {
            wrapper.eq(Course::getAuditStatus, auditStatus);
        }
        Page<Course> result = courseMapper.selectPage(page, wrapper);
        List<Map<String, Object>> list = result.getRecords().stream().map(this::toMap).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), list);
    }

    public void auditCourse(Long courseId, AuditCourseRequest request) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        if (!Set.of("APPROVED", "REJECTED", "PENDING").contains(request.getAuditStatus())) {
            throw new BizException("审核状态非法");
        }
        course.setAuditStatus(request.getAuditStatus());
        courseMapper.updateById(course);

        CourseAuditRecord record = new CourseAuditRecord();
        record.setCourseId(courseId);
        record.setAuditorId(AuthContext.userId());
        record.setAuditStatus(request.getAuditStatus());
        record.setRemark(request.getRemark());
        record.setAuditTime(LocalDateTime.now());
        courseAuditRecordMapper.insert(record);
    }

    public PageResult<Map<String, Object>> studentAvailableCourses(Integer pageNum, Integer pageSize, String keyword) {
        Page<Course> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getAuditStatus, "APPROVED")
                .eq(Course::getPublishStatus, 1)
                .orderByDesc(Course::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Course::getCourseName, keyword).or().like(Course::getCourseCode, keyword));
        }
        Page<Course> result = courseMapper.selectPage(page, wrapper);
        Long studentId = AuthContext.userId();
        Set<Long> enrolledCourseIds = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                        .eq(CourseEnrollment::getStudentId, studentId)
                        .eq(CourseEnrollment::getStatus, 1))
                .stream().map(CourseEnrollment::getCourseId).collect(Collectors.toSet());

        List<Map<String, Object>> list = result.getRecords().stream().map(course -> {
            Map<String, Object> map = toMap(course);
            map.put("enrolled", enrolledCourseIds.contains(course.getId()));
            return map;
        }).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), list);
    }

    public void enrollCourse(Long courseId) {
        Long studentId = AuthContext.userId();
        String role = AuthContext.role();
        if (!RoleConst.STUDENT.equals(role)) {
            throw new BizException(403, "仅学生可选课");
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null || !"APPROVED".equals(course.getAuditStatus())) {
            throw new BizException("课程不可选");
        }
        CourseEnrollment exists = courseEnrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStudentId, studentId));
        if (exists != null) {
            if (exists.getStatus() != null && exists.getStatus() == 1) {
                return;
            }
            exists.setStatus(1);
            exists.setEnrollTime(LocalDateTime.now());
            courseEnrollmentMapper.updateById(exists);
            return;
        }
        CourseEnrollment enroll = new CourseEnrollment();
        enroll.setCourseId(courseId);
        enroll.setStudentId(studentId);
        enroll.setStatus(1);
        enroll.setEnrollTime(LocalDateTime.now());
        courseEnrollmentMapper.insert(enroll);
    }

    public List<Map<String, Object>> studentMyCourses() {
        Long studentId = AuthContext.userId();
        List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getStudentId, studentId).eq(CourseEnrollment::getStatus, 1));
        if (enrollments.isEmpty()) {
            return List.of();
        }
        return enrollments.stream().map(e -> courseMapper.selectById(e.getCourseId()))
                .filter(c -> c != null)
                .map(this::toMap)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> teacherStudents(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        assertTeacherOwnsCourse(course);
        List<CourseEnrollment> enrollmentList = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getCourseId, courseId).eq(CourseEnrollment::getStatus, 1));
        return enrollmentList.stream().map(CourseEnrollment::getStudentId).distinct().map(uid -> {
            SysUser user = sysUserMapper.selectById(uid);
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", uid);
            map.put("username", user == null ? "" : user.getUsername());
            map.put("realName", user == null ? "" : user.getRealName());
            map.put("departmentOrClass", user == null ? "" : user.getDepartmentOrClass());
            return map;
        }).collect(Collectors.toList());
    }

    public Course requireCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        return course;
    }

    public void assertTeacherOwnsCourse(Course course) {
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        if (!course.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "仅可操作本人课程");
        }
    }

    private Map<String, Object> toMap(Course course) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", course.getId());
        map.put("courseName", course.getCourseName());
        map.put("courseCode", course.getCourseCode());
        map.put("courseIntro", course.getCourseIntro());
        map.put("courseCover", course.getCourseCover());
        map.put("contentSummary", course.getContentSummary());
        map.put("teacherId", course.getTeacherId());
        SysUser teacher = sysUserMapper.selectById(course.getTeacherId());
        map.put("teacherName", teacher == null ? "" : teacher.getRealName());
        map.put("auditStatus", course.getAuditStatus());
        map.put("publishStatus", course.getPublishStatus());
        map.put("createdTime", course.getCreatedTime());
        map.put("updatedTime", course.getUpdatedTime());
        return map;
    }
}
