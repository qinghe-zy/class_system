<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Quiz</div><h1 class="page-title">测验管理</h1><p class="page-subtitle">教师可基于课堂活动发布测验、查看题目与学生提交情况，保持“课堂动作驱动”的页面表达。</p></div><div class="action-row"><el-button type="primary" @click="createQuiz">新建测验</el-button><el-button @click="load">刷新</el-button></div></div>
  <div class="toolbar"><el-select v-model="sessionId" filterable clearable placeholder="选择课堂" style="width: 320px" @change="load"><el-option v-for="item in sessions" :key="item.id" :label="`${item.sessionTitle} · ${courseName(item.courseId)}`" :value="item.id" /></el-select></div>
  <el-table :data="quizzes" border><el-table-column prop="quizTitle" label="测验名称" min-width="180" /><el-table-column label="所属课堂" min-width="180"><template #default="{ row }">{{ sessionName(row.sessionId) }}</template></el-table-column><el-table-column label="测验时段" min-width="220"><template #default="{ row }">{{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}</template></el-table-column><el-table-column label="状态" width="120"><template #default="{ row }"><StatusBadge :text="quizTimeText(row.timeStatus)" /></template></el-table-column><el-table-column label="操作" min-width="250"><template #default="{ row }"><div class="action-row"><el-button size="small" @click="editQuiz(row)">编辑</el-button><el-button size="small" @click="viewQuestions(row)">题目</el-button><el-button size="small" type="primary" plain @click="viewSubmissions(row)">提交</el-button></div></template></el-table-column></el-table>
  <el-drawer v-model="drawerVisible" :title="drawerTitle" size="46%"><el-table v-if="drawerMode === 'questions'" :data="questions" border><el-table-column prop="questionTitle" label="题目" min-width="220" /><el-table-column prop="questionType" label="题型" width="100" /><el-table-column prop="score" label="分值" width="90" /></el-table><el-table v-else :data="submissions" border><el-table-column prop="studentName" label="学生" min-width="140" /><el-table-column prop="score" label="得分" width="90" /><el-table-column label="提交时间" min-width="160"><template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template></el-table-column></el-table></el-drawer>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatDateTime, quizTimeText } from '../../utils/format'
const route = useRoute(); const router = useRouter(); const sessionId = ref(route.query.sessionId ? Number(route.query.sessionId) : undefined); const sessions = ref([]); const courses = ref([]); const quizzes = ref([]); const questions = ref([]); const submissions = ref([]); const drawerVisible = ref(false); const drawerMode = ref('questions'); const drawerTitle = ref('测验详情');
const courseMap = computed(() => Object.fromEntries(courses.value.map((item) => [item.id, item.courseName]))); const sessionMap = computed(() => Object.fromEntries(sessions.value.map((item) => [item.id, item]))); const courseName = (courseId) => courseMap.value[courseId] || `课程 ${courseId}`; const sessionName = (id) => sessionMap.value[id]?.sessionTitle || `课堂 ${id}`
const loadBase = async () => { const [courseRes, sessionRes] = await Promise.all([api.teacherCourses({ pageNum: 1, pageSize: 100 }), api.teacherSessions({})]); courses.value = courseRes.records || []; sessions.value = sessionRes || [] }
const load = async () => { quizzes.value = await api.teacherQuizzes({ sessionId: sessionId.value }) }
const createQuiz = () => { const target = sessionId.value || sessions.value[0]?.id; const session = sessionMap.value[target]; router.push({ path: '/teacher/quiz-editor', query: { sessionId: target, courseId: session?.courseId } }) }
const editQuiz = (row) => router.push({ path: '/teacher/quiz-editor', query: { quizId: row.id, sessionId: row.sessionId, courseId: row.courseId } })
const viewQuestions = async (row) => { drawerMode.value = 'questions'; drawerTitle.value = `${row.quizTitle} · 题目列表`; questions.value = await api.teacherQuizQuestions(row.id); drawerVisible.value = true }
const viewSubmissions = async (row) => { drawerMode.value = 'submissions'; drawerTitle.value = `${row.quizTitle} · 学生提交`; submissions.value = await api.teacherSubmissions(row.id); drawerVisible.value = true }
onMounted(async () => { await loadBase(); if (!sessionId.value && sessions.value.length) sessionId.value = sessions.value[0].id; await load() })
</script>
