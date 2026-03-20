<template>
  <div class="page-card">
    <div class="page-hero"><div><div class="section-eyebrow">Workbench</div><h1 class="page-title">{{ heroTitle }}</h1><p class="page-subtitle">{{ heroSubtitle }}</p></div><div class="action-row"><el-button v-for="action in quickActions" :key="action.to" type="primary" plain @click="$router.push(action.to)">{{ action.label }}</el-button></div></div>
    <div class="metric-grid"><StatCard v-for="item in metrics" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" /></div>
    <div class="content-grid">
      <section class="surface-card"><h3 class="surface-card__title">{{ leftTitle }}</h3><div v-if="leftItems.length" class="timeline-list"><article v-for="item in leftItems" :key="item.title + item.desc" class="timeline-item"><div class="timeline-item__time">{{ item.time }}</div><h4 class="timeline-item__title">{{ item.title }}</h4><p class="timeline-item__desc">{{ item.desc }}</p><div v-if="item.to" class="action-row"><el-button text type="primary" @click="$router.push(item.to)">查看详情</el-button></div></article></div><el-empty v-else description="暂无内容" /></section>
      <section class="surface-card"><h3 class="surface-card__title">{{ rightTitle }}</h3><div v-if="rightItems.length" class="timeline-list"><article v-for="item in rightItems" :key="item.title + item.desc" class="timeline-item"><div class="timeline-item__time">{{ item.time }}</div><h4 class="timeline-item__title">{{ item.title }}</h4><p class="timeline-item__desc">{{ item.desc }}</p><div v-if="item.to" class="action-row"><el-button text type="primary" @click="$router.push(item.to)">进入页面</el-button></div></article></div><el-empty v-else description="暂无提醒" /></section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useAuthStore } from '../../store/auth'
import { api } from '../../api'
import StatCard from '../../components/StatCard.vue'
import { formatCount, formatDateTime } from '../../utils/format'
const auth = useAuthStore()
const metrics = ref([]); const leftItems = ref([]); const rightItems = ref([]); const heroTitle = ref('工作台'); const heroSubtitle = ref(''); const leftTitle = ref('最新动态'); const rightTitle = ref('待办事项'); const quickActions = ref([])
async function loadAdmin() {
  const [overview, courses, contents] = await Promise.all([api.adminStatsOverview(), api.adminCourses({ pageNum: 1, pageSize: 4 }), api.adminCourseContents({ auditStatus: 'PENDING' })])
  metrics.value = [
    { label: '平台用户', value: formatCount(overview.userTotal), hint: `教师 ${formatCount(overview.teacherTotal)} · 学生 ${formatCount(overview.studentTotal)}` },
    { label: '课程总数', value: formatCount(overview.courseTotal), hint: `课堂活动 ${formatCount(overview.sessionTotal)}` },
    { label: '学习记录', value: formatCount(overview.detectTotal), hint: '学习过程数据与风险提示' },
    { label: '风险提醒', value: formatCount(overview.abnormalTotal), hint: '需要管理员关注的异常课程与学习记录' }
  ]
  leftItems.value = (courses.records || []).slice(0, 4).map((item) => ({ time: formatDateTime(item.updatedTime), title: item.courseName, desc: `教师：${item.teacherName || '未命名'} · 审核状态：${item.auditStatus}`, to: '/admin/course-audit' }))
  rightItems.value = (contents || []).slice(0, 4).map((item) => ({ time: formatDateTime(item.updatedTime), title: item.contentTitle, desc: '待审核资源 · 可在资源审核页处理', to: '/admin/course-content-audit' }))
  heroTitle.value = '管理员工作台'; heroSubtitle.value = '聚焦课程审核、资源审核、平台趋势与权限治理，优先处理待审与高风险事项。'; leftTitle.value = '最近课程动态'; rightTitle.value = '待审核资源'; quickActions.value = [{ label: '进入课程审核', to: '/admin/course-audit' }, { label: '进入资源审核', to: '/admin/course-content-audit' }, { label: '查看平台分析', to: '/admin/stats' }]
}
async function loadTeacher() {
  const [courses, sessions, stats] = await Promise.all([api.teacherCourses({ pageNum: 1, pageSize: 6 }), api.teacherSessions({}), api.teacherStatsOverview({})])
  metrics.value = [
    { label: '我的课程', value: formatCount(courses.total || courses.records?.length || 0), hint: '已创建课程总数' },
    { label: '签到覆盖率', value: `${stats.signRate ?? 0}%`, hint: '来自签到记录的课堂参与数据' },
    { label: '测验参与率', value: `${stats.quizJoinRate ?? 0}%`, hint: `测验均分 ${stats.quizAvgScore ?? 0}` },
    { label: '待关注学生', value: formatCount(stats.abnormalStudentCount), hint: '系统只提供风险提示，教师仍需结合证据链判断' }
  ]
  leftItems.value = (courses.records || []).slice(0, 4).map((item) => ({ time: formatDateTime(item.updatedTime), title: item.courseName, desc: `${item.contentSummary || '请完善课程简介与学习要求'}` }))
  rightItems.value = (sessions || []).slice(0, 4).map((item) => ({ time: formatDateTime(item.sessionStartTime), title: item.sessionTitle, desc: '可在课堂管理页发起签到、测验与课堂提醒', to: '/teacher/sessions' }))
  heroTitle.value = '教师工作台'; heroSubtitle.value = '从建课、上传资源、开始课堂到查看学习进度，整条链路都使用自然业务语言。'; leftTitle.value = '最近课程'; rightTitle.value = '课堂安排'; quickActions.value = [{ label: '新建课程', to: '/teacher/course-editor' }, { label: '上传资源', to: '/teacher/course-content' }, { label: '查看学习分析', to: '/teacher/stats' }]
}
async function loadStudent() {
  const [courses, stats, tasks, quizzes] = await Promise.all([api.studentMyCourses(), api.studentStatsOverview(), api.studentSignTasks({}), api.studentQuizzes({})])
  metrics.value = [
    { label: '已选课程', value: formatCount(courses.length), hint: '进入课程后可继续学习文档、课件与视频' },
    { label: '签到次数', value: formatCount(stats.signCount), hint: '系统会基于任务时段记录签到状态' },
    { label: '测验均分', value: stats.quizAvgScore ?? 0, hint: `累计测验 ${formatCount(stats.quizCount)}` },
    { label: '风险提醒', value: formatCount(stats.abnormalCount), hint: '风险仅作学习提醒，不直接定性处罚' }
  ]
  leftItems.value = courses.slice(0, 4).map((item) => ({ time: formatDateTime(item.updatedTime), title: item.courseName, desc: `${item.teacherName || '教师未命名'} · 点击后进入统一学习器`, to: `/student/classroom?courseId=${item.id}` }))
  rightItems.value = [...(tasks || []).slice(0, 2).map((item) => ({ time: formatDateTime(item.endTime), title: item.taskTitle, desc: '签到任务待处理，请注意结束时间', to: '/student/signin' })), ...(quizzes || []).slice(0, 2).map((item) => ({ time: formatDateTime(item.endTime), title: item.quizTitle, desc: '课堂测验待完成，请按时提交', to: '/student/quiz' }))]
  heroTitle.value = '学习首页'; heroSubtitle.value = '你可以从课程、任务、测验与学习记录四个维度快速进入当前学习状态。'; leftTitle.value = '最近课程'; rightTitle.value = '今日待办'; quickActions.value = [{ label: '继续学习', to: '/student/my-courses' }, { label: '查看任务', to: '/student/signin' }, { label: '学习记录', to: '/student/stats' }]
}
onMounted(async () => { if (auth.role === 'ADMIN') await loadAdmin(); if (auth.role === 'TEACHER') await loadTeacher(); if (auth.role === 'STUDENT') await loadStudent() })
</script>
