USE ocms;
SET NAMES utf8mb4;

INSERT INTO sys_role(id, role_code, role_name) VALUES
(1, 'ADMIN', '管理员'),
(2, 'TEACHER', '教师'),
(3, 'STUDENT', '学生');

INSERT INTO sys_user(id, username, password, real_name, gender, phone, email, department_or_class, status, created_time, updated_time, deleted_flag) VALUES
(1, 'admin', '5cf858bd416c13e705d324a71525b00a6bf602bbdcbfb5ff928dd80592fb6c10', '系统管理员', '男', '13800000000', 'admin@ocms.com', '教务处', 1, NOW(), NOW(), 0),
(2, 'teacher01', '5cf858bd416c13e705d324a71525b00a6bf602bbdcbfb5ff928dd80592fb6c10', '张老师', '女', '13800000001', 'teacher01@ocms.com', '计算机学院', 1, NOW(), NOW(), 0),
(3, 'teacher02', '5cf858bd416c13e705d324a71525b00a6bf602bbdcbfb5ff928dd80592fb6c10', '李老师', '男', '13800000002', 'teacher02@ocms.com', '信息工程学院', 1, NOW(), NOW(), 0),
(4, 'student01', '5cf858bd416c13e705d324a71525b00a6bf602bbdcbfb5ff928dd80592fb6c10', '王同学', '男', '13800000011', 'student01@ocms.com', '计科2201班', 1, NOW(), NOW(), 0),
(5, 'student02', '5cf858bd416c13e705d324a71525b00a6bf602bbdcbfb5ff928dd80592fb6c10', '赵同学', '女', '13800000012', 'student02@ocms.com', '计科2201班', 1, NOW(), NOW(), 0),
(6, 'student03', '5cf858bd416c13e705d324a71525b00a6bf602bbdcbfb5ff928dd80592fb6c10', '钱同学', '男', '13800000013', 'student03@ocms.com', '软工2202班', 1, NOW(), NOW(), 0),
(7, 'student04', '5cf858bd416c13e705d324a71525b00a6bf602bbdcbfb5ff928dd80592fb6c10', '孙同学', '女', '13800000014', 'student04@ocms.com', '软工2202班', 1, NOW(), NOW(), 0),
(8, 'student05', '5cf858bd416c13e705d324a71525b00a6bf602bbdcbfb5ff928dd80592fb6c10', '周同学', '男', '13800000015', 'student05@ocms.com', '网工2201班', 1, NOW(), NOW(), 0);

INSERT INTO sys_user_role(user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 2),
(4, 3),
(5, 3),
(6, 3),
(7, 3),
(8, 3);

INSERT INTO sys_permission(perm_code, perm_name, description) VALUES
('ADMIN_ALL', '管理员全权限', '管理员可访问全量数据'),
('TEACHER_OWN', '教师课程权限', '教师仅可操作本人课程与课堂数据'),
('STUDENT_SELF', '学生个人权限', '学生仅可查看个人学习数据');

INSERT INTO course(id, course_name, course_code, course_intro, content_summary, teacher_id, audit_status, publish_status, created_time, updated_time, deleted_flag) VALUES
(1, 'Java Web程序设计', 'CS-JAVA-001', '面向Web开发基础', 'Spring Boot、MVC、REST接口实践', 2, 'APPROVED', 1, NOW(), NOW(), 0),
(2, '数据库原理与应用', 'CS-DB-002', '数据库设计与SQL实战', 'MySQL建模、索引、事务、性能优化', 2, 'APPROVED', 1, NOW(), NOW(), 0),
(3, '前端工程化实践', 'CS-FE-003', 'Vue3前端开发实战', 'Vue3、Vite、组件化与状态管理', 3, 'PENDING', 1, NOW(), NOW(), 0);

INSERT INTO course_audit_record(course_id, auditor_id, audit_status, remark, audit_time) VALUES
(1, 1, 'APPROVED', '课程内容完整，审核通过', NOW()),
(2, 1, 'APPROVED', '符合开课标准', NOW());

INSERT INTO course_content(course_id, teacher_id, content_title, content_body, attachment_name, attachment_url, attachment_type, audit_status, auditor_id, audit_remark, publish_status, created_time, updated_time, deleted_flag) VALUES
(1, 2, '第1讲课件与代码示例', '包含Spring Boot快速入门课件、示例工程链接与课堂练习说明。', 'springboot-intro.pptx', '/uploads/demo/springboot-intro.pptx', 'pptx', 'APPROVED', 1, '内容完整，审核通过', 1, NOW(), NOW(), 0),
(2, 2, '第2讲索引优化案例', '包含执行计划案例、索引优化步骤与作业要求。', 'mysql-index.docx', '/uploads/demo/mysql-index.docx', 'docx', 'PENDING', NULL, NULL, 1, NOW(), NOW(), 0);

INSERT INTO course_enrollment(course_id, student_id, enroll_time, status) VALUES
(1, 4, NOW(), 1),
(1, 5, NOW(), 1),
(1, 6, NOW(), 1),
(2, 4, NOW(), 1),
(2, 7, NOW(), 1),
(2, 8, NOW(), 1);

INSERT INTO class_session(id, course_id, teacher_id, session_title, session_start_time, session_end_time, session_status, created_time, updated_time, deleted_flag) VALUES
(1, 1, 2, '第1讲：Spring Boot快速入门', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 90 MINUTE, 'FINISHED', NOW(), NOW(), 0),
(2, 1, 2, '第2讲：接口设计与权限控制', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 90 MINUTE, 'FINISHED', NOW(), NOW(), 0),
(3, 2, 2, '第1讲：MySQL表设计规范', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 90 MINUTE, 'FINISHED', NOW(), NOW(), 0),
(4, 2, 2, '第2讲：索引与执行计划', NOW(), NOW() + INTERVAL 90 MINUTE, 'ONGOING', NOW(), NOW(), 0);

INSERT INTO sign_in_task(id, session_id, course_id, teacher_id, task_title, start_time, end_time, task_status, penalty_score, created_time, updated_time, deleted_flag) VALUES
(1, 1, 1, 2, '第1讲签到', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 15 MINUTE, 'CLOSED', 5, NOW(), NOW(), 0),
(2, 2, 1, 2, '第2讲签到', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 15 MINUTE, 'CLOSED', 5, NOW(), NOW(), 0),
(3, 4, 2, 2, '第2讲签到', NOW(), NOW() + INTERVAL 15 MINUTE, 'OPEN', 5, NOW(), NOW(), 0);

INSERT INTO sign_in_record(task_id, session_id, course_id, student_id, sign_in_time, sign_in_status) VALUES
(1, 1, 1, 4, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 3 MINUTE, 'SIGNED'),
(1, 1, 1, 5, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 4 MINUTE, 'SIGNED'),
(1, 1, 1, 6, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 10 MINUTE, 'LATE'),
(2, 2, 1, 4, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 2 MINUTE, 'SIGNED'),
(2, 2, 1, 5, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 3 MINUTE, 'SIGNED');

INSERT INTO quiz(id, session_id, course_id, teacher_id, quiz_title, quiz_desc, total_score, start_time, end_time, quiz_status, created_time, updated_time, deleted_flag) VALUES
(1, 2, 1, 2, '接口规范小测', '考察REST接口规范', 100, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 40 MINUTE, 'PUBLISHED', NOW(), NOW(), 0),
(2, 3, 2, 2, 'SQL基础小测', '考察SQL语法与索引', 100, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 40 MINUTE, 'PUBLISHED', NOW(), NOW(), 0);

INSERT INTO quiz_question(id, quiz_id, question_title, question_type, options_json, correct_answer, score, sort_no) VALUES
(1, 1, 'RESTful接口中查询操作推荐使用哪种HTTP方法？', 'SINGLE', '["GET","POST","PUT","DELETE"]', 'GET', 50, 1),
(2, 1, 'JWT通常用于解决什么问题？', 'SINGLE', '["身份认证","图片压缩","数据库备份","负载均衡"]', '身份认证', 50, 2),
(3, 2, '下列哪个语句用于创建索引？', 'SINGLE', '["ALTER TABLE","CREATE INDEX","DROP INDEX","TRUNCATE"]', 'CREATE INDEX', 50, 1),
(4, 2, 'MySQL中查看执行计划使用什么关键字？', 'SINGLE', '["SHOW","PLAN","EXPLAIN","PROFILE"]', 'EXPLAIN', 50, 2);

INSERT INTO quiz_submission(quiz_id, session_id, course_id, student_id, answer_json, score, submit_status, submit_time) VALUES
(1, 2, 1, 4, '[{"questionId":1,"answer":"GET"},{"questionId":2,"answer":"身份认证"}]', 100, 'SUBMITTED', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 20 MINUTE),
(1, 2, 1, 5, '[{"questionId":1,"answer":"POST"},{"questionId":2,"answer":"身份认证"}]', 50, 'SUBMITTED', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 22 MINUTE),
(2, 3, 2, 4, '[{"questionId":3,"answer":"CREATE INDEX"},{"questionId":4,"answer":"EXPLAIN"}]', 100, 'SUBMITTED', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 25 MINUTE),
(2, 3, 2, 7, '[{"questionId":3,"answer":"ALTER TABLE"},{"questionId":4,"answer":"EXPLAIN"}]', 50, 'SUBMITTED', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 28 MINUTE);

INSERT INTO behavior_raw_data(student_id, teacher_id, course_id, class_session_id, online_flag, last_active_time, heartbeat_time, page_stay_duration_sec, focus_flag, blur_duration_sec, mouse_click_count, key_input_count, sign_in_flag, quiz_join_flag, class_stay_duration_sec, base_activity_score, raw_data_json, created_time, updated_time, deleted_flag) VALUES
(4, 2, 1, 2, 1, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 10 MINUTE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 10 MINUTE, 360, 1, 20, 18, 11, 1, 1, 1800, 75, '{"demo":true}', NOW(), NOW(), 0),
(5, 2, 1, 2, 1, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 20 MINUTE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 20 MINUTE, 360, 0, 220, 3, 1, 1, 0, 1800, 45, '{"demo":true}', NOW(), NOW(), 0),
(6, 2, 1, 2, 0, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 30 MINUTE, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 30 MINUTE, 360, 0, 300, 0, 0, 0, 0, 1800, 25, '{"demo":true}', NOW(), NOW(), 0),
(4, 2, 2, 3, 1, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 MINUTE, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 MINUTE, 360, 1, 40, 15, 10, 1, 1, 1800, 80, '{"demo":true}', NOW(), NOW(), 0),
(7, 2, 2, 3, 1, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 30 MINUTE, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 30 MINUTE, 360, 0, 260, 1, 1, 0, 0, 1800, 20, '{"demo":true}', NOW(), NOW(), 0);

INSERT INTO behavior_record(student_id, teacher_id, course_id, class_session_id, detect_time, behavior_status, behavior_type, status_description, activity_score, focus_flag, exception_flag, data_source, raw_data_summary, created_time, updated_time, deleted_flag) VALUES
(4, 2, 1, 2, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 10 MINUTE, '正常学习', 'NORMAL', '已签到，心跳正常，页面活跃', 86, 1, 0, 'INIT_DATA', 'online=1,focus=1,score=86', NOW(), NOW(), 0),
(5, 2, 1, 2, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 20 MINUTE, '轻度分心', 'WARNING', '页面长时间失焦', 38, 0, 1, 'INIT_DATA', 'online=1,focus=0,score=38', NOW(), NOW(), 0),
(6, 2, 1, 2, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 30 MINUTE, '学习状态异常', 'HIGH_RISK', '未签到；在线/交互指标显示可能挂机；未参与测验且活跃度较低', 10, 0, 1, 'INIT_DATA', 'online=0,focus=0,score=10', NOW(), NOW(), 0),
(4, 2, 2, 3, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 MINUTE, '正常学习', 'NORMAL', '已签到，心跳正常，页面活跃', 88, 1, 0, 'INIT_DATA', 'online=1,focus=1,score=88', NOW(), NOW(), 0),
(7, 2, 2, 3, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 30 MINUTE, '互动不足', 'WARNING', '未参与测验且活跃度较低', 22, 0, 1, 'INIT_DATA', 'online=1,focus=0,score=22', NOW(), NOW(), 0);

INSERT INTO operation_log(operator_id, operator_role, operation_type, operation_desc, request_path, request_method, operation_time) VALUES
(1, 'ADMIN', 'COURSE_AUDIT', '审核课程Java Web程序设计', '/api/admin/courses/1/audit', 'POST', NOW()),
(2, 'TEACHER', 'SESSION_CREATE', '创建课堂会话：接口设计与权限控制', '/api/teacher/sessions', 'POST', NOW()),
(4, 'STUDENT', 'COURSE_ENROLL', '选课：数据库原理与应用', '/api/student/courses/2/enroll', 'POST', NOW());

INSERT INTO sign_in_penalty(task_id, course_id, student_id, penalty_type, penalty_score, penalty_reason, created_time) VALUES
(1, 1, 7, 'MISSED_SIGNIN', 5, '任务截止未签到，自动记惩罚', NOW());
