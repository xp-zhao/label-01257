import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, getUserInfo, logout } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menus = ref([])
  const permissions = ref([])

  const isLoggedIn = computed(() => !!token.value)

  // 登录
  async function doLogin(loginForm) {
    try {
      const res = await login(loginForm)
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      userInfo.value = res.data
      // 登录时也设置权限（登录接口也返回权限列表）
      permissions.value = res.data.permissions || []
      return res
    } catch (error) {
      throw error
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      menus.value = res.data.menus || []
      permissions.value = res.data.permissions || []
      return res
    } catch (error) {
      throw error
    }
  }

  // 登出
  async function doLogout() {
    try {
      await logout()
    } catch (error) {
      console.error('登出失败', error)
    } finally {
      token.value = ''
      userInfo.value = null
      menus.value = []
      permissions.value = []
      localStorage.removeItem('token')
      router.push('/login')
    }
  }

  // 检查权限
  function hasPermission(permission) {
    return permissions.value.includes(permission)
  }

  return {
    token,
    userInfo,
    menus,
    permissions,
    isLoggedIn,
    doLogin,
    fetchUserInfo,
    doLogout,
    hasPermission
  }
})
