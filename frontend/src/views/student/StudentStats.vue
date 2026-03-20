<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Learning Record</div><h1 class="page-title">学习记录</h1><p class="page-subtitle">展示你的签到、测验与学习趋势，帮助你判断最近的学习节奏是否稳定。</p></div><el-button @click="load">刷新</el-button></div>
  <div class="metric-grid"><StatCard v-for="item in metrics" :key="item.label" :label="item.label" :value="item.value" :hint="item.hint" /></div>
  <section class="surface-card"><h3 class="surface-card__title">最近趋势</h3><div v-if="trend.length" class="timeline-list"><article v-for="item in trend" :key="item.time" class="timeline-item"><div class="timeline-item__time">{{ item.time }}</div><h4 class="timeline-item__title">平均活动分 {{ Number(item.avgScore || 0).toFixed(2) }}</h4><p class="timeline-item__desc">来自学习过程记录，建议结合测验与签到结果一起理解。</p></article></div><el-empty v-else description="当前没有趋势数据" /></section></div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'
import StatCard from '../../components/StatCard.vue'
import { formatCount } from '../../utils/format'
const metrics = ref([]); const trend = ref([])
const load = async () => { const [overview, trendRes] = await Promise.all([api.studentStatsOverview(), api.studentStatsTrend({})]); metrics.value = [{ label: '签到次数', value: formatCount(overview.signCount), hint: '按你已完成的签到任务累计' }, { label: '测验次数', value: formatCount(overview.quizCount), hint: `当前测验均分 ${overview.quizAvgScore ?? 0}` }, { label: '学习记录', value: formatCount(overview.behaviorCount), hint: '学习器过程记录总量' }, { label: '风险提醒', value: formatCount(overview.abnormalCount), hint: '建议及时查看原因并调整学习方式' }]; trend.value = trendRes || [] }
onMounted(load)
</script>
