<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">库存报表</h2>
        <p class="text-sm text-slate-500 mt-0.5">查看库存统计信息</p>
      </div>
      <button @click="exportCsv" class="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
        导出CSV
      </button>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-slate-100 bg-slate-50">
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">商品名称</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">当前库存</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">安全库存</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">库存状态</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="row in tableData" :key="row.productName" class="hover:bg-slate-50 transition-colors">
              <td class="px-4 py-3 text-sm font-medium text-slate-900">{{row.productName}}</td>
              <td class="px-4 py-3 text-sm" :class="row.currentStock<=row.safetyStock?'text-red-600 font-semibold':'text-slate-900'">{{row.currentStock}}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{row.safetyStock}}</td>
              <td class="px-4 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="row.currentStock<=row.safetyStock?'bg-rose-50 text-rose-700':'bg-emerald-50 text-emerald-700'">
                  {{row.currentStock<=row.safetyStock?'预警':'正常'}}
                </span>
              </td>
            </tr>
            <tr v-if="!tableData.length">
              <td colspan="4" class="px-4 py-12 text-center text-sm text-slate-400">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import request from '@/api/request'

const tableData = ref([])

const loadData = async () => {
  try {
    const res = await request.get('/report/stock')
    tableData.value = res.data || []
  } catch(e) {}
}

const exportCsv = async () => {
  try {
    const res = await request.get('/data/export/products', { responseType: 'blob' })
    const url = URL.createObjectURL(res instanceof Blob ? res : new Blob([res]))
    const a = document.createElement('a')
    a.href = url
    a.download = 'stock_report.csv'
    a.click()
  } catch(e) {}
}

onMounted(() => loadData())
</script>