<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Today Tasks</div><h1 class="page-title">今日任务</h1><p class="page-subtitle">集中处理签到任务，清楚看到时间范围、完成状态与过期惩罚说明。</p></div><el-button @click="load">刷新</el-button></div>
  <div class="toolbar"><el-select v-model="courseId" clearable filterable placeholder="选择课程" style="width: 260px" @change="load"><el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" /></el-select></div>
  <el-table :data="tasks" border><el-table-column prop="taskTitle" label="任务名称" min-width="180" /><el-table-column prop="courseName" label="课程" min-width="160" /><el-table-column label="时间范围" min-width="220"><template #default="{ row }">{{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}</template></el-table-column><el-table-column label="状态" width="120"><template #default="{ row }"><StatusBadge :text="signTaskText(row.taskStatus)" /></template></el-table-column><el-table-column prop="penaltyReason" label="说明" min-width="220" /><el-table-column label="操作" width="130"><template #default="{ row }"><el-button size="small" type="primary" :disabled="row.signed || row.expired" @click="sign(row)">{{ row.signed ? '已签到' : row.expired ? '已过期' : '立即签到' }}</el-button></template></el-table-column></el-table>
  <el-empty v-if="!tasks.length" description="当前没有签到任务" /></div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatDateTime, signTaskText } from '../../utils/format'
const route = useRoute(); const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined); const courses = ref([]); const tasks = ref([]); const courseMap = ref({})
const loadBase = async () => { courses.value = await api.studentMyCourses(); courseMap.value = Object.fromEntries(courses.value.map((item) => [item.id, item.courseName])) }
const load = async () => { const list = await api.studentSignTasks({ courseId: courseId.value }); tasks.value = (list || []).map((item) => ({ ...item, courseName: courseMap.value[item.courseId] || `课程 ${item.courseId}` })) }
const sign = async (row) => { await api.studentSignIn(row.id); ElMessage.success(`已完成《${row.taskTitle}》签到`); await load() }
onMounted(async () => { await loadBase(); await load() })
</script>
