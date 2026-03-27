import request from '@/utils/request'

// 登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 获取用户信息
export function getUserInfo() {
  return request.get('/auth/info')
}

// 登出
export function logout() {
  return request.post('/auth/logout')
}
