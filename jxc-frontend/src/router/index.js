import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'user', name: 'UserList', component: () => import('@/views/user/UserList.vue'), meta: { title: '用户管理' } },
      { path: 'product', name: 'ProductList', component: () => import('@/views/product/ProductList.vue'), meta: { title: '商品管理' } },
      { path: 'category', name: 'CategoryList', component: () => import('@/views/category/CategoryList.vue'), meta: { title: '分类管理' } },
      { path: 'supplier', name: 'SupplierList', component: () => import('@/views/supplier/SupplierList.vue'), meta: { title: '供应商管理' } },
      { path: 'customer', name: 'CustomerList', component: () => import('@/views/customer/CustomerList.vue'), meta: { title: '客户管理' } },
      { path: 'stock/in', name: 'StockIn', component: () => import('@/views/stock/StockIn.vue'), meta: { title: '入库操作' } },
      { path: 'stock/out', name: 'StockOut', component: () => import('@/views/stock/StockOut.vue'), meta: { title: '出库操作' } },
      { path: 'stock/list', name: 'StockList', component: () => import('@/views/stock/StockList.vue'), meta: { title: '库存盘点' } },
      { path: 'product-batch', name: 'ProductBatch', component: () => import('@/views/product/ProductBatch.vue'), meta: { title: '批次管理' } },
      { path: 'warehouse', name: 'WarehouseList', component: () => import('@/views/warehouse/WarehouseList.vue'), meta: { title: '仓库管理' } },
      { path: 'stock/history', name: 'StockHistory', component: () => import('@/views/stock/StockHistory.vue'), meta: { title: '出入库记录' } },
      { path: 'order', name: 'OrderList', component: () => import('@/views/order/OrderList.vue'), meta: { title: '订单管理' } },
      { path: 'order/create', name: 'OrderCreate', component: () => import('@/views/order/OrderCreate.vue'), meta: { title: '创建订单' } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/order/OrderDetail.vue'), meta: { title: '订单详情' } },
      { path: 'return', name: 'ReturnList', component: () => import('@/views/order/ReturnList.vue'), meta: { title: '退换货' } },
      { path: 'report/sales', name: 'SalesReport', component: () => import('@/views/report/SalesReport.vue'), meta: { title: '销售报表' } },
      { path: 'report/stock', name: 'StockReport', component: () => import('@/views/report/StockReport.vue'), meta: { title: '库存报表' } },
      { path: 'payment', name: 'PaymentList', component: () => import('@/views/payment/PaymentList.vue'), meta: { title: '付款记录' } },
      { path: 'purchase-order', name: 'PurchaseOrderList', component: () => import('@/views/purchase/PurchaseOrderList.vue'), meta: { title: '采购管理' } },
      { path: 'account', name: 'AccountList', component: () => import('@/views/account/AccountList.vue'), meta: { title: '应收应付' } },
      { path: 'data', name: 'DataImportExport', component: () => import('@/views/DataImportExport.vue'), meta: { title: '数据导入导出' } },
      { path: 'notification', name: 'Notification', component: () => import('@/views/Notification.vue'), meta: { title: '消息中心' } },
      { path: 'log', name: 'LogList', component: () => import('@/views/log/LogList.vue'), meta: { title: '操作日志' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router