<template>
  <div class="page-card"><div class="page-hero"><div><div class="section-eyebrow">System Settings</div><h1 class="page-title">系统设置</h1><p class="page-subtitle">当前版本统一展示权限说明、阈值建议与通知口径，帮助产品、研发与答辩时保持一致表达。</p></div></div>
  <div class="content-grid"><section class="surface-card"><h3 class="surface-card__title">权限说明</h3><el-table :data="permissions" border><el-table-column prop="permName" label="权限名称" min-width="180" /><el-table-column prop="permCode" label="权限编码" min-width="180" /><el-table-column prop="description" label="说明" min-width="220" /></el-table></section><section class="surface-card"><h3 class="surface-card__title">平台阈值建议</h3><div class="timeline-list"><article class="timeline-item"><div class="timeline-item__time">学习过程分析</div><h4 class="timeline-item__title">只输出“进度、参与、风险”</h4><p class="timeline-item__desc">禁止把点击、失焦、心跳保活直接解释为“是否认真听课”的绝对结论。</p></article><article class="timeline-item"><div class="timeline-item__time">随机确认</div><h4 class="timeline-item__title">建议每节课 1–3 次</h4><p class="timeline-item__desc">触发对象以长文档、长视频为主，失败结果只纳入风险模型，不直接处罚。</p></article><article class="timeline-item"><div class="timeline-item__time">通知文案</div><h4 class="timeline-item__title">强调下一步，而不是纯技术结论</h4><p class="timeline-item__desc">例如：当前视频存在跳播风险，建议返回关键片段继续学习。</p></article></div></section></div></div>
</template>
<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../../api'
const permissions = ref([])
onMounted(async () => { permissions.value = await api.adminPermissions() })
</script>
