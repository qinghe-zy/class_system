<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">My Courses</div><h1 class="page-title">我的课程</h1><p class="page-subtitle">课程卡片展示你最关心的学习入口、老师信息与下一步动作，点击即可进入统一学习器。</p></div><div class="action-row"><el-button type="primary" plain @click="$router.push('/student/course-list')">继续选课</el-button><el-button @click="load">刷新</el-button></div></div>
  <div class="card-grid"><article v-for="item in list" :key="item.id" class="surface-card"><h3 class="surface-card__title">{{ item.courseName }}</h3><div class="surface-card__meta">{{ item.teacherName || '未命名教师' }} · {{ item.courseCode }}</div><p class="surface-card__meta" style="margin: 12px 0 14px;">{{ item.contentSummary || item.courseIntro || '暂无课程摘要。' }}</p><div class="action-row"><el-button type="primary" @click="$router.push(`/student/classroom?courseId=${item.id}`)">继续学习</el-button><el-button plain @click="$router.push(`/student/signin?courseId=${item.id}`)">查看任务</el-button></div></article></div>
  <el-empty v-if="!list.length" description="你还没有加入课程，先去课程广场看看吧" />
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'
const list = ref([])
const load = async () => { list.value = await api.studentMyCourses() }
onMounted(load)
</script>
