const pad = (v) => `${v}`.padStart(2, '0')

export const toLocalDateTimeString = (value) => {
  if (!value) return ''
  const date = value instanceof Date ? value : new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const y = date.getFullYear()
  const m = pad(date.getMonth() + 1)
  const d = pad(date.getDate())
  const hh = pad(date.getHours())
  const mm = pad(date.getMinutes())
  const ss = pad(date.getSeconds())
  return `${y}-${m}-${d}T${hh}:${mm}:${ss}`
}

export const quizTimeStatusText = (status) => {
  if (status === 'NOT_STARTED') return '未开始'
  if (status === 'ONGOING') return '进行中'
  if (status === 'EXPIRED') return '已结束'
  return status || '-'
}
