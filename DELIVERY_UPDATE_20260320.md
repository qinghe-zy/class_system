# 本次迭代说明

已完成以下改动：

1. 教师端资源上传支持 MP4 视频。
2. 管理员端资源审核页支持更完整的资源预览布局，视频可直接预览，PDF 可页内预览，其他文件可打开附件。
3. 学生端“学习内容”页改为“开始学习”入口，进入学习器后自动触发学习采集。
4. 学生端学习器新增返回上一级按钮。
5. 学生端学习器默认打开教师已发布资源，并在页面内优先预览视频/PDF/附件。
6. 学习采集入口由手动开关改为“开始学习 / 结束学习”按钮，更符合业务语义。

## 重点变更文件

- backend/src/main/java/com/ocms/backend/util/FileStorageService.java
- frontend/src/views/teacher/TeacherCourseContent.vue
- frontend/src/views/admin/AdminCourseContentAudit.vue
- frontend/src/views/student/StudentCourseContent.vue
- frontend/src/views/student/StudentClassroom.vue

## 验证情况

- 前端已执行 `npm ci` 后 `npm run build`，构建通过。
- 当前环境无 Maven，后端未执行打包命令；本次后端修改仅涉及文件上传类型与大小限制，改动较小。
