<template>
  <div class="space-y-6">
    <div><h2 class="text-lg font-semibold text-slate-900">入库操作</h2><p class="text-sm text-slate-500 mt-0.5">新增商品入库记录</p></div>
    <div class="bg-white rounded-xl border border-slate-200 p-6 max-w-xl">
      <el-form ref="formRef" :model="form" :rules="formRules" label-position="top">
        <div class="space-y-4">
          <div><label class="block text-sm font-medium text-slate-700 mb-1.5">商品 <span class="text-red-500">*</span></label><el-select v-model="form.productId" filterable placeholder="请选择商品" style="width:100%"><el-option v-for="p in products" :key="p.id" :label="p.name + ' (库存: ' + p.stock + ')'" :value="p.id" /></el-select></div>
          <div><label class="block text-sm font-medium text-slate-700 mb-1.5">供应商 <span class="text-red-500">*</span></label><el-select v-model="form.supplierId" filterable placeholder="请选择供应商" style="width:100%"><el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" /></el-select></div>
          <div><label class="block text-sm font-medium text-slate-700 mb-1.5">入库仓库</label><el-select v-model="form.warehouseId" filterable clearable placeholder="请选择仓库（可选）" style="width:100%"><el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" /></el-select></div>
          <div class="grid grid-cols-2 gap-4"><div><label class="block text-sm font-medium text-slate-700 mb-1.5">数量 <span class="text-red-500">*</span></label><el-input-number v-model="form.quantity" :min="1" style="width:100%" /></div><div><label class="block text-sm font-medium text-slate-700 mb-1.5">单价 <span class="text-red-500">*</span></label><el-input-number v-model="form.unitPrice" :min="0" :precision="2" style="width:100%" /></div></div>
          <div class="grid grid-cols-2 gap-4"><div><label class="block text-sm font-medium text-slate-700 mb-1.5">批次号</label><el-input v-model="form.batchNo" placeholder="可选" /></div><div><label class="block text-sm font-medium text-slate-700 mb-1.5">序列号</label><el-input v-model="form.serialNo" placeholder="可选" /></div></div>
          <div class="pt-2"><button @click="handleSubmit" :disabled="submitting" class="h-10 px-6 rounded-lg bg-slate-900 text-white text-sm font-medium hover:bg-slate-800 disabled:opacity-50 transition-colors">{{submitting?'提交中...':'确认入库'}}</button></div>
        </div>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
const formRef = ref(null); const submitting = ref(false); const products = ref([]); const suppliers = ref([]); const warehouses = ref([])
const form = reactive({ productId: null, supplierId: null, warehouseId: null, quantity: 1, unitPrice: 0, batchNo: '', serialNo: '' })
const formRules = { productId:[{required:true,message:'请选择商品',trigger:'change'}], supplierId:[{required:true,message:'请选择供应商',trigger:'change'}] }
const loadOptions = async () => { try { const [p,s,w] = await Promise.all([request.get('/product/list',{params:{pageNum:1,pageSize:999}}), request.get('/supplier/list',{params:{pageNum:1,pageSize:999}}), request.get('/warehouse/all')]); products.value = p.data?.rows||[]; suppliers.value = s.data?.rows||[]; warehouses.value = w.data||[] } catch(e){} }
const handleSubmit = () => { formRef.value.validate(async(v) => { if(!v) return; submitting.value=true; try { await request.post('/stock/in',form); ElMessage.success('入库成功'); Object.assign(form,{productId:null,supplierId:null,warehouseId:null,quantity:1,unitPrice:0,batchNo:'',serialNo:''}) } catch(e){} finally { submitting.value=false } }) }
onMounted(() => loadOptions())
</script>