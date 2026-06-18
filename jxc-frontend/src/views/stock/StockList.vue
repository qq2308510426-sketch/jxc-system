<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">库存盘点</h2>
        <p class="text-sm text-slate-500 mt-0.5">管理库存盘点和库存调整</p>
      </div>
      <div class="flex gap-2">
        <button @click="showCheckHistory = true" class="h-9 px-4 rounded-lg border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">
          盘点记录
        </button>
        <button @click="startCheck" class="h-9 px-4 rounded-lg bg-slate-900 text-white text-sm font-medium hover:bg-slate-800 transition-colors">
          开始盘点
        </button>
      </div>
    </div>

    <!-- 当前库存列表 -->
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead>
            <tr class="border-b border-slate-100 bg-slate-50">
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">#</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">商品名称</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">规格</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">系统库存</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">安全库存</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">实际库存</th>
              <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">差异</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row,idx) in tableData" :key="row.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-4 py-3 text-sm text-slate-500">{{idx+1}}</td>
              <td class="px-4 py-3 text-sm font-medium text-slate-900">{{row.name}}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{row.spec}}</td>
              <td class="px-4 py-3 text-sm" :class="row.stock<=row.safetyStock?'text-red-600 font-semibold':'text-slate-900'">
                {{row.stock}}
              </td>
              <td class="px-4 py-3 text-sm text-slate-600">{{row.safetyStock}}</td>
              <td class="px-4 py-3">
                <input v-if="checking" v-model.number="row.actualStock" type="number" min="0"
                  class="w-20 h-8 px-2 rounded border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900" />
                <span v-else class="text-sm text-slate-400">-</span>
              </td>
              <td class="px-4 py-3">
                <span v-if="checking && row.actualStock !== undefined && row.actualStock !== null"
                  :class="row.stock - row.actualStock === 0 ? 'text-emerald-600' : 'text-red-600'" class="text-sm font-medium">
                  {{row.stock - row.actualStock}}
                </span>
                <span v-else class="text-sm text-slate-400">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="checking" class="px-4 py-3 border-t border-slate-100 flex justify-end gap-3">
        <button @click="cancelCheck" class="h-9 px-4 rounded-lg border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50">
          取消
        </button>
        <button @click="submitCheck" :disabled="submitting" class="h-9 px-4 rounded-lg bg-emerald-600 text-white text-sm font-medium hover:bg-emerald-700 disabled:opacity-50">
          {{submitting ? '提交中...' : '提交盘点'}}
        </button>
      </div>
    </div>

    <!-- 盘点记录对话框 -->
    <el-dialog v-model="showCheckHistory" title="盘点记录" width="700px">
      <div class="space-y-4">
        <div v-for="check in checkHistory" :key="check.id" class="p-4 border border-slate-200 rounded-lg">
          <div class="flex items-center justify-between mb-2">
            <div>
              <span class="font-medium text-sm">{{check.checkNo}}</span>
              <el-tag :type="check.status === 1 ? 'success' : 'warning'" size="small" class="ml-2">
                {{check.status === 1 ? '已完成' : '进行中'}}
              </el-tag>
            </div>
            <span class="text-xs text-slate-500">{{check.createTime}}</span>
          </div>
          <button v-if="check.status === 0" @click="completeCheck(check.id)"
            class="text-sm text-blue-600 hover:text-blue-800">
            完成盘点（调整库存）
          </button>
        </div>
        <div v-if="!checkHistory.length" class="text-center py-8 text-slate-400 text-sm">
          暂无盘点记录
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const tableData = ref([])
const checking = ref(false)
const submitting = ref(false)
const showCheckHistory = ref(false)
const checkHistory = ref([])

const loadData = async () => {
  try {
    const res = await request.get('/product/list', { params: { pageNum: 1, pageSize: 999 } })
    tableData.value = (res.data.rows || []).map(p => ({ ...p, actualStock: undefined }))
  } catch (e) {
    console.error('Load products failed:', e)
  }
}

const loadCheckHistory = async () => {
  try {
    const res = await request.get('/inventory-check/list', { params: { pageNum: 1, pageSize: 50 } })
    checkHistory.value = res.data.rows || []
  } catch (e) {
    console.error('Load check history failed:', e)
  }
}

const startCheck = () => {
  checking.value = true
  tableData.value.forEach(p => { p.actualStock = undefined })
}

const cancelCheck = () => {
  checking.value = false
  tableData.value.forEach(p => { p.actualStock = undefined })
}

const submitCheck = async () => {
  const items = tableData.value
    .filter(p => p.actualStock !== undefined && p.actualStock !== null)
    .map(p => ({
      productId: p.id,
      systemStock: p.stock,
      actualStock: p.actualStock,
      difference: p.stock - p.actualStock
    }))

  if (items.length === 0) {
    ElMessage.warning('请至少填写一个商品的实际库存')
    return
  }

  try {
    await ElMessageBox.confirm('确认提交盘点结果？提交后将自动调整库存差异。', '确认盘点', { type: 'warning' })
    submitting.value = true
    await request.post('/inventory-check', items)
    ElMessage.success('盘点单已创建')
    checking.value = false
    await loadData()
    await loadCheckHistory()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Submit check failed:', e)
    }
  } finally {
    submitting.value = false
  }
}

const completeCheck = async (checkId) => {
  try {
    await ElMessageBox.confirm('确认完成盘点？将根据盘点结果调整库存。', '完成盘点', { type: 'warning' })
    await request.put(`/inventory-check/${checkId}/complete`)
    ElMessage.success('盘点已完成，库存已调整')
    await loadCheckHistory()
    await loadData()
  } catch (e) {
    if (e !== 'cancel') {
      console.error('Complete check failed:', e)
    }
  }
}

onMounted(() => {
  loadData()
  loadCheckHistory()
})
</script>