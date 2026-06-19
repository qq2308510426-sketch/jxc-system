<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-lg font-semibold text-slate-900">操作日志</h2>
      <p class="text-sm text-slate-500 mt-0.5">查看系统操作记录</p>
    </div>

    <!-- 筛选条件 -->
    <div class="bg-white rounded-xl border border-slate-200 p-4">
      <div class="flex items-center gap-3 flex-wrap">
        <input v-model="filters.action" placeholder="搜索操作类型..." 
          class="h-10 px-4 rounded-lg border border-slate-200 text-sm placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-slate-900" />
        <input v-model="filters.startDate" type="date" class="h-10 px-3 rounded-lg border border-slate-200 text-sm" />
        <span class="text-slate-400">至</span>
        <input v-model="filters.endDate" type="date" class="h-10 px-3 rounded-lg border border-slate-200 text-sm" />
        <button @click="loadData" class="h-10 px-4 bg-slate-100 text-slate-700 text-sm font-medium rounded-lg hover:bg-slate-200 transition-colors">筛选</button>
        <button @click="resetFilters" class="h-10 px-4 text-slate-500 text-sm hover:text-slate-700">重置</button>
      </div>
    </div>

    <!-- 日志列表 -->
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-slate-100 bg-slate-50">
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">#</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">操作类型</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">操作详情</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">IP地址</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">操作时间</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row, idx) in tableData" :key="row.id" class="hover:bg-slate-50">
              <td class="px-4 py-3 text-sm text-slate-500">{{ (currentPage-1)*pageSize+idx+1 }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="getActionClass(row.action)">
                  {{ row.action }}
                </span>
              </td>
              <td class="px-4 py-3 text-sm text-slate-600 max-w-md truncate" :title="row.detail">
                {{ row.detail || '-' }}
              </td>
              <td class="px-4 py-3 text-sm text-slate-500">{{ row.ip }}</td>
              <td class="px-4 py-3 text-sm text-slate-500">{{ row.createTime }}</td>
            </tr>
            <tr v-if="!tableData.length">
              <td colspan="5" class="px-4 py-12 text-center text-sm text-slate-400">暂无日志</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-4 py-3 border-t border-slate-100 flex items-center justify-between">
        <span class="text-sm text-slate-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-1">
          <button @click="goPage(currentPage - 1)" :disabled="currentPage <= 1" class="w-8 h-8 rounded-lg text-sm font-medium transition-colors text-slate-600 hover:bg-slate-100 disabled:opacity-30">&lt;</button>
          <template v-for="p in visiblePages" :key="p">
            <span v-if="p === '...'" class="w-8 h-8 flex items-center justify-center text-sm text-slate-400">...</span>
            <button v-else @click="goPage(p)"
              class="w-8 h-8 rounded-lg text-sm font-medium transition-colors"
              :class="p === currentPage ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'">
              {{ p }}
            </button>
          </template>
          <button @click="goPage(currentPage + 1)" :disabled="currentPage >= totalPages" class="w-8 h-8 rounded-lg text-sm font-medium transition-colors text-slate-600 hover:bg-slate-100 disabled:opacity-30">&gt;</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/api/request'

const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(15)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value
  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    pages.push(1)
    if (current > 3) pages.push('...')
    for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
      pages.push(i)
    }
    if (current < total - 2) pages.push('...')
    pages.push(total)
  }
  return pages
})
const goPage = (p) => { if (p >= 1 && p <= totalPages.value) { currentPage.value = p; loadData() } }

const filters = ref({
  action: '',
  startDate: '',
  endDate: ''
})

const loadData = async () => {
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    }
    if (filters.value.action) params.action = filters.value.action
    if (filters.value.startDate) params.startDate = filters.value.startDate
    if (filters.value.endDate) params.endDate = filters.value.endDate

    const res = await request.get('/log/list', { params })
    tableData.value = res.data.rows || []
    total.value = res.data.total || 0
  } catch (e) {
    console.error('Load logs failed:', e)
  }
}

const resetFilters = () => {
  filters.value = { action: '', startDate: '', endDate: '' }
  currentPage.value = 1
  loadData()
}

const getActionClass = (action) => {
  if (action.includes('创建') || action.includes('新增')) return 'bg-emerald-50 text-emerald-700'
  if (action.includes('删除')) return 'bg-red-50 text-red-700'
  if (action.includes('修改') || action.includes('更新')) return 'bg-blue-50 text-blue-700'
  if (action.includes('登录')) return 'bg-purple-50 text-purple-700'
  return 'bg-slate-100 text-slate-600'
}

onMounted(() => loadData())
</script>
