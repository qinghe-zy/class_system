<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Unified Learning Player</div>
        <h1 class="page-title">统一学习器</h1>
        <p class="page-subtitle">
          学生不需要输入课程 ID 或课堂 ID，只需从课程进入后按资源目录、任务区与进度提示完成学习。
        </p>
      </div>
      <div class="action-row">
        <el-button type="primary" plain @click="$router.push(`/student/signin?courseId=${courseId}`)">
          查看任务
        </el-button>
        <el-button @click="refreshAll">刷新学习器</el-button>
      </div>
    </div>

    <div class="content-grid">
      <section class="surface-card">
        <div class="toolbar">
          <el-select
            v-model="courseId"
            filterable
            placeholder="选择课程"
            style="width: 260px"
            @change="refreshAll"
          >
            <el-option
              v-for="item in courses"
              :key="item.id"
              :label="item.courseName"
              :value="item.id"
            />
          </el-select>
          <el-select
            v-model="contentId"
            clearable
            filterable
            placeholder="选择学习内容"
            style="width: 320px"
            @change="syncCurrentContent"
          >
            <el-option
              v-for="item in contents"
              :key="item.id"
              :label="item.contentTitle"
              :value="item.id"
            />
          </el-select>
          <el-switch
            v-model="running"
            active-text="学习采集中"
            inactive-text="暂停采集"
            @change="toggleReporting"
          />
        </div>

        <div v-if="currentContent" class="timeline-list">
          <article class="timeline-item">
            <div class="timeline-item__time">当前资源</div>
            <h4 class="timeline-item__title">{{ currentContent.contentTitle }}</h4>
            <p class="timeline-item__desc break-words">
              {{ currentContent.contentBody || '当前资源没有文本说明。' }}
            </p>
          </article>
          <article class="timeline-item">
            <div class="timeline-item__time">附件信息</div>
            <h4 class="timeline-item__title">{{ currentContent.attachmentName || '无附件' }}</h4>
            <p class="timeline-item__desc break-words">
              {{ currentContent.attachmentUrl || '当前资源未上传附件地址。' }}
            </p>
          </article>
        </div>
        <el-empty v-else description="请选择一个学习内容" />
      </section>

      <section class="surface-card">
        <h3 class="surface-card__title">学习进度与任务</h3>
        <div class="info-list">
          <div class="info-item"><span>当前课程</span><strong>{{ courseName(courseId) }}</strong></div>
          <div class="info-item"><span>资源数量</span><strong>{{ contents.length }}</strong></div>
          <div class="info-item"><span>签到任务</span><strong>{{ signTasks.length }}</strong></div>
          <div class="info-item"><span>测验任务</span><strong>{{ quizzes.length }}</strong></div>
          <div class="info-item">
            <span>最近学习状态</span>
            <strong>{{ behaviorText(lastResult.behaviorStatus) }}</strong>
          </div>
          <div class="info-item"><span>活动分</span><strong>{{ lastResult.activityScore ?? 0 }}</strong></div>
        </div>

        <el-alert
          title="系统说明"
          type="info"
          :closable="false"
          style="margin-top: 12px;"
          description="系统采集学习器停留、任务参与与课堂状态，只输出学习进度、参与程度与风险提示，不直接判定你是否认真。"
        />

        <div class="action-row" style="margin-top: 12px;">
          <el-button type="primary" plain @click="$router.push(`/student/quiz?courseId=${courseId}`)">
            去做测验
          </el-button>
          <el-button plain @click="$router.push(`/student/behavior?courseId=${courseId}`)">
            查看风险说明
          </el-button>
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
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import AppDialog from '../../components/AppDialog.vue'
import { behaviorText } from '../../utils/format'

const route = useRoute()
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

let reportTimer = null
let presenceTimer = null
let countdownTimer = null
let currentSessionId = null

const courseMap = computed(() => Object.fromEntries(courses.value.map((item) => [item.id, item.courseName])))
const courseName = (id) => courseMap.value[id] || '未选择课程'

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

const report = async () => {
  if (!running.value || !courseId.value || !currentSessionId) return
  lastResult.value = await api.reportBehavior({
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
    classStayDurationSec: 30,
    baseActivityScore: 60
  })
}

const openPresenceCheck = () => {
  presenceVisible.value = true
  countdown.value = 10
  clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      presenceVisible.value = false
      ElMessage.warning('未及时确认在岗，系统将其记录为一次风险提示。')
    }
  }, 1000)
}

const confirmPresence = () => {
  presenceVisible.value = false
  clearInterval(countdownTimer)
  ElMessage.success('已记录在岗确认')
}

const toggleReporting = async () => {
  clearInterval(reportTimer)
  clearInterval(presenceTimer)
  clearInterval(countdownTimer)
  presenceVisible.value = false
  if (!running.value) return
  await report()
  reportTimer = setInterval(report, 15000)
  presenceTimer = setInterval(openPresenceCheck, 60000)
}

onMounted(async () => {
  await loadBase()
  await refreshAll()
})

onBeforeUnmount(() => {
  clearInterval(reportTimer)
  clearInterval(presenceTimer)
  clearInterval(countdownTimer)
})
</script>
