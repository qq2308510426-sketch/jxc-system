<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">销售报表</h2>
        <p class="text-sm text-slate-500 mt-0.5">查看销售数据统计</p>
      </div>
      <button @click="exportCsv" class="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
        导出CSV
      </button>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 p-4">
      <div class="flex items-center gap-3">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width:360px" />
        <button @click="loadData" class="h-10 px-4 bg-slate-100 text-slate-700 text-sm font-medium rounded-lg hover:bg-slate-200 transition-colors">查询</button>
      </div>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-slate-100 bg-slate-50">
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">日期</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">订单数</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">销售额</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">成本额</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">利润</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="row in tableData" :key="row.period" class="hover:bg-slate-50 transition-colors">
              <td class="px-4 py-3 text-sm text-slate-900">{{row.period}}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{row.orderCount}}</td>
              <td class="px-4 py-3 text-sm text-slate-900">{{row.totalSales}}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{row.totalCost}}</td>
              <td class="px-4 py-3 text-sm font-medium" :class="row.profit>=0?'text-emerald-600':'text-red-600'">{{row.profit}}</td>
            </tr>
            <tr v-if="!tableData.length">
              <td colspan="5" class="px-4 py-12 text-center text-sm text-slate-400">暂无数据</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '@/api/request'

const dateRange = ref([])
const tableData = ref([])

const loadData = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return
  }
  try {
    const res = await request.get('/report/sales', {
      params: { startDate: dateRange.value[0], endDate: dateRange.value[1] }
    })
    tableData.value = res.data || []
  } catch(e) {}
}

const exportCsv = async () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    return
  }
  try {
    const res = await request.get('/report/sales/export', {
      params: { startDate: dateRange.value[0], endDate: dateRange.value[1] },
      responseType: 'blob'
    })
    const url = URL.createObjectURL(res instanceof Blob ? res : new Blob([res]))
    const a = document.createElement('a')
    a.href = url
    a.download = 'sales_report.csv'
    a.click()
  } catch(e) {}
}
</script>