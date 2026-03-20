<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Quiz Center</div>
        <h1 class="page-title">课堂测验</h1>
        <p class="page-subtitle">
          学生可在此查看可参与的课堂测验，并在开放时间内完成作答。
        </p>
      </div>
      <div class="action-row">
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

    <el-table :data="quizzes" border>
      <el-table-column prop="courseName" label="所属课程" min-width="160" />
      <el-table-column prop="quizTitle" label="测验名称" min-width="180" />
      <el-table-column label="开放时间" min-width="220">
        <template #default="{ row }">
          {{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <StatusBadge :text="quizTimeText(row.timeStatus)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="200">
        <template #default="{ row }">
          <div class="action-row">
            <el-button size="small" @click="openQuiz(row)">查看题目</el-button>
            <el-button size="small" type="primary" :disabled="!row.canAnswer" @click="openQuiz(row)">
              {{ row.submitted ? '已提交' : row.canAnswer ? '开始作答' : '不可作答' }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <AppDialog
      v-model="dialogVisible"
      :title="activeQuiz?.quizTitle || '测验作答'"
      :width="760"
    >
      <div v-if="activeQuiz" class="dialog-body-stack">
        <div class="dialog-note">
          请按照题目顺序完成作答。系统会保存本次提交结果，提交后不能再次修改。
        </div>

        <div v-for="item in questions" :key="item.id" class="timeline-item">
          <div class="timeline-item__time">
            {{ item.questionType === 'SINGLE' ? '单选题' : '填空题' }} · {{ item.score }} 分
          </div>
          <h4 class="timeline-item__title">{{ item.questionTitle }}</h4>

          <el-radio-group
            v-if="item.questionType === 'SINGLE'"
            v-model="answers[item.id]"
            class="dialog-radio-list"
          >
            <el-radio
              v-for="option in parseOptions(item.optionsJson)"
              :key="option"
              :label="option"
            >
              {{ option }}
            </el-radio>
          </el-radio-group>

          <el-input
            v-else
            v-model="answers[item.id]"
            type="textarea"
            :rows="3"
            placeholder="请输入答案"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!activeQuiz?.canAnswer" @click="submitQuiz">
            提交测验
          </el-button>
        </div>
      </template>
    </AppDialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { api } from '../../api'
import AppDialog from '../../components/AppDialog.vue'
import StatusBadge from '../../components/StatusBadge.vue'
import { formatDateTime, quizTimeText } from '../../utils/format'

const route = useRoute()
const courseId = ref(route.query.courseId ? Number(route.query.courseId) : undefined)
const courses = ref([])
const quizzes = ref([])
const questions = ref([])
const activeQuiz = ref(null)
const dialogVisible = ref(false)
const answers = reactive({})
const courseMap = ref({})

const parseOptions = (value) => {
  try {
    return JSON.parse(value || '[]')
  } catch {
    return []
  }
}

const loadBase = async () => {
  courses.value = await api.studentMyCourses()
  courseMap.value = Object.fromEntries(courses.value.map((item) => [item.id, item.courseName]))
}

const load = async () => {
  const list = await api.studentQuizzes({})
  quizzes.value = (list || [])
    .filter((item) => !courseId.value || Number(item.courseId) === Number(courseId.value))
    .map((item) => ({
      ...item,
      courseName: courseMap.value[item.courseId] || `课程 ${item.courseId}`
    }))
}

const openQuiz = async (row) => {
  activeQuiz.value = row
  questions.value = await api.studentQuizQuestions(row.id)
  questions.value.forEach((item) => {
    answers[item.id] = answers[item.id] || ''
  })
  dialogVisible.value = true
}

const submitQuiz = async () => {
  const payload = {
    sessionId: activeQuiz.value.sessionId,
    courseId: activeQuiz.value.courseId,
    answers: questions.value.map((item) => ({
      questionId: item.id,
      answer: answers[item.id] || ''
    }))
  }
  const res = await api.studentSubmitQuiz(activeQuiz.value.id, payload)
  ElMessage.success(`测验已提交，得分 ${res.score}`)
  dialogVisible.value = false
  await load()
}

onMounted(async () => {
  await loadBase()
  await load()
})
</script>
