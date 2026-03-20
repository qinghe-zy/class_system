<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Classroom Management</div>
        <h1 class="page-title">课堂管理</h1>
        <p class="page-subtitle">
          教师通过课程自然进入课堂活动，不需要手动输入会话编号，即可安排课堂、签到与测验。
        </p>
      </div>
      <div class="action-row">
        <el-button type="primary" @click="openDialog()">新建课堂</el-button>
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

    <div class="card-grid">
      <article v-for="item in list" :key="item.id" class="surface-card">
        <div class="action-row" style="justify-content: space-between; align-items: flex-start;">
          <div>
            <h3 class="surface-card__title">{{ item.sessionTitle }}</h3>
            <div class="surface-card__meta">{{ courseName(item.courseId) }}</div>
          </div>
          <StatusBadge :text="sessionText(item.sessionStatus)" />
        </div>

        <div class="info-list" style="margin-top: 10px;">
          <div class="info-item">
            <span>开始时间</span>
            <strong>{{ formatDateTime(item.sessionStartTime) }}</strong>
          </div>
          <div class="info-item">
            <span>结束时间</span>
            <strong>{{ formatDateTime(item.sessionEndTime) }}</strong>
          </div>
        </div>

        <div class="action-row" style="margin-top: 12px;">
          <el-button size="small" @click="openDialog(item)">编辑课堂</el-button>
          <el-button size="small" type="primary" plain @click="toSign(item)">签到</el-button>
          <el-button size="small" type="primary" plain @click="toQuiz(item)">测验</el-button>
          <el-button size="small" @click="toStats(item)">学习分析</el-button>
        </div>
      </article>
    </div>

    <AppDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑课堂活动' : '新建课堂活动'"
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

          <el-form-item label="课堂名称">
            <el-input v-model="form.sessionTitle" placeholder="请输入课堂名称" />
          </el-form-item>

          <el-form-item label="开始时间">
            <el-date-picker
              v-model="form.sessionStartTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开始时间"
            />
          </el-form-item>

          <el-form-item label="结束时间">
            <el-date-picker
              v-model="form.sessionEndTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择结束时间"
            />
          </el-form-item>

          <el-form-item label="状态">
            <el-select v-model="form.sessionStatus" placeholder="请选择状态">
              <el-option label="待开始" value="NOT_STARTED" />
              <el-option label="进行中" value="ONGOING" />
              <el-option label="已结束" value="FINISHED" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">
            保存课堂
          </el-button>
        </div>
      </template>
    </AppDialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import AppDialog from '../../components/AppDialog.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatDateTime, sessionText } from '../../utils/format'

const router = useRouter()
const route = useRoute()
const courses = ref([])
const list = ref([])
const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined)
const dialogVisible = ref(false)
const saving = ref(false)

const form = reactive({
  id: null,
  courseId: undefined,
  sessionTitle: '',
  sessionStartTime: '',
  sessionEndTime: '',
  sessionStatus: 'NOT_STARTED'
})

const courseMap = computed(() => Object.fromEntries(courses.value.map((item) => [item.id, item.courseName])))
const courseName = (id) => courseMap.value[id] || `课程 ${id}`

const loadCourses = async () => {
  const res = await api.teacherCourses({ pageNum: 1, pageSize: 100 })
  courses.value = res.records || []
}

const load = async () => {
  list.value = await api.teacherSessions({ courseId: courseId.value })
}

const openDialog = (row = null) => {
  dialogVisible.value = true
  form.id = row?.id || null
  form.courseId = row?.courseId || courseId.value || courses.value[0]?.id
  form.sessionTitle = row?.sessionTitle || ''
  form.sessionStartTime = row?.sessionStartTime || ''
  form.sessionEndTime = row?.sessionEndTime || ''
  form.sessionStatus = row?.sessionStatus || 'NOT_STARTED'
}

const submit = async () => {
  saving.value = true
  try {
    if (form.id) {
      await api.updateSession(form.id, { ...form })
      ElMessage.success('课堂活动已更新')
    } else {
      await api.createSession({ ...form })
      ElMessage.success('课堂活动已创建')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const toSign = (item) => router.push({ path: '/teacher/signin', query: { sessionId: item.id, courseId: item.courseId } })
const toQuiz = (item) => router.push({ path: '/teacher/quiz', query: { sessionId: item.id, courseId: item.courseId } })
const toStats = (item) => router.push({ path: '/teacher/stats', query: { sessionId: item.id, courseId: item.courseId } })

onMounted(async () => {
  await loadCourses()
  if (!courseId.value && courses.value.length) {
    courseId.value = courses.value[0].id
  }
  await load()
})
</script>
