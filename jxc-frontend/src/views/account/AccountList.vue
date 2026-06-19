<template>
  <div class="space-y-6">
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div class="bg-white rounded-xl border border-slate-200 p-5">
        <p class="text-sm text-slate-500">应收总额</p>
        <p class="text-2xl font-bold text-emerald-600 mt-1">¥{{ stats.outstandingReceivable?.toFixed(2) || '0.00' }}</p>
        <p class="text-xs text-slate-400 mt-2">已收: ¥{{ stats.totalReceived?.toFixed(2) || '0.00' }}</p>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-5">
        <p class="text-sm text-slate-500">应付总额</p>
        <p class="text-2xl font-bold text-red-600 mt-1">¥{{ stats.outstandingPayable?.toFixed(2) || '0.00' }}</p>
        <p class="text-xs text-slate-400 mt-2">已付: ¥{{ stats.totalPaid?.toFixed(2) || '0.00' }}</p>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-5">
        <p class="text-sm text-slate-500">逾期应收</p>
        <p class="text-2xl font-bold text-amber-600 mt-1">{{ stats.overdueReceivableCount || 0 }} 笔</p>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-5">
        <p class="text-sm text-slate-500">逾期应付</p>
        <p class="text-2xl font-bold text-amber-600 mt-1">{{ stats.overduePayableCount || 0 }} 笔</p>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex gap-1 bg-slate-100 p-1 rounded-lg w-fit">
      <button @click="activeTab = 'receivable'" class="px-4 py-2 rounded-md text-sm font-medium transition-colors" :class="activeTab === 'receivable' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600'">应收账款</button>
      <button @click="activeTab = 'payable'" class="px-4 py-2 rounded-md text-sm font-medium transition-colors" :class="activeTab === 'payable' ? 'bg-white text-slate-900 shadow-sm' : 'text-slate-600'">应付账款</button>
    </div>

    <!-- Receivable Table -->
    <div v-if="activeTab === 'receivable'" class="bg-white rounded-xl border border-slate-200">
      <table class="w-full">
        <thead>
          <tr class="border-b border-slate-100">
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">客户</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">应收总额</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">已收</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">未收</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">逾期笔数</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="item in receivables" :key="item.customerId" class="hover:bg-slate-50">
            <td class="px-4 py-3 text-sm font-medium">{{ item.customerName }}</td>
            <td class="px-4 py-3 text-sm">¥{{ item.totalAmount?.toFixed(2) }}</td>
            <td class="px-4 py-3 text-sm text-emerald-600">¥{{ item.paidAmount?.toFixed(2) }}</td>
            <td class="px-4 py-3 text-sm font-medium text-red-600">¥{{ item.outstanding?.toFixed(2) }}</td>
            <td class="px-4 py-3"><span v-if="item.overdueCount > 0" class="px-2 py-0.5 rounded-full text-xs font-medium bg-red-50 text-red-700">{{ item.overdueCount }} 笔</span><span v-else class="text-xs text-slate-400">无</span></td>
            <td class="px-4 py-3">
              <button @click="showPayDialog('receivable', item)" class="text-xs text-blue-600 hover:text-blue-800">收款</button>
            </td>
          </tr>
          <tr v-if="!receivables.length"><td colspan="6" class="px-4 py-8 text-center text-sm text-slate-400">暂无应收账款</td></tr>
        </tbody>
      </table>
    </div>

    <!-- Payable Table -->
    <div v-if="activeTab === 'payable'" class="bg-white rounded-xl border border-slate-200">
      <table class="w-full">
        <thead>
          <tr class="border-b border-slate-100">
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">供应商</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">应付总额</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">已付</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">未付</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">逾期笔数</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="item in payables" :key="item.supplierId" class="hover:bg-slate-50">
            <td class="px-4 py-3 text-sm font-medium">{{ item.supplierName }}</td>
            <td class="px-4 py-3 text-sm">¥{{ item.totalAmount?.toFixed(2) }}</td>
            <td class="px-4 py-3 text-sm text-emerald-600">¥{{ item.paidAmount?.toFixed(2) }}</td>
            <td class="px-4 py-3 text-sm font-medium text-red-600">¥{{ item.outstanding?.toFixed(2) }}</td>
            <td class="px-4 py-3"><span v-if="item.overdueCount > 0" class="px-2 py-0.5 rounded-full text-xs font-medium bg-red-50 text-red-700">{{ item.overdueCount }} 笔</span><span v-else class="text-xs text-slate-400">无</span></td>
            <td class="px-4 py-3">
              <button @click="showPayDialog('payable', item)" class="text-xs text-blue-600 hover:text-blue-800">付款</button>
            </td>
          </tr>
          <tr v-if="!payables.length"><td colspan="6" class="px-4 py-8 text-center text-sm text-slate-400">暂无应付账款</td></tr>
        </tbody>
      </table>
    </div>

    <!-- Payment Dialog -->
    <el-dialog v-model="showPay" :title="payType === 'receivable' ? '收款确认' : '付款确认'" width="400px">
      <div class="space-y-4">
        <p class="text-sm text-slate-600">{{ payType === 'receivable' ? '客户' : '供应商' }}: <span class="font-medium">{{ payTarget?.customerName || payTarget?.supplierName }}</span></p>
        <p class="text-sm text-slate-600">未结金额: <span class="font-medium text-red-600">¥{{ payTarget?.outstanding?.toFixed(2) }}</span></p>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">{{ payType === 'receivable' ? '收款' : '付款' }}金额</label>
          <el-input v-model.number="payAmount" type="number" :max="payTarget?.outstanding" placeholder="请输入金额" />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="showPay = false" class="px-4 py-2 text-sm text-slate-600">取消</button>
          <button @click="handlePay" class="px-4 py-2 text-sm bg-slate-900 text-white rounded-lg hover:bg-slate-800">确认</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const activeTab = ref('receivable')
const stats = ref({})
const receivables = ref([])
const payables = ref([])
const showPay = ref(false)
const payType = ref('')
const payTarget = ref(null)
const payAmount = ref(0)

const loadData = async () => {
  try {
    const [statsRes, recRes, payRes] = await Promise.all([
      request.get('/account/stats'),
      request.get('/account/receivable/summary'),
      request.get('/account/payable/summary')
    ])
    stats.value = statsRes.data || {}
    receivables.value = recRes.data || []
    payables.value = payRes.data || []
  } catch (e) { console.warn('Load failed:', e.message) }
}

const showPayDialog = (type, item) => {
  payType.value = type
  payTarget.value = item
  payAmount.value = item.outstanding
  showPay.value = true
}

const handlePay = async () => {
  if (!payAmount.value || payAmount.value <= 0) { ElMessage.warning('请输入有效金额'); return }
  try {
    const isReceivable = payType.value === 'receivable'
    const endpoint = isReceivable ? '/account/receivable/pay' : '/account/payable/pay'
    const payload = { amount: payAmount.value }
    if (isReceivable) payload.customerId = payTarget.value.customerId
    else payload.supplierId = payTarget.value.supplierId
    await request.post(endpoint, payload)
    ElMessage.success(isReceivable ? '收款成功' : '付款成功')
    showPay.value = false
    loadData()
  } catch (e) {}
}

onMounted(loadData)
</script>