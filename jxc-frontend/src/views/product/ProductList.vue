<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">商品管理</h2>
        <p class="text-sm text-slate-500 mt-0.5">管理所有在售商品信息</p>
      </div>
      <button @click="openDialog()" class="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"/></svg>
        新增商品
      </button>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 p-4">
      <div class="flex items-center gap-3">
        <input v-model="keyword" placeholder="搜索商品名称..." class="flex-1 h-10 px-4 rounded-lg border border-slate-200 text-sm" @keyup.enter="loadData" />
        <button @click="loadData" class="h-10 px-4 bg-slate-100 text-slate-700 text-sm font-medium rounded-lg hover:bg-slate-200">搜索</button>
      </div>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full">
          <thead><tr class="border-b border-slate-100 bg-slate-50">
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">#</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">商品名称</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">规格</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">售价</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">库存</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">可用库存</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">成本价</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">安全库存</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">状态</th>
            <th class="px-4 py-3 text-left text-xs font-medium text-slate-500 uppercase">操作</th>
          </tr></thead>
          <tbody class="divide-y divide-slate-50">
            <tr v-for="(row, idx) in tableData" :key="row.id" class="hover:bg-slate-50 transition-colors">
              <td class="px-4 py-3 text-sm text-slate-500">{{ (currentPage - 1) * pageSize + idx + 1 }}</td>
              <td class="px-4 py-3 text-sm font-medium text-slate-900">{{ row.name }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.spec }}</td>
              <td class="px-4 py-3 text-sm text-slate-900">{{ row.price }}</td>
              <td class="px-4 py-3 text-sm" :class="row.stock <= row.safetyStock ? 'text-red-600 font-semibold' : 'text-slate-900'">{{ row.stock }}</td>
              <td class="px-4 py-3 text-sm" :class="(row.stock - (row.reservedStock||0)) <= row.safetyStock ? 'text-red-600 font-semibold' : 'text-slate-900'">{{ row.stock - (row.reservedStock||0) }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.costPrice ? '¥' + row.costPrice : '-' }}</td>
              <td class="px-4 py-3 text-sm text-slate-600">{{ row.safetyStock }}</td>
              <td class="px-4 py-3">
                <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="row.status === 1 ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-600'">{{ row.status === 1 ? '启用' : '禁用' }}</span>
              </td>
              <td class="px-4 py-3">
                <div class="flex items-center gap-2">
                  <button @click="openDialog(row)" class="text-sm text-slate-600 hover:text-slate-900">编辑</button>
                  <button @click="handleDelete(row.id)" class="text-sm text-red-600 hover:text-red-800">删除</button>
                </div>
              </td>
            </tr>
            <tr v-if="!tableData.length"><td colspan="10" class="px-4 py-12 text-center text-sm text-slate-400">暂无商品</td></tr>
          </tbody>
        </table>
      </div>
      <div class="px-4 py-3 border-t border-slate-100 flex items-center justify-between">
        <span class="text-sm text-slate-500">共 {{ total }} 条</span>
        <div class="flex items-center gap-1">
          <button v-for="p in totalPages" :key="p" @click="handlePageChange(p)" class="w-8 h-8 rounded-lg text-sm font-medium transition-colors" :class="p === currentPage ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100'">{{ p }}</button>
        </div>
      </div>
    </div>

    <Teleport to="body">
      <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/40" @click="dialogVisible = false"></div>
        <div class="relative bg-white rounded-2xl shadow-xl w-full max-w-2xl mx-4">
          <div class="px-6 py-4 border-b border-slate-100">
            <h3 class="text-lg font-semibold text-slate-900">{{ form.id ? '编辑商品' : '新增商品' }}</h3>
          </div>
          <div class="px-6 py-4 space-y-4">
            <div class="grid grid-cols-2 gap-4">
              <div><label class="block text-sm font-medium text-slate-700 mb-1.5">商品名称</label><input v-model="form.name" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div>
              <div><label class="block text-sm font-medium text-slate-700 mb-1.5">规格</label><input v-model="form.spec" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div>
              <div><label class="block text-sm font-medium text-slate-700 mb-1.5">售价</label><input v-model.number="form.price" type="number" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div>
              <div><label class="block text-sm font-medium text-slate-700 mb-1.5">安全库存</label><input v-model.number="form.safetyStock" type="number" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div>
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">分类</label>
                <select v-model="form.categoryId" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm">
                  <option value="">-- 请选择 --</option>
                  <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                </select>
              </div>
              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1.5">供应商</label>
                <select v-model="form.supplierId" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm">
                  <option value="">-- 请选择 --</option>
                  <option v-for="s in suppliers" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1.5">状态</label>
              <div class="flex items-center gap-3">
                <button @click="form.status = form.status === 1 ? 0 : 1" :class="form.status === 1 ? 'bg-emerald-500' : 'bg-slate-300'" class="relative inline-flex h-6 w-11 items-center rounded-full transition-colors">
                  <span :class="form.status === 1 ? 'translate-x-6' : 'translate-x-1'" class="inline-block h-4 w-4 transform rounded-full bg-white transition-transform"></span>
                </button>
                <span class="text-sm text-slate-500">{{ form.status === 1 ? '启用' : '禁用' }}</span>
              </div>
            </div>
          </div>
          <div class="px-6 py-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <button @click="dialogVisible = false" class="h-9 px-4 rounded-lg border border-slate-200 text-sm text-slate-600 hover:bg-slate-50">取消</button>
            <button @click="handleSubmit" :disabled="submitting" class="h-9 px-4 rounded-lg bg-slate-900 text-white text-sm font-medium hover:bg-slate-800 disabled:opacity-50">{{ submitting ? '提交中...' : '确定' }}</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/api/request'

const keyword = ref('')
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const submitting = ref(false)
const categories = ref([])
const suppliers = ref([])
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const form = reactive({ id: null, name: '', categoryId: null, spec: '', price: 0, safetyStock: 0, supplierId: null, status: 1 })

const loadData = async () => {
  try {
    const res = await request.get('/product/list', { params: { keyword: keyword.value, pageNum: currentPage.value, pageSize: pageSize.value } })
    tableData.value = res.data.rows || []
    total.value = res.data.total || 0
  } catch (err) { /* handled */ }
}

const loadOptions = async () => {
  try {
    const [cRes, sRes] = await Promise.all([request.get('/category/list'), request.get('/supplier/list')])
    categories.value = cRes.data || []
    suppliers.value = sRes.data.rows || sRes.data || []
  } catch (err) { /* handled */ }
}

const openDialog = (row) => {
  if (row) { Object.assign(form, row) } else { Object.assign(form, { id: null, name: '', categoryId: null, spec: '', price: 0, safetyStock: 0, supplierId: null, status: 1 }) }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!form.name) return
  submitting.value = true
  try {
    if (form.id) { await request.put('/product', form) } else { await request.post('/product', form) }
    dialogVisible.value = false
    loadData()
  } catch (err) { /* handled */ } finally { submitting.value = false }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除该商品吗？')) return
  try { await request.delete('/product/' + id); loadData() } catch (err) { /* handled */ }
}

const handlePageChange = (page) => { currentPage.value = page; loadData() }
onMounted(() => { loadData(); loadOptions() })
</script>