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
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '\u9996\u9875\u4eea\u8868\u76d8' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '\u7528\u6237\u7ba1\u7406' }
      },
      {
        path: 'product',
        name: 'ProductList',
        component: () => import('@/views/product/ProductList.vue'),
        meta: { title: '\u5546\u54c1\u7ba1\u7406' }
      },
      {
        path: 'category',
        name: 'CategoryList',
        component: () => import('@/views/category/CategoryList.vue'),
        meta: { title: '\u5206\u7c7b\u7ba1\u7406' }
      },
      {
        path: 'supplier',
        name: 'SupplierList',
        component: () => import('@/views/supplier/SupplierList.vue'),
        meta: { title: '\u4f9b\u5e94\u5546\u7ba1\u7406' }
      },
      {
        path: 'customer',
        name: 'CustomerList',
        component: () => import('@/views/customer/CustomerList.vue'),
        meta: { title: '\u5ba2\u6237\u7ba1\u7406' }
      },
      {
        path: 'stock/in',
        name: 'StockIn',
        component: () => import('@/views/stock/StockIn.vue'),
        meta: { title: '\u5165\u5e93\u64cd\u4f5c' }
      },
      {
        path: 'stock/out',
        name: 'StockOut',
        component: () => import('@/views/stock/StockOut.vue'),
        meta: { title: '\u51fa\u5e93\u64cd\u4f5c' }
      },
      {
        path: 'stock/list',
        name: 'StockList',
        component: () => import('@/views/stock/StockList.vue'),
        meta: { title: '\u5e93\u5b58\u76d8\u70b9' }
      },
      {
        path: 'product-batch',
        name: 'ProductBatch',
        component: () => import('@/views/product/ProductBatch.vue'),
        meta: { title: '\u6279\u6b21\u7ba1\u7406' }
      },
      { path: 'warehouse',
        name: 'WarehouseList',
        component: () => import('@/views/warehouse/WarehouseList.vue'),
        meta: { title: '\u4ed3\u5e93\u7ba1\u7406' }
      },
      { path: 'stock/history',
        name: 'StockHistory',
        component: () => import('@/views/stock/StockHistory.vue'),
        meta: { title: '\u51fa\u5165\u5e93\u8bb0\u5f55' }
      },
      {
        path: 'order',
        name: 'OrderList',
        component: () => import('@/views/order/OrderList.vue'),
        meta: { title: '\u8ba2\u5355\u7ba1\u7406' }
      },
      {
        path: 'order/create',
        name: 'OrderCreate',
        component: () => import('@/views/order/OrderCreate.vue'),
        meta: { title: '\u521b\u5efa\u8ba2\u5355' }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '\u8ba2\u5355\u8be6\u60c5' }
      },
      {
        path: 'return',
        name: 'ReturnList',
        component: () => import('@/views/order/ReturnList.vue'),
        meta: { title: '\u9000\u8d27\u6362\u8d27' }
      },
      {
        path: 'report/sales',
        name: 'SalesReport',
        component: () => import('@/views/report/SalesReport.vue'),
        meta: { title: '\u9500\u552e\u62a5\u8868' }
      },
      {
        path: 'report/stock',
        name: 'StockReport',
        component: () => import('@/views/report/StockReport.vue'),
        meta: { title: '\u5e93\u5b58\u62a5\u8868' }
      },
      {
        path: 'payment',
        name: 'PaymentList',
        component: () => import('@/views/payment/PaymentList.vue'),
        meta: { title: '\u4ed8\u6b3e\u8bb0\u5f55' }
      },
      {
        path: 'data',
        name: 'DataImportExport',
        component: () => import('@/views/DataImportExport.vue'),
        meta: { title: '\u6570\u636e\u5bfc\u5165\u5bfc\u51fa' }
      },
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('@/views/Notification.vue'),
        meta: { title: '\u6d88\u606f\u4e2d\u5fc3' }
      },
      {
        path: 'log',
        name: 'LogList',
        component: () => import('@/views/log/LogList.vue'),
        meta: { title: '\u64cd\u4f5c\u65e5\u5fd7' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
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