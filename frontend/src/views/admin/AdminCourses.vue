<template>
  <div class="page-card">
    <h2 class="page-title">课程管理</h2>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="courseName" label="课程名称" />
      <el-table-column prop="courseCode" label="课程编号" />
      <el-table-column prop="teacherName" label="教师" />
      <el-table-column prop="auditStatus" label="审核状态" />
      <el-table-column prop="publishStatus" label="发布状态" />
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../../api'
const list = ref([])
const load = async () => {
  const res = await api.adminCourses({ pageNum: 1, pageSize: 100 })
  list.value = res.records
}
onMounted(load)
</script>
