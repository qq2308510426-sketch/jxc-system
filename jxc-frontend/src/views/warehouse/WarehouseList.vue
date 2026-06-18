<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div><h2 class="text-lg font-semibold text-slate-900">仓库管理</h2><p class="text-sm text-slate-500 mt-0.5">管理所有仓库信息</p></div>
      <button @click="openDialog()" class="px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800">新增仓库</button>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 overflow-hidden">
      <table class="w-full">
        <thead><tr class="border-b border-slate-100 bg-slate-50">
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">#</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">仓库名称</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">地址</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">联系人</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">电话</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">状态</th>
          <th class="px-4 py-3 text-left text-xs font-medium text-slate-500">操作</th>
        </tr></thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="(row,idx) in tableData" :key="row.id" class="hover:bg-slate-50">
            <td class="px-4 py-3 text-sm text-slate-500">{{(currentPage-1)*pageSize+idx+1}}</td>
            <td class="px-4 py-3 text-sm font-medium text-slate-900">{{row.name}}</td>
            <td class="px-4 py-3 text-sm text-slate-600">{{row.address||'-'}}</td>
            <td class="px-4 py-3 text-sm text-slate-600">{{row.contact||'-'}}</td>
            <td class="px-4 py-3 text-sm text-slate-600">{{row.phone||'-'}}</td>
            <td class="px-4 py-3"><span class="px-2 py-0.5 rounded-full text-xs font-medium" :class="row.status===1?'bg-emerald-50 text-emerald-700':'bg-slate-100 text-slate-600'">{{row.status===1?'启用':'禁用'}}</span></td>
            <td class="px-4 py-3"><div class="flex gap-2"><button @click="openDialog(row)" class="text-sm text-slate-600 hover:text-slate-900">编辑</button><button @click="handleDelete(row.id)" class="text-sm text-red-500 hover:text-red-700">删除</button></div></td>
          </tr>
          <tr v-if="!tableData.length"><td colspan="7" class="px-4 py-12 text-center text-sm text-slate-400">暂无数据</td></tr>
        </tbody>
      </table>
    </div>
    <Teleport to="body">
      <div v-if="dialogVisible" class="fixed inset-0 z-50 flex items-center justify-center">
        <div class="absolute inset-0 bg-black/50" @click="dialogVisible=false"></div>
        <div class="relative bg-white rounded-xl shadow-xl w-full max-w-lg mx-4 p-6 space-y-4">
          <h3 class="text-lg font-semibold">{{form.id?'编辑仓库':'新增仓库'}}</h3>
          <div><label class="block text-sm font-medium mb-1">仓库名称 *</label><input v-model="form.name" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div>
          <div><label class="block text-sm font-medium mb-1">地址</label><input v-model="form.address" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div>
          <div class="grid grid-cols-2 gap-4"><div><label class="block text-sm font-medium mb-1">联系人</label><input v-model="form.contact" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div><div><label class="block text-sm font-medium mb-1">电话</label><input v-model="form.phone" class="w-full h-10 px-3 rounded-lg border border-slate-200 text-sm" /></div></div>
          <div class="flex justify-end gap-3 pt-4 border-t"><button @click="dialogVisible=false" class="h-9 px-4 rounded-lg border text-sm">取消</button><button @click="handleSubmit" class="h-9 px-4 rounded-lg bg-slate-900 text-white text-sm">确定</button></div>
        </div>
      </div>
    </Teleport>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '@/api/request'
const tableData = ref([]); const total = ref(0); const currentPage = ref(1); const pageSize = ref(10); const dialogVisible = ref(false)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const form = reactive({ id:null, name:'', address:'', contact:'', phone:'', status:1, remark:'' })
const loadData = async (page=1) => { currentPage.value=page; try { const res = await request.get('/warehouse/list',{params:{pageNum:page,pageSize:pageSize.value}}); tableData.value=res.data?.rows||[]; total.value=res.data?.total||0 } catch(e){} }
const openDialog = (row) => { if(row) Object.assign(form,row); else Object.assign(form,{id:null,name:'',address:'',contact:'',phone:'',status:1,remark:''}); dialogVisible.value=true }
const handleSubmit = async () => { if(!form.name) return; try { if(form.id) await request.put('/warehouse',form); else await request.post('/warehouse',form); dialogVisible.value=false; loadData() } catch(e){} }
const handleDelete = async (id) => { if(!confirm('确定删除？')) return; try { await request.delete('/warehouse/'+id); loadData() } catch(e){} }
onMounted(() => loadData())
</script>
