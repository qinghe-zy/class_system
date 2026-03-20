<template>
  <div class="login-shell">
    <section class="login-panel">
      <div>
        <div class="section-eyebrow">Welcome Back</div>
        <h1 class="page-title">登录在线课堂管理系统</h1>
        <p class="page-subtitle">本系统以课程审核、学习过程分析、课堂互动与风险解释为核心，支持管理员、教师、学生三端协同。</p>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="submit">
        <el-form-item label="账号"><el-input v-model="form.username" name="username" autocomplete="username" aria-label="账号输入框" placeholder="请输入账号…" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" name="password" type="password" show-password autocomplete="current-password" aria-label="密码输入框" placeholder="请输入密码…" /></el-form-item>
        <div class="action-row">
          <el-button type="primary" :loading="loading" native-type="submit">进入系统</el-button>
          <el-button @click="fillDemo('admin')">管理员示例</el-button>
          <el-button @click="fillDemo('teacher01')">教师示例</el-button>
          <el-button @click="fillDemo('student01')">学生示例</el-button>
        </div>
      </el-form>
      <div class="surface-card">
        <h3 class="surface-card__title">默认演示账号</h3>
        <div class="info-list">
          <div class="info-item"><span>管理员</span><strong>admin / 123456</strong></div>
          <div class="info-item"><span>教师</span><strong>teacher01 / 123456</strong></div>
          <div class="info-item"><span>学生</span><strong>student01 / 123456</strong></div>
        </div>
      </div>
    </section>
    <section class="login-showcase">
      <div class="surface-card">
        <div class="section-eyebrow">Why This Version</div>
        <h2 class="surface-card__title">从“伪专注检测”升级为“真实学习过程分析”</h2>
        <p class="surface-card__meta">系统不把点击和失焦直接等同于专注结论，而是综合课程内容、签到、测验、学习轨迹与异常提醒，帮助教师做出可解释判断。</p>
      </div>
      <div class="card-grid">
        <div class="stat-card"><div class="stat-card__label">课程审核</div><div class="stat-card__value">3 端</div><div class="stat-card__hint">管理员审核课程与资源，教师发布内容，学生统一学习。</div></div>
        <div class="stat-card"><div class="stat-card__label">学习轨迹</div><div class="stat-card__value">可解释</div><div class="stat-card__hint">记录阅读、观看、签到、测验与学习状态，证据链清晰。</div></div>
        <div class="stat-card"><div class="stat-card__label">异常识别</div><div class="stat-card__value">风险提示</div><div class="stat-card__hint">只提供辅助提醒，不做绝对处罚结论。</div></div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../store/auth'
const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: '', password: '123456' })
const fillDemo = (username) => { form.username = username; form.password = '123456' }
const submit = async () => { loading.value = true; try { await auth.login(form); router.push('/') } finally { loading.value = false } }
</script>

<style scoped>
.login-shell { min-height: 100vh; display: grid; grid-template-columns: minmax(360px, 460px) minmax(0, 1fr); gap: 20px; padding: 20px; background: radial-gradient(circle at top left, rgba(43, 106, 255, 0.12), transparent 34%), radial-gradient(circle at bottom right, rgba(25, 167, 90, 0.10), transparent 30%), linear-gradient(180deg, #f6f8ff 0%, #eff4ff 100%); }
.login-panel, .login-showcase { background: rgba(255, 255, 255, 0.84); border: 1px solid rgba(255, 255, 255, 0.68); border-radius: 28px; padding: 28px; backdrop-filter: blur(18px); box-shadow: 0 20px 44px rgba(28, 35, 57, 0.08); }
.login-panel { display: grid; gap: 22px; align-self: stretch; }
.login-showcase { display: grid; gap: 18px; align-content: center; }
@media (max-width: 960px) { .login-shell { grid-template-columns: 1fr; padding: 14px; } }
</style>
