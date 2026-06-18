<template>
  <div class="space-y-6">
    <div>
      <h2 class="text-lg font-semibold text-slate-900">数据导入导出</h2>
      <p class="text-sm text-slate-500 mt-0.5">批量导入导出商品、客户、供应商数据</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- 商品 -->
      <div class="bg-white rounded-xl border border-slate-200 p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center">
            <svg class="w-5 h-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
            </svg>
          </div>
          <h3 class="font-semibold text-slate-900">商品数据</h3>
        </div>
        <div class="space-y-3">
          <a href="/api/data/export/products" download class="block w-full h-10 rounded-lg bg-slate-100 text-slate-700 text-sm font-medium hover:bg-slate-200 transition-colors text-center leading-10">
            导出商品CSV
          </a>
          <div>
            <input type="file" ref="productFile" accept=".csv" class="hidden" @change="handleProductImport" />
            <button @click="$refs.productFile.click()" class="w-full h-10 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 transition-colors">
              导入商品CSV
            </button>
          </div>
        </div>
      </div>

      <!-- 客户 -->
      <div class="bg-white rounded-xl border border-slate-200 p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-lg bg-emerald-100 flex items-center justify-center">
            <svg class="w-5 h-5 text-emerald-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
            </svg>
          </div>
          <h3 class="font-semibold text-slate-900">客户数据</h3>
        </div>
        <div class="space-y-3">
          <a href="/api/data/export/customers" download class="block w-full h-10 rounded-lg bg-slate-100 text-slate-700 text-sm font-medium hover:bg-slate-200 transition-colors text-center leading-10">
            导出客户CSV
          </a>
          <div>
            <input type="file" ref="customerFile" accept=".csv" class="hidden" @change="handleCustomerImport" />
            <button @click="$refs.customerFile.click()" class="w-full h-10 rounded-lg bg-emerald-600 text-white text-sm font-medium hover:bg-emerald-700 transition-colors">
              导入客户CSV
            </button>
          </div>
        </div>
      </div>

      <!-- 供应商 -->
      <div class="bg-white rounded-xl border border-slate-200 p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-lg bg-purple-100 flex items-center justify-center">
            <svg class="w-5 h-5 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
            </svg>
          </div>
          <h3 class="font-semibold text-slate-900">供应商数据</h3>
        </div>
        <div class="space-y-3">
          <a href="/api/data/export/suppliers" download class="block w-full h-10 rounded-lg bg-slate-100 text-slate-700 text-sm font-medium hover:bg-slate-200 transition-colors text-center leading-10">
            导出供应商CSV
          </a>
          <div>
            <input type="file" ref="supplierFile" accept=".csv" class="hidden" @change="handleSupplierImport" />
            <button @click="$refs.supplierFile.click()" class="w-full h-10 rounded-lg bg-purple-600 text-white text-sm font-medium hover:bg-purple-700 transition-colors">
              导入供应商CSV
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- CSV格式说明 -->
    <div class="bg-white rounded-xl border border-slate-200 p-6">
      <h3 class="font-semibold text-slate-900 mb-4">CSV格式说明</h3>
      <div class="space-y-4 text-sm text-slate-600">
        <div>
          <p class="font-medium text-slate-700 mb-2">商品CSV格式：</p>
          <code class="block bg-slate-50 p-3 rounded-lg text-xs">ID,商品名称,规格,单价,库存,安全库存</code>
        </div>
        <div>
          <p class="font-medium text-slate-700 mb-2">客户CSV格式：</p>
          <code class="block bg-slate-50 p-3 rounded-lg text-xs">ID,客户名称,联系人,电话,地址</code>
        </div>
        <div>
          <p class="font-medium text-slate-700 mb-2">供应商CSV格式：</p>
          <code class="block bg-slate-50 p-3 rounded-lg text-xs">ID,供应商名称,联系人,电话,地址</code>
        </div>
        <p class="text-amber-600">注意：导入时第一行为表头，将自动跳过。ID列可留空。</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const productFile = ref(null)
const customerFile = ref(null)
const supplierFile = ref(null)

const uploadFile = async (type, file) => {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await request.post(`/data/import/${type}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success(res.data || '导入成功')
  } catch (e) {
    ElMessage.error('导入失败: ' + (e.response?.data?.message || e.message))
  }
}

const handleProductImport = (e) => {
  if (e.target.files[0]) uploadFile('products', e.target.files[0])
}

const handleCustomerImport = (e) => {
  if (e.target.files[0]) uploadFile('customers', e.target.files[0])
}

const handleSupplierImport = (e) => {
  if (e.target.files[0]) uploadFile('suppliers', e.target.files[0])
}
</script>