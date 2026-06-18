<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">退货换货管理</h2>
        <p class="text-sm text-slate-500 mt-0.5">管理退货换货申请</p>
      </div>
      <button @click="showCreateDialog = true" class="h-9 px-4 rounded-lg bg-slate-900 text-white text-sm font-medium hover:bg-slate-800 transition-colors">
        新建退货单
      </button>
    </div>

    <!-- 状态筛选 -->
    <div class="bg-white rounded-xl border border-slate-200 p-4">
      <div class="flex items-center gap-3">
        <select v-model="statusFilter" class="h-10 px-3 rounded-lg border border-slate-200 text-sm">
          <option value="">全部状态</option>
          <option :value="0">待处理</option>
          <option :value="1">已处理</option>
          <option :value="2">已拒绝</option>
        </select>
        <button @click="loadData" class="h-10 px-4 bg-slate-100 text-slate-700 text-sm rounded-lg hover:bg-slate-200">筛选</button>
      </div>
    </div>

    <!-- 退货列表 -->
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-slate-100 bg-slate-50">
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">#</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">退货单号</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">类型</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">退款金额</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">状态</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">创建时间</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">操作</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row, idx) in tableData" :key="row.id" class="hover:bg-slate-50">
              <td class="px-4 py-3 text-sm text-slate-500">{{ idx + 1 }}</td>
              <td class="px-4 py-3 text-sm font-medium">{{ row.returnNo }}</td>
              <td class="px-4 py-3">
                <el-tag :type="row.type === 1 ? 'warning' : 'info'" size="small">
                  {{ row.type === 1 ? '退货' : '换货' }}
                </el-tag>
              </td>
              <td class="px-4 py-3 text-sm">¥{{ row.totalAmount }}</td>
              <td class="px-4 py-3">
                <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
              </td>
              <td class="px-4 py-3 text-sm text-slate-500">{{ row.createTime }}</td>
              <td class="px-4 py-3">
                <div class="flex gap-2">
                  <button v-if="row.status === 0" @click="processReturn(row.id, 1)" class="text-sm text-emerald-600 hover:text-emerald-800">批准</button>
                  <button v-if="row.status === 0" @click="processReturn(row.id, 2)" class="text-sm text-red-600 hover:text-red-800">拒绝</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新建退货对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建退货单" width="600px">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium mb-1.5">类型</label>
            <el-select v-model="form.type" style="width:100%">
              <el-option :value="1" label="退货" />
              <el-option :value="2" label="换货" />
            </el-select>
          </div>
          <div>
            <label class="block text-sm font-medium mb-1.5">客户</label>
            <el-select v-model="form.customerId" filterable style="width:100%">
              <el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
        </div>
        <div>
          <label class="block text-sm font-medium mb-1.5">退货原因</label>
          <el-input v-model="form.reason" type="textarea" rows="2" />
        </div>
        <div>
          <label class="block text-sm font-medium mb-1.5">退货商品</label>
          <div v-for="(item, idx) in form.items" :key="idx" class="flex gap-2 mb-2">
            <el-select v-model="item.productId" filterable placeholder="商品" style="width:200px">
              <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
            </el-select>
            <el-input-number v-model="item.quantity" :min="1" style="width:100px" />
            <el-input-number v-model="item.unitPrice" :min="0" :precision="2" style="width:120px" />
            <button @click="form.items.splice(idx, 1)" class="text-red-500 hover:text-red-700">删除</button>
          </div>
          <button @click="form.items.push({ productId: null, quantity: 1, unitPrice: 0 })" class="text-sm text-blue-600 hover:text-blue-800">+ 添加商品</button>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="showCreateDialog = false" class="px-4 py-2 text-sm text-slate-600">取消</button>
          <button @click="submitReturn" class="px-4 py-2 text-sm bg-slate-900 text-white rounded-lg">提交</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tableData = ref([])
const statusFilter = ref('')
const showCreateDialog = ref(false)
const products = ref([])
const customers = ref([])

const form = reactive({
  type: 1,
  customerId: null,
  reason: '',
  items: [{ productId: null, quantity: 1, unitPrice: 0 }]
})

const loadData = async () => {
  try {
    const params = { pageNum: 1, pageSize: 50 }
    if (statusFilter.value !== '') params.status = statusFilter.value
    const res = await request.get('/return/list', { params })
    tableData.value = res.data.rows || []
  } catch (e) {}
}

const loadOptions = async () => {
  try {
    const [p, c] = await Promise.all([
      request.get('/product/list', { params: { pageNum: 1, pageSize: 999 } }),
      request.get('/customer/list', { params: { pageNum: 1, pageSize: 999 } })
    ])
    products.value = p.data?.rows || []
    customers.value = c.data?.rows || []
  } catch (e) {}
}

const submitReturn = async () => {
  if (!form.customerId) { ElMessage.warning('请选择客户'); return }
  if (!form.items.length || !form.items[0].productId) { ElMessage.warning('请添加退货商品'); return }
  try {
    await request.post('/return', form)
    ElMessage.success('退货单已创建')
    showCreateDialog.value = false
    loadData()
  } catch (e) {}
}

const processReturn = async (id, status) => {
  const action = status === 1 ? '批准' : '拒绝'
  try {
    await ElMessageBox.confirm(`确认${action}该退货单？`, '确认', { type: 'warning' })
    await request.put(`/return/${id}/process?status=${status}`)
    ElMessage.success(`已${action}`)
    loadData()
  } catch (e) {}
}

const getStatusText = (s) => ['待处理', '已处理', '已拒绝'][s] || '未知'
const getStatusType = (s) => ['warning', 'success', 'danger'][s] || 'info'

onMounted(() => { loadData(); loadOptions() })
</script>