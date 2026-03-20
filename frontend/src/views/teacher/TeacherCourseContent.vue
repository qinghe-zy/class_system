<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Resource Library</div>
        <h1 class="page-title">资源库</h1>
        <p class="page-subtitle">
          教师在这里为课程上传 PDF、课件、讲义与附件，提交后进入管理员资源审核链路。
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
              :on-change="handleFileChange"
              accept=".pdf,.doc,.docx,.ppt,.pptx"
            >
              <el-button>选择文件</el-button>
            </el-upload>
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
          <el-button type="primary" :loading="saving" @click="submit">
            保存并提交审核
          </el-button>
        </div>
      </template>
    </AppDialog>

    <el-drawer v-model="drawerVisible" :title="active?.contentTitle || '资源预览'" size="42%">
      <div v-if="active" class="timeline-list">
        <article class="timeline-item">
          <div class="timeline-item__time">所属课程</div>
          <h4 class="timeline-item__title">{{ active.courseName || '未命名课程' }}</h4>
          <p class="timeline-item__desc">{{ active.contentBody || '暂无资源说明。' }}</p>
        </article>
        <article class="timeline-item">
          <div class="timeline-item__time">附件信息</div>
          <h4 class="timeline-item__title">{{ active.attachmentName || '无附件' }}</h4>
          <p class="timeline-item__desc break-words">
            {{ active.attachmentUrl || '当前资源未上传附件。' }}
          </p>
        </article>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import AppDialog from '../../components/AppDialog.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { auditText, formatDateTime } from '../../utils/format'

const route = useRoute()
const courses = ref([])
const list = ref([])
const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const saving = ref(false)
const active = ref(null)
const uploadNote = ref('支持 Word / PPT / PDF，大小不超过 20MB。')

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
    : '支持 Word / PPT / PDF，大小不超过 20MB。'
}

const preview = (row) => {
  active.value = row
  drawerVisible.value = true
}

const handleFileChange = async (uploadFile) => {
  const file = uploadFile.raw
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  const res = await api.uploadTeacherFile(fd)
  form.attachmentName = res.attachmentName
  form.attachmentUrl = res.attachmentUrl
  form.attachmentType = res.attachmentType
  uploadNote.value = `已上传：${res.attachmentName}`
  ElMessage.success('附件上传成功')
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
