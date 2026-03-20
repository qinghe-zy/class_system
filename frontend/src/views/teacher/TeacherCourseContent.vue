<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Resource Library</div>
        <h1 class="page-title">资源库</h1>
        <p class="page-subtitle">
          教师可上传文档、课件、PDF 与视频资源，提交后进入管理员资源审核链路。
        </p>
      </div>
      <div class="action-row">
        <el-button type="primary" @click="openDialog()">新增资源</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-select
        v-model="courseId"
        clearable
        filterable
        placeholder="选择课程"
        style="width: 260px"
        @change="load"
      >
        <el-option
          v-for="item in courses"
          :key="item.id"
          :label="item.courseName"
          :value="item.id"
        />
      </el-select>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="courseName" label="所属课程" min-width="160" />
      <el-table-column prop="contentTitle" label="资源标题" min-width="180" />
      <el-table-column prop="attachmentName" label="附件名称" min-width="180" />
      <el-table-column prop="attachmentType" label="类型" width="100" />
      <el-table-column label="审核状态" width="120">
        <template #default="{ row }">
          <StatusBadge :text="auditText(row.auditStatus)" />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="160">
        <template #default="{ row }">
          {{ formatDateTime(row.updatedTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="180">
        <template #default="{ row }">
          <div class="action-row">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="primary" plain @click="preview(row)">预览</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <AppDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑资源' : '新增资源'"
      :width="760"
    >
      <el-form :model="form" label-position="top" class="dialog-form">
        <div class="dialog-form-grid">
          <el-form-item label="所属课程">
            <el-select v-model="form.courseId" filterable placeholder="请选择课程">
              <el-option
                v-for="item in courses"
                :key="item.id"
                :label="item.courseName"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="资源标题">
            <el-input v-model="form.contentTitle" placeholder="请输入资源标题" />
          </el-form-item>
        </div>

        <el-form-item label="资源说明">
          <el-input
            v-model="form.contentBody"
            type="textarea"
            :rows="6"
            maxlength="1000"
            show-word-limit
            placeholder="请输入资源简介、适用范围、课堂使用说明等"
          />
        </el-form-item>

        <div class="dialog-form-grid">
          <el-form-item label="附件上传">
            <el-upload
              :show-file-list="false"
              :auto-upload="false"
              :disabled="uploading"
              :on-change="handleFileChange"
              accept=".pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.mp4,.png,.jpg,.jpeg,.gif,.webp,video/mp4,image/*,text/plain"
            >
              <el-button :loading="uploading">{{ uploading ? '上传中...' : '选择文件' }}</el-button>
            </el-upload>
            <el-progress
              v-if="uploading"
              :percentage="uploadProgress"
              :stroke-width="8"
              style="margin-top: 10px;"
            />
            <div class="list-note" style="margin-top: 8px;">
              {{ uploadNote }}
            </div>
          </el-form-item>
          <el-form-item label="当前附件">
            <el-input
              v-model="form.attachmentName"
              readonly
              placeholder="未上传附件"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" :disabled="uploading" @click="submit">
            保存并提交审核
          </el-button>
        </div>
      </template>
    </AppDialog>

    <el-drawer v-model="drawerVisible" :title="active?.contentTitle || '资源预览'" size="50%">
      <div v-if="active" class="preview-drawer">
        <div class="preview-panel">
          <template v-if="previewType(active) === 'video'">
            <div v-if="videoPreviewError" class="fallback-preview">
              <el-icon><Document /></el-icon>
              <p>{{ videoPreviewErrorText }}</p>
              <div class="action-row">
                <el-button type="primary" @click="openAttachment(active)">打开视频</el-button>
                <el-button plain @click="copyAttachmentLink(active)">复制地址</el-button>
              </div>
            </div>
            <video
              v-else
              :src="previewUrl(active)"
              controls
              class="media-preview"
              @error="handleVideoError"
              @loadedmetadata="handleVideoMetaLoaded"
            >当前浏览器暂不支持视频预览</video>
          </template>
          <template v-else-if="previewType(active) === 'pdf' || previewType(active) === 'office'">
            <iframe :src="previewUrl(active)" class="file-preview" :title="previewType(active) === 'office' ? 'Office 文本预览' : '资源预览'"></iframe>
          </template>
          <template v-else-if="previewType(active) === 'image'">
            <div class="image-preview-wrap">
              <img :src="previewUrl(active)" class="image-preview" alt="资源图片预览" />
            </div>
          </template>
          <template v-else>
            <div class="fallback-preview">
              <el-icon><Document /></el-icon>
              <p>当前资源类型暂不支持页内高保真预览，可先查看资源说明后在新窗口打开附件。</p>
              <div class="action-row">
                <el-button type="primary" @click="openAttachment(active)">打开附件</el-button>
                <el-button plain @click="copyAttachmentLink(active)">复制地址</el-button>
              </div>
            </div>
          </template>
        </div>

        <div class="timeline-list">
          <article class="timeline-item">
            <div class="timeline-item__time">所属课程</div>
            <h4 class="timeline-item__title">{{ active.courseName || '未命名课程' }}</h4>
            <p class="timeline-item__desc">{{ active.contentBody || '暂无资源说明。' }}</p>
          </article>
          <article class="timeline-item">
            <div class="timeline-item__time">附件信息</div>
            <h4 class="timeline-item__title">{{ active.attachmentName || '无附件' }}</h4>
            <p class="timeline-item__desc break-words">{{ active.attachmentUrl || '当前资源未上传附件。' }}</p>
          </article>
          <article class="timeline-item">
            <div class="timeline-item__time">审核状态</div>
            <h4 class="timeline-item__title">{{ auditText(active.auditStatus) }}</h4>
            <p class="timeline-item__desc">{{ active.auditRemark || '当前暂无审核备注。' }}</p>
          </article>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import { api } from '../../api'
import AppDialog from '../../components/AppDialog.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { auditText, formatDateTime } from '../../utils/format'
import { buildAttachmentOpenUrl, buildPreviewUrl, resolvePreviewType, validateVideoFile } from '../../utils/assets'

const ALLOW_FILE_TYPES = new Set(['pdf', 'doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx', 'txt', 'mp4', 'png', 'jpg', 'jpeg', 'gif', 'webp'])
const MAX_UPLOAD_SIZE = 500 * 1024 * 1024

const route = useRoute()
const courses = ref([])
const list = ref([])
const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const saving = ref(false)
const uploading = ref(false)
const uploadProgress = ref(0)
const active = ref(null)
const videoPreviewError = ref(false)
const videoPreviewErrorText = ref('')
const uploadNote = ref('支持 Word/PPT/Excel/PDF/TXT/图片/MP4，视频建议控制在 500MB 以内。')

const form = reactive({
  id: null,
  courseId: undefined,
  contentTitle: '',
  contentBody: '',
  attachmentName: '',
  attachmentUrl: '',
  attachmentType: '',
  publishStatus: 1
})

const loadCourses = async () => {
  const res = await api.teacherCourses({ pageNum: 1, pageSize: 100 })
  courses.value = res.records || []
}

const load = async () => {
  list.value = await api.teacherCourseContents({ courseId: courseId.value })
}

const openDialog = (row = null) => {
  dialogVisible.value = true
  form.id = row?.id || null
  form.courseId = row?.courseId || courseId.value || courses.value[0]?.id
  form.contentTitle = row?.contentTitle || ''
  form.contentBody = row?.contentBody || ''
  form.attachmentName = row?.attachmentName || ''
  form.attachmentUrl = row?.attachmentUrl || ''
  form.attachmentType = row?.attachmentType || ''
  form.publishStatus = row?.publishStatus ?? 1
  uploadNote.value = row?.attachmentName
    ? `当前附件：${row.attachmentName}`
    : '支持 Word/PPT/Excel/PDF/TXT/图片/MP4，视频建议控制在 500MB 以内。'
}

const copyAttachmentLink = async (row) => {
  const url = buildAttachmentOpenUrl(row?.attachmentUrl, row?.attachmentType)
  if (!url) {
    ElMessage.warning('当前资源未上传附件')
    return
  }
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('附件地址已复制')
  } catch (error) {
    ElMessage.warning('复制失败，请手动复制地址')
  }
}

const preview = (row) => {
  active.value = row
  videoPreviewError.value = false
  videoPreviewErrorText.value = ''
  drawerVisible.value = true
}

const previewType = (row) => {
  return resolvePreviewType(row?.attachmentType, row?.attachmentUrl)
}

const previewUrl = (row) => buildPreviewUrl(row?.attachmentUrl, row?.attachmentType)

const handleVideoError = () => {
  videoPreviewError.value = true
  videoPreviewErrorText.value = '视频加载失败，可能是编码不兼容。建议使用 H.264 + AAC 的 MP4。'
}

const handleVideoMetaLoaded = (event) => {
  const width = event?.target?.videoWidth || 0
  const height = event?.target?.videoHeight || 0
  if (width <= 0 || height <= 0) {
    videoPreviewError.value = true
    videoPreviewErrorText.value = '该视频出现“有声无画”风险，建议转换为 H.264 + AAC 的 MP4。'
    return
  }
  videoPreviewError.value = false
  videoPreviewErrorText.value = ''
}

const openAttachment = (row) => {
  const url = buildAttachmentOpenUrl(row?.attachmentUrl, row?.attachmentType)
  if (!url) {
    ElMessage.warning('当前资源未上传附件')
    return
  }
  window.open(url, '_blank', 'noopener')
}

const handleFileChange = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  const extension = String(file.name || '').split('.').pop()?.toLowerCase()
  if (!extension || !ALLOW_FILE_TYPES.has(extension)) {
    ElMessage.warning('文件类型不支持，请选择 Word/PPT/Excel/PDF/TXT/图片/MP4')
    return
  }
  if (file.size > MAX_UPLOAD_SIZE) {
    ElMessage.warning('文件大小不能超过 500MB')
    return
  }
  if (extension === 'mp4') {
    const videoValidation = await validateVideoFile(file)
    if (!videoValidation.ok) {
      ElMessage.warning(videoValidation.message)
      return
    }
  }
  const fd = new FormData()
  fd.append('file', file)
  uploading.value = true
  uploadProgress.value = 0
  try {
    const res = await api.uploadTeacherFile(fd, {
      onUploadProgress: (progressEvent) => {
        if (!progressEvent.total) return
        const percentage = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        uploadProgress.value = Math.min(100, Math.max(0, percentage))
      }
    })
    form.attachmentName = res.attachmentName
    form.attachmentUrl = res.attachmentUrl
    form.attachmentType = res.attachmentType
    uploadNote.value = `已上传：${res.attachmentName}`
    ElMessage.success('附件上传成功')
  } catch {
    uploadNote.value = '上传失败，请检查网络、文件大小和后端服务状态后重试。'
  } finally {
    uploading.value = false
  }
}

const submit = async () => {
  saving.value = true
  try {
    if (form.id) {
      await api.updateCourseContent(form.id, { ...form })
      ElMessage.success('资源已更新并重新提交审核')
    } else {
      await api.createCourseContent({ ...form })
      ElMessage.success('资源已提交审核')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await loadCourses()
  if (!courseId.value && courses.value.length) {
    courseId.value = courses.value[0].id
  }
  await load()
})
</script>

<style scoped>
.preview-drawer {
  display: grid;
  gap: 16px;
}

.preview-panel {
  min-height: 280px;
  background: #f6f8ff;
  border: 1px solid #dbe4ff;
  border-radius: 20px;
  overflow: hidden;
}

.file-preview,
.media-preview {
  width: 100%;
  height: 420px;
  border: 0;
  display: block;
  background: #000;
}

.image-preview-wrap {
  height: 420px;
  display: grid;
  place-items: center;
  background: #f6f8ff;
}

.image-preview {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.fallback-preview {
  min-height: 280px;
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 32px;
  text-align: center;
  color: #5f6b84;
}
</style>
