<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div><h2 class="text-lg font-semibold text-slate-900">批次管理</h2><p class="text-sm text-slate-500 mt-0.5">管理商品批次和序列号信息</p></div>
      <button @click="openDialog()" class="px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800">新增批次</button>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 p-4">
      <div class="flex items-center gap-3">
        <select v-model="productFilter" class="h-10 px-3 rounded-lg border border-slate-200 text-sm bg-white">
          <option value="">全部商品</option>
          <option v-for="p in allProducts" :key="p.id" :value="p.id">{{p.name}}</option>
        </select>
        <button @click="loadData()" class="h-10 px-4 bg-slate-100 text-slate-700 text-sm font-medium rounded-lg hover:bg-slate-200">查询</button>
      </div>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <table class="w-full">
        <thead><tr class="border-b border-slate-100 bg-slate-50">
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">#</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">商品</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">批次号</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">序列号</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">数量</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">采购价</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">生产日期</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">过期日期</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">状态</th>
        </tr></thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="(row,idx) in tableData" :key="row.id" class="hover:bg-slate-50">
            <td class="px-4 py-3 text-sm text-slate-500">{{(currentPage-1)*pageSize+idx+1}}</td>
            <td class="px-4 py-3 text-sm font-medium text-slate-900">{{productMap[row.productId]||row.productId}}</td>
            <td class="px-4 py-3 text-sm text-slate-900">{{row.batchNo}}</td>
            <td class="px-4 py-3 text-sm text-slate-600">{{row.serialNo||'-'}}</td>
            <td class="px-4 py-3 text-sm text-slate-900">{{row.quantity}}</td>
            <td class="px-4 py-3 text-sm text-slate-900">{{row.purchasePrice?'¥'+row.purchasePrice:'-'}}</td>
            <td class="px-4 py-3 text-sm text-slate-600">{{row.productionDate||'-'}}</td>
            <td class="px-4 py-3 text-sm" :class="isExpiringSoon(row.expiryDate)?'text-red-600 font-medium':'text-slate-600'">{{row.expiryDate||'-'}}</td>
            <td class="px-4 py-3"><span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="row.status===1?'bg-emerald-50 text-emerald-700':'bg-slate-100 text-slate-600'">{{row.status===1?'正常':'已过期'}}</span></td>
          </tr>
          <tr v-if="!tableData.length"><td colspan="9" class="px-4 py-12 text-center text-sm text-slate-400">暂无数据</td></tr>
        </tbody>
      </table>
      <div class="px-4 py-3 border-t border-slate-100 flex items-center justify-between">
        <span class="text-sm text-slate-500">共 {{total}} 条</span>
        <div class="flex items-center gap-1"><button v-for="p in totalPages" :key="p" @click="loadData(p)" class="w-8 h-8 rounded-lg text-sm font-medium" :class="p===currentPage?'bg-slate-900 text-white':'text-slate-600 hover:bg-slate-100'">{{p}}</button></div>
      </div>
    </div>
    <Teleport to="body">
      <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="dialogVisible=false"></div>
        <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 p-6 space-y-4">
          <h3 class="text-lg font-semibold">新增批次</h3>
          <div><label class="block text-sm font-medium mb-1">商品 *</label><select v-model="form.productId" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm bg-white"><option v-for="p in allProducts" :key="p.id" :value="p.id">{{p.name}}</option></select></div>
          <div class="grid grid-cols-2 gap-4"><div><label class="block text-sm font-medium mb-1">批次号 *</label><input v-model="form.batchNo" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div><div><label class="block text-sm font-medium mb-1">序列号</label><input v-model="form.serialNo" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div></div>
          <div class="grid grid-cols-2 gap-4"><div><label class="block text-sm font-medium mb-1">数量</label><input v-model.number="form.quantity" type="number" min="1" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div><div><label class="block text-sm font-medium mb-1">采购价</label><input v-model.number="form.purchasePrice" type="number" min="0" step="0.01" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div></div>
          <div class="grid grid-cols-2 gap-4"><div><label class="block text-sm font-medium mb-1">生产日期</label><input v-model="form.productionDate" type="date" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div><div><label class="block text-sm font-medium mb-1">过期日期</label><input v-model="form.expiryDate" type="date" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div></div>
          <div class="flex justify-end gap-3 pt-4 border-t"><button @click="dialogVisible=false" class="h-9 px-4 rounded-lg border text-sm">取消</button><button @click="handleSubmit" class="h-9 px-4 rounded-lg bg-slate-900 text-white text-sm">确定</button></div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
const tableData = ref([]); const total = ref(0); const currentPage = ref(1); const pageSize = ref(10); const dialogVisible = ref(false)
const allProducts = ref([]); const productFilter = ref(''); const productMap = ref({})
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const form = reactive({ productId:null, batchNo:'', serialNo:'', quantity:1, purchasePrice:0, productionDate:'', expiryDate:'', supplierId:null })
const isExpiringSoon = (d) => { if(!d) return false; return new Date(d) < new Date(Date.now()+30*86400000) }
const loadData = async (page=1) => {
  currentPage.value=page
  try {
    const params = {pageNum:page, pageSize:pageSize.value}
    if(productFilter.value) params.productId = productFilter.value
    const res = await request.get('/product-batch/list',{params})
    tableData.value=res.data?.rows||[]; total.value=res.data?.total||0
  } catch(e){}
}
const loadProducts = async () => {
  try {
    const res = await request.get('/product/list',{params:{pageNum:1,pageSize:999}})
    allProducts.value = res.data?.rows||[]
    allProducts.value.forEach(p => productMap.value[p.id] = p.name)
  } catch(e){}
}
const openDialog = () => { Object.assign(form,{productId:null,batchNo:'',serialNo:'',quantity:1,purchasePrice:0,productionDate:'',expiryDate:'',supplierId:null}); dialogVisible.value=true }
const handleSubmit = async () => {
  if(!form.productId||!form.batchNo) { ElMessage.warning('请填写必填项'); return }
  try { await request.post('/product-batch',form); ElMessage.success('创建成功'); dialogVisible.value=false; loadData() } catch(e){}
}
onMounted(() => { loadData(); loadProducts() })
</script>
