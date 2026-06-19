import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/api/request'

export const useDashboardStore = defineStore('dashboard', () => {
  const stats = ref([])
  const recentOrders = ref([])
  const stockWarnings = ref([])
  const salesData = ref([])
  const loading = ref(false)
  const error = ref(null)

  async function loadData() {
    loading.value = true
    error.value = null
    try {
      const [productRes, supplierRes, orderRes, stockRes, salesRes] = await Promise.all([
        request.get('/product/list', { params: { pageNum: 1, pageSize: 1 } }),
        request.get('/supplier/list', { params: { pageNum: 1, pageSize: 1 } }),
        request.get('/order/list', { params: { pageNum: 1, pageSize: 5 } }),
        request.get('/report/stock'),
        request.get('/report/sales', {
          params: {
            startDate: new Date(Date.now() - 6 * 86400000).toISOString().slice(0, 10),
            endDate: new Date().toISOString().slice(0, 10)
          }
        })
      ])

      const totalStockValue = (stockRes.data || []).reduce((sum, s) => sum + (s.currentStock || 0), 0)
      const totalProfit = (salesRes.data || []).reduce((sum, s) => sum + (s.profit || 0), 0)

      stats.value = [
        { label: '商品总数', value: productRes.data?.total || 0, icon: '📦', bgClass: 'bg-blue-50', desc: '所有在售商品' },
        { label: '供应商', value: supplierRes.data?.total || 0, icon: '🤝', bgClass: 'bg-emerald-50', desc: '合作供应商数量' },
        { label: '库存总量', value: totalStockValue, icon: '📊', bgClass: 'bg-amber-50', desc: '所有商品库存数量' },
        { label: '累计利润', value: '¥' + totalProfit.toFixed(2), icon: '💰', bgClass: 'bg-purple-50', desc: '近7天销售利润' }
      ]

      // Load customer names for order display
      const custRes = await request.get('/customer/list', { params: { pageNum: 1, pageSize: 999 } })
      const customerMap = {}
      ;(custRes.data?.rows || []).forEach(c => { customerMap[c.id] = c.name })

      recentOrders.value = (orderRes.data?.rows || []).map(o => ({
        ...o,
        customerName: customerMap[o.customerId] || '未知客户'
      }))

      stockWarnings.value = (stockRes.data || []).filter(s => s.warning)
      salesData.value = salesRes.data || []
    } catch (e) {
      error.value = e.message
      console.warn('Dashboard load failed:', e.message)
    } finally {
      loading.value = false
    }
  }

  return {
    stats,
    recentOrders,
    stockWarnings,
    salesData,
    loading,
    error,
    loadData
  }
})
