<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Platform Analytics</div><h1 class="page-title">平台分析</h1><p class="page-subtitle">展示平台级课程规模、学习记录趋势与高风险课程排行，帮助管理员安排审核与运营优先级。</p></div><el-button @click="load">刷新数据</el-button></div>
  <div class="metric-grid"><StatCard v-for="item in metrics" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" /></div>
  <div class="content-grid"><section class="surface-card"><h3 class="surface-card__title">高风险课程排行</h3><div v-if="courses.length" class="timeline-list"><article v-for="item in courses" :key="item.courseId" class="timeline-item"><div class="timeline-item__time">课程风险提醒</div><h4 class="timeline-item__title">{{ item.courseName }}</h4><p class="timeline-item__desc">累计风险记录 {{ item.abnormalCount }} 条，建议优先查看课程审核与教师侧分析页面。</p></article></div><el-empty v-else description="暂无高风险课程" /></section><section class="surface-card"><h3 class="surface-card__title">学习记录趋势</h3><div v-if="trends.length" class="timeline-list"><article v-for="item in trends" :key="item.date" class="timeline-item"><div class="timeline-item__time">{{ item.date }}</div><h4 class="timeline-item__title">{{ item.total }} 条学习记录</h4><p class="timeline-item__desc">趋势数据来自学习过程记录与异常提醒统计。</p></article></div><el-empty v-else description="暂无趋势数据" /></section></div></div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'
import StatCard from '../../components/StatCard.vue'
import { formatCount } from '../../utils/format'
const metrics = ref([]); const courses = ref([]); const trends = ref([])
const load = async () => { const [overview, abnormalCourse, detectionTrend] = await Promise.all([api.adminStatsOverview(), api.adminStatsCourse(), api.adminStatsTrend()]); metrics.value = [{ label: '平台用户', value: formatCount(overview.userTotal), hint: `教师 ${formatCount(overview.teacherTotal)} · 学生 ${formatCount(overview.studentTotal)}` }, { label: '课程总量', value: formatCount(overview.courseTotal), hint: `课堂活动 ${formatCount(overview.sessionTotal)}` }, { label: '学习记录', value: formatCount(overview.detectTotal), hint: '包含学习过程与异常提醒统计' }, { label: '风险提醒', value: formatCount(overview.abnormalTotal), hint: '系统只提供辅助判断，不直接处罚' }]; courses.value = abnormalCourse || []; trends.value = detectionTrend || [] }
onMounted(load)
</script>
