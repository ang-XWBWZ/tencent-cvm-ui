<template>
  <div class="billing-management">
    <div class="header-section">
      <h2>{{ t('billingManagementTitle') }}</h2>
    </div>

    <el-row :gutter="20" class="balance-section">
      <el-col :span="8"><el-card shadow="never"><template #header><span>{{ t('availableBalance') }}</span></template><div class="balance-value"><span class="amount">{{ balance.availableBalance ? formatCurrency(balance.availableBalance) : '--' }}</span><span class="currency">{{ balance.currency }}</span></div></el-card></el-col>
      <el-col :span="8"><el-card shadow="never"><template #header><span>{{ t('frozenBalance') }}</span></template><div class="balance-value"><span class="amount">{{ balance.frozenBalance ? formatCurrency(balance.frozenBalance) : '--' }}</span><span class="currency">{{ balance.currency }}</span></div></el-card></el-col>
      <el-col :span="8"><el-card shadow="never"><template #header><span>{{ t('totalBalance') }}</span></template><div class="balance-value"><span class="amount">{{ balance.totalBalance ? formatCurrency(balance.totalBalance) : '--' }}</span><span class="currency">{{ balance.currency }}</span></div></el-card></el-col>
    </el-row>

    <el-row :gutter="12" class="summary-row">
      <el-col :span="6"><el-card shadow="never">账单条数：<b>{{ filteredBills.length }}</b></el-card></el-col>
      <el-col :span="6"><el-card shadow="never">总费用：<b>{{ formatCurrency(summary.totalCost) }}</b></el-card></el-col>
      <el-col :span="6"><el-card shadow="never">现金支付：<b>{{ formatCurrency(summary.cashPay) }}</b></el-card></el-col>
      <el-col :span="6"><el-card shadow="never">代金券：<b>{{ formatCurrency(summary.voucherPay) }}</b></el-card></el-col>
    </el-row>

    <el-card shadow="never" class="bill-list-card">
      <template #header>
        <div class="header-flex">
          <span>{{ t('billList') }}</span>
          <div class="header-actions">
            <el-input v-model="keyword" placeholder="按账单ID/产品名筛选" clearable style="width: 220px" />
            <el-select v-model="billingCycle" clearable :placeholder="t('placeholderBillingCycle')" size="small" style="width: 150px">
              <el-option v-for="cycle in billingCycles" :key="cycle" :label="cycle" :value="cycle" />
            </el-select>
            <el-button @click="loadBills" :loading="loading">{{ t('refresh') }}</el-button>
          </div>
        </div>
      </template>

      <el-table :data="filteredBills" stripe v-loading="loading">
        <el-table-column prop="billId" :label="t('billId')" min-width="180" />
        <el-table-column prop="productName" :label="t('productName')" min-width="150" />
        <el-table-column prop="billingCycle" :label="t('billingCycle')" width="120" />
        <el-table-column prop="billDate" :label="t('billDate')" width="120"><template #default="{ row }"><span>{{ row.billDate || '--' }}</span></template></el-table-column>
        <el-table-column prop="totalCost" :label="t('totalCost')" width="120" align="right"><template #default="{ row }"><span>{{ row.totalCost ? formatCurrency(row.totalCost) : '--' }}</span></template></el-table-column>
        <el-table-column prop="cashPayAmount" :label="t('cashPayAmount')" width="130" align="right"><template #default="{ row }"><span>{{ row.cashPayAmount ? formatCurrency(row.cashPayAmount) : '--' }}</span></template></el-table-column>
        <el-table-column prop="voucherPayAmount" :label="t('voucherPayAmount')" width="130" align="right"><template #default="{ row }"><span>{{ row.voucherPayAmount ? formatCurrency(row.voucherPayAmount) : '--' }}</span></template></el-table-column>
        <el-table-column :label="t('actions')" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewBillDetail(row)">{{ t('view') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="billList.totalCount || 0"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadBills"
          @current-change="loadBills"
        />
      </div>
    </el-card>

    <el-dialog v-model="showBillDetail" :title="t('billDetail')" width="700px">
      <el-descriptions :column="2" border v-if="currentBill">
        <el-descriptions-item :label="t('billId')">{{ currentBill.billId }}</el-descriptions-item>
        <el-descriptions-item :label="t('productName')">{{ currentBill.productName }}</el-descriptions-item>
        <el-descriptions-item :label="t('billingCycle')">{{ currentBill.billingCycle }}</el-descriptions-item>
        <el-descriptions-item :label="t('billDate')">{{ currentBill.billDate }}</el-descriptions-item>
        <el-descriptions-item :label="t('totalCost')">{{ formatCurrency(currentBill.totalCost) }}</el-descriptions-item>
        <el-descriptions-item :label="t('cashPayAmount')">{{ formatCurrency(currentBill.cashPayAmount) }}</el-descriptions-item>
        <el-descriptions-item :label="t('voucherPayAmount')">{{ formatCurrency(currentBill.voucherPayAmount) }}</el-descriptions-item>
        <el-descriptions-item :label="t('taxAmount')">{{ formatCurrency(currentBill.taxAmount) }}</el-descriptions-item>
        <el-descriptions-item :label="t('currency')">{{ currentBill.currency }}</el-descriptions-item>
      </el-descriptions>
      <div v-else class="empty-detail">{{ t('noData') }}</div>
      <template #footer><el-button @click="showBillDetail = false">{{ t('close') }}</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getBalance, getBills, getBillDetail } from './api'

const props = defineProps({ t: { type: Function, required: true } })

const loading = ref(false)
const keyword = ref('')
const billingCycle = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const showBillDetail = ref(false)

const billingCycles = ref([])
const balance = reactive({ totalBalance: null, availableBalance: null, frozenBalance: null, currency: 'CNY' })
const billList = reactive({ totalCount: 0, items: [] })
const currentBill = ref(null)

const filteredBills = computed(() => {
  const kw = (keyword.value || '').toLowerCase().trim()
  return (billList.items || []).filter(item => {
    if (!kw) return true
    return [item.billId, item.productName].filter(Boolean).join(' ').toLowerCase().includes(kw)
  })
})

const summary = computed(() => {
  return filteredBills.value.reduce((acc, item) => {
    acc.totalCost += Number(item.totalCost || 0)
    acc.cashPay += Number(item.cashPayAmount || 0)
    acc.voucherPay += Number(item.voucherPayAmount || 0)
    return acc
  }, { totalCost: 0, cashPay: 0, voucherPay: 0 })
})

async function loadBills() {
  loading.value = true
  try {
    const data = await getBills(currentPage.value, pageSize.value, billingCycle.value)
    billList.totalCount = data.totalCount
    billList.items = data.items || []
    const set = new Set((billList.items || []).map(i => i.billingCycle).filter(Boolean))
    if (set.size) billingCycles.value = Array.from(set)
  } catch (error) {
    ElMessage.error((props.t('errorLoadBills') || '加载账单失败') + ': ' + error.message)
  } finally {
    loading.value = false
  }
}

async function loadBalance() {
  try {
    const data = await getBalance()
    balance.totalBalance = data.totalBalance
    balance.availableBalance = data.availableBalance
    balance.frozenBalance = data.frozenBalance
    balance.currency = data.currency || 'CNY'
  } catch (error) {
    ElMessage.error((props.t('errorLoadBalance') || '加载余额失败') + ': ' + error.message)
  }
}

function formatCurrency(value) {
  if (value == null) return '--'
  return `¥ ${Number(value).toFixed(2)}`
}

async function viewBillDetail(row) {
  try {
    const detail = await getBillDetail(row.billId)
    currentBill.value = detail || row
  } catch (error) {
    currentBill.value = row
  }
  showBillDetail.value = true
}

onMounted(async () => {
  const secretId = localStorage.getItem('cvm.secretId')
  const secretKey = localStorage.getItem('cvm.secretKey')
  if (!secretId || !secretKey) {
    ElMessage.warning('请先在右上角配置腾讯云凭证，再查看钱包数据')
    return
  }
  await loadBalance()
  await loadBills()
})
</script>

<style scoped>
.billing-management { padding: 20px; }
.header-section { margin-bottom: 20px; }
.header-section h2 { margin: 0 0 10px 0; }
.balance-section { margin-bottom: 12px; }
.summary-row { margin-bottom: 12px; }
.balance-value { text-align: center; padding: 10px 0; }
.balance-value .amount { font-size: 28px; font-weight: bold; color: #409eff; }
.balance-value .currency { font-size: 14px; color: #909399; margin-left: 5px; }
.bill-list-card { margin-top: 20px; }
.header-flex { display: flex; align-items: center; justify-content: space-between; }
.bill-list-card .header-actions { display: flex; gap: 10px; align-items: center; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.empty-detail { text-align: center; padding: 40px; color: #909399; }
</style>
