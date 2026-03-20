<template>
  <div class="page-card">
    <h2 class="page-title">行为检测总览</h2>
    <el-table :data="list" border>
      <el-table-column prop="detectTime" label="检测时间" width="170" />
      <el-table-column prop="studentId" label="学生ID" width="80" />
      <el-table-column prop="courseId" label="课程ID" width="80" />
      <el-table-column prop="behaviorStatus" label="状态标签" />
      <el-table-column prop="statusDescription" label="状态描述" />
      <el-table-column prop="activityScore" label="活跃度" width="90" />
      <el-table-column prop="exceptionFlag" label="异常" width="80">
        <template #default="{ row }">{{ row.exceptionFlag === 1 ? '是' : '否' }}</template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" layout="total, prev, pager, next" :total="total" @current-change="load" />
  </div>
</template>
<script setup>
import { reactive, ref, onMounted } from 'vue'
import { api } from '../../api'
const query = reactive({ pageNum: 1, pageSize: 10 })
const list = ref([])
const total = ref(0)
const load = async () => {
  const res = await api.adminBehavior(query)
  list.value = res.records
  total.value = res.total
}
onMounted(load)
</script>
