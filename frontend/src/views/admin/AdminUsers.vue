<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">User & Role</div>
        <h1 class="page-title">用户与角色</h1>
        <p class="page-subtitle">
          统一维护教师、学生与管理员账号，避免后续审核与课程流程出现角色混乱。
        </p>
      </div>
      <div class="action-row">
        <el-button type="primary" @click="openDialog()">新增用户</el-button>
        <el-button @click="load">刷新列表</el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        clearable
        style="max-width: 280px"
        placeholder="搜索姓名或账号…"
        aria-label="搜索用户"
      />
      <el-button type="primary" plain @click="load">搜索</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="realName" label="姓名" min-width="140" />
      <el-table-column prop="username" label="账号" min-width="140" />
      <el-table-column prop="roleName" label="角色" width="110" />
      <el-table-column prop="departmentOrClass" label="部门 / 班级" min-width="160" />
      <el-table-column prop="phone" label="电话" min-width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <StatusBadge :text="Number(row.status) === 1 ? '启用' : '停用'" />
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="240" fixed="right">
        <template #default="{ row }">
          <div class="action-row">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" type="danger" plain @click="removeUser(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <AppDialog
      v-model="dialogVisible"
      :title="form.id ? '编辑用户' : '新增用户'"
      :width="720"
    >
      <el-form :model="form" label-position="top" class="dialog-form">
        <div class="dialog-form-grid">
          <el-form-item label="账号">
            <el-input
              v-model="form.username"
              name="username"
              autocomplete="off"
              spellcheck="false"
              placeholder="请输入登录账号"
            />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input
              v-model="form.realName"
              name="realName"
              autocomplete="name"
              placeholder="请输入姓名"
            />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="form.roleCode" aria-label="选择角色" placeholder="请选择角色">
              <el-option label="管理员" value="ADMIN" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="学生" value="STUDENT" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.status" aria-label="选择状态" placeholder="请选择状态">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="电话">
            <el-input
              v-model="form.phone"
              name="phone"
              autocomplete="tel"
              inputmode="tel"
              placeholder="请输入联系电话"
            />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input
              v-model="form.email"
              name="email"
              autocomplete="email"
              inputmode="email"
              placeholder="请输入邮箱地址"
            />
          </el-form-item>
          <el-form-item label="部门 / 班级">
            <el-input
              v-model="form.departmentOrClass"
              name="departmentOrClass"
              autocomplete="off"
              placeholder="请输入部门或班级"
            />
          </el-form-item>
          <el-form-item label="初始密码">
            <el-input
              v-model="form.password"
              name="password"
              type="password"
              autocomplete="new-password"
              placeholder="默认密码为 123456"
            />
          </el-form-item>
        </div>
      </el-form>

      <template #footer>
        <div class="dialog-footer-actions">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">保存</el-button>
        </div>
      </template>
    </AppDialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api'
import AppDialog from '../../components/AppDialog.vue'
import StatusBadge from '../../components/StatusBadge.vue'

const keyword = ref('')
const list = ref([])
const dialogVisible = ref(false)

const form = reactive({
  id: null,
  username: '',
  realName: '',
  roleCode: 'STUDENT',
  phone: '',
  email: '',
  departmentOrClass: '',
  status: 1,
  password: '123456'
})

const load = async () => {
  const res = await api.adminUsers({ pageNum: 1, pageSize: 200, keyword: keyword.value })
  list.value = (res.records || []).map((item) => ({
    ...item,
    roleName: ({ ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }[item.roleCode]) || item.roleCode
  }))
}

const openDialog = (row = null) => {
  dialogVisible.value = true
  form.id = row?.id || null
  form.username = row?.username || ''
  form.realName = row?.realName || ''
  form.roleCode = row?.roleCode || 'STUDENT'
  form.phone = row?.phone || ''
  form.email = row?.email || ''
  form.departmentOrClass = row?.departmentOrClass || ''
  form.status = row?.status ?? 1
  form.password = '123456'
}

const submit = async () => {
  const payload = { ...form }
  if (form.id) {
    await api.updateUser(form.id, payload)
    ElMessage.success('用户已更新')
  } else {
    await api.createUser(payload)
    ElMessage.success('用户已创建')
  }
  dialogVisible.value = false
  await load()
}

const resetPwd = async (row) => {
  await api.resetUserPwd(row.id)
  ElMessage.success(`已将 ${row.realName} 的密码重置为 123456`)
}

const removeUser = async (row) => {
  await ElMessageBox.confirm(`确认删除 ${row.realName} 吗？`, '删除用户')
  await api.deleteUser(row.id)
  ElMessage.success('用户已删除')
  await load()
}

onMounted(load)
</script>
