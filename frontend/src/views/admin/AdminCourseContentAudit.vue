<template>
  <div class="page-card">
    <div class="page-hero">
      <div>
        <div class="section-eyebrow">Resource Review</div>
        <h1 class="page-title">资源审核</h1>
        <p class="page-subtitle">审核教师上传的文档、课件、PDF、视频或附件，优先支持预览判断而不是先下载后判断。</p>
      </div>
      <el-button @click="load">刷新</el-button>
    </div>

    <div class="toolbar">
      <el-select v-model="status" clearable placeholder="审核状态" style="width: 160px">
        <el-option label="待审核" value="PENDING" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已驳回" value="REJECTED" />
      </el-select>
      <el-button type="primary" plain @click="load">筛选</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="courseName" label="课程" min-width="160" />
      <el-table-column prop="teacherName" label="教师" width="120" />
      <el-table-column prop="contentTitle" label="资源标题" min-width="180" />
      <el-table-column prop="attachmentType" label="类型" width="100" />
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <StatusBadge :text="auditText(row.auditStatus)" />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" min-width="160">
        <template #default="{ row }">{{ formatDateTime(row.updatedTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" min-width="260" fixed="right">
        <template #default="{ row }">
          <div class="action-row">
            <el-button size="small" @click="preview(row)">预览</el-button>
            <el-button size="small" type="success" @click="audit(row, 'APPROVED')">通过</el-button>
            <el-button size="small" type="danger" plain @click="audit(row, 'REJECTED')">驳回</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-drawer v-model="drawerVisible" :title="activeItem?.contentTitle || '资源预览'" size="72%">
      <div v-if="activeItem" class="review-drawer">
        <section class="review-preview surface-box">
          <template v-if="previewType(activeItem) === 'video'">
            <div v-if="videoPreviewError" class="review-fallback">
              <p>{{ videoPreviewErrorText }}</p>
              <div class="action-row">
                <el-button type="primary" @click="openAttachment(activeItem)">打开视频</el-button>
                <el-button plain @click="copyAttachmentLink(activeItem)">复制地址</el-button>
              </div>
            </div>
            <video
              v-else
              :src="openUrl(activeItem)"
              controls
              class="review-media"
              @error="handleVideoError"
              @loadedmetadata="handleVideoMetaLoaded"
            >当前浏览器暂不支持视频预览</video>
          </template>
          <template v-else-if="previewType(activeItem) === 'pdf'">
            <iframe :src="previewUrl(activeItem)" class="review-frame" title="资源预览"></iframe>
          </template>
          <template v-else-if="previewType(activeItem) === 'office'">
            <iframe :src="previewUrl(activeItem)" class="review-frame review-frame--doc" title="Office 文本预览"></iframe>
          </template>
          <template v-else-if="previewType(activeItem) === 'image'">
            <div class="review-image-wrap">
              <img :src="previewUrl(activeItem)" class="review-image" alt="资源图片预览" />
            </div>
          </template>
          <template v-else>
            <div class="review-fallback">
              <p>当前资源类型暂不支持高保真页内预览，可先查看文本说明，再点击打开原始附件。</p>
              <div class="action-row">
                <el-button type="primary" @click="openAttachment(activeItem)">打开附件</el-button>
                <el-button plain @click="copyAttachmentLink(activeItem)">复制地址</el-button>
              </div>
            </div>
          </template>
        </section>

        <section class="review-meta surface-box">
          <div class="meta-grid">
            <div class="meta-item"><span>所属课程</span><strong>{{ activeItem.courseName || `课程 ${activeItem.courseId}` }}</strong></div>
            <div class="meta-item"><span>教师</span><strong>{{ activeItem.teacherName || `教师 ${activeItem.teacherId}` }}</strong></div>
            <div class="meta-item"><span>资源类型</span><strong>{{ activeItem.attachmentType || '未识别' }}</strong></div>
            <div class="meta-item"><span>审核状态</span><strong>{{ auditText(activeItem.auditStatus) }}</strong></div>
          </div>

          <div class="meta-block">
            <div class="meta-title">资源说明</div>
            <p class="meta-desc break-words">{{ activeItem.contentBody || '暂无文本说明。' }}</p>
          </div>

          <div class="meta-block">
            <div class="meta-title">附件地址</div>
            <p class="meta-desc break-words">{{ activeItem.attachmentUrl || '当前资源未包含附件地址。' }}</p>
          </div>

          <div class="meta-block">
            <div class="meta-title">审核备注</div>
            <p class="meta-desc break-words">{{ activeItem.auditRemark || '当前暂无审核备注。' }}</p>
          </div>

          <div class="drawer-actions">
            <el-button type="primary" plain @click="openAttachment(activeItem)">打开原附件</el-button>
            <el-button type="success" @click="audit(activeItem, 'APPROVED')">通过资源</el-button>
            <el-button type="danger" plain @click="audit(activeItem, 'REJECTED')">驳回资源</el-button>
          </div>
        </section>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { api } from '../../api'
import StatusBadge from '../../components/StatusBadge.vue'
import { auditText, formatDateTime } from '../../utils/format'
import { buildAttachmentOpenUrl, buildPreviewUrl, resolvePreviewType } from '../../utils/assets'

const list = ref([])
const status = ref('PENDING')
const drawerVisible = ref(false)
const activeItem = ref(null)
const videoPreviewError = ref(false)
const videoPreviewErrorText = ref('')

const load = async () => {
  list.value = await api.adminCourseContents({ auditStatus: status.value || undefined })
}

const previewType = (row) => {
  return resolvePreviewType(row?.attachmentType, row?.attachmentUrl)
}

const previewUrl = (row) => buildPreviewUrl(row?.attachmentUrl, row?.attachmentType)
const openUrl = (row) => buildAttachmentOpenUrl(row?.attachmentUrl, row?.attachmentType)

const openAttachment = (row) => {
  const url = openUrl(row)
  if (!url) {
    ElMessage.warning('当前资源未上传附件')
    return
  }
  window.open(url, '_blank', 'noopener')
}

const copyAttachmentLink = async (row) => {
  const url = openUrl(row)
  if (!url) {
    ElMessage.warning('当前资源未上传附件')
    return
  }
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('附件地址已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制地址')
  }
}

const preview = (row) => {
  activeItem.value = row
  videoPreviewError.value = false
  videoPreviewErrorText.value = ''
  drawerVisible.value = true
}

const handleVideoError = () => {
  videoPreviewError.value = true
  videoPreviewErrorText.value = '视频加载失败，可能是编码不兼容。建议教师上传 H.264 + AAC 的 MP4。'
}

const handleVideoMetaLoaded = (event) => {
  const width = event?.target?.videoWidth || 0
  const height = event?.target?.videoHeight || 0
  if (width <= 0 || height <= 0) {
    videoPreviewError.value = true
    videoPreviewErrorText.value = '检测到视频可能“有声无画”，建议教师转码后重新上传。'
    return
  }
  videoPreviewError.value = false
  videoPreviewErrorText.value = ''
}

const audit = async (row, auditStatus) => {
  const prompt = await ElMessageBox.prompt(
    auditStatus === 'APPROVED' ? '请输入审核说明（可选）' : '请输入驳回原因',
    auditStatus === 'APPROVED' ? '资源审核通过' : '资源审核驳回',
    {
      inputPlaceholder: auditStatus === 'APPROVED' ? '例如：资源命名规范，内容可发布…' : '例如：标题不明确、说明不足、需补充附件…',
      inputValue: auditStatus === 'APPROVED' ? '资源合规，可进入学生端' : ''
    }
  ).catch(() => null)
  if (!prompt) return
  await api.adminAuditCourseContent(row.id, { auditStatus, auditRemark: prompt.value || '' })
  ElMessage.success('资源审核已完成')
  drawerVisible.value = false
  await load()
}

onMounted(load)
</script>

<style scoped>
.review-drawer {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(320px, 0.9fr);
  gap: 16px;
}

.surface-box {
  background: #fff;
  border: 1px solid #dbe4ff;
  border-radius: 20px;
  padding: 16px;
  box-shadow: 0 10px 30px rgba(43, 106, 255, 0.06);
}

.review-media,
.review-frame {
  width: 100%;
  height: 620px;
  border: 0;
  border-radius: 16px;
  background: #fff;
}

.review-frame--doc {
  background: #f6f8ff;
}

.review-fallback {
  min-height: 620px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 16px;
  text-align: center;
  color: #5f6b84;
}

.review-image-wrap {
  min-height: 620px;
  display: grid;
  place-items: center;
  background: #f6f8ff;
  border-radius: 16px;
}

.review-image {
  max-width: 100%;
  max-height: 620px;
  object-fit: contain;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.meta-item {
  background: #f6f8ff;
  border-radius: 16px;
  padding: 14px;
}

.meta-item span {
  display: block;
  color: #5f6b84;
  margin-bottom: 6px;
}

.meta-block + .meta-block {
  margin-top: 14px;
}

.meta-title {
  font-size: 13px;
  color: #5f6b84;
  margin-bottom: 8px;
}

.meta-desc {
  margin: 0;
  color: #1c2339;
  line-height: 1.7;
}

.drawer-actions {
  display: flex;
  gap: 12px;
  margin-top: 18px;
  flex-wrap: wrap;
}

@media (max-width: 1280px) {
  .review-drawer {
    grid-template-columns: 1fr;
  }
}
</style>
