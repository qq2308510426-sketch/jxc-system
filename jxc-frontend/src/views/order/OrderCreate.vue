<template>
  <div class="space-y-6">
    <div><h2 class="text-lg font-semibold text-slate-900">创建订单</h2><p class="text-sm text-slate-500 mt-0.5">新建销售订单</p></div>
    <div class="bg-white rounded-xl border border-slate-200 p-6 max-w-2xl">
      <div class="space-y-4 mb-6">
        <div><label class="block text-sm font-medium text-slate-700 mb-1.5">客户 <span class="text-red-500">*</span></label><el-select v-model="form.customerId" filterable placeholder="请选择客户" style="width:100%"><el-option v-for="c in customers" :key="c.id" :label="c.name" :value="c.id" /></el-select></div>
        <div><label class="block text-sm font-medium text-slate-700 mb-1.5">备注</label><textarea v-model="form.remark" rows="2" class="w-full px-3 py-2 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent resize-none" placeholder="请输入订单备注"></textarea></div>
      </div>
      <div class="border-t border-slate-100 pt-4 mb-4"><h3 class="text-sm font-semibold text-slate-900">订单明细</h3></div>
      <div v-for="(item, index) in form.items" :key="index" class="flex items-center gap-3 mb-3 p-3 bg-slate-50 rounded-lg">
        <el-select v-model="item.productId" filterable placeholder="选择商品" style="width:200px"><el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" /></el-select>
        <el-input-number v-model="item.quantity" :min="1" style="width:120px" />
        <el-input-number v-model="item.unitPrice" :min="0" :precision="2" style="width:140px" />
        <span class="text-sm text-slate-600 min-w-[100px]">小计: ¥{{(item.quantity*item.unitPrice).toFixed(2)}}</span>
        <button @click="form.items.splice(index,1)" :disabled="form.items.length<=1" class="text-sm text-red-500 hover:text-red-700 disabled:opacity-30">删除</button>
      </div>
      <button @click="addItem" class="text-sm text-slate-600 hover:text-slate-900 mb-4">+ 添加商品</button>
      <div class="border-t border-slate-100 pt-4 flex items-center justify-between">
        <span class="text-lg font-bold text-slate-900">合计: ¥{{totalAmount}}</span>
        <div class="flex items-center gap-3"><button @click="router.back()" class="h-9 px-4 rounded-lg border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">取消</button><button @click="handleSubmit" :disabled="submitting" class="h-9 px-6 rounded-lg bg-slate-900 text-white text-sm font-medium hover:bg-slate-800 disabled:opacity-50 transition-colors">{{submitting?'提交中...':'提交订单'}}</button></div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
const router = useRouter(); const submitting = ref(false); const customers = ref([]); const products = ref([])
const form = reactive({ customerId: null, remark: '', items: [{ productId: null, quantity: 1, unitPrice: 0 }] })
const totalAmount = computed(() => form.items.reduce((sum, i) => sum + (i.quantity || 0) * (i.unitPrice || 0), 0).toFixed(2))
const addItem = () => form.items.push({ productId: null, quantity: 1, unitPrice: 0 })
const loadOptions = async () => { try { const [c,p] = await Promise.all([request.get('/customer/list',{params:{pageNum:1,pageSize:999}}), request.get('/product/list',{params:{pageNum:1,pageSize:999}})]); customers.value = c.data?.rows||[]; products.value = p.data?.rows||[] } catch(e){} }
const handleSubmit = async () => { if(!form.customerId) return ElMessage.warning('请选择客户'); if(!form.items.some(i=>i.productId)) return ElMessage.warning('请至少添加一个商品'); submitting.value=true; try { await request.post('/order',form); ElMessage.success('订单创建成功'); router.push('/order') } catch(e){} finally { submitting.value=false } }
onMounted(() => loadOptions())
</script>

