<template>
  <div class="page-card">
    <h2 class="page-title">课堂会话总览</h2>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="courseId" label="课程ID" />
      <el-table-column prop="teacherId" label="教师ID" />
      <el-table-column prop="sessionTitle" label="会话标题" />
      <el-table-column prop="sessionStartTime" label="开始时间" />
      <el-table-column prop="sessionEndTime" label="结束时间" />
      <el-table-column prop="sessionStatus" label="状态" />
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../../api'
const list = ref([])
const load = async () => { list.value = await api.adminSessions() }
onMounted(load)
</script>
