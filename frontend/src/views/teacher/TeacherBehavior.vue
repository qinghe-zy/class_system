<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Learning Analysis</div><h1 class="page-title">学习过程分析</h1><p class="page-subtitle">本页展示学习记录、活动分与风险等级，强调证据链与解释说明。</p></div><el-button @click="load">刷新</el-button></div>
  <div class="toolbar"><el-select v-model="courseId" clearable filterable placeholder="选择课程" style="width: 240px" @change="load"><el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" /></el-select><el-select v-model="sessionId" clearable filterable placeholder="选择课堂" style="width: 280px" @change="load"><el-option v-for="item in filteredSessions" :key="item.id" :label="item.sessionTitle" :value="item.id" /></el-select></div>
  <el-table :data="records" border><el-table-column label="检测时间" min-width="160"><template #default="{ row }">{{ formatDateTime(row.detectTime) }}</template></el-table-column><el-table-column prop="studentName" label="学生" min-width="120" /><el-table-column prop="courseName" label="课程" min-width="140" /><el-table-column prop="sessionTitle" label="课堂" min-width="140" /><el-table-column label="学习状态" width="120"><template #default="{ row }"><StatusBadge :text="behaviorText(row.behaviorStatus)" /></template></el-table-column><el-table-column label="风险等级" width="110"><template #default="{ row }"><StatusBadge :text="riskLabel(row.exceptionFlag, row.activityScore)" /></template></el-table-column><el-table-column prop="activityScore" label="活动分" width="90" /><el-table-column prop="statusDescription" label="解释说明" min-width="240" /></el-table>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../../api'
import StatusBadge from '../../components/StatusBadge.vue'
import { behaviorText, formatDateTime, riskLabel } from '../../utils/format'
const courses = ref([]); const sessions = ref([]); const courseId = ref(undefined); const sessionId = ref(undefined); const records = ref([])
const courseMap = computed(() => Object.fromEntries(courses.value.map((item) => [item.id, item.courseName]))); const sessionMap = computed(() => Object.fromEntries(sessions.value.map((item) => [item.id, item.sessionTitle]))); const filteredSessions = computed(() => sessions.value.filter((item) => !courseId.value || Number(item.courseId) === Number(courseId.value)))
const loadBase = async () => { const [courseRes, sessionRes] = await Promise.all([api.teacherCourses({ pageNum: 1, pageSize: 100 }), api.teacherSessions({})]); courses.value = courseRes.records || []; sessions.value = sessionRes || [] }
const load = async () => { const res = await api.teacherBehavior({ pageNum: 1, pageSize: 100, courseId: courseId.value, sessionId: sessionId.value }); records.value = (res.records || []).map((item) => ({ ...item, courseName: courseMap.value[item.courseId] || `课程 ${item.courseId}`, sessionTitle: sessionMap.value[item.classSessionId] || `课堂 ${item.classSessionId}`, studentName: item.studentName || `学生 ${item.studentId}` })) }
onMounted(async () => { await loadBase(); await load() })
</script>
