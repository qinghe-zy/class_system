package com.ocms.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ocms.backend.common.BizException;
import com.ocms.backend.mapper.ClassSessionMapper;
import com.ocms.backend.mapper.CourseEnrollmentMapper;
import com.ocms.backend.mapper.CourseMapper;
import com.ocms.backend.mapper.QuizMapper;
import com.ocms.backend.mapper.QuizQuestionMapper;
import com.ocms.backend.mapper.QuizSubmissionMapper;
import com.ocms.backend.mapper.SysUserMapper;
import com.ocms.backend.model.dto.QuizCreateRequest;
import com.ocms.backend.model.dto.QuizQuestionRequest;
import com.ocms.backend.model.dto.QuizSubmitRequest;
import com.ocms.backend.model.entity.ClassSession;
import com.ocms.backend.model.entity.CourseEnrollment;
import com.ocms.backend.model.entity.Course;
import com.ocms.backend.model.entity.Quiz;
import com.ocms.backend.model.entity.QuizQuestion;
import com.ocms.backend.model.entity.QuizSubmission;
import com.ocms.backend.model.entity.SysUser;
import com.ocms.backend.security.AuthContext;
import com.ocms.backend.util.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizMapper quizMapper;
    private final QuizQuestionMapper quizQuestionMapper;
    private final QuizSubmissionMapper quizSubmissionMapper;
    private final CourseEnrollmentMapper courseEnrollmentMapper;
    private final CourseMapper courseMapper;
    private final ClassSessionMapper classSessionMapper;
    private final SysUserMapper sysUserMapper;
    private final JsonUtil jsonUtil;

    public QuizService(QuizMapper quizMapper,
                       QuizQuestionMapper quizQuestionMapper,
                       QuizSubmissionMapper quizSubmissionMapper,
                       CourseEnrollmentMapper courseEnrollmentMapper,
                       CourseMapper courseMapper,
                       ClassSessionMapper classSessionMapper,
                       SysUserMapper sysUserMapper,
                       JsonUtil jsonUtil) {
        this.quizMapper = quizMapper;
        this.quizQuestionMapper = quizQuestionMapper;
        this.quizSubmissionMapper = quizSubmissionMapper;
        this.courseEnrollmentMapper = courseEnrollmentMapper;
        this.courseMapper = courseMapper;
        this.classSessionMapper = classSessionMapper;
        this.sysUserMapper = sysUserMapper;
        this.jsonUtil = jsonUtil;
    }

    public Long create(QuizCreateRequest request) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null) {
            throw new BizException(404, "课程不存在");
        }
        if (!course.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "仅可对本人课程发布测验");
        }
        ClassSession session = classSessionMapper.selectById(request.getSessionId());
        if (session == null || !session.getCourseId().equals(request.getCourseId())) {
            throw new BizException("课堂会话与课程不匹配");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BizException("测验开始时间必须早于结束时间");
        }
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new BizException("测验题目不能为空");
        }

        Quiz quiz = new Quiz();
        quiz.setSessionId(request.getSessionId());
        quiz.setCourseId(request.getCourseId());
        quiz.setTeacherId(AuthContext.userId());
        quiz.setQuizTitle(request.getQuizTitle());
        quiz.setQuizDesc(request.getQuizDesc());
        quiz.setTotalScore(request.getTotalScore());
        quiz.setStartTime(request.getStartTime());
        quiz.setEndTime(request.getEndTime());
        quiz.setQuizStatus("PUBLISHED");
        quizMapper.insert(quiz);

        for (QuizQuestionRequest q : request.getQuestions()) {
            validateQuestion(q);
            if ("SINGLE".equalsIgnoreCase(q.getQuestionType()) && !StringUtils.hasText(q.getOptionsJson())) {
                throw new BizException("单选题必须配置选项");
            }
            QuizQuestion question = new QuizQuestion();
            question.setQuizId(quiz.getId());
            question.setQuestionTitle(q.getQuestionTitle());
            question.setQuestionType(q.getQuestionType());
            question.setOptionsJson(StringUtils.hasText(q.getOptionsJson()) ? q.getOptionsJson() : "[]");
            question.setCorrectAnswer(q.getCorrectAnswer());
            question.setScore(q.getScore());
            question.setSortNo(q.getSortNo());
            quizQuestionMapper.insert(question);
        }
        return quiz.getId();
    }

    public void update(Long quizId, QuizCreateRequest request) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BizException(404, "测验不存在");
        }
        if (!quiz.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "仅可编辑本人测验");
        }
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BizException("测验开始时间必须早于结束时间");
        }
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new BizException("测验题目不能为空");
        }

        Long submissionCount = quizSubmissionMapper.selectCount(new LambdaQueryWrapper<QuizSubmission>()
                .eq(QuizSubmission::getQuizId, quizId));
        if (submissionCount != null && submissionCount > 0) {
            throw new BizException("测验已有学生提交，不允许再编辑题目");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null || !course.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException("课程不存在或无权限");
        }
        ClassSession session = classSessionMapper.selectById(request.getSessionId());
        if (session == null || !session.getCourseId().equals(request.getCourseId())) {
            throw new BizException("课堂会话与课程不匹配");
        }

        quiz.setSessionId(request.getSessionId());
        quiz.setCourseId(request.getCourseId());
        quiz.setQuizTitle(request.getQuizTitle());
        quiz.setQuizDesc(request.getQuizDesc());
        quiz.setTotalScore(request.getTotalScore());
        quiz.setStartTime(request.getStartTime());
        quiz.setEndTime(request.getEndTime());
        quiz.setQuizStatus("PUBLISHED");
        quizMapper.updateById(quiz);

        quizQuestionMapper.delete(new LambdaQueryWrapper<QuizQuestion>().eq(QuizQuestion::getQuizId, quizId));
        for (QuizQuestionRequest q : request.getQuestions()) {
            validateQuestion(q);
            QuizQuestion question = new QuizQuestion();
            question.setQuizId(quizId);
            question.setQuestionTitle(q.getQuestionTitle());
            question.setQuestionType(q.getQuestionType());
            question.setOptionsJson("SINGLE".equalsIgnoreCase(q.getQuestionType())
                    ? (StringUtils.hasText(q.getOptionsJson()) ? q.getOptionsJson() : "[]")
                    : "[]");
            question.setCorrectAnswer(q.getCorrectAnswer());
            question.setScore(q.getScore());
            question.setSortNo(q.getSortNo());
            quizQuestionMapper.insert(question);
        }
    }

    public List<Map<String, Object>> teacherList(Long sessionId) {
        return quizMapper.selectList(new LambdaQueryWrapper<Quiz>()
                        .eq(Quiz::getTeacherId, AuthContext.userId())
                        .eq(sessionId != null, Quiz::getSessionId, sessionId)
                        .orderByDesc(Quiz::getId))
                .stream().map(this::quizMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> studentList(Long sessionId) {
        Long studentId = AuthContext.userId();
        List<CourseEnrollment> enrollments = courseEnrollmentMapper.selectList(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getStudentId, studentId).eq(CourseEnrollment::getStatus, 1));
        List<Long> courseIds = enrollments.stream().map(CourseEnrollment::getCourseId).toList();
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return quizMapper.selectList(new LambdaQueryWrapper<Quiz>()
                        .in(Quiz::getCourseId, courseIds)
                        .eq(Quiz::getQuizStatus, "PUBLISHED")
                        .eq(sessionId != null, Quiz::getSessionId, sessionId)
                        .orderByDesc(Quiz::getId))
                .stream().map(quiz -> {
                    Map<String, Object> map = quizMap(quiz);
                    QuizSubmission submission = quizSubmissionMapper.selectOne(new LambdaQueryWrapper<QuizSubmission>()
                            .eq(QuizSubmission::getQuizId, quiz.getId())
                            .eq(QuizSubmission::getStudentId, studentId)
                            .last("limit 1"));
                    map.put("submitted", submission != null);
                    String timeStatus = buildTimeStatus(quiz.getStartTime(), quiz.getEndTime(), LocalDateTime.now());
                    map.put("timeStatus", timeStatus);
                    map.put("canAnswer", "ONGOING".equals(timeStatus) && submission == null);
                    return map;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> quizQuestions(Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BizException(404, "测验不存在");
        }
        Long studentId = AuthContext.userId();
        CourseEnrollment enrollment = courseEnrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getCourseId, quiz.getCourseId())
                .eq(CourseEnrollment::getStatus, 1)
                .last("limit 1"));
        if (enrollment == null) {
            throw new BizException(403, "未选修该课程，无法查看测验题目");
        }
        return quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                        .eq(QuizQuestion::getQuizId, quizId)
                        .orderByAsc(QuizQuestion::getSortNo))
                .stream().map(q -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", q.getId());
                    map.put("quizId", q.getQuizId());
                    map.put("questionTitle", q.getQuestionTitle());
                    map.put("questionType", q.getQuestionType());
                    map.put("optionsJson", q.getOptionsJson());
                    map.put("score", q.getScore());
                    map.put("sortNo", q.getSortNo());
                    return map;
                }).collect(Collectors.toList());
    }

    public Map<String, Object> submit(Long quizId, QuizSubmitRequest request) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null) {
            throw new BizException(404, "测验不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(quiz.getStartTime()) || now.isAfter(quiz.getEndTime())) {
            throw new BizException("当前不在测验作答时间范围内");
        }
        Long studentId = AuthContext.userId();
        CourseEnrollment enrollment = courseEnrollmentMapper.selectOne(new LambdaQueryWrapper<CourseEnrollment>()
                .eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getCourseId, quiz.getCourseId())
                .eq(CourseEnrollment::getStatus, 1));
        if (enrollment == null) {
            throw new BizException(403, "未选修该课程");
        }

        QuizSubmission existing = quizSubmissionMapper.selectOne(new LambdaQueryWrapper<QuizSubmission>()
                .eq(QuizSubmission::getQuizId, quizId)
                .eq(QuizSubmission::getStudentId, studentId));
        if (existing != null) {
            throw new BizException("该测验已提交");
        }

        Map<Long, String> answers = request.getAnswers().stream()
                .collect(Collectors.toMap(QuizSubmitRequest.AnswerItem::getQuestionId, QuizSubmitRequest.AnswerItem::getAnswer, (a, b) -> b));

        List<QuizQuestion> questions = quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>().eq(QuizQuestion::getQuizId, quizId));
        int score = 0;
        for (QuizQuestion question : questions) {
            String answer = answers.get(question.getId());
            if (isAnswerCorrect(answer, question.getCorrectAnswer())) {
                score += question.getScore();
            }
        }

        QuizSubmission submission = new QuizSubmission();
        submission.setQuizId(quizId);
        submission.setSessionId(request.getSessionId());
        submission.setCourseId(request.getCourseId());
        submission.setStudentId(studentId);
        submission.setAnswerJson(jsonUtil.toJson(request.getAnswers()));
        submission.setScore(score);
        submission.setSubmitStatus("SUBMITTED");
        submission.setSubmitTime(LocalDateTime.now());
        quizSubmissionMapper.insert(submission);

        Map<String, Object> result = new HashMap<>();
        result.put("submissionId", submission.getId());
        result.put("score", score);
        return result;
    }

    public List<Map<String, Object>> teacherSubmissions(Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null || !quiz.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "无权查看该测验提交");
        }
        return quizSubmissionMapper.selectList(new LambdaQueryWrapper<QuizSubmission>()
                        .eq(QuizSubmission::getQuizId, quizId)
                        .orderByDesc(QuizSubmission::getId))
                .stream().map(this::submissionMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> teacherQuestions(Long quizId) {
        Quiz quiz = quizMapper.selectById(quizId);
        if (quiz == null || !quiz.getTeacherId().equals(AuthContext.userId())) {
            throw new BizException(403, "无权查看该测验题目");
        }
        return quizQuestionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                        .eq(QuizQuestion::getQuizId, quizId)
                        .orderByAsc(QuizQuestion::getSortNo))
                .stream().map(q -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", q.getId());
                    map.put("quizId", q.getQuizId());
                    map.put("questionTitle", q.getQuestionTitle());
                    map.put("questionType", q.getQuestionType());
                    map.put("optionsJson", q.getOptionsJson());
                    map.put("correctAnswer", q.getCorrectAnswer());
                    map.put("score", q.getScore());
                    map.put("sortNo", q.getSortNo());
                    return map;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> studentSubmissions() {
        return quizSubmissionMapper.selectList(new LambdaQueryWrapper<QuizSubmission>()
                        .eq(QuizSubmission::getStudentId, AuthContext.userId())
                        .orderByDesc(QuizSubmission::getId))
                .stream().map(this::submissionMap).collect(Collectors.toList());
    }

    private Map<String, Object> quizMap(Quiz quiz) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", quiz.getId());
        map.put("sessionId", quiz.getSessionId());
        map.put("courseId", quiz.getCourseId());
        map.put("teacherId", quiz.getTeacherId());
        map.put("quizTitle", quiz.getQuizTitle());
        map.put("quizDesc", quiz.getQuizDesc());
        map.put("totalScore", quiz.getTotalScore());
        map.put("startTime", quiz.getStartTime());
        map.put("endTime", quiz.getEndTime());
        map.put("quizStatus", quiz.getQuizStatus());
        map.put("timeStatus", buildTimeStatus(quiz.getStartTime(), quiz.getEndTime(), LocalDateTime.now()));
        return map;
    }

    private void validateQuestion(QuizQuestionRequest q) {
        String type = q.getQuestionType() == null ? "" : q.getQuestionType().trim().toUpperCase();
        if (!Set.of("SINGLE", "FILL").contains(type)) {
            throw new BizException("题型仅支持 SINGLE / FILL");
        }
        if ("SINGLE".equals(type) && !StringUtils.hasText(q.getOptionsJson())) {
            throw new BizException("单选题必须配置选项");
        }
        q.setQuestionType(type);
    }

    private String buildTimeStatus(LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        if (now.isBefore(start)) {
            return "NOT_STARTED";
        }
        if (now.isAfter(end)) {
            return "EXPIRED";
        }
        return "ONGOING";
    }

    private boolean isAnswerCorrect(String actual, String expected) {
        if (!StringUtils.hasText(actual) || !StringUtils.hasText(expected)) {
            return false;
        }
        String normalizedActual = normalizeAnswer(actual);
        String[] expectedCandidates = expected.split("\\|");
        for (String candidate : expectedCandidates) {
            if (normalizedActual.equalsIgnoreCase(normalizeAnswer(candidate))) {
                return true;
            }
        }
        return false;
    }

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim().replaceAll("\\s+", " ");
    }

    private Map<String, Object> submissionMap(QuizSubmission s) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", s.getId());
        map.put("quizId", s.getQuizId());
        map.put("sessionId", s.getSessionId());
        map.put("courseId", s.getCourseId());
        map.put("studentId", s.getStudentId());
        SysUser user = sysUserMapper.selectById(s.getStudentId());
        map.put("studentName", user == null ? "" : user.getRealName());
        map.put("answerJson", s.getAnswerJson());
        map.put("score", s.getScore());
        map.put("submitStatus", s.getSubmitStatus());
        map.put("submitTime", s.getSubmitTime());
        return map;
    }
}
