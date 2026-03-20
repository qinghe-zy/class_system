<template>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳转到主要内容</a>
    <aside class="app-shell__aside">
      <div class="brand-card">
        <div class="brand-card__eyebrow">Education SaaS</div>
        <h1 class="brand-card__title">在线课堂管理系统</h1>
        <p class="brand-card__desc">课程审核、学习过程分析与异常学习提醒的一体化平台。</p>
      </div>

      <nav class="nav-list" aria-label="主导航">
        <router-link
          v-for="item in menuList"
          :key="item.path"
          :to="item.path"
          class="nav-list__item"
          :class="{ 'is-active': route.path === item.path }"
        >
          <span class="nav-list__title">{{ item.title }}</span>
          <span class="nav-list__hint">{{ item.hint }}</span>
        </router-link>
      </nav>
    </aside>

    <main id="main-content" class="app-shell__main">
      <header class="topbar">
        <div class="topbar__main">
          <div class="topbar__eyebrow">{{ roleText }}</div>
          <h2 class="topbar__title">{{ currentTitle }}</h2>
        </div>
        <div class="topbar__actions">
          <div class="user-pill">
            <strong>{{ auth.user?.realName || '未登录' }}</strong>
            <span>{{ auth.user?.departmentOrClass || '未设置组织信息' }}</span>
          </div>
          <el-button type="primary" plain @click="router.push('/profile')">个人中心</el-button>
          <el-button type="danger" plain @click="logout">退出登录</el-button>
        </div>
      </header>
      <section class="app-shell__content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const menus = {
  ADMIN: [
    { path: '/', title: '工作台', hint: '审核与平台总览' },
    { path: '/admin/users', title: '用户与角色', hint: '账号、角色、状态管理' },
    { path: '/admin/course-audit', title: '课程审核', hint: '课程基础信息审核' },
    { path: '/admin/course-content-audit', title: '资源审核', hint: '文档、课件、视频审核' },
    { path: '/admin/stats', title: '平台分析', hint: '平台趋势与风险概览' },
    { path: '/admin/permission', title: '系统设置', hint: '阈值与通知说明' }
  ],
  TEACHER: [
    { path: '/', title: '工作台', hint: '今日课程与快捷入口' },
    { path: '/teacher/courses', title: '我的课程', hint: '建课与课程维护' },
    { path: '/teacher/course-content', title: '资源库', hint: '上传文档、课件与附件' },
    { path: '/teacher/sessions', title: '课堂管理', hint: '课堂活动、签到与测验' },
    { path: '/teacher/stats', title: '学习分析', hint: '进度、参与与风险趋势' },
    { path: '/profile', title: '个人中心', hint: '资料与账号安全' }
  ],
  STUDENT: [
    { path: '/', title: '学习首页', hint: '今日任务与课程提醒' },
    { path: '/student/my-courses', title: '我的课程', hint: '课程进度与继续学习' },
    { path: '/student/signin', title: '今日任务', hint: '签到与待完成任务' },
    { path: '/student/stats', title: '学习记录', hint: '进度、测验与趋势' },
    { path: '/student/behavior', title: '风险说明', hint: '异常提醒与解释说明' },
    { path: '/profile', title: '个人中心', hint: '资料与通知偏好' }
  ]
}

const roleText = computed(() => ({ ADMIN: '管理员端', TEACHER: '教师端', STUDENT: '学生端' }[auth.role] || '在线课堂平台'))
const menuList = computed(() => menus[auth.role] || [])
const currentTitle = computed(() => route.meta?.title || menuList.value.find((item) => item.path === route.path)?.title || '在线课堂管理系统')

const logout = () => {
  auth.logout()
  router.push('/login')
}
</script>
