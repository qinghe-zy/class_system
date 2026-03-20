<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Learning Content</div>
        <h1 class="page-title">学习内容</h1>
        <p class="page-subtitle">统一展示文档、课件、视频与附件资源，点击开始学习后自动进入学习器并触发学习采集。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="courseId" clearable filterable placeholder="选择课程" style="width: 260px" @change="load">
        <el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" />
      </el-select>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="courseName" label="课程" min-width="160" />
      <el-table-column prop="contentTitle" label="资源标题" min-width="180" />
      <el-table-column prop="attachmentName" label="附件名称" min-width="160" />
      <el-table-column prop="attachmentType" label="类型" width="100" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="startLearning(row)">开始学习</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!list.length" description="当前课程暂无可学习资源" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../../api'

const route = useRoute()
const router = useRouter()
const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined)
const courses = ref([])
const list = ref([])
const courseMap = ref({})

const loadBase = async () => {
  const res = await api.studentMyCourses()
  courses.value = res || []
  courseMap.value = Object.fromEntries(courses.value.map((item) => [item.id, item.courseName]))
}

const load = async () => {
  const rows = await api.studentCourseContents({ courseId: courseId.value })
  list.value = (rows || []).map((item) => ({
    ...item,
    courseName: courseMap.value[item.courseId] || `课程 ${item.courseId}`
  }))
}

const startLearning = (row) => {
  router.push(`/student/classroom?courseId=${row.courseId}&contentId=${row.id}&autoStart=1&from=content`)
}

onMounted(async () => {
  await loadBase()
  if (!courseId.value && courses.value.length) courseId.value = courses.value[0].id
  await load()
})
</script>
