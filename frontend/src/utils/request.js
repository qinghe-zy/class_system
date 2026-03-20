import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '').trim().replace(/\/+$/, '')
const requestTimeout = Number(import.meta.env.VITE_HTTP_TIMEOUT_MS || 30000)

const request = axios.create({
  // 使用可配置基地址；为空时默认同源，方便通过反向代理或 Vite 代理统一转发。
  baseURL: apiBaseUrl,
  timeout: Number.isFinite(requestTimeout) && requestTimeout > 0 ? requestTimeout : 30000
})

request.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const { code, message, data } = res.data
    if (code !== 0) {
      if (code === 401) {
        const auth = useAuthStore()
        auth.logout()
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
    return data
  },
  (error) => {
    const isTimeout = error?.code === 'ECONNABORTED'
    if (error?.response?.status === 401) {
      const auth = useAuthStore()
      auth.logout()
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    ElMessage.error(isTimeout ? '请求超时，请稍后重试' : (error?.response?.data?.message || error.message || '网络异常'))
    return Promise.reject(error)
  }
)

export default request
