<template>
  <div class="space-y-6">
    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <div v-for="stat in dashboard.stats" :key="stat.label" class="bg-white rounded-xl border border-slate-200 p-5">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-500">{{ stat.label }}</p>
            <p class="text-2xl font-bold text-slate-900 mt-1">{{ stat.value }}</p>
          </div>
          <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="stat.bgClass">
            <span class="text-lg">{{ stat.icon }}</span>
          </div>
        </div>
        <p class="text-xs text-slate-400 mt-3">{{ stat.desc }}</p>
      </div>
    </div>

    <!-- Charts -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <div class="bg-white rounded-xl border border-slate-200 p-6">
        <h3 class="text-sm font-semibold text-slate-900 mb-4">近7天销售趋势</h3>
        <div class="h-64"><canvas ref="salesChart"></canvas></div>
      </div>
      <div class="bg-white rounded-xl border border-slate-200 p-6">
        <h3 class="text-sm font-semibold text-slate-900 mb-4">库存状态分布</h3>
        <div class="h-64"><canvas ref="stockChart"></canvas></div>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="bg-white rounded-xl border border-slate-200 p-6">
      <h3 class="text-sm font-semibold text-slate-900 mb-4">快捷操作</h3>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
        <router-link to="/stock/in" class="flex items-center gap-3 p-3 rounded-lg border border-slate-100 hover:bg-slate-50 transition-colors">
          <div class="w-9 h-9 rounded-lg bg-emerald-50 flex items-center justify-center"><span class="text-emerald-600">+</span></div>
          <div><p class="text-sm font-medium text-slate-900">入库操作</p><p class="text-xs text-slate-400">新增入库记录</p></div>
        </router-link>
        <router-link to="/stock/out" class="flex items-center gap-3 p-3 rounded-lg border border-slate-100 hover:bg-slate-50 transition-colors">
          <div class="w-9 h-9 rounded-lg bg-rose-50 flex items-center justify-center"><span class="text-rose-600">-</span></div>
          <div><p class="text-sm font-medium text-slate-900">出库操作</p><p class="text-xs text-slate-400">新增出库记录</p></div>
        </router-link>
        <router-link to="/order/create" class="flex items-center gap-3 p-3 rounded-lg border border-slate-100 hover:bg-slate-50 transition-colors">
          <div class="w-9 h-9 rounded-lg bg-blue-50 flex items-center justify-center"><span class="text-blue-600">O</span></div>
          <div><p class="text-sm font-medium text-slate-900">创建订单</p><p class="text-xs text-slate-400">新建销售订单</p></div>
        </router-link>
        <router-link to="/report/sales" class="flex items-center gap-3 p-3 rounded-lg border border-slate-100 hover:bg-slate-50 transition-colors">
          <div class="w-9 h-9 rounded-lg bg-purple-50 flex items-center justify-center"><span class="text-purple-600">R</span></div>
          <div><p class="text-sm font-medium text-slate-900">销售报表</p><p class="text-xs text-slate-400">查看销售数据</p></div>
        </router-link>
      </div>
    </div>

    <!-- Recent Orders -->
    <div class="bg-white rounded-xl border border-slate-200 p-6">
      <h3 class="text-sm font-semibold text-slate-900 mb-4">最近订单</h3>
      <table class="w-full">
        <thead>
          <tr class="border-b border-slate-100">
            <th class="px-4 py-2 text-left text-xs font-medium text-slate-500">订单号</th>
            <th class="px-4 py-2 text-left text-xs font-medium text-slate-500">客户</th>
            <th class="px-4 py-2 text-left text-xs font-medium text-slate-500">金额</th>
            <th class="px-4 py-2 text-left text-xs font-medium text-slate-500">状态</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="o in dashboard.recentOrders" :key="o.id" class="hover:bg-slate-50">
            <td class="px-4 py-2 text-sm font-medium">{{ o.orderNo }}</td>
            <td class="px-4 py-2 text-sm">{{ o.customerName }}</td>
            <td class="px-4 py-2 text-sm font-medium">¥{{ o.totalAmount }}</td>
            <td class="px-4 py-2">
              <span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(o.status)">
                {{ statusText(o.status) }}
              </span>
            </td>
          </tr>
          <tr v-if="!dashboard.recentOrders.length">
            <td colspan="4" class="px-4 py-8 text-center text-sm text-slate-400">暂无订单</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Stock Warnings -->
    <div class="bg-white rounded-xl border border-slate-200 p-6">
      <h3 class="text-sm font-semibold text-slate-900 mb-4">库存预警</h3>
      <table class="w-full">
        <thead>
          <tr class="border-b border-slate-100">
            <th class="px-4 py-2 text-left text-xs font-medium text-slate-500">商品</th>
            <th class="px-4 py-2 text-left text-xs font-medium text-slate-500">当前库存</th>
            <th class="px-4 py-2 text-left text-xs font-medium text-slate-500">安全库存</th>
            <th class="px-4 py-2 text-left text-xs font-medium text-slate-500">状态</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="item in dashboard.stockWarnings" :key="item.productId" class="hover:bg-slate-50">
            <td class="px-4 py-2 text-sm font-medium">{{ item.productName }}</td>
            <td class="px-4 py-2 text-sm text-red-600 font-semibold">{{ item.currentStock }}</td>
            <td class="px-4 py-2 text-sm">{{ item.safetyStock }}</td>
            <td class="px-4 py-2">
              <span class="px-2 py-0.5 rounded-full text-xs font-medium bg-rose-50 text-rose-700">库存不足</span>
            </td>
          </tr>
          <tr v-if="!dashboard.stockWarnings.length">
            <td colspan="4" class="px-4 py-8 text-center text-sm text-slate-400">库存充足，无预警</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Loading State -->
    <div v-if="dashboard.loading" class="flex items-center justify-center py-8">
      <div class="flex items-center gap-3 text-slate-500">
        <svg class="w-5 h-5 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
        </svg>
        <span class="text-sm">加载中...</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import { useDashboardStore } from '@/stores/dashboard'

Chart.register(...registerables)

const dashboard = useDashboardStore()
const salesChart = ref(null)
const stockChart = ref(null)

const statusText = (s) => ['待处理', '已确认', '发货中', '已完成', '已取消'][s] || '未知'
const statusClass = (s) => ['bg-amber-50 text-amber-700', 'bg-blue-50 text-blue-700', 'bg-purple-50 text-purple-700', 'bg-emerald-50 text-emerald-700', 'bg-slate-100 text-slate-600'][s] || ''

const renderCharts = () => {
  if (salesChart.value && dashboard.salesData.length) {
    new Chart(salesChart.value, {
      type: 'line',
      data: {
        labels: dashboard.salesData.map(s => s.period?.slice(5)),
        datasets: [
          {
            label: '销售额',
            data: dashboard.salesData.map(s => s.totalSales),
            borderColor: 'rgb(99,102,241)',
            backgroundColor: 'rgba(99,102,241,0.1)',
            fill: true,
            tension: 0.4
          },
          {
            label: '利润',
            data: dashboard.salesData.map(s => s.profit),
            borderColor: 'rgb(16,185,129)',
            backgroundColor: 'rgba(16,185,129,0.1)',
            fill: true,
            tension: 0.4
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } }
      }
    })
  }

  if (stockChart.value) {
    const totalProducts = dashboard.stats[0]?.value || 1
    const warningCount = dashboard.stockWarnings.length
    new Chart(stockChart.value, {
      type: 'doughnut',
      data: {
        labels: ['库存充足', '库存预警'],
        datasets: [{
          data: [Math.max(0, totalProducts - warningCount), warningCount],
          backgroundColor: ['rgb(16,185,129)', 'rgb(239,68,68)']
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false
      }
    })
  }
}

onMounted(async () => {
  await dashboard.loadData()
  await nextTick()
  renderCharts()
})
</script>
