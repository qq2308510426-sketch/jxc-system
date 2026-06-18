<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">消息中心</h2>
        <p class="text-sm text-slate-500 mt-0.5">查看系统通知和预警信息</p>
      </div>
      <button @click="markAllAsRead" class="h-9 px-4 rounded-lg border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">
        全部标为已读
      </button>
    </div>

    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="divide-y divide-slate-100">
        <div v-for="item in notifications" :key="item.id" 
             :class="['p-4 hover:bg-slate-50 transition-colors cursor-pointer', item.isRead === 0 ? 'bg-blue-50' : '']"
             @click="markAsRead(item)">
          <div class="flex items-start justify-between">
            <div class="flex-1">
              <div class="flex items-center gap-2">
                <span v-if="item.isRead === 0" class="w-2 h-2 rounded-full bg-blue-500"></span>
                <h3 class="text-sm font-medium text-slate-900">{{item.title}}</h3>
              </div>
              <p class="text-sm text-slate-600 mt-1">{{item.content}}</p>
              <p class="text-xs text-slate-400 mt-2">{{item.createTime}}</p>
            </div>
            <el-tag :type="getTagType(item.type)" size="small">{{getTypeLabel(item.type)}}</el-tag>
          </div>
        </div>
        <div v-if="!notifications.length" class="p-12 text-center text-slate-400 text-sm">
          暂无通知
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const notifications = ref([])

const loadNotifications = async () => {
  try {
    const res = await request.get('/notification/list', { params: { pageNum: 1, pageSize: 100 } })
    notifications.value = res.data.rows || []
  } catch (e) {
    console.error('Load notifications failed:', e)
  }
}

const markAsRead = async (item) => {
  if (item.isRead === 0) {
    try {
      await request.put(`/notification/${item.id}/read`)
      item.isRead = 1
    } catch (e) {
      console.error('Mark as read failed:', e)
    }
  }
}

const markAllAsRead = async () => {
  try {
    await request.put('/notification/read-all')
    notifications.value.forEach(n => { n.isRead = 1 })
  } catch (e) {
    console.error('Mark all as read failed:', e)
  }
}

const getTagType = (type) => {
  const types = { 'STOCK_LOW': 'danger', 'ORDER_PENDING': 'warning' }
  return types[type] || 'info'
}

const getTypeLabel = (type) => {
  const labels = { 'STOCK_LOW': '库存预警', 'ORDER_PENDING': '订单提醒' }
  return labels[type] || '系统通知'
}

onMounted(() => loadNotifications())
</script>