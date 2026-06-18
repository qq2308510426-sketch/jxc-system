<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">订单管理</h2>
        <p class="text-sm text-slate-500 mt-0.5">管理所有销售订单</p>
      </div>
      <button @click="router.push('/order/create')" class="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/></svg>
        创建订单
      </button>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 p-4">
      <div class="flex items-center gap-3">
        <select v-model="statusFilter" class="h-10 px-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 bg-white">
          <option value="">全部状态</option>
          <option :value="0">待处理</option>
          <option :value="1">已确认</option>
          <option :value="2">发货中</option>
          <option :value="3">已完成</option>
          <option :value="4">已取消</option>
        </select>
        <button @click="loadData" class="h-10 px-4 bg-slate-100 text-slate-700 text-sm font-medium rounded-lg hover:bg-slate-200">筛选</button>
      </div>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead><tr class="border-b border-slate-100 bg-slate-50">
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">#</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">订单号</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">客户</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">金额</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">状态</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">创建时间</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">操作</th>
          </tr></thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row,idx) in tableData" :key="row.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-4 py-3 text-sm text-slate-500">{{(currentPage-1)*pageSize+idx+1}}</td>
              <td class="px-4 py-3 text-sm font-medium text-slate-900">{{row.orderNo}}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{row.customerName}}</td>
              <td class="px-4 py-3 text-sm text-slate-900 font-medium">&yen;{{row.totalAmount?.toFixed(2)}}</td>
              <td class="px-4 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(row.status)">{{statusText(row.status)}}</span>
              </td>
              <td class="px-4 py-3 text-sm text-slate-500">{{row.createTime?.slice(0,10)}}</td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <button @click="router.push('/order/'+row.id)" class="text-sm text-slate-600 hover:text-slate-900">详情</button>
                </div>
              </td>
            </tr>
            <tr v-if="!tableData.length">
              <td colspan="7" class="px-4 py-12 text-center text-sm text-slate-400">暂无订单</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-4 py-3 border-t border-slate-100 flex items-center justify-between">
        <span class="text-sm text-slate-500">共 {{total}} 条</span>
        <div class="flex items-center gap-1">
          <button v-for="p in totalPages" :key="p" @click="handlePageChange(p)" class="w-8 h-8 rounded-lg text-sm font-medium transition-colors" :class="p===currentPage?'bg-slate-900 text-white':'text-slate-600 hover:bg-slate-100'">{{p}}</button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'

const router = useRouter()
const statusFilter = ref('')
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const statusText = (s) => ['待处理','已确认','发货中','已完成','已取消'][s]||'未知'
const statusClass = (s) => ['bg-amber-50 text-amber-700','bg-blue-50 text-blue-700','bg-purple-50 text-purple-700','bg-emerald-50 text-emerald-700','bg-slate-100 text-slate-600'][s]||''

const loadData = async () => {
  try {
    const params = { pageNum: currentPage.value, pageSize: pageSize.value }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const [orderRes, custRes] = await Promise.all([
      request.get('/order/list', { params }),
      request.get('/customer/list', { params: { pageNum: 1, pageSize: 999 } })
    ])
    const customerMap = {}
    ;(custRes.data?.rows||[]).forEach(c => { customerMap[c.id] = c.name })
    tableData.value = (orderRes.data.rows||[]).map(o => ({...o, customerName: customerMap[o.customerId]||'未知客户'}))
    total.value = orderRes.data.total || 0
  } catch(e) {}
}

const handlePageChange = (p) => { currentPage.value = p; loadData() }
onMounted(() => loadData())
</script>