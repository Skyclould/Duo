import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: 'http://146.56.250.163/duo', // adjust base URL according to backend setup
  timeout: 50000 // request timeout
})

// Request interceptor
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token} `
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 200 is success based on custom backend R wrapper
    if (res.code === 200) {
      return res
    } else {
      ElMessage.error(res.message || 'Error occurred')
      return Promise.reject(new Error(res.message || 'Error'))
    }
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      ElMessage.error('Authentication failed. Please log in again.')
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.message || 'Server Error')
    }
    return Promise.reject(error)
  }
)

export default service
