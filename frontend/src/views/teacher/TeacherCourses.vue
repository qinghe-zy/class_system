<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">My Courses</div>
        <h1 class="page-title">我的课程</h1>
        <p class="page-subtitle">课程卡片只展示教师真正关心的课程名称、简介、审核状态与后续动作，不再把内部编号作为主入口。</p>
      </div>
      <div class="action-row">
        <el-button type="primary" @click="$router.push('/teacher/course-editor')">新建课程</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>
    <div class="toolbar">
      <el-input v-model="keyword" style="max-width: 280px" clearable placeholder="搜索课程名称或课程编号…" aria-label="搜索课程" />
      <el-button type="primary" plain @click="load">搜索</el-button>
    </div>
    <div class="card-grid">
      <article v-for="item in list" :key="item.id" class="surface-card">
        <div class="action-row" style="justify-content: space-between; align-items: flex-start;">
          <div>
            <h3 class="surface-card__title">{{ item.courseName }}</h3>
            <div class="surface-card__meta">{{ item.courseCode }} · 更新于 {{ formatDateTime(item.updatedTime) }}</div>
          </div>
          <div class="action-row">
            <StatusBadge :text="auditText(item.auditStatus)" />
            <StatusBadge :text="publishText(item.publishStatus)" />
          </div>
        </div>
        <p class="surface-card__meta" style="margin: 14px 0;">{{ item.courseIntro || item.contentSummary || '请补充课程简介，让学生更容易理解课程目标与学习收益。' }}</p>
        <div class="action-row">
          <el-button size="small" @click="edit(item)">编辑课程</el-button>
          <el-button size="small" @click="openContent(item)">资源库</el-button>
          <el-button size="small" @click="openSessions(item)">课堂管理</el-button>
          <el-button size="small" type="primary" plain @click="openStats(item)">学习分析</el-button>
        </div>
        <div class="action-row" style="margin-top: 10px;"><el-button text type="primary" @click="showStudents(item)">查看选课学生</el-button></div>
      </article>
    </div>
    <el-empty v-if="!list.length" description="当前还没有课程，先创建第一门课程吧" />
  </div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { api } from '../../api'
import StatusBadge from '../../components/StatusBadge.vue'
import { auditText, formatDateTime, publishText } from '../../utils/format'
const list = ref([])
const keyword = ref('')
const load = async () => { const res = await api.teacherCourses({ pageNum: 1, pageSize: 100, keyword: keyword.value }); list.value = res.records || [] }
const edit = (item) => window.location.assign(`/teacher/course-editor?id=${item.id}`)
const openContent = (item) => window.location.assign(`/teacher/course-content?courseId=${item.id}`)
const openSessions = (item) => window.location.assign(`/teacher/sessions?courseId=${item.id}`)
const openStats = (item) => window.location.assign(`/teacher/stats?courseId=${item.id}`)
const showStudents = async (item) => {
  const students = await api.teacherStudents(item.id)
  await ElMessageBox.alert(students.length ? students.map((student) => `${student.realName}（${student.departmentOrClass || '未设置班级'}）`).join('<br/>') : '当前暂无选课学生', `${item.courseName} · 选课学生`, { dangerouslyUseHTMLString: true })
}
onMounted(load)
</script>
