import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/api/request'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const realName = ref(localStorage.getItem('realName') || '')
  const role = ref(localStorage.getItem('role') || '')
  const roleLabel = ref(localStorage.getItem('roleLabel') || '')
  const warehouseId = ref(localStorage.getItem('warehouseId') || '')
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))

  const isLoggedIn = computed(() => !!token.value)
  const isSuperAdmin = computed(() => role.value === 'super_admin')
  const displayName = computed(() => realName.value || username.value)

  function hasPermission(perm) {
    if (permissions.value.includes('all')) return true
    return permissions.value.includes(perm)
  }

  function hasAnyPermission(perms) {
    if (permissions.value.includes('all')) return true
    return perms.some(p => permissions.value.includes(p))
  }

  async function setAuth(data) {
    token.value = data.token
    username.value = data.userInfo?.username || ''
    realName.value = data.userInfo?.realName || ''
    role.value = data.userInfo?.role || ''
    roleLabel.value = data.userInfo?.roleLabel || ''
    warehouseId.value = data.userInfo?.warehouseId || ''
    permissions.value = data.userInfo?.permissions || []

    localStorage.setItem('token', token.value)
    localStorage.setItem('username', username.value)
    localStorage.setItem('realName', realName.value)
    localStorage.setItem('role', role.value)
    localStorage.setItem('roleLabel', roleLabel.value)
    localStorage.setItem('warehouseId', warehouseId.value)
    localStorage.setItem('permissions', JSON.stringify(permissions.value))
  }

  async function fetchUserInfo() {
    try {
      const res = await request.get('/user/info')
      const info = res.data
      role.value = info.role
      roleLabel.value = info.roleLabel || ''
      warehouseId.value = info.warehouseId || ''
      permissions.value = info.permissions || []
      localStorage.setItem('role', role.value)
      localStorage.setItem('roleLabel', roleLabel.value)
      localStorage.setItem('warehouseId', warehouseId.value)
      localStorage.setItem('permissions', JSON.stringify(permissions.value))
    } catch (e) {
      console.warn('Failed to fetch user info:', e.message)
    }
  }

  function clearAuth() {
    token.value = ''
    username.value = ''
    realName.value = ''
    role.value = ''
    roleLabel.value = ''
    warehouseId.value = ''
    permissions.value = []

    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
    localStorage.removeItem('role')
    localStorage.removeItem('roleLabel')
    localStorage.removeItem('warehouseId')
    localStorage.removeItem('permissions')
  }

  async function logout() {
    try {
      await request.post('/auth/logout')
    } catch (e) {
      // Ignore errors
    }
    clearAuth()
    router.push('/login')
  }

  return {
    token, username, realName, role, roleLabel, warehouseId, permissions,
    isLoggedIn, isSuperAdmin, displayName,
    hasPermission, hasAnyPermission, setAuth, fetchUserInfo, clearAuth, logout
  }
})