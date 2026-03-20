<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Course Plaza</div><h1 class="page-title">课程广场</h1><p class="page-subtitle">你可以在这里浏览审核通过且已发布的课程，完成选课后将自动进入你的学习首页。</p></div><el-button @click="load">刷新</el-button></div>
  <div class="toolbar"><el-input v-model="keyword" clearable style="max-width: 280px" placeholder="搜索课程名称或课程编号…" /><el-button type="primary" plain @click="load">搜索</el-button></div>
  <div class="card-grid"><article v-for="item in list" :key="item.id" class="surface-card"><div class="action-row" style="justify-content: space-between; align-items: flex-start;"><div><h3 class="surface-card__title">{{ item.courseName }}</h3><div class="surface-card__meta">{{ item.teacherName || '未命名教师' }} · {{ item.courseCode }}</div></div><StatusBadge :text="item.enrolled ? '已选修' : '可加入'" /></div><p class="surface-card__meta" style="margin: 12px 0;">{{ item.courseIntro || item.contentSummary || '暂无课程简介。' }}</p><div class="action-row"><el-button :disabled="item.enrolled" type="primary" @click="enroll(item)">{{ item.enrolled ? '已选课' : '加入课程' }}</el-button><el-button plain @click="$router.push(`/student/classroom?courseId=${item.id}`)">预览学习器</el-button></div></article></div>
  <el-empty v-if="!list.length" description="暂无可加入课程" />
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import StatusBadge from '../../components/StatusBadge.vue'
const keyword = ref(''); const list = ref([])
const load = async () => { const res = await api.studentCourseList({ pageNum: 1, pageSize: 100, keyword: keyword.value }); list.value = res.records || [] }
const enroll = async (item) => { await api.studentEnroll(item.id); ElMessage.success(`已加入《${item.courseName}》`); await load() }
onMounted(load)
</script>
