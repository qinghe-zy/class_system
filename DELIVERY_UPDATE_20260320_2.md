# 本次更新说明（文档/资源预览修复）

## 已修复

1. 修复了资源附件使用相对路径时，前端会去 `5173/uploads/...` 而不是 `8080/uploads/...` 请求文件的问题。
2. 学生端学习器现在会把附件地址自动解析为后端完整地址。
3. 教师端资源预览抽屉现在会把附件地址自动解析为后端完整地址。
4. 管理员端资源审核预览现在会把附件地址自动解析为后端完整地址。
5. 对 `doc/docx/ppt/pptx` 增加了更清晰的提示：
   - 浏览器通常不能直接嵌入预览 Office 文件
   - 可一键打开原文件
   - 可复制资源地址
6. 学生点击“开始学习”后，若当前资源是 Office 文件，会自动在新窗口打开原文件，避免页面黑屏但没有反馈。

## 说明

- `pdf` 和 `mp4` 仍然优先页内预览。
- `doc/docx/ppt/pptx` 是否能页内直接显示，取决于浏览器本身，不是普通 iframe 一定能做到。
- 这次已经把“黑屏无反馈”改成了“可访问原文件 + 明确提示”的可用方案。

## 这次新增/修改文件

- `frontend/src/utils/assets.js`
- `frontend/src/views/student/StudentClassroom.vue`
- `frontend/src/views/teacher/TeacherCourseContent.vue`
- `frontend/src/views/admin/AdminCourseContentAudit.vue`
