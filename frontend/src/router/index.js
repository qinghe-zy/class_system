import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  { path: '/login', component: () => import('../views/common/Login.vue'), meta: { title: '登录' } },
  {
    path: '/',
    component: MainLayout,
    children: [
      { path: '', name: 'dashboard', component: () => import('../views/common/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'profile', name: 'profile', component: () => import('../views/common/Profile.vue'), meta: { title: '个人中心' } },

      { path: 'admin/users', component: () => import('../views/admin/AdminUsers.vue'), meta: { role: ['ADMIN'], title: '用户与角色' } },
      { path: 'admin/course-audit', component: () => import('../views/admin/AdminCourseAudit.vue'), meta: { role: ['ADMIN'], title: '课程审核' } },
      { path: 'admin/course-content-audit', component: () => import('../views/admin/AdminCourseContentAudit.vue'), meta: { role: ['ADMIN'], title: '资源审核' } },
      { path: 'admin/stats', component: () => import('../views/admin/AdminStats.vue'), meta: { role: ['ADMIN'], title: '平台分析' } },
      { path: 'admin/permission', component: () => import('../views/admin/AdminPermission.vue'), meta: { role: ['ADMIN'], title: '系统设置' } },
      { path: 'admin/courses', redirect: '/admin/course-audit' },
      { path: 'admin/sessions', redirect: '/admin/stats' },
      { path: 'admin/behavior', redirect: '/admin/stats' },

      { path: 'teacher/courses', component: () => import('../views/teacher/TeacherCourses.vue'), meta: { role: ['TEACHER'], title: '我的课程' } },
      { path: 'teacher/course-editor', component: () => import('../views/teacher/TeacherCourseEditor.vue'), meta: { role: ['TEACHER'], title: '课程编辑' } },
      { path: 'teacher/sessions', component: () => import('../views/teacher/TeacherSessions.vue'), meta: { role: ['TEACHER'], title: '课堂管理' } },
      { path: 'teacher/signin', component: () => import('../views/teacher/TeacherSignin.vue'), meta: { role: ['TEACHER'], title: '签到管理' } },
      { path: 'teacher/quiz', component: () => import('../views/teacher/TeacherQuiz.vue'), meta: { role: ['TEACHER'], title: '测验管理' } },
      { path: 'teacher/quiz-editor', component: () => import('../views/teacher/TeacherQuizEditor.vue'), meta: { role: ['TEACHER'], title: '测验编辑' } },
      { path: 'teacher/behavior', component: () => import('../views/teacher/TeacherBehavior.vue'), meta: { role: ['TEACHER'], title: '学习过程分析' } },
      { path: 'teacher/stats', component: () => import('../views/teacher/TeacherStats.vue'), meta: { role: ['TEACHER'], title: '学习分析' } },
      { path: 'teacher/course-content', component: () => import('../views/teacher/TeacherCourseContent.vue'), meta: { role: ['TEACHER'], title: '资源库' } },

      { path: 'student/course-list', component: () => import('../views/student/StudentCourseList.vue'), meta: { role: ['STUDENT'], title: '课程广场' } },
      { path: 'student/my-courses', component: () => import('../views/student/StudentMyCourses.vue'), meta: { role: ['STUDENT'], title: '我的课程' } },
      { path: 'student/classroom', component: () => import('../views/student/StudentClassroom.vue'), meta: { role: ['STUDENT'], title: '统一学习器' } },
      { path: 'student/signin', component: () => import('../views/student/StudentSignin.vue'), meta: { role: ['STUDENT'], title: '今日任务' } },
      { path: 'student/quiz', component: () => import('../views/student/StudentQuiz.vue'), meta: { role: ['STUDENT'], title: '课堂测验' } },
      { path: 'student/behavior', component: () => import('../views/student/StudentBehavior.vue'), meta: { role: ['STUDENT'], title: '风险说明' } },
      { path: 'student/stats', component: () => import('../views/student/StudentStats.vue'), meta: { role: ['STUDENT'], title: '学习记录' } },
      { path: 'student/course-content', component: () => import('../views/student/StudentCourseContent.vue'), meta: { role: ['STUDENT'], title: '学习内容' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login') return true
  if (!auth.token) return '/login'
  if (!auth.user) {
    try {
      await auth.fetchMe()
    } catch (error) {
      auth.logout()
      return '/login'
    }
  }
  const roles = to.meta?.role
  if (roles && !roles.includes(auth.role)) return '/'
  return true
})

export default router
