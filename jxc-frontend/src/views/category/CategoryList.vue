<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between"><div><h2 class="text-lg font-semibold text-slate-900">分类管理</h2><p class="text-sm text-slate-500 mt-0.5">管理商品分类（支持树形结构）</p></div><button @click="openDialog()" class="inline-flex items-center gap-2 px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors"><svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>新增分类</button></div>
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <el-table :data="tableData" row-key="id" :tree-props="{ children: 'children' }" style="width: 100%">
        <el-table-column label="#" width="80"><template #default="scope">{{ scope.$index + 1 }}</template></el-table-column>
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right"><template #default="{ row }"><div class="flex items-center gap-2"><button @click="openDialog(row)" class="text-sm text-slate-600 hover:text-slate-900">编辑</button><button @click="handleDelete(row.id)" class="text-sm text-red-500 hover:text-red-700">删除</button></div></template></el-table-column>
      </el-table>
    </div>
    <Teleport to="body"><div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center"><div class="absolute inset-0 bg-black/50" @click="dialogVisible=false"></div><div class="relative bg-white rounded-xl shadow-xl w-full max-w-md mx-4"><div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between"><h3 class="text-base font-semibold text-slate-900">{{form.id?'编辑分类':'新增分类'}}</h3><button @click="dialogVisible=false" class="text-slate-400 hover:text-slate-600"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" /></svg></button></div><div class="p-6 space-y-4"><div><label class="block text-sm font-medium text-slate-700 mb-1.5">分类名称 <span class="text-red-500">*</span></label><input v-model="form.name" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent" /></div><div><label class="block text-sm font-medium text-slate-700 mb-1.5">父分类</label><select v-model="form.parentId" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent bg-white"><option :value="0">无（顶级分类）</option><option v-for="c in flatCategories" :key="c.id" :value="c.id">{{c.name}}</option></select></div><div><label class="block text-sm font-medium text-slate-700 mb-1.5">排序</label><input v-model.number="form.sort" type="number" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-slate-900 focus:border-transparent" /></div></div><div class="px-6 py-4 border-t border-slate-100 flex items-center justify-end gap-3"><button @click="dialogVisible=false" class="h-9 px-4 rounded-lg border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">取消</button><button @click="handleSubmit" :disabled="submitting" class="h-9 px-4 rounded-lg bg-slate-900 text-white text-sm font-medium hover:bg-slate-800 disabled:opacity-50 transition-colors">{{submitting?'保存中...':'确定'}}</button></div></div></div></Teleport>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/api/request'
const tableData = ref([]); const dialogVisible = ref(false); const submitting = ref(false)
const form = reactive({ id: null, name: '', parentId: 0, sort: 0 })
const flatCategories = computed(() => { const flat = []; const walk = (list) => { list.forEach(c => { flat.push(c); if(c.children) walk(c.children) }) }; walk(tableData.value); return flat })
const loadData = async () => { try { const res = await request.get('/category/list'); tableData.value = res.data || [] } catch(e){} }
const openDialog = (row) => { if(row) Object.assign(form,row); else Object.assign(form,{id:null,name:'',parentId:0,sort:0}); dialogVisible.value = true }
const handleSubmit = async () => { if(!form.name) return; submitting.value = true; try { if(form.id) await request.put('/category',form); else await request.post('/category',form); dialogVisible.value = false; loadData() } catch(e){} finally { submitting.value = false } }
const handleDelete = async (id) => { if(!confirm('确定删除？')) return; try { await request.delete('/category/'+id); loadData() } catch(e){} }
onMounted(() => loadData())
</script>
