<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">Course Review</div><h1 class="page-title">课程审核</h1><p class="page-subtitle">管理员在此审核课程基础信息、教学简介、适用对象与发布状态，避免学生端接触未准备完成的课程。</p></div></div>
  <div class="toolbar"><el-input v-model="keyword" clearable style="max-width: 280px" placeholder="搜索课程名称或课程编号…" /><el-select v-model="auditStatus" clearable placeholder="审核状态" style="width: 160px"><el-option label="待审核" value="PENDING" /><el-option label="已通过" value="APPROVED" /><el-option label="已驳回" value="REJECTED" /></el-select><el-button type="primary" plain @click="load">筛选</el-button></div>
  <div class="card-grid"><article v-for="item in list" :key="item.id" class="surface-card"><div class="action-row" style="justify-content: space-between; align-items: flex-start;"><div><h3 class="surface-card__title">{{ item.courseName }}</h3><div class="surface-card__meta">{{ item.teacherName || '未指定教师' }} · {{ item.courseCode }}</div></div><StatusBadge :text="auditText(item.auditStatus)" /></div><p class="surface-card__meta" style="margin: 14px 0 10px;">{{ item.courseIntro || item.contentSummary || '暂无课程简介，请教师补充教学目标与适用对象。' }}</p><div class="info-list"><div class="info-item"><span>内容概述</span><strong class="break-words">{{ item.contentSummary || '待补充' }}</strong></div><div class="info-item"><span>更新时间</span><strong>{{ formatDateTime(item.updatedTime) }}</strong></div><div class="info-item"><span>发布状态</span><strong>{{ Number(item.publishStatus) === 1 ? '开放发布' : '暂不发布' }}</strong></div></div><div class="action-row" style="margin-top: 12px;"><el-button type="success" @click="audit(item, 'APPROVED')">通过</el-button><el-button type="danger" plain @click="audit(item, 'REJECTED')">驳回</el-button></div></article></div>
  <el-empty v-if="!list.length" description="当前没有符合条件的课程" /></div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { api } from '../../api'
import StatusBadge from '../../components/StatusBadge.vue'
import { auditText, formatDateTime } from '../../utils/format'
const list = ref([]); const keyword = ref(''); const auditStatus = ref('')
const load = async () => { const res = await api.adminCourses({ pageNum: 1, pageSize: 100, keyword: keyword.value, auditStatus: auditStatus.value }); list.value = res.records || [] }
const audit = async (item, status) => { const remark = await ElMessageBox.prompt(status === 'APPROVED' ? '可填写审核通过说明（可选）' : '请填写驳回原因，便于教师修改', status === 'APPROVED' ? '课程审核通过' : '课程审核驳回', { inputPlaceholder: status === 'APPROVED' ? '例如：课程结构清晰，可发布' : '例如：课程简介缺失、章节说明不足…', inputValue: status === 'APPROVED' ? '课程信息完整，可进入发布流程' : '' }).catch(() => null); if (!remark) return; await api.auditCourse(item.id, { auditStatus: status, remark: remark.value || '' }); ElMessage.success('审核结果已保存'); await load() }
onMounted(load)
</script>
