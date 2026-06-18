<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-lg font-semibold text-slate-900">付款记录</h2>
      <p class="text-sm text-slate-500 mt-0.5">查看所有付款流水记录</p>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-slate-100 bg-slate-50">
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">#</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">付款流水号</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">订单ID</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">付款金额</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">付款方式</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">备注</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">付款时间</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row, idx) in tableData" :key="row.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-4 py-3 text-sm text-slate-500">{{ (currentPage - 1) * pageSize + idx + 1 }}</td>
              <td class="px-4 py-3 text-sm font-medium text-slate-900">{{ row.paymentNo }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.orderId }}</td>
              <td class="px-4 py-3 text-sm text-emerald-600 font-medium">¥{{ row.amount?.toFixed(2) }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ methodText(row.paymentMethod) }}</td>
              <td class="px-4 py-3 text-sm text-slate-500">{{ row.remark || '-' }}</td>
              <td class="px-4 py-3 text-sm text-slate-500">{{ row.createTime?.slice(0, 16).replace('T', ' ') }}</td>
            </tr>
            <tr v-if="!tableData.length">
              <td colspan="7" class="px-4 py-12 text-center text-sm text-slate-400">暂无付款记录</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="px-4 py-3 border-t border-slate-100 flex items-center justify-between">
        <span class="text-sm text-slate-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-1">
          <button v-for="p in totalPages" :key="p" @click="handlePageChange(p)" 
            class="w-8 h-8 rounded-lg text-sm font-medium transition-colors" 
            :class="p === currentPage ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'">{{ p }}</button>
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
const pageSize = ref(10)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const methodText = (m) => ({ cash: '现金', bank: '银行转账', wechat: '微信', alipay: '支付宝' }[m] || m)

const loadData = async (page = 1) => {
  currentPage.value = page
  try {
    const res = await request.get('/payment/list', { params: { pageNum: page, pageSize: pageSize.value } })
    tableData.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } catch (e) {}
}

const handlePageChange = (p) => loadData(p)
onMounted(() => loadData())
</script>