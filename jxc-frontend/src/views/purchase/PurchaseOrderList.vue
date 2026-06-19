<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-3">
        <select v-model="filters.status" class="h-9 px-3 rounded-lg border border-slate-200 text-sm">
          <option value="">全部状态</option>
          <option value="0">待审批</option>
          <option value="1">已审批</option>
          <option value="2">已收货</option>
          <option value="3">已完成</option>
          <option value="4">已取消</option>
        </select>
        <button @click="loadData" class="h-9 px-4 rounded-lg bg-slate-100 text-sm text-slate-700 hover:bg-slate-200">刷新</button>
      </div>
      <button @click="showCreate = true" class="h-9 px-4 rounded-lg bg-slate-900 text-white text-sm hover:bg-slate-800">+ 新建采购单</button>
    </div>

    <div class="bg-white rounded-xl border border-slate-200">
      <table class="w-full">
        <thead>
          <tr class="border-b border-slate-100">
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">采购单号</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">供应商</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">金额</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">状态</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">付款状态</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">创建时间</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="item in list" :key="item.id" class="hover:bg-slate-50">
            <td class="px-4 py-3 text-sm font-medium">{{ item.orderNo }}</td>
            <td class="px-4 py-3 text-sm">{{ getSupplierName(item.supplierId) }}</td>
            <td class="px-4 py-3 text-sm font-medium">¥{{ item.totalAmount }}</td>
            <td class="px-4 py-3"><span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(item.status)">{{ statusText(item.status) }}</span></td>
            <td class="px-4 py-3"><span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="payStatusClass(item.paymentStatus)">{{ payStatusText(item.paymentStatus) }}</span></td>
            <td class="px-4 py-3 text-sm text-slate-500">{{ formatDate(item.createTime) }}</td>
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <button v-if="item.status === 0" @click="approve(item.id)" class="text-xs text-blue-600 hover:text-blue-800">审批</button>
                <button v-if="item.status === 1" @click="receive(item.id)" class="text-xs text-emerald-600 hover:text-emerald-800">收货</button>
                <button v-if="item.status < 2" @click="cancel(item.id)" class="text-xs text-red-600 hover:text-red-800">取消</button>
              </div>
            </td>
          </tr>
          <tr v-if="!list.length"><td colspan="7" class="px-4 py-8 text-center text-sm text-slate-400">暂无采购单</td></tr>
        </tbody>
      </table>
    </div>

    <el-dialog v-model="showCreate" title="新建采购单" width="600px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">供应商</label>
          <select v-model="form.supplierId" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm">
            <option value="">请选择供应商</option>
            <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">备注</label>
          <el-input v-model="form.remark" placeholder="采购备注" />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">采购明细</label>
          <div class="space-y-2">
            <div v-for="(item, idx) in form.items" :key="idx" class="flex items-center gap-2">
              <select v-model="item.productId" class="flex-1 h-9 px-3 rounded-lg border border-slate-200 text-sm">
                <option value="">选择商品</option>
                <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
              </select>
              <input v-model.number="item.quantity" type="number" min="1" placeholder="数量" class="w-24 h-9 px-3 rounded-lg border border-slate-200 text-sm" />
              <input v-model.number="item.unitPrice" type="number" min="0" step="0.01" placeholder="单价" class="w-28 h-9 px-3 rounded-lg border border-slate-200 text-sm" />
              <button @click="form.items.splice(idx, 1)" class="text-red-500 hover:text-red-700">删除</button>
            </div>
            <button @click="form.items.push({productId:'',quantity:1,unitPrice:0})" class="text-sm text-blue-600 hover:text-blue-800">+ 添加明细</button>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="showCreate = false" class="px-4 py-2 text-sm text-slate-600 hover:text-slate-900">取消</button>
          <button @click="handleCreate" class="px-4 py-2 text-sm bg-slate-900 text-white rounded-lg hover:bg-slate-800">创建</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const list = ref([])
const suppliers = ref([])
const products = ref([])
const showCreate = ref(false)
const filters = reactive({ status: '' })
const form = reactive({ supplierId: '', remark: '', items: [{ productId: '', quantity: 1, unitPrice: 0 }] })

const statusText = (s) => ['待审批', '已审批', '已收货', '已完成', '已取消'][s] || '未知'
const statusClass = (s) => ['bg-amber-50 text-amber-700', 'bg-blue-50 text-blue-700', 'bg-emerald-50 text-emerald-700', 'bg-green-50 text-green-700', 'bg-slate-100 text-slate-600'][s] || ''
const payStatusText = (s) => ['未付', '部分付', '已付清'][s] || '未知'
const payStatusClass = (s) => ['bg-red-50 text-red-700', 'bg-amber-50 text-amber-700', 'bg-emerald-50 text-emerald-700'][s] || ''
const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''
const getSupplierName = (id) => suppliers.value.find(s => s.id === id)?.name || '未知'

const loadData = async () => {
  try {
    const params = { pageNum: 1, pageSize: 50 }
    if (filters.status !== '') params.status = filters.status
    const res = await request.get('/purchase-order/list', { params })
    list.value = res.data?.rows || []
  } catch (e) { console.warn('Load failed:', e.message) }
}

const loadOptions = async () => {
  try {
    const [sRes, pRes] = await Promise.all([
      request.get('/supplier/list', { params: { pageNum: 1, pageSize: 999 } }),
      request.get('/product/list', { params: { pageNum: 1, pageSize: 999 } })
    ])
    suppliers.value = sRes.data?.rows || []
    products.value = pRes.data?.rows || []
  } catch (e) {}
}

const handleCreate = async () => {
  if (!form.supplierId) { ElMessage.warning('请选择供应商'); return }
  if (!form.items.length || form.items.some(i => !i.productId)) { ElMessage.warning('请完善采购明细'); return }
  try {
    await request.post('/purchase-order', form)
    ElMessage.success('采购单创建成功')
    showCreate.value = false
    form.supplierId = ''; form.remark = ''; form.items = [{ productId: '', quantity: 1, unitPrice: 0 }]
    loadData()
  } catch (e) {}
}

const approve = async (id) => {
  try { await request.put(`/purchase-order/${id}/approve`); ElMessage.success('审批成功'); loadData() } catch (e) {}
}
const receive = async (id) => {
  try { await request.put(`/purchase-order/${id}/receive`); ElMessage.success('收货成功'); loadData() } catch (e) {}
}
const cancel = async (id) => {
  try { await request.put(`/purchase-order/${id}/cancel`); ElMessage.success('已取消'); loadData() } catch (e) {}
}

watch(() => filters.status, loadData)
onMounted(() => { loadData(); loadOptions() })
</script>