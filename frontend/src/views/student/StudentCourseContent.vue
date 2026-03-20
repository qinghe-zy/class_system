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
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" plain @click="preview(row)">预览</el-button>
          <el-button size="small" type="primary" @click="startLearning(row)">开始学习</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawerVisible" :title="active?.contentTitle || '资源预览'" size="72%">
      <div v-if="active" class="preview-drawer">
        <section class="preview-surface">
          <template v-if="previewType === 'video'">
            <video :src="previewUrl" controls class="preview-media">当前浏览器暂不支持视频预览</video>
          </template>
          <template v-else-if="previewType === 'pdf' || previewType === 'office'">
            <iframe :src="previewUrl" class="preview-media" :title="previewType === 'office' ? 'Office 文本预览' : '资源预览'"></iframe>
          </template>
          <template v-else-if="previewType === 'image'">
            <div class="preview-image-wrap">
              <img :src="previewUrl" class="preview-image" alt="资源图片预览" />
            </div>
          </template>
          <template v-else>
            <div class="preview-fallback">
              <p>当前资源类型不支持页内预览，可打开附件查看完整内容。</p>
              <el-button type="primary" @click="openAttachment">打开附件</el-button>
            </div>
          </template>
        </section>

        <section class="preview-side">
          <div class="info-item"><span>课程</span><strong>{{ active.courseName }}</strong></div>
          <div class="info-item"><span>附件</span><strong>{{ active.attachmentName || '无附件' }}</strong></div>
          <div class="info-item"><span>类型</span><strong>{{ active.attachmentType || '未识别' }}</strong></div>
          <div class="info-item"><span>说明</span><strong>{{ active.contentBody || '暂无资源说明' }}</strong></div>
          <div class="action-row" style="margin-top: 12px;">
            <el-button type="primary" @click="startLearning(active)">开始学习</el-button>
            <el-button plain @click="openAttachment">打开附件</el-button>
          </div>
        </section>
      </div>
    </el-drawer>

    <el-empty v-if="!list.length" description="当前课程暂无可学习资源" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import { buildAttachmentOpenUrl, buildPreviewUrl, resolvePreviewType } from '../../utils/assets'

const route = useRoute()
const router = useRouter()
const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined)
const courses = ref([])
const list = ref([])
const courseMap = ref({})
const drawerVisible = ref(false)
const active = ref(null)

const previewType = computed(() => resolvePreviewType(active.value?.attachmentType, active.value?.attachmentUrl))
const previewUrl = computed(() => buildPreviewUrl(active.value?.attachmentUrl, active.value?.attachmentType))

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

const preview = (row) => {
  active.value = row
  drawerVisible.value = true
}

const openAttachment = () => {
  const url = buildAttachmentOpenUrl(active.value?.attachmentUrl, active.value?.attachmentType)
  if (!url) {
    ElMessage.warning('当前资源未上传附件')
    return
  }
  window.open(url, '_blank', 'noopener')
}

onMounted(async () => {
  await loadBase()
  if (!courseId.value && courses.value.length) courseId.value = courses.value[0].id
  await load()
})
</script>

<style scoped>
.preview-drawer {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.8fr);
  gap: 16px;
}

.preview-surface,
.preview-side {
  border: 1px solid #dbe4ff;
  border-radius: 18px;
  padding: 16px;
  background: #fff;
}

.preview-media {
  width: 100%;
  min-height: 600px;
  border: 0;
  border-radius: 14px;
}

.preview-image-wrap {
  min-height: 600px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: #f6f8ff;
}

.preview-image {
  max-width: 100%;
  max-height: 600px;
  object-fit: contain;
}

.preview-fallback {
  min-height: 600px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  color: #5f6b84;
  text-align: center;
}

.preview-side {
  display: grid;
  align-content: start;
  gap: 10px;
}

.info-item {
  display: grid;
  gap: 6px;
  background: #f6f8ff;
  border-radius: 12px;
  padding: 12px;
}

.info-item span {
  font-size: 13px;
  color: #5f6b84;
}

@media (max-width: 1280px) {
  .preview-drawer {
    grid-template-columns: 1fr;
  }
}
</style>
