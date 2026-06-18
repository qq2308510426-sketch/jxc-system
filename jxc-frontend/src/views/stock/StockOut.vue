<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">出库操作</h2>
        <p class="text-sm text-slate-500 mt-0.5">创建商品出库记录</p>
      </div>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 p-6">
      <form @submit.prevent="handleSubmit">
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">客户</label>
            <select v-model="form.customerId" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" required>
              <option value="">请选择客户</option>
              <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">商品</label>
            <select v-model="form.productId" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" required>
              <option value="">请选择商品</option>
              <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }} (库存: {{ p.stock }})</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">数量</label>
            <input v-model.number="form.quantity" type="number" min="1" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" required />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">单价</label>
            <input v-model.number="form.unitPrice" type="number" step="0.01" min="0" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" required />
          </div>
        </div>
        <div v-if="pendingOrders.length > 0" class="mb-4 p-3 bg-amber-50 rounded-lg">
          <p class="text-sm text-amber-700 mb-2">该客户有待处理的订单，是否关联到出库记录？</p>
          <select v-model="form.orderId" class="w-full h-10 px-3 rounded-lg border border-amber-200 text-sm bg-white">
            <option :value="null">不关联订单</option>
            <option v-for="o in pendingOrders" :key="o.id" :value="o.id">{{ o.orderNo }} (¥{{ o.totalAmount }})</option>
          </select>
        </div>
        <button type="submit" :disabled="submitting" class="h-10 px-6 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 disabled:opacity-50">{{ submitting ? '提交中...' : '确认出库' }}</button>
      </form>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const products = ref([])
const customers = ref([])
const pendingOrders = ref([])
const submitting = ref(false)
const form = reactive({ productId: null, customerId: null, warehouseId: null, quantity: 1, unitPrice: 0, orderId: null })

const loadOptions = async () => {
  try {
    const [p, c] = await Promise.all([
      request.get('/product/list', { params: { pageNum: 1, pageSize: 999 } }),
      request.get('/customer/list', { params: { pageNum: 1, pageSize: 999 } })
    ])
    products.value = p.data?.rows || []
    customers.value = c.data?.rows || []
  } catch (e) { /* handled */ }
}

watch(() => form.customerId, async (customerId) => {
  if (!customerId) { pendingOrders.value = []; return }
  try {
    const res = await request.get('/order/pending/' + customerId)
    pendingOrders.value = res.data || []
  } catch (e) { pendingOrders.value = [] }
})

const handleSubmit = async () => {
  if (!form.productId || !form.customerId) { ElMessage.warning('请填写完整信息'); return }
  submitting.value = true
  try {
    await request.post('/stock/out', form)
    ElMessage.success('出库成功')
    Object.assign(form, { productId: null, customerId: null, warehouseId: null, quantity: 1, unitPrice: 0, orderId: null })
  } catch (e) { /* handled */ } finally { submitting.value = false }
}

onMounted(() => loadOptions())
</script>