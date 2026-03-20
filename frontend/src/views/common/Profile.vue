<template>
  <div class="page-card">
    <div class="page-hero"><div><div class="section-eyebrow">Account Center</div><h1 class="page-title">个人中心</h1><p class="page-subtitle">维护你的基础资料、联系方式与组织信息，便于课程协同与通知触达。</p></div></div>
    <div class="content-grid">
      <section class="surface-card">
        <h3 class="surface-card__title">基础资料</h3>
        <el-form :model="form" label-position="top" @submit.prevent="submit">
          <div class="form-grid">
            <el-form-item label="姓名"><el-input v-model="form.realName" name="realName" autocomplete="name" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" name="phone" autocomplete="tel" inputmode="tel" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="form.email" name="email" autocomplete="email" inputmode="email" spellcheck="false" /></el-form-item>
            <el-form-item :label="auth.role === 'STUDENT' ? '班级' : '部门 / 班级'"><el-input v-model="form.departmentOrClass" name="departmentOrClass" autocomplete="off" /></el-form-item>
          </div>
          <el-form-item label="头像地址"><el-input v-model="form.avatar" name="avatar" autocomplete="off" /></el-form-item>
          <div class="action-row"><el-button type="primary" :loading="loading" native-type="submit">保存资料</el-button></div>
        </el-form>
      </section>
      <section class="surface-card">
        <h3 class="surface-card__title">账号说明</h3>
        <div class="timeline-list">
          <article class="timeline-item"><div class="timeline-item__time">当前角色</div><h4 class="timeline-item__title">{{ roleText }}</h4><p class="timeline-item__desc">不同角色拥有不同菜单、接口与数据权限，页面只显示与你相关的业务动作。</p></article>
          <article class="timeline-item"><div class="timeline-item__time">通知偏好</div><h4 class="timeline-item__title">本期采用站内提醒</h4><p class="timeline-item__desc">系统会针对审核结果、签到与学习风险提供说明，不输出绝对处罚结论。</p></article>
          <article class="timeline-item"><div class="timeline-item__time">账号安全</div><h4 class="timeline-item__title">默认密码建议及时修改</h4><p class="timeline-item__desc">生产环境请接入完整密码修改与二次校验流程。</p></article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, watch, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { api } from '../../api'
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ realName: '', phone: '', email: '', departmentOrClass: '', avatar: '' })
watch(() => auth.user, (user) => { if (!user) return; form.realName = user.realName || ''; form.phone = user.phone || ''; form.email = user.email || ''; form.departmentOrClass = user.departmentOrClass || ''; form.avatar = user.avatar || '' }, { immediate: true })
const roleText = computed(() => ({ ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }[auth.role] || '系统用户'))
const submit = async () => { loading.value = true; try { await api.updateProfile({ ...form }); await auth.fetchMe(); ElMessage.success('资料已更新') } finally { loading.value = false } }
</script>
