import request from '../utils/request'

export const api = {
  login: (data) => request.post('/api/auth/login', data),
  me: () => request.get('/api/auth/me'),

  adminUsers: (params) => request.get('/api/admin/users', { params }),
  createUser: (data) => request.post('/api/admin/users', data),
  updateUser: (id, data) => request.put(`/api/admin/users/${id}`, data),
  deleteUser: (id) => request.delete(`/api/admin/users/${id}`),
  resetUserPwd: (id, password = '123456') => request.post(`/api/admin/users/${id}/reset-password`, null, { params: { password } }),

  teacherCourses: (params) => request.get('/api/teacher/courses', { params }),
  createCourse: (data) => request.post('/api/teacher/courses', data),
  updateCourse: (id, data) => request.put(`/api/teacher/courses/${id}`, data),
  deleteCourse: (id) => request.delete(`/api/teacher/courses/${id}`),
  teacherStudents: (id) => request.get(`/api/teacher/courses/${id}/students`),

  adminCourses: (params) => request.get('/api/admin/courses', { params }),
  auditCourse: (id, data) => request.post(`/api/admin/courses/${id}/audit`, data),

  studentCourseList: (params) => request.get('/api/student/courses/available', { params }),
  studentEnroll: (id) => request.post(`/api/student/courses/${id}/enroll`),
  studentMyCourses: () => request.get('/api/student/courses/mine'),

  teacherSessions: (params) => request.get('/api/teacher/sessions', { params }),
  createSession: (data) => request.post('/api/teacher/sessions', data),
  updateSession: (id, data) => request.put(`/api/teacher/sessions/${id}`, data),
  adminSessions: () => request.get('/api/admin/sessions'),
  studentSessions: (params) => request.get('/api/student/sessions', { params }),

  createSignTask: (data) => request.post('/api/teacher/signin/tasks', data),
  teacherSignTasks: (params) => request.get('/api/teacher/signin/tasks', { params }),
  teacherSignRecords: (params) => request.get('/api/teacher/signin/records', { params }),
  teacherSignPenalties: (params) => request.get('/api/teacher/signin/penalties', { params }),
  studentSignTasks: (params) => request.get('/api/student/signin/tasks', { params }),
  studentSignIn: (taskId) => request.post(`/api/student/signin/${taskId}`),
  studentSignRecords: () => request.get('/api/student/signin/records'),
  studentSignPenalties: () => request.get('/api/student/signin/penalties'),
  adminSignOverview: () => request.get('/api/admin/signin/overview'),
  adminSignPenalties: () => request.get('/api/admin/signin/penalties'),

  createQuiz: (data) => request.post('/api/teacher/quizzes', data),
  updateQuiz: (quizId, data) => request.put(`/api/teacher/quizzes/${quizId}`, data),
  teacherQuizzes: (params) => request.get('/api/teacher/quizzes', { params }),
  teacherQuizQuestions: (quizId) => request.get(`/api/teacher/quizzes/${quizId}/questions`),
  teacherSubmissions: (quizId) => request.get(`/api/teacher/quizzes/${quizId}/submissions`),
  studentQuizzes: (params) => request.get('/api/student/quizzes', { params }),
  studentQuizQuestions: (quizId) => request.get(`/api/student/quizzes/${quizId}/questions`),
  studentSubmitQuiz: (quizId, data) => request.post(`/api/student/quizzes/${quizId}/submit`, data),
  studentQuizSubmissions: () => request.get('/api/student/quizzes/submissions'),

  reportBehavior: (data) => request.post('/api/student/behavior/report', data),
  adminBehavior: (params) => request.get('/api/admin/behavior/records', { params }),
  teacherBehavior: (params) => request.get('/api/teacher/behavior/records', { params }),
  studentBehavior: (params) => request.get('/api/student/behavior/records', { params }),

  adminStatsOverview: () => request.get('/api/admin/stats/overview'),
  adminStatsCourse: () => request.get('/api/admin/stats/abnormal-course'),
  adminStatsTrend: () => request.get('/api/admin/stats/detection-trend'),

  teacherStatsOverview: (params) => request.get('/api/teacher/stats/overview', { params }),
  teacherStatsTrend: (params) => request.get('/api/teacher/stats/session-trend', { params }),

  studentStatsOverview: () => request.get('/api/student/stats/overview'),
  studentStatsTrend: (params) => request.get('/api/student/stats/activity-trend', { params }),

  adminPermissions: () => request.get('/api/admin/permissions'),
  teacherCourseContents: (params) => request.get('/api/teacher/course-contents', { params }),
  createCourseContent: (data) => request.post('/api/teacher/course-contents', data),
  updateCourseContent: (id, data) => request.put(`/api/teacher/course-contents/${id}`, data),
  uploadTeacherFile: (formData, options = {}) => request.post('/api/teacher/files/upload', formData, {
    timeout: Number(import.meta.env.VITE_UPLOAD_TIMEOUT_MS || 10 * 60 * 1000),
    onUploadProgress: options.onUploadProgress
  }),
  adminCourseContents: (params) => request.get('/api/admin/course-contents', { params }),
  adminAuditCourseContent: (id, data) => request.post(`/api/admin/course-contents/${id}/audit`, data),
  studentCourseContents: (params) => request.get('/api/student/course-contents', { params }),
  updateProfile: (data) => request.put('/api/profile', data)
}
