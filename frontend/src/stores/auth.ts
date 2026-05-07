import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginForm } from '@/types'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const permissions = computed(() => userInfo.value?.permissions || [])
  const roles = computed(() => userInfo.value?.roles || [])

  function hasPermission(permission: string): boolean {
    return permissions.value.includes(permission)
  }

  function hasRole(role: string): boolean {
    return roles.value.includes(role)
  }

  async function login(form: LoginForm) {
    const { data } = await loginApi(form)
    const result = data.data

    token.value = result.accessToken
    refreshToken.value = result.refreshToken
    userInfo.value = result.userInfo

    localStorage.setItem('token', result.accessToken)
    localStorage.setItem('refreshToken', result.refreshToken)

    return result
  }

  async function fetchUserInfo() {
    if (!token.value) return null

    try {
      const { data } = await getCurrentUser()
      userInfo.value = data.data
      return data.data
    } catch {
      logout()
      return null
    }
  }

  async function logout() {
    try {
      if (token.value) {
        await logoutApi()
      }
    } finally {
      token.value = ''
      refreshToken.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    }
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    permissions,
    roles,
    hasPermission,
    hasRole,
    login,
    fetchUserInfo,
    logout
  }
})
