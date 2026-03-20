export const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
})

export function formatDateTime(value) {
  if (!value) return '—'
  const date = value instanceof Date ? value : new Date(value)
  return Number.isNaN(date.getTime()) ? '—' : dateFormatter.format(date)
}

export function formatCount(value) {
  return new Intl.NumberFormat('zh-CN').format(Number(value || 0))
}

export function pickTagType(status) {
  const value = String(status || '').toUpperCase()
  if (['APPROVED', 'PUBLISHED', 'OPEN', 'ONGOING', 'SIGNED', 'SUBMITTED', 'NORMAL', 'ACTIVE', 'LOW_RISK', '已选修', '可加入', '启用'].includes(value)) return 'success'
  if (['PENDING', 'NOT_STARTED', 'MEDIUM_RISK', '待审核', '签到中', '作答中'].includes(value)) return 'warning'
  if (['REJECTED', 'CLOSED', 'EXPIRED', 'UNSIGNED', 'ABNORMAL', 'HIGH_RISK', '停用'].includes(value)) return 'danger'
  return 'info'
}

export function auditText(status) {
  const map = { PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回' }
  return map[status] || status || '—'
}

export function publishText(status) {
  return Number(status) === 1 ? '已发布' : '未发布'
}

export function sessionText(status) {
  const map = { DRAFT: '待开始', NOT_STARTED: '待开始', ONGOING: '进行中', FINISHED: '已结束', CLOSED: '已结束' }
  return map[status] || status || '—'
}

export function signTaskText(status) {
  const map = { NOT_STARTED: '待开始', OPEN: '签到中', CLOSED: '已结束' }
  return map[status] || status || '—'
}

export function quizTimeText(status) {
  const map = { NOT_STARTED: '未开始', ONGOING: '作答中', EXPIRED: '已截止' }
  return map[status] || status || '—'
}

export function behaviorText(status) {
  const map = { NORMAL: '进度正常', WARNING: '需关注', ABNORMAL: '存在风险', ACTIVE: '参与较好', INACTIVE: '低交互' }
  return map[status] || status || '—'
}

export function riskLabel(flag, activityScore = 0) {
  if (Number(flag) === 1 && Number(activityScore) < 40) return '高风险'
  if (Number(flag) === 1) return '中风险'
  return '低风险'
}
