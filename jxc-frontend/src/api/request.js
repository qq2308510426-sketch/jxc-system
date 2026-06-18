import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
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
  } else if (!error.response || error.code === 'ERR_NETWORK' || error.code === 'ECONNREFUSED') {
    console.warn('Network error, backend may not be ready:', error.message)
  } else {
    ElMessage.error(error.message || '网络错误')
  }
  return Promise.reject(error)
})

export default request