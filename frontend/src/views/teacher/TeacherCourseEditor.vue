<template>
  <div class="page-card">
    <div class="page-hero"><div><div class="section-eyebrow">Course Editor</div><h1 class="page-title">{{ form.id ? '编辑课程' : '新建课程' }}</h1><p class="page-subtitle">课程主数据以课程名称、简介、适用说明与发布状态为核心，后续资源与课堂活动围绕课程自然展开。</p></div><div class="action-row"><el-button @click="$router.push('/teacher/courses')">返回课程列表</el-button></div></div>
    <el-form :model="form" label-position="top" @submit.prevent="submit">
      <div class="content-grid">
        <section class="surface-card"><h3 class="surface-card__title">课程基础信息</h3><div class="form-grid"><el-form-item label="课程名称"><el-input v-model="form.courseName" /></el-form-item><el-form-item label="课程编号"><el-input v-model="form.courseCode" spellcheck="false" /></el-form-item><el-form-item label="封面地址"><el-input v-model="form.courseCover" /></el-form-item><el-form-item label="发布状态"><el-select v-model="form.publishStatus"><el-option label="发布" :value="1" /><el-option label="暂不发布" :value="0" /></el-select></el-form-item></div><el-form-item label="课程简介"><el-input v-model="form.courseIntro" type="textarea" :rows="4" maxlength="500" show-word-limit /></el-form-item><el-form-item label="内容概述与学习要求"><el-input v-model="form.contentSummary" type="textarea" :rows="6" maxlength="800" show-word-limit /></el-form-item></section>
        <section class="surface-card"><h3 class="surface-card__title">发布说明</h3><div class="timeline-list"><article class="timeline-item"><div class="timeline-item__time">审核流程</div><h4 class="timeline-item__title">教师提交后进入管理员审核</h4><p class="timeline-item__desc">课程基础信息修改后会重新进入待审核状态，确保学生端只看到审核通过版本。</p></article><article class="timeline-item"><div class="timeline-item__time">页面原则</div><h4 class="timeline-item__title">禁止把课程 ID 当主入口</h4><p class="timeline-item__desc">教师后续进入资源库、课堂管理与学习分析时，都从课程卡片自然进入。</p></article></div></section>
      </div>
      <div class="action-row" style="margin-top: 18px;"><el-button type="primary" :loading="loading" native-type="submit">保存课程</el-button></div>
    </el-form>
  </div>
</template>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
const route = useRoute(); const router = useRouter(); const loading = ref(false)
const form = reactive({ id: null, courseName: '', courseCode: '', courseIntro: '', courseCover: '', contentSummary: '', publishStatus: 1 })
onMounted(async () => { const id = route.query.id; if (!id) return; const res = await api.teacherCourses({ pageNum: 1, pageSize: 100 }); const current = (res.records || []).find((item) => String(item.id) === String(id)); if (!current) return; Object.assign(form, current) })
const submit = async () => { loading.value = true; try { if (form.id) { await api.updateCourse(form.id, { ...form }); ElMessage.success('课程已更新') } else { await api.createCourse({ ...form }); ElMessage.success('课程已创建') } router.push('/teacher/courses') } finally { loading.value = false } }
</script>
