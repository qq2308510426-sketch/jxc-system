import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api/request'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)
  const loading = ref(false)
  let pollTimer = null

  async function fetchUnreadCount() {
    try {
      loading.value = true
      const res = await request.get('/notification/unread-count')
      unreadCount.value = res.data?.count || 0
    } catch (e) {
      console.warn('Failed to fetch unread count:', e.message)
    } finally {
      loading.value = false
    }
  }

  function startPolling(intervalMs = 300000) {
    stopPolling()
    fetchUnreadCount()
    pollTimer = setInterval(fetchUnreadCount, intervalMs)
  }

  function stopPolling() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
  }

  return {
    unreadCount,
    loading,
    fetchUnreadCount,
    startPolling,
    stopPolling
  }
})
