import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { Result } from '@/types'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const request: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const authStore = useAuthStore()
    const token = authStore.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    const res = response.data
    if (!res.success) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      const data = error.response.data as Result<unknown>
      const requestUrl = error.config?.url || ''

      switch (status) {
        case 401:
          // 登录接口的 401 显示后端返回的消息
          if (requestUrl.includes('/api/auth/login')) {
            ElMessage.error(data?.message || '用户名或密码错误')
          } else {
            // 其他接口的 401 表示 token 过期
            ElMessage.error('登录已过期，请重新登录')
            const authStore = useAuthStore()
            authStore.logout()
            router.push('/login')
          }
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        default:
          ElMessage.error(data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request
