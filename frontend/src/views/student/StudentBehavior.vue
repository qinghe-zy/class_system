<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Risk Explanation</div><h1 class="page-title">风险说明</h1><p class="page-subtitle">系统会把学习过程中的异常模式整理为风险提示，并向你解释原因，避免黑箱感与误解。</p></div><el-button @click="load">刷新</el-button></div>
  <div class="metric-grid"><StatCard label="说明原则" value="辅助判断" hint="系统不会直接给出惩罚结论，只提示风险与下一步建议。" /><StatCard label="常见提醒" value="跳播 / 后台 / 低交互" hint="这些信号需要结合课程内容、签到与测验共同理解。" /><StatCard label="学生可见" value="可解释" hint="你可以看到风险时间点、状态文案与学习建议。" /></div>
  <el-table :data="records" border><el-table-column label="记录时间" min-width="160"><template #default="{ row }">{{ formatDateTime(row.detectTime) }}</template></el-table-column><el-table-column prop="courseName" label="课程" min-width="140" /><el-table-column prop="sessionTitle" label="课堂" min-width="140" /><el-table-column label="学习状态" width="120"><template #default="{ row }"><StatusBadge :text="behaviorText(row.behaviorStatus)" /></template></el-table-column><el-table-column label="风险等级" width="110"><template #default="{ row }"><StatusBadge :text="riskLabel(row.exceptionFlag, row.activityScore)" /></template></el-table-column><el-table-column prop="activityScore" label="活动分" width="90" /><el-table-column prop="statusDescription" label="说明" min-width="240" /></el-table>
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'
import StatCard from '../../components/StatCard.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { behaviorText, formatDateTime, riskLabel } from '../../utils/format'
const records = ref([]); const courses = ref([]); const sessions = ref([]); const courseMap = ref({}); const sessionMap = ref({})
const loadBase = async () => { const [courseRes, sessionRes] = await Promise.all([api.studentMyCourses(), api.studentSessions({})]); courses.value = courseRes || []; sessions.value = sessionRes || []; courseMap.value = Object.fromEntries(courses.value.map((item) => [item.id, item.courseName])); sessionMap.value = Object.fromEntries(sessions.value.map((item) => [item.id, item.sessionTitle])) }
const load = async () => { const res = await api.studentBehavior({ pageNum: 1, pageSize: 100 }); records.value = (res.records || []).map((item) => ({ ...item, courseName: courseMap.value[item.courseId] || `课程 ${item.courseId}`, sessionTitle: sessionMap.value[item.classSessionId] || `课堂 ${item.classSessionId}` })) }
onMounted(async () => { await loadBase(); await load() })
</script>
