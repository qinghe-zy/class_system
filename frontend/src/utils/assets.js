const rawApiBase = (import.meta.env.VITE_API_BASE_URL || '').trim()

const OFFICE_TYPES = new Set(['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx'])
const IMAGE_TYPES = new Set(['png', 'jpg', 'jpeg', 'gif', 'webp', 'bmp', 'svg'])
const VIDEO_TYPES = new Set(['mp4', 'webm', 'ogg', 'mov', 'm4v'])

function trimEndSlash(value) {
  return String(value || '').replace(/\/+$/, '')
}

function withLeadingSlash(value) {
  const normalized = String(value || '').trim()
  if (!normalized) return ''
  return normalized.startsWith('/') ? normalized : `/${normalized}`
}

function parseUrlPath(url) {
  if (!url) return ''
  try {
    const parsed = new URL(url, window.location.origin)
    return parsed.pathname || ''
  } catch {
    const withoutQuery = String(url).split('?')[0]
    return withoutQuery.split('#')[0]
  }
}

function decodeSafe(value) {
  if (!value) return ''
  try {
    return decodeURIComponent(value)
  } catch {
    return value
  }
}

function tryExtractInnerUrl(openUrl) {
  if (!openUrl) return ''
  try {
    const parsed = new URL(openUrl, window.location.origin)
    if (!parsed.pathname.endsWith('/api/files/open')) return ''
    return parsed.searchParams.get('url') || ''
  } catch {
    const match = String(openUrl).match(/[?&]url=([^&]+)/i)
    return match?.[1] || ''
  }
}

function normalizeAttachmentUrl(url) {
  let normalized = String(url || '').trim()
  if (!normalized) return ''
  for (let i = 0; i < 3; i += 1) {
    const inner = tryExtractInnerUrl(normalized)
    if (!inner) break
    normalized = decodeSafe(inner).trim()
  }
  return normalized
}

export function getApiBaseUrl() {
  if (!rawApiBase) return ''
  if (/^https?:\/\//i.test(rawApiBase)) return trimEndSlash(rawApiBase)
  return trimEndSlash(`${window.location.origin}${withLeadingSlash(rawApiBase)}`)
}

function joinApiBase(path) {
  const normalizedPath = withLeadingSlash(path)
  if (!normalizedPath) return ''
  const base = getApiBaseUrl()
  return base ? `${base}${normalizedPath}` : normalizedPath
}

export function resolveAttachmentType(type, url) {
  const normalizedType = String(type || '').trim().toLowerCase()
  if (normalizedType) return normalizedType
  const path = parseUrlPath(url)
  const index = path.lastIndexOf('.')
  if (index < 0) return ''
  return path.slice(index + 1).toLowerCase()
}

export function resolveAssetUrl(url) {
  if (!url) return ''
  const normalized = normalizeAttachmentUrl(url)
  if (!normalized) return ''
  if (/^https?:\/\//i.test(normalized)) return normalized
  if (normalized.startsWith('//')) return `${window.location.protocol}${normalized}`
  return joinApiBase(normalized)
}

export function buildFileOpenUrl(url) {
  if (!url) return ''
  return joinApiBase(`/api/files/open?url=${encodeURIComponent(url)}`)
}

export function isOfficeFile(type, url) {
  return OFFICE_TYPES.has(resolveAttachmentType(type, url))
}

export function resolvePreviewType(type, url) {
  if (!url) return 'other'
  const value = resolveAttachmentType(type, url)
  if (VIDEO_TYPES.has(value)) return 'video'
  if (value === 'pdf') return 'pdf'
  if (OFFICE_TYPES.has(value)) return 'office'
  if (IMAGE_TYPES.has(value)) return 'image'
  return 'other'
}

export function buildPreviewUrl(url, type) {
  if (!url) return ''
  const previewType = resolvePreviewType(type, url)
  if (previewType === 'office') {
    return joinApiBase(`/api/files/preview-text?url=${encodeURIComponent(url)}`)
  }
  if (previewType === 'video' || previewType === 'pdf' || previewType === 'image') {
    return resolveAssetUrl(url)
  }
  return buildFileOpenUrl(url)
}

export function buildAttachmentOpenUrl(url, type) {
  if (!url) return ''
  const previewType = resolvePreviewType(type, url)
  if (previewType === 'office' || previewType === 'other') {
    return buildFileOpenUrl(url)
  }
  return resolveAssetUrl(url)
}

export function validateVideoFile(file, timeoutMs = 10000) {
  if (!(file instanceof File)) {
    return Promise.resolve({ ok: false, message: '视频文件无效，请重新选择。' })
  }
  return new Promise((resolve) => {
    const objectUrl = URL.createObjectURL(file)
    const video = document.createElement('video')
    let done = false
    const timer = setTimeout(() => {
      finish(false, '视频解析超时，请确认文件未损坏并使用 H.264 编码 MP4。')
    }, timeoutMs)

    const finish = (ok, message = '') => {
      if (done) return
      done = true
      clearTimeout(timer)
      video.removeAttribute('src')
      video.load()
      URL.revokeObjectURL(objectUrl)
      resolve({ ok, message })
    }

    video.preload = 'metadata'
    video.muted = true
    video.onloadedmetadata = () => {
      if (video.videoWidth > 0 && video.videoHeight > 0) {
        finish(true)
      } else {
        finish(false, '该视频仅检测到音频轨道或编码不兼容，建议转为 H.264 + AAC 的 MP4 后重新上传。')
      }
    }
    video.onerror = () => {
      finish(false, '浏览器无法解析该视频，建议转为 H.264 + AAC 的 MP4 后重新上传。')
    }
    video.src = objectUrl
  })
}
