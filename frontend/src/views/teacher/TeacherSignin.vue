<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Sign-In</div>
        <h1 class="page-title">签到管理</h1>
        <p class="page-subtitle">
          面向本次课堂创建签到任务，并查看签到记录与缺勤惩罚，不需要教师面对任何抽象编号。
        </p>
      </div>
      <div class="action-row">
        <el-button type="primary" @click="openDialog()">发布签到</el-button>
        <el-button @click="load">刷新</el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-select
        v-model="sessionId"
        filterable
        clearable
        placeholder="选择课堂"
        style="width: 320px"
        @change="load"
      >
        <el-option
          v-for="item in sessions"
          :key="item.id"
          :label="`${item.sessionTitle} · ${courseName(item.courseId)}`"
          :value="item.id"
        />
      </el-select>
    </div>

    <el-table :data="tasks" border>
      <el-table-column prop="taskTitle" label="签到名称" min-width="160" />
      <el-table-column label="所属课堂" min-width="180">
        <template #default="{ row }">
          {{ sessionName(row.sessionId) }}
        </template>
      </el-table-column>
      <el-table-column label="时间范围" min-width="220">
        <template #default="{ row }">
          {{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <StatusBadge :text="signTaskText(row.taskStatus)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template #default="{ row }">
          <div class="action-row">
            <el-button size="small" @click="viewRecords(row)">查看记录</el-button>
            <el-button size="small" @click="viewPenalties">查看惩罚</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <AppDialog v-model="dialogVisible" title="发布签到任务" :width="760">
      <el-form :model="form" label-position="top" class="dialog-form">
        <div class="dialog-form-grid">
          <el-form-item label="所属课堂">
            <el-select v-model="form.sessionId" filterable placeholder="请选择课堂" @change="syncFormCourse">
              <el-option
                v-for="item in sessions"
                :key="item.id"
                :label="`${item.sessionTitle} · ${courseName(item.courseId)}`"
                :value="item.id"
              />
            </el-select>
          </el-form-item>

          <el-form-item label="签到名称">
            <el-input v-model="form.taskTitle" placeholder="请输入签到名称" />
          </el-form-item>

          <el-form-item label="开始时间">
            <el-date-picker
              v-model="form.startTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择开始时间"
            />
          </el-form-item>

          <el-form-item label="结束时间">
            <el-date-picker
              v-model="form.endTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="请选择结束时间"
            />
          </el-form-item>

          <el-form-item label="缺勤扣分">
            <el-input-number v-model="form.penaltyScore" :min="0" :max="100" />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">
            发布签到
          </el-button>
        </div>
      </template>
    </AppDialog>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="42%">
      <el-table v-if="drawerMode === 'records'" :data="records" border>
        <el-table-column prop="studentName" label="学生" min-width="140" />
        <el-table-column label="签到时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.signInTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="signInStatus" label="状态" width="100" />
      </el-table>
      <el-table v-else :data="penalties" border>
        <el-table-column prop="studentName" label="学生" min-width="140" />
        <el-table-column prop="penaltyScore" label="扣分" width="100" />
        <el-table-column prop="penaltyReason" label="原因" min-width="200" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { api } from '../../api'
import AppDialog from '../../components/AppDialog.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatDateTime, signTaskText } from '../../utils/format'

const route = useRoute()
const sessions = ref([])
const tasks = ref([])
const sessionId = ref(route.query.sessionId ? Number(route.query.sessionId) : undefined)
const records = ref([])
const penalties = ref([])
const drawerVisible = ref(false)
const drawerTitle = ref('签到详情')
const drawerMode = ref('records')
const dialogVisible = ref(false)
const saving = ref(false)
const courseMap = ref({})

const sessionMap = computed(() => Object.fromEntries(sessions.value.map((item) => [item.id, item])))
const form = reactive({
  sessionId: sessionId.value,
  courseId: route.query.courseId ? Number(route.query.courseId) : undefined,
  taskTitle: '',
  startTime: '',
  endTime: '',
  penaltyScore: 5
})

const courseName = (courseId) => courseMap.value[courseId] || `课程 ${courseId}`
const sessionName = (id) => sessionMap.value[id]?.sessionTitle || `课堂 ${id}`

const loadBase = async () => {
  const [courseRes, sessionRes] = await Promise.all([
    api.teacherCourses({ pageNum: 1, pageSize: 100 }),
    api.teacherSessions({})
  ])
  courseMap.value = Object.fromEntries((courseRes.records || []).map((item) => [item.id, item.courseName]))
  sessions.value = sessionRes || []
}

const load = async () => {
  tasks.value = await api.teacherSignTasks({ sessionId: sessionId.value })
}

const syncFormCourse = () => {
  form.courseId = sessionMap.value[form.sessionId]?.courseId
}

const openDialog = () => {
  dialogVisible.value = true
  form.sessionId = sessionId.value || sessions.value[0]?.id
  syncFormCourse()
  form.taskTitle = ''
  form.startTime = ''
  form.endTime = ''
  form.penaltyScore = 5
}

const submit = async () => {
  saving.value = true
  syncFormCourse()
  try {
    await api.createSignTask({ ...form })
    ElMessage.success('签到任务已发布')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const viewRecords = async (row) => {
  drawerMode.value = 'records'
  drawerTitle.value = `${row.taskTitle} · 签到记录`
  records.value = await api.teacherSignRecords({ taskId: row.id })
  drawerVisible.value = true
}

const viewPenalties = async () => {
  drawerMode.value = 'penalties'
  drawerTitle.value = '签到惩罚记录'
  penalties.value = await api.teacherSignPenalties({})
  drawerVisible.value = true
}

onMounted(async () => {
  await loadBase()
  if (!sessionId.value && sessions.value.length) {
    sessionId.value = sessions.value[0].id
  }
  await load()
})
</script>
