<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-lg font-semibold text-slate-900">出入库记录</h2>
      <p class="text-sm text-slate-500 mt-0.5">查看所有入库和出库操作记录</p>
    </div>
    <div class="flex gap-2">
      <button @click="activeTab = 'in'" class="px-4 py-2 rounded-lg text-sm font-medium transition-colors" :class="activeTab === 'in' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'">入库记录</button>
      <button @click="activeTab = 'out'" class="px-4 py-2 rounded-lg text-sm font-medium transition-colors" :class="activeTab === 'out' ? 'bg-slate-900 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'">出库记录</button>
    </div>
    <div v-if="activeTab === 'in'" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead><tr class="border-b border-slate-100 bg-slate-50">
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">#</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">商品名称</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">供应商</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">数量</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">单价</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">金额</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">操作人</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">入库时间</th>
          </tr></thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row, idx) in stockInData" :key="row.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-4 py-3 text-sm text-slate-500">{{ (currentPage - 1) * pageSize + idx + 1 }}</td>
              <td class="px-4 py-3 text-sm font-medium text-slate-900">{{ row.productName }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.supplierName }}</td>
              <td class="px-4 py-3 text-sm text-emerald-600 font-medium">+{{ row.quantity }}</td>
              <td class="px-4 py-3 text-sm text-slate-900">&yen;{{ row.unitPrice?.toFixed(2) }}</td>
              <td class="px-4 py-3 text-sm text-slate-900 font-medium">&yen;{{ (row.quantity * (row.unitPrice || 0)).toFixed(2) }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.operatorName }}</td>
              <td class="px-4 py-3 text-sm text-slate-500">{{ row.createTime?.slice(0, 16).replace('T', ' ') }}</td>
            </tr>
            <tr v-if="!stockInData.length"><td colspan="8" class="px-4 py-12 text-center text-sm text-slate-400">暂无入库记录</td></tr>
          </tbody>
        </table>
      </div>
      <div class="px-4 py-3 border-t border-slate-100 flex items-center justify-between">
        <span class="text-sm text-slate-500">共 {{ stockInTotal }} 条</span>
        <div class="flex items-center gap-1">
          <button v-for="p in stockInTotalPages" :key="p" @click="loadStockIn(p)" class="w-8 h-8 rounded-lg text-sm font-medium transition-colors" :class="p === currentPage ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'">{{ p }}</button>
        </div>
      </div>
    </div>
    <div v-if="activeTab === 'out'" class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead><tr class="border-b border-slate-100 bg-slate-50">
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">#</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">商品名称</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">客户</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">关联订单</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">数量</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">单价</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">金额</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">操作人</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">出库时间</th>
          </tr></thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row, idx) in stockOutData" :key="row.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-4 py-3 text-sm text-slate-500">{{ (outPage - 1) * pageSize + idx + 1 }}</td>
              <td class="px-4 py-3 text-sm font-medium text-slate-900">{{ row.productName }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.customerName || '-' }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.orderNo || '-' }}</td>
              <td class="px-4 py-3 text-sm text-red-600 font-medium">-{{ row.quantity }}</td>
              <td class="px-4 py-3 text-sm text-slate-900">&yen;{{ row.unitPrice?.toFixed(2) }}</td>
              <td class="px-4 py-3 text-sm text-slate-900 font-medium">&yen;{{ (row.quantity * (row.unitPrice || 0)).toFixed(2) }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.operatorName }}</td>
              <td class="px-4 py-3 text-sm text-slate-500">{{ row.createTime?.slice(0, 16).replace('T', ' ') }}</td>
            </tr>
            <tr v-if="!stockOutData.length"><td colspan="9" class="px-4 py-12 text-center text-sm text-slate-400">暂无出库记录</td></tr>
          </tbody>
        </table>
      </div>
      <div class="px-4 py-3 border-t border-slate-100 flex items-center justify-between">
        <span class="text-sm text-slate-500">共 {{ stockOutTotal }} 条</span>
        <div class="flex items-center gap-1">
          <button v-for="p in stockOutTotalPages" :key="p" @click="loadStockOut(p)" class="w-8 h-8 rounded-lg text-sm font-medium transition-colors" :class="p === outPage ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'">{{ p }}</button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import request from '@/api/request'

const activeTab = ref('in')
const currentPage = ref(1)
const outPage = ref(1)
const pageSize = ref(10)
const stockInData = ref([])
const stockInTotal = ref(0)
const stockOutData = ref([])
const stockOutTotal = ref(0)
const stockInTotalPages = computed(() => Math.max(1, Math.ceil(stockInTotal.value / pageSize.value)))
const stockOutTotalPages = computed(() => Math.max(1, Math.ceil(stockOutTotal.value / pageSize.value)))

const loadStockIn = async (page = 1) => {
  currentPage.value = page
  try {
    const [stockRes, prodRes, suppRes, userRes] = await Promise.all([
      request.get('/stock/in/list', { params: { pageNum: page, pageSize: pageSize.value } }),
      request.get('/product/list', { params: { pageNum: 1, pageSize: 999 } }),
      request.get('/supplier/list', { params: { pageNum: 1, pageSize: 999 } }),
      request.get('/user/list', { params: { pageNum: 1, pageSize: 999 } })
    ])
    const productMap = {}
    ;(prodRes.data?.rows || []).forEach(p => { productMap[p.id] = p.name })
    const supplierMap = {}
    ;(suppRes.data?.rows || []).forEach(s => { supplierMap[s.id] = s.name })
    const userMap = {}
    ;(userRes.data?.rows || []).forEach(u => { userMap[u.id] = u.realName || u.username })
    stockInData.value = (stockRes.data?.rows || []).map(r => ({
      ...r,
      productName: productMap[r.productId] || '未知商品',
      supplierName: supplierMap[r.supplierId] || '未知供应商',
      operatorName: userMap[r.operatorId] || '未知用户'
    }))
    stockInTotal.value = stockRes.data?.total || 0
  } catch (e) { /* handled */ }
}

const loadStockOut = async (page = 1) => {
  outPage.value = page
  try {
    const [stockRes, prodRes, custRes, orderRes, userRes] = await Promise.all([
      request.get('/stock/out/list', { params: { pageNum: page, pageSize: pageSize.value } }),
      request.get('/product/list', { params: { pageNum: 1, pageSize: 999 } }),
      request.get('/customer/list', { params: { pageNum: 1, pageSize: 999 } }),
      request.get('/order/list', { params: { pageNum: 1, pageSize: 999 } }),
      request.get('/user/list', { params: { pageNum: 1, pageSize: 999 } })
    ])
    const productMap = {}
    ;(prodRes.data?.rows || []).forEach(p => { productMap[p.id] = p.name })
    const customerMap = {}
    ;(custRes.data?.rows || []).forEach(c => { customerMap[c.id] = c.name })
    const orderMap = {}
    ;(orderRes.data?.rows || []).forEach(o => { orderMap[o.id] = o.orderNo })
    const userMap = {}
    ;(userRes.data?.rows || []).forEach(u => { userMap[u.id] = u.realName || u.username })
    stockOutData.value = (stockRes.data?.rows || []).map(r => ({
      ...r,
      productName: productMap[r.productId] || '未知商品',
      customerName: customerMap[r.customerId] || null,
      orderNo: orderMap[r.orderId] || null,
      operatorName: userMap[r.operatorId] || '未知用户'
    }))
    stockOutTotal.value = stockRes.data?.total || 0
  } catch (e) { /* handled */ }
}

watch(activeTab, (tab) => {
  if (tab === 'in') loadStockIn(1)
  else loadStockOut(1)
})

onMounted(() => loadStockIn())
</script>