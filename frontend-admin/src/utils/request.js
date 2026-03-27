import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是200，说明接口有问题
    if (res.code !== 200) {
      // 401 只弹 confirm，避免与下面的 confirm 重复弹窗
      if (res.code === 401) {
        ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          localStorage.removeItem('token')
          router.push('/login')
        })
      } else {
        ElMessage.error(res.message || '请求失败')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 400:
          message = error.response.data?.message || '请求参数错误'
          break
        case 401:
          message = '未登录或登录已过期'
          localStorage.removeItem('token')
          router.push('/login')
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = error.response.data?.message || '服务器内部错误'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时'
    } else if (error.message.includes('Network')) {
      message = '网络错误'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
