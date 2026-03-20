const API_ORIGIN = 'http://127.0.0.1:8080'

export function resolveAssetUrl(url) {
  if (!url) return ''
  if (/^https?:\/\//i.test(url)) return url
  if (url.startsWith('//')) return `${window.location.protocol}${url}`
  return `${API_ORIGIN}${url.startsWith('/') ? url : `/${url}`}`
}

export function isOfficeFile(type) {
  const value = String(type || '').toLowerCase()
  return ['doc', 'docx', 'ppt', 'pptx'].includes(value)
}

export function buildFileOpenUrl(url) {
  if (!url) return ''
  return `${API_ORIGIN}/api/files/open?url=${encodeURIComponent(url)}`
}

export function buildPreviewUrl(url, type) {
  if (!url) return ''
  const value = String(type || '').toLowerCase()
  if (['doc', 'docx', 'ppt', 'pptx'].includes(value)) {
    return `${API_ORIGIN}/api/files/preview-text?url=${encodeURIComponent(url)}`
  }
  return buildFileOpenUrl(url)
}