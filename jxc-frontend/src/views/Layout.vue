<template>
  <div class="flex h-screen bg-slate-50">
    <aside class="w-64 bg-white border-r border-slate-200 flex flex-col">
      <div class="h-16 flex items-center gap-3 px-5 border-b border-slate-100">
        <div class="w-9 h-9 rounded-lg bg-slate-900 flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/></svg>
        </div>
        <span class="font-bold text-slate-900 text-sm">进销存管理系统</span>
      </div>
      <nav class="flex-1 overflow-y-auto py-4 px-3 space-y-1">
        <template v-for="item in menuItems" :key="item.path">
          <router-link v-if="!item.children" :to="item.path" class="flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors" :class="isActive(item.path) ? 'bg-slate-900 text-white' : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'">
            <span>{{ item.label }}</span>
          </router-link>
          <div v-else>
            <button @click="toggleSubmenu(item.key)" class="w-full flex items-center justify-between px-3 py-2 rounded-lg text-sm text-slate-600 hover:bg-slate-100 hover:text-slate-900 transition-colors">
              <span>{{ item.label }}</span>
              <svg class="w-4 h-4 transition-transform" :class="openSubmenus[item.key] ? 'rotate-90' : ''" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/></svg>
            </button>
            <div v-show="openSubmenus[item.key]" class="ml-4 mt-1 space-y-1 border-l border-slate-100 pl-3">
              <router-link v-for="child in item.children" :key="child.path" :to="child.path" class="block px-3 py-1.5 rounded-lg text-sm transition-colors" :class="isActive(child.path) ? 'bg-slate-900 text-white' : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'">
                {{ child.label }}
              </router-link>
            </div>
          </div>
        </template>
      </nav>
      <div class="p-3 border-t border-slate-100">
        <div class="flex items-center gap-3 px-3 py-2">
          <div class="w-8 h-8 rounded-full bg-slate-900 flex items-center justify-center text-white text-xs font-medium">{{ (realName || username || 'U')[0] }}</div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-slate-900 truncate">{{ realName || username }}</p>
            <p class="text-xs text-slate-500">{{ role === 'admin' ? '管理员' : '用户' }}</p>
          </div>
          <button @click="showChangePwd = true" class="text-xs text-blue-600 hover:text-blue-800">修改密码</button>
          <button @click="handleLogout" class="text-slate-400 hover:text-slate-600 transition-colors" title="退出登录">
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"/></svg>
          </button>
        </div>
      </div>
    </aside>
    <div class="flex-1 flex flex-col overflow-hidden">
      <header class="h-16 bg-white border-b border-slate-200 flex items-center justify-between px-6">
        <h1 class="text-lg font-semibold text-slate-900">{{ route.meta.title || '首页' }}</h1>
        <div class="flex items-center gap-4">
          <button @click="router.push('/notification')" class="relative p-2 text-slate-500 hover:text-slate-900 transition-colors">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>
            <span v-if="unreadCount > 0" class="absolute -top-0.5 -right-0.5 w-4 h-4 rounded-full bg-red-500 text-white text-xs flex items-center justify-center">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
          </button>
          <span class="text-sm text-slate-600">{{ realName || username }}</span>
        </div>
      </header>
      <main class="flex-1 overflow-y-auto p-6">
        <router-view />
      </main>
    </div>
    <el-dialog v-model="showChangePwd" title="修改密码" width="400px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">原密码</label>
          <el-input v-model="pwdForm.oldPassword" type="password" placeholder="请输入原密码" />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">新密码</label>
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码(至少6位)" />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">确认新密码</label>
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="showChangePwd = false" class="px-4 py-2 text-sm text-slate-600 hover:text-slate-900">取消</button>
          <button @click="handleChangePwd" class="px-4 py-2 text-sm bg-slate-900 text-white rounded-lg hover:bg-slate-800">确认修改</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const route = useRoute()
const router = useRouter()
const username = localStorage.getItem('username') || ''
const realName = localStorage.getItem('realName') || ''
const role = localStorage.getItem('role') || ''
const unreadCount = ref(0)
const showChangePwd = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const openSubmenus = reactive({})

const toggleSubmenu = (key) => { openSubmenus[key] = !openSubmenus[key] }
const isActive = (path) => route.path === path || route.path.startsWith(path + '/')

const menuItems = [
  { path: '/dashboard', label: '仪表盘' },
  { key: 'base', label: '基础数据', children: [
    { path: '/product', label: '商品管理' },
    { path: '/category', label: '分类管理' },
    { path: '/supplier', label: '供应商管理' },
    { path: '/customer', label: '客户管理' }
  ]},
  { key: 'stock', label: '库存管理', children: [
    { path: '/stock/in', label: '入库操作' },
    { path: '/stock/out', label: '出库操作' },
    { path: '/stock/list', label: '库存盘点' },
    { path: '/stock/history', label: '出入库记录' }
  ]},
  { path: '/order', label: '订单管理' },
  { path: '/return', label: '退货换货' },
  { path: '/payment', label: '付款记录' },
  { path: '/data', label: '数据导入导出' },
  { key: 'report', label: '报表统计', children: [
    { path: '/report/sales', label: '销售报表' },
    { path: '/report/stock', label: '库存报表' }
  ]},
  { path: '/log', label: '操作日志' },
  { path: '/user', label: '用户管理' }
]

watch(() => route.path, (path) => {
  menuItems.forEach(item => {
    if (item.children) {
      const active = item.children.some(c => path === c.path || path.startsWith(c.path + '/'))
      if (active) openSubmenus[item.key] = true
    }
  })
}, { immediate: true })

const handleChangePwd = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) { ElMessage.warning('请填写完整'); return }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次密码不一致'); return }
  if (pwdForm.newPassword.length < 6) { ElMessage.warning('密码长度不能少于6位'); return }
  try {
    await request.put('/user/change-password', { oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    showChangePwd.value = false
    Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  } catch(e) {}
}

const loadUnreadCount = async () => {
  try { const res = await request.get('/notification/unread-count'); unreadCount.value = res.data?.count || 0 } catch(e) {}
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  localStorage.removeItem('realName')
  localStorage.removeItem('role')
  router.push('/login')
}

onMounted(() => {
  loadUnreadCount()
  setInterval(loadUnreadCount, 300000)
})
</script>