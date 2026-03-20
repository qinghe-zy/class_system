<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Teacher Analytics</div><h1 class="page-title">学习分析</h1><p class="page-subtitle">教师视角查看签到、测验、风险学生与课堂趋势。</p></div><el-button @click="load">刷新数据</el-button></div>
  <div class="toolbar"><el-select v-model="courseId" clearable filterable placeholder="选择课程" style="width: 260px" @change="load"><el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" /></el-select><el-select v-model="sessionId" clearable filterable placeholder="选择课堂" style="width: 280px" @change="load"><el-option v-for="item in filteredSessions" :key="item.id" :label="item.sessionTitle" :value="item.id" /></el-select></div>
  <div class="metric-grid"><StatCard v-for="item in metrics" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" /></div>
  <div class="content-grid"><section class="surface-card"><h3 class="surface-card__title">课堂趋势</h3><div v-if="trend.length" class="timeline-list"><article v-for="item in trend" :key="item.time" class="timeline-item"><div class="timeline-item__time">{{ item.time }}</div><h4 class="timeline-item__title">{{ item.count }} 条学习记录</h4><p class="timeline-item__desc">系统按时间聚合课堂过程数据，可用于观察本节课堂活跃变化。</p></article></div><el-empty v-else description="当前课堂暂无趋势数据" /></section><section class="surface-card"><h3 class="surface-card__title">状态分布</h3><div v-if="distribution.length" class="timeline-list"><article v-for="item in distribution" :key="item.status" class="timeline-item"><div class="timeline-item__time">{{ behaviorText(item.status) }}</div><h4 class="timeline-item__title">{{ item.count }} 次记录</h4><p class="timeline-item__desc">帮助你快速判断本节课堂整体参与与风险情况。</p></article></div><el-empty v-else description="暂无状态分布" /></section></div>
  </div>
</template>
<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../../api'
import StatCard from '../../components/StatCard.vue'
import { behaviorText, formatCount } from '../../utils/format'
const route = useRoute(); const courses = ref([]); const sessions = ref([]); const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined); const sessionId = ref(route.query.sessionId ? Number(route.query.sessionId) : undefined); const metrics = ref([]); const trend = ref([]); const distribution = ref([])
const filteredSessions = computed(() => sessions.value.filter((item) => !courseId.value || Number(item.courseId) === Number(courseId.value)))
const loadBase = async () => { const [courseRes, sessionRes] = await Promise.all([api.teacherCourses({ pageNum: 1, pageSize: 100 }), api.teacherSessions({})]); courses.value = courseRes.records || []; sessions.value = sessionRes || [] }
const load = async () => { const [overview, trendRes] = await Promise.all([api.teacherStatsOverview({ courseId: courseId.value }), api.teacherStatsTrend({ sessionId: sessionId.value })]); metrics.value = [{ label: '签到覆盖率', value: `${overview.signRate ?? 0}%`, hint: '基于签到任务与选课人数计算' }, { label: '测验参与率', value: `${overview.quizJoinRate ?? 0}%`, hint: `测验均分 ${overview.quizAvgScore ?? 0}` }, { label: '待关注学生', value: formatCount(overview.abnormalStudentCount), hint: '建议结合风险详情与课堂证据链进行判断' }, { label: '状态分布', value: formatCount((overview.behaviorDistribution || []).length), hint: '本期采样到的课堂状态种类' }]; trend.value = trendRes || []; distribution.value = overview.behaviorDistribution || [] }
onMounted(async () => { await loadBase(); await load() })
</script>
