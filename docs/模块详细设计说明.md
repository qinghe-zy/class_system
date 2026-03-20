# 模块详细设计说明

## 1. 设计目标

本文档给出在线课堂管理系统的模块级详细设计，覆盖三端业务、权限边界、关键流程、核心规则和异常处理策略，保证实现可追踪、可测试、可维护。

## 2. 总体分层设计

### 2.1 后端分层

- Controller 层：参数接收、权限注解、统一响应。
- Service 层：业务编排、数据权限、事务逻辑、规则执行。
- Mapper 层：MyBatis Plus 持久化。
- Entity/DTO 层：数据库对象与请求对象。
- Security 层：JWT 解析、角色校验、上下文注入。

### 2.2 前端分层

- View 页面层：管理员/教师/学生页面。
- API 层：统一接口封装。
- Store 层：登录态与用户态管理。
- Router 层：路由与角色拦截。
- Utils 层：时间格式化、通用工具。

### 2.3 统一规范

- 统一返回：`code + message + data`。
- 统一认证：`Authorization: Bearer <token>`。
- 统一时间：前端提交 `yyyy-MM-ddTHH:mm:ss`，后端 `LocalDateTime`。
- 统一软删除字段：`deleted_flag`。

## 3. 认证与权限模块

### 3.1 业务目标

- 实现登录认证。
- 实现接口级角色权限控制。
- 实现路由级访问控制。
- 实现服务层数据权限隔离。

### 3.2 核心设计

- `AuthService.login`：用户名密码校验、角色查询、JWT 签发。
- `AuthInterceptor`：Token 解析、`@RequireRole` 校验。
- `AuthContext`：线程上下文保存当前用户信息。
- 前端 `router.beforeEach`：未登录重定向、角色路由拦截。

### 3.3 关键接口

- `POST /api/auth/login`
- `GET /api/auth/me`

### 3.4 边界处理

- Token 过期：返回 401，前端自动清理登录态并跳转登录页。
- 角色不匹配：返回 403。
- 用户禁用：登录拒绝。

## 4. 用户管理模块

### 4.1 业务目标

- 管理员管理教师与学生账户，支持增删改查、分页、重置密码、启用禁用。

### 4.2 核心表

- `sys_user`
- `sys_role`
- `sys_user_role`

### 4.3 核心规则

- 用户名唯一。
- 密码加密存储。
- 删除采用逻辑删除。
- 仅管理员可管理用户。

### 4.4 关键接口

- `GET /api/admin/users`
- `POST /api/admin/users`
- `PUT /api/admin/users/{id}`
- `DELETE /api/admin/users/{id}`
- `POST /api/admin/users/{id}/reset-password`

## 5. 课程与审核模块

### 5.1 业务目标

- 教师创建课程。
- 管理员审核课程。
- 学生仅可见审核通过课程。

### 5.2 核心表

- `course`
- `course_audit_record`

### 5.3 审核状态

- `PENDING`
- `APPROVED`
- `REJECTED`

### 5.4 关键规则

- 教师仅可操作自己的课程。
- 课程审核动作由管理员执行并记录审核日志。

### 5.5 关键接口

- `POST /api/teacher/courses`
- `PUT /api/teacher/courses/{id}`
- `POST /api/admin/courses/{id}/audit`
- `GET /api/student/courses/available`

## 6. 课堂内容发布与附件模块

### 6.1 业务目标

- 教师发布在线学习内容。
- 支持上传 Word/PPT/PDF 附件。
- 管理员审核后学生可见内容与附件。

### 6.2 核心表

- `course_content`

### 6.3 核心字段

- `attachment_name`
- `attachment_url`
- `attachment_type`
- `audit_status`
- `publish_status`

### 6.4 核心设计

- 文件上传接口限制扩展名：`doc/docx/ppt/pptx/pdf`。
- 文件保存至 `<backend>/uploads/yyyyMMdd/`。
- `WebMvcConfig` 暴露 `/uploads/**` 静态资源路径。
- 内容审核通过后，学生端可查询与下载。

### 6.5 关键接口

- `POST /api/teacher/files/upload`
- `POST /api/teacher/course-contents`
- `POST /api/admin/course-contents/{id}/audit`
- `GET /api/student/course-contents`

## 7. 选课与课堂会话模块

### 7.1 业务目标

- 学生选课。
- 教师创建课堂会话。
- 会话作为签到、测验、行为检测统一关联主线。

### 7.2 核心表

- `course_enrollment`
- `class_session`

### 7.3 核心规则

- 学生仅可选修审核通过课程。
- 教师仅可创建自己课程的会话。
- 会话状态区分：`NOT_STARTED / ONGOING / FINISHED`。

### 7.4 关键接口

- `POST /api/student/courses/{id}/enroll`
- `POST /api/teacher/sessions`
- `GET /api/student/sessions`

## 8. 签到与惩罚模块

### 8.1 业务目标

- 教师发起签到。
- 学生在时窗内签到。
- 过期不允许签到并自动惩罚。

### 8.2 核心表

- `sign_in_task`
- `sign_in_record`
- `sign_in_penalty`

### 8.3 核心规则

- 签到任务校验开始时间早于结束时间。
- 任务状态动态计算：`NOT_STARTED / OPEN / CLOSED`。
- 结束后未签到学生自动生成惩罚记录。
- 学生过期签到请求直接拒绝并写惩罚。

### 8.4 关键接口

- `POST /api/teacher/signin/tasks`
- `GET /api/student/signin/tasks`
- `POST /api/student/signin/{taskId}`
- `GET /api/student/signin/penalties`
- `GET /api/teacher/signin/penalties`

## 9. 测验与题目编辑模块

### 9.1 业务目标

- 教师发布测验并维护题目。
- 教师进入独立页面编辑题目与选项。
- 学生可同步看到教师发布测验。

### 9.2 核心表

- `quiz`
- `quiz_question`
- `quiz_submission`

### 9.3 题型设计

- `SINGLE`：单选题，必须有选项。
- `FILL`：填空题，允许多个参考答案（`|` 分隔）。

### 9.4 同步与状态

- 学生测验列表返回 `timeStatus`：`NOT_STARTED / ONGOING / EXPIRED`。
- 返回 `canAnswer`：仅在进行中且未提交为 `true`。
- 学生页面定时刷新，确保教师发布后可见。

### 9.5 编辑约束

- 已有学生提交的测验禁止修改题目。
- 编辑后重建题目明细，保持题号与排序一致。

### 9.6 关键接口

- `POST /api/teacher/quizzes`
- `PUT /api/teacher/quizzes/{quizId}`
- `GET /api/student/quizzes`
- `GET /api/student/quizzes/{quizId}/questions`
- `POST /api/student/quizzes/{quizId}/submit`

## 10. 行为检测核心模块

### 10.1 业务目标

- 学生在线课堂持续上报行为数据。
- 后端实时分析学习状态并持续落库。

### 10.2 服务拆分

- `BehaviorCollectService`
- `BehaviorAnalyzeService`
- `BehaviorRuleEngine`
- `BehaviorRecordService`

### 10.3 核心流程

1. 学生进入课堂，前端开始采集行为。
2. 每 15 秒上报一次心跳数据。
3. 后端保存 `behavior_raw_data`。
4. 规则引擎计算状态与活跃度。
5. 写入 `behavior_record`。
6. 三端按权限查询展示。

### 10.4 关键规则示例

- 已签到 + 心跳正常 + 页面聚焦：正常学习。
- 长时间无操作：疑似挂机。
- 失焦时长过长：轻度分心。
- 未签到或低活跃叠加：学习状态异常。

### 10.5 关键接口

- `POST /api/student/behavior/report`
- `GET /api/teacher/behavior/records`
- `GET /api/admin/behavior/records`
- `GET /api/student/behavior/records`

## 11. 统计分析模块

### 11.1 业务目标

- 管理员查看全局指标。
- 教师查看课程与班级指标。
- 学生查看个人学习趋势。

### 11.2 数据来源

- 用户、课程、会话、签到、测验、行为检测真实业务表。

### 11.3 图表输出

- 异常行为分布图。
- 检测趋势折线图。
- 教师课程指标图。
- 学生个人活跃趋势图。

### 11.4 关键接口

- `GET /api/admin/stats/overview`
- `GET /api/admin/stats/abnormal-course`
- `GET /api/admin/stats/detection-trend`
- `GET /api/teacher/stats/overview`
- `GET /api/student/stats/overview`

## 12. 可扩展设计

### 12.1 AI 模型扩展

- 在 `BehaviorAnalyzeService` 增加策略接口。
- 规则引擎与模型引擎可并行评分。

### 12.2 视频识别扩展

- 增加视频特征上报字段或独立表。
- 将视觉特征并入行为评分。

### 12.3 告警扩展

- 对 `HIGH_RISK` 记录发送站内通知。
- 后续可接入短信/邮件/企业微信。

