import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 30000
})

request.interceptors.request.use(config => {
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
    ElMessage.error(res.data.message || '\u8bf7\u6c42\u5931\u8d25')
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
    ElMessage.error('\u767b\u5f55\u5df2\u8fc7\u671f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55')
  } else if (error.response && error.response.status === 403) {
    ElMessage.error('\u65e0\u6743\u8bbf\u95ee')
  } else if (!error.response || error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED') {
    console.warn('Network error, backend may not be ready:', error.message)
  } else {
    const msg = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(msg)
  }
  return Promise.reject(error)
})

export default request