<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-900">订单详情</h2>
        <p class="text-sm text-slate-500 mt-0.5">查看订单详细信息和状态</p>
      </div>
      <button @click="router.back()" class="h-9 px-4 rounded-lg border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">返回</button>
    </div>
    <div class="bg-white rounded-xl border border-slate-200 p-6">
      <div class="grid grid-cols-2 gap-4 mb-6">
        <div><p class="text-xs text-slate-500 mb-1">订单号</p><p class="text-sm font-medium text-slate-900">{{order.orderNo}}</p></div>
        <div><p class="text-xs text-slate-500 mb-1">客户</p><p class="text-sm font-medium text-slate-900">{{order.customerName||order.customerId}}</p></div>
        <div><p class="text-xs text-slate-500 mb-1">订单金额</p><p class="text-sm font-bold text-slate-900">¥{{order.totalAmount}}</p></div>
        <div><p class="text-xs text-slate-500 mb-1">订单状态</p><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="statusClass(order.status)">{{statusText(order.status)}}</span></div>
        <div><p class="text-xs text-slate-500 mb-1">付款状态</p><span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium" :class="payStatusClass(order.paymentStatus)">{{payStatusText(order.paymentStatus)}}</span></div>
        <div><p class="text-xs text-slate-500 mb-1">已付金额</p><p class="text-sm font-medium text-slate-900">¥{{order.paidAmount||0}}</p></div>
        <div><p class="text-xs text-slate-500 mb-1">备注</p><p class="text-sm text-slate-600">{{order.remark||'无'}}</p></div>
        <div><p class="text-xs text-slate-500 mb-1">创建时间</p><p class="text-sm text-slate-600">{{order.createTime}}</p></div>
      </div>
      <div v-if="order.shippingNo" class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <h4 class="text-sm font-semibold text-blue-900 mb-2">物流信息</h4>
        <div class="grid grid-cols-3 gap-4 text-sm">
          <div><span class="text-blue-600">快递公司:</span> {{order.shippingCompany}}</div>
          <div><span class="text-blue-600">单号:</span> {{order.shippingNo}}</div>
          <div><span class="text-blue-600">发货时间:</span> {{order.shippingTime}}</div>
        </div>
      </div>
      <div class="border-t border-slate-100 pt-4 mb-4"><h3 class="text-sm font-semibold text-slate-900">订单明细</h3></div>
      <table class="w-full">
        <thead><tr class="border-b border-slate-100">
          <th class="px-4 py-2 text-left text-xs font-medium text-slate-500 uppercase">商品ID</th>
          <th class="px-4 py-2 text-left text-xs font-medium text-slate-500 uppercase">数量</th>
          <th class="px-4 py-2 text-left text-xs font-medium text-slate-500 uppercase">单价</th>
          <th class="px-4 py-2 text-left text-xs font-medium text-slate-500 uppercase">小计</th>
        </tr></thead>
        <tbody class="divide-y divide-slate-50">
          <tr v-for="item in order.items||[]" :key="item.id">
            <td class="px-4 py-2 text-sm text-slate-600">{{item.productId}}</td>
            <td class="px-4 py-2 text-sm text-slate-900">{{item.quantity}}</td>
            <td class="px-4 py-2 text-sm text-slate-600">¥{{item.unitPrice}}</td>
            <td class="px-4 py-2 text-sm font-medium text-slate-900">¥{{item.subtotal}}</td>
          </tr>
        </tbody>
      </table>
      <div class="border-t border-slate-100 pt-4 mt-4 flex items-center gap-3 flex-wrap">
        <button v-if="order.status===0" @click="updateStatus(1)" class="h-9 px-4 rounded-lg bg-emerald-600 text-white text-sm font-medium hover:bg-emerald-700 transition-colors">确认订单</button>
        <button v-if="order.status===1" @click="showShipDialog=true" class="h-9 px-4 rounded-lg bg-purple-600 text-white text-sm font-medium hover:bg-purple-700 transition-colors">发货</button>
        <button v-if="order.status===2" @click="updateStatus(3)" class="h-9 px-4 rounded-lg bg-emerald-600 text-white text-sm font-medium hover:bg-emerald-700 transition-colors">完成订单</button>
        <button v-if="order.status<=2" @click="updateStatus(4)" class="h-9 px-4 rounded-lg border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">取消订单</button>
        <button v-if="order.status>=1 && order.paymentStatus!==2" @click="showPayDialog=true" class="h-9 px-4 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 transition-colors">收款</button>
        <button @click="printOrder" class="h-9 px-4 rounded-lg border border-slate-200 text-sm font-medium text-slate-700 hover:bg-slate-50 transition-colors">打印</button>
      </div>
    </div>

    <!-- 发货对话框 -->
    <el-dialog v-model="showShipDialog" title="发货信息" width="400px">
      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">快递公司</label>
          <el-select v-model="shipping.company" placeholder="选择快递公司" style="width:100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通快递" value="圆通快递" />
            <el-option label="陕西百丁" value="陕西百丁" />
            <el-option label="邮政 EMS" value="邮政 EMS" />
          </el-select>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">快递单号</label>
          <el-input v-model="shipping.no" placeholder="请输入快递单号" />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="showShipDialog = false" class="px-4 py-2 text-sm text-slate-600 hover:text-slate-900">取消</button>
          <button @click="submitShip" class="px-4 py-2 text-sm bg-purple-600 text-white rounded-lg hover:bg-purple-700">确认发货</button>
        </div>
      </template>
    </el-dialog>

    <!-- 收款对话框 -->
    <el-dialog v-model="showPayDialog" title="收款" width="400px">
      <div class="space-y-4">
        <div class="bg-slate-50 rounded-lg p-3 text-sm">
          <p>订单金额: <span class="font-bold">¥{{order.totalAmount}}</span></p>
          <p>已付金额: <span class="font-bold text-emerald-600">¥{{order.paidAmount||0}}</span></p>
          <p>应付余额: <span class="font-bold text-red-600">¥{{(order.totalAmount||0)-(order.paidAmount||0)}}</span></p>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">付款金额</label>
          <el-input-number v-model="payForm.amount" :min="0" :max="(order.totalAmount||0)-(order.paidAmount||0)" :precision="2" style="width:100%" />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">付款方式</label>
          <el-select v-model="payForm.paymentMethod" style="width:100%">
            <el-option label="现金" value="cash" />
            <el-option label="银行转账" value="bank" />
            <el-option label="微信支付" value="wechat" />
            <el-option label="支付宝" value="alipay" />
          </el-select>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1.5">备注</label>
          <el-input v-model="payForm.remark" placeholder="可选" />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <button @click="showPayDialog = false" class="px-4 py-2 text-sm text-slate-600 hover:text-slate-900">取消</button>
          <button @click="submitPayment" class="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700">确认收款</button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const order = ref({})
const showShipDialog = ref(false)
const showPayDialog = ref(false)
const shipping = reactive({ company: '', no: '' })
const payForm = reactive({ amount: 0, paymentMethod: 'cash', remark: '' })

const statusText = (s) => ['待处理','已确认','发货中','已完成','已取消'][s]||'未知'
const statusClass = (s) => ['bg-amber-50 text-amber-700','bg-blue-50 text-blue-700','bg-purple-50 text-purple-700','bg-emerald-50 text-emerald-700','bg-slate-100 text-slate-600'][s]||''
const payStatusText = (s) => ['未付款','部分付款','已付清'][s]||'未付款'
const payStatusClass = (s) => ['bg-red-50 text-red-700','bg-amber-50 text-amber-700','bg-emerald-50 text-emerald-700'][s]||''

const loadOrder = async () => {
  try {
    const res = await request.get('/order/' + route.params.id)
    order.value = res.data || {}
    const remaining = (order.value.totalAmount || 0) - (order.value.paidAmount || 0)
    payForm.amount = remaining > 0 ? remaining : 0
  } catch(e) {}
}

const submitShip = async () => {
  if (!shipping.company || !shipping.no) { ElMessage.warning('请填写完整'); return }
  try {
    await request.put('/order/' + route.params.id + '/ship', { shippingNo: shipping.no, shippingCompany: shipping.company })
    ElMessage.success('发货成功')
    showShipDialog.value = false
    loadOrder()
  } catch(e) {}
}

const submitPayment = async () => {
  if (payForm.amount <= 0) { ElMessage.warning('请输入付款金额'); return }
  try {
    await request.post('/payment', { orderId: parseInt(route.params.id), ...payForm })
    ElMessage.success('收款成功')
    showPayDialog.value = false
    loadOrder()
  } catch(e) {}
}

const printOrder = async () => {
  try {
    const res = await request.get('/order/' + route.params.id + '/print')
    const printWindow = window.open('', '_blank')
    printWindow.document.write(res.data)
    printWindow.document.close()
    printWindow.print()
  } catch(e) {}
}

const updateStatus = async (status) => {
  try {
    await request.put('/order/' + route.params.id + '/status/' + status)
    ElMessage.success('状态更新成功')
    loadOrder()
  } catch(e) {}
}

onMounted(() => loadOrder())
</script>