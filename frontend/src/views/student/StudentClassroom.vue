<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Unified Learning Player</div>
        <h1 class="page-title">统一学习器</h1>
        <p class="page-subtitle">从课程直接进入即可开始学习。点击“开始学习”后自动触发学习采集，并默认打开教师发布的资源内容。</p>
      </div>
      <div class="action-row">
        <el-button plain @click="goBack">返回上一级</el-button>
        <el-button type="primary" plain @click="$router.push(`/student/signin?courseId=${courseId}`)">查看任务</el-button>
        <el-button @click="refreshAll">刷新学习器</el-button>
      </div>
    </div>

    <div class="player-layout">
      <section class="surface-card player-main">
        <div class="toolbar toolbar--wrap">
          <el-select
            v-model="courseId"
            filterable
            placeholder="选择课程"
            style="width: 260px"
            @change="refreshAll"
          >
            <el-option v-for="item in courses" :key="item.id" :label="item.courseName" :value="item.id" />
          </el-select>

          <el-select
            v-model="contentId"
            clearable
            filterable
            placeholder="选择学习内容"
            style="width: 320px"
            @change="syncCurrentContent"
          >
            <el-option v-for="item in contents" :key="item.id" :label="item.contentTitle" :value="item.id" />
          </el-select>

          <div class="learn-status-card">
            <span>学习状态</span>
            <strong>{{ running ? '学习采集中' : '尚未开始' }}</strong>
          </div>

          <el-button type="primary" :disabled="!currentContent || running" @click="startLearning">开始学习</el-button>
          <el-button :disabled="!running" @click="stopLearning">结束学习</el-button>
        </div>

        <div v-if="currentContent" class="player-stack">
          <div class="resource-summary">
            <div>
              <div class="section-eyebrow">当前资源</div>
              <h3 class="surface-card__title">{{ currentContent.contentTitle }}</h3>
              <p class="resource-summary__desc">{{ currentContent.contentBody || '当前资源没有文本说明。' }}</p>
            </div>
            <div class="resource-summary__meta">
              <span>附件：{{ currentContent.attachmentName || '无附件' }}</span>
              <span>类型：{{ currentContent.attachmentType || '未识别' }}</span>
            </div>
          </div>

          <div class="resource-preview-card">
            <template v-if="previewType === 'video'">
              <video ref="videoRef" :src="openUrl" controls class="resource-video"></video>
            </template>
            <template v-else-if="previewType === 'pdf'">
              <iframe :src="previewUrl" class="resource-frame" title="学习资源预览"></iframe>
            </template>
            <template v-else-if="previewType === 'office'">
              <iframe :src="previewUrl" class="resource-frame resource-frame--doc" title="Office 文本预览"></iframe>
            </template>
            <template v-else>
              <div class="resource-fallback">
                <p>当前资源类型暂不支持页内预览，但你仍可点击按钮查看完整附件。</p>
                <el-button type="primary" @click="openAttachment">打开附件</el-button>
              </div>
            </template>
          </div>
        </div>

        <el-empty v-else description="请选择一个学习内容" />
      </section>

      <section class="surface-card player-side">
        <h3 class="surface-card__title">学习进度与任务</h3>
        <div class="info-list">
          <div class="info-item"><span>当前课程</span><strong>{{ courseName(courseId) }}</strong></div>
          <div class="info-item"><span>资源数量</span><strong>{{ contents.length }}</strong></div>
          <div class="info-item"><span>签到任务</span><strong>{{ signTasks.length }}</strong></div>
          <div class="info-item"><span>测验任务</span><strong>{{ quizzes.length }}</strong></div>
          <div class="info-item"><span>最近学习状态</span><strong>{{ behaviorText(lastResult.behaviorStatus) }}</strong></div>
          <div class="info-item"><span>活动分</span><strong>{{ lastResult.activityScore ?? 0 }}</strong></div>
        </div>

        <el-alert
          title="系统说明"
          type="info"
          :closable="false"
          style="margin-top: 12px;"
          description="学习采集会在你点击开始学习后自动触发，系统只输出学习进度、参与程度与风险提示，不直接判定你是否认真。"
        />

        <div class="action-row" style="margin-top: 12px;">
          <el-button type="primary" plain @click="$router.push(`/student/quiz?courseId=${courseId}`)">去做测验</el-button>
          <el-button plain @click="$router.push(`/student/behavior?courseId=${courseId}`)">查看风险说明</el-button>
        </div>
      </section>
    </div>

    <AppDialog
      v-model="presenceVisible"
      title="仍在学习吗？"
      :width="420"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="dialog-body-stack">
        <p class="page-subtitle" style="margin: 0;">
          为了保证学习记录真实有效，请在倒计时结束前确认你仍在当前学习内容中。
        </p>
        <div class="dialog-highlight">
          <div class="dialog-highlight__label">倒计时</div>
          <div class="dialog-highlight__value">{{ countdown }}s</div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer-actions">
          <el-button type="primary" @click="confirmPresence">我仍在学习</el-button>
        </div>
      </template>
    </AppDialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import AppDialog from '../../components/AppDialog.vue'
import { behaviorText } from '../../utils/format'
import { buildFileOpenUrl, buildPreviewUrl, isOfficeFile } from '../../utils/assets'

const route = useRoute()
const router = useRouter()

const courses = ref([])
const contents = ref([])
const signTasks = ref([])
const quizzes = ref([])
const sessions = ref([])
const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined)
const contentId = ref(route.query.contentId ? Number(route.query.contentId) : undefined)
const currentContent = ref(null)
const running = ref(false)
const lastResult = ref({})
const presenceVisible = ref(false)
const countdown = ref(10)
const videoRef = ref(null)

let reportTimer = null
let presenceTimer = null
let countdownTimer = null
let currentSessionId = null
let startAt = null

const courseMap = computed(() => Object.fromEntries(courses.value.map((item) => [item.id, item.courseName])))
const courseName = (id) => courseMap.value[id] || '未选择课程'

const previewType = computed(() => {
  const type = (currentContent.value?.attachmentType || '').toLowerCase()
  if (type === 'mp4') return 'video'
  if (type === 'pdf') return 'pdf'
  if (isOfficeFile(type)) return 'office'
  return 'fallback'
})

const previewUrl = computed(() => buildPreviewUrl(currentContent.value?.attachmentUrl, currentContent.value?.attachmentType))
const openUrl = computed(() => buildFileOpenUrl(currentContent.value?.attachmentUrl))

const goBack = () => {
  if (route.query.from === 'content') {
    router.push(`/student/course-content?courseId=${courseId.value || ''}`)
    return
  }
  router.back()
}

const openAttachment = () => {
  if (!openUrl.value) {
    ElMessage.warning('当前资源未上传附件')
    return
  }
  window.open(openUrl.value, '_blank', 'noopener')
}

const loadBase = async () => {
  courses.value = await api.studentMyCourses()
  if (!courseId.value && courses.value.length) {
    courseId.value = courses.value[0].id
  }
}

const syncCurrentContent = () => {
  currentContent.value = contents.value.find((item) => Number(item.id) === Number(contentId.value)) || contents.value[0] || null
  contentId.value = currentContent.value?.id
}

const refreshAll = async () => {
  if (!courseId.value) return
  const [contentRes, signRes, quizRes, sessionRes] = await Promise.all([
    api.studentCourseContents({ courseId: courseId.value }),
    api.studentSignTasks({ courseId: courseId.value }),
    api.studentQuizzes({}),
    api.studentSessions({ courseId: courseId.value })
  ])
  contents.value = contentRes || []
  signTasks.value = signRes || []
  quizzes.value = (quizRes || []).filter((item) => Number(item.courseId) === Number(courseId.value))
  sessions.value = sessionRes || []
  currentSessionId = sessions.value[0]?.id
  syncCurrentContent()
}

const buildReportPayload = () => {
  const elapsedSec = startAt ? Math.max(30, Math.floor((Date.now() - startAt) / 1000)) : 30
  const video = videoRef.value
  return {
    classSessionId: currentSessionId,
    courseId: Number(courseId.value),
    onlineFlag: 1,
    lastActiveTime: new Date(),
    heartbeatTime: new Date(),
    pageStayDurationSec: 30,
    focusFlag: document.visibilityState === 'visible' ? 1 : 0,
    blurDurationSec: document.visibilityState === 'visible' ? 0 : 5,
    mouseClickCount: 1,
    keyInputCount: 0,
    signInFlag: signTasks.value.some((item) => item.signed) ? 1 : 0,
    quizJoinFlag: quizzes.value.some((item) => item.submitted) ? 1 : 0,
    classStayDurationSec: elapsedSec,
    baseActivityScore: video && !video.paused ? 80 : 60
  }
}

const reportNow = async () => {
  if (!running.value || !courseId.value || !currentSessionId) return
  try {
    lastResult.value = await api.reportBehavior(buildReportPayload())
  } catch {
    // 静默
  }
}

const clearTimers = () => {
  if (reportTimer) clearInterval(reportTimer)
  if (presenceTimer) clearInterval(presenceTimer)
  if (countdownTimer) clearInterval(countdownTimer)
  reportTimer = null
  presenceTimer = null
  countdownTimer = null
}

const openPresenceDialog = () => {
  presenceVisible.value = true
  countdown.value = 10
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      presenceVisible.value = false
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const confirmPresence = () => {
  presenceVisible.value = false
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
  ElMessage.success('已确认仍在学习')
}

const startLearning = async () => {
  if (!currentContent.value) {
    ElMessage.warning('请先选择学习内容')
    return
  }
  running.value = true
  startAt = Date.now()
  ElMessage.success('学习采集已开始')
  await reportNow()
  reportTimer = setInterval(reportNow, 30000)
  presenceTimer = setInterval(openPresenceDialog, 180000)
}

const stopLearning = async () => {
  running.value = false
  clearTimers()
  await reportNow()
  ElMessage.success('学习采集已结束')
}

watch(courseId, async (value) => {
  if (value) {
    await refreshAll()
  }
})

onMounted(async () => {
  await loadBase()
  await refreshAll()
})

onBeforeUnmount(() => {
  clearTimers()
})
</script>

<style scoped>
.player-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.65fr);
  gap: 16px;
}

.player-main,
.player-side,
.resource-summary,
.resource-preview-card,
.learn-status-card {
  border: 1px solid #dbe4ff;
  background: #fff;
  border-radius: 20px;
}

.player-main,
.player-side {
  padding: 16px;
}

.player-stack {
  display: grid;
  gap: 16px;
  margin-top: 16px;
}

.resource-summary {
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  background: #f6f8ff;
}

.resource-summary__desc {
  margin: 8px 0 0;
  color: #5f6b84;
  line-height: 1.7;
}

.resource-summary__meta {
  display: grid;
  gap: 8px;
  min-width: 320px;
  color: #5f6b84;
}

.resource-preview-card {
  min-height: 620px;
  overflow: hidden;
}

.resource-video,
.resource-frame {
  width: 100%;
  height: 620px;
  border: 0;
  background: #fff;
}

.resource-frame--doc {
  background: #f6f8ff;
}

.resource-fallback {
  min-height: 620px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 16px;
  color: #5f6b84;
  text-align: center;
  padding: 24px;
}

.learn-status-card {
  padding: 10px 16px;
  background: #f6f8ff;
  display: grid;
}

.learn-status-card span {
  color: #5f6b84;
  font-size: 13px;
}

.info-list {
  display: grid;
  gap: 12px;
  margin-top: 12px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f6f8ff;
}

.dialog-body-stack {
  display: grid;
  gap: 12px;
}

.dialog-highlight {
  background: #f6f8ff;
  border-radius: 16px;
  padding: 16px;
  text-align: center;
}

.dialog-highlight__label {
  color: #5f6b84;
  margin-bottom: 6px;
}

.dialog-highlight__value {
  font-size: 28px;
  font-weight: 700;
  color: #2b6aff;
}

@media (max-width: 1280px) {
  .player-layout {
    grid-template-columns: 1fr;
  }

  .resource-summary {
    flex-direction: column;
  }

  .resource-summary__meta {
    min-width: 0;
  }
}
</style>