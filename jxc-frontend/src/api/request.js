import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

function getApiBaseUrl() {
  const envUrl = import.meta.env.VITE_API_BASE_URL
  if (envUrl && envUrl !== '' && !envUrl.includes('koyeb')) {
    return envUrl
  }
  return 'http://localhost:8080/api'
}

const request = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 30000
})

request.interceptors.request.use(config => {
  config.headers['ngrok-skip-browser-warning'] = 'true'
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
}, error => {
  return Promise.reject(error)
})

request.interceptors.response.use(res => {
  if (res.config.responseType === 'blob') {
    return res.data
  }
  if (res.data.code !== 200) {
    ElMessage.error(res.data.message || '请求失败')
    if (res.data.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('realName')
      localStorage.removeItem('role')
      router.push('/login')
    }
    return Promise.reject(res.data)
  }
  return res.data
}, error => {
  if (error.response && error.response.status === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
    localStorage.removeItem('role')
    router.push('/login')
    ElMessage.error('登录已过期，请重新登录')
  } else if (error.response && error.response.status === 403) {
    ElMessage.error('无权访问')
  } else if (!error.response || error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED') {
    console.warn('Network error, backend may not be ready:', error.message)
  } else {
    const msg = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(msg)
  }
  return Promise.reject(error)
})

export default request