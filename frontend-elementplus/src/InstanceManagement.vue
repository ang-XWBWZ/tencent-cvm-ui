<template>
  <div class="instance-management">
    <div class="header-section">
      <h2>{{ t('instanceManagementTitle') }}</h2>
      <div class="header-actions">
        <el-select
          v-model="localRegion"
          placeholder="请选择地区"
          size="small"
          style="width: 220px"
          @change="onRegionChange"
        >
          <el-option
            v-for="region in props.regions"
            :key="region"
            :label="regionOptionLabel(region)"
            :value="region"
          />
        </el-select>
        <el-button @click="loadInstances">{{ t('refresh') }}</el-button>
      </div>
    </div>

    <el-row :gutter="12" class="stats-row">
      <el-col :span="6"><el-card shadow="never">总实例：<b>{{ stats.total }}</b></el-card></el-col>
      <el-col :span="6"><el-card shadow="never">运行中：<b style="color:#67c23a">{{ stats.running }}</b></el-card></el-col>
      <el-col :span="6"><el-card shadow="never">已关机：<b style="color:#909399">{{ stats.stopped }}</b></el-card></el-col>
      <el-col :span="6"><el-card shadow="never">异常/其他：<b style="color:#e6a23c">{{ stats.other }}</b></el-card></el-col>
    </el-row>

    <el-card shadow="never" class="instance-list-card">
      <template #header>
        <div class="card-header-flex">
          <span>{{ t('instanceList') }}</span>
          <span class="total-count">{{ t('totalInstances', { total: instanceList.totalCount || 0 }) }}</span>
        </div>
      </template>

      <div class="filter-row">
        <el-input v-model="filters.keyword" placeholder="按实例ID/名称/规格筛选" clearable style="width: 280px" />
        <el-select v-model="filters.state" clearable placeholder="按状态筛选" style="width: 180px">
          <el-option label="RUNNING" value="RUNNING" />
          <el-option label="STOPPED" value="STOPPED" />
          <el-option label="PENDING" value="PENDING" />
          <el-option label="SHUTDOWN" value="SHUTDOWN" />
          <el-option label="TERMINATING" value="TERMINATING" />
        </el-select>

        <el-button :disabled="!selectedRows.length" @click="batchStart">批量开机</el-button>
        <el-button :disabled="!selectedRows.length" @click="batchStop">批量关机</el-button>
        <el-button :disabled="!selectedRows.length" @click="batchReboot">批量重启</el-button>
        <el-button type="danger" :disabled="!selectedRows.length" @click="batchDestroy">批量销毁</el-button>
      </div>

      <el-table :data="filteredItems" stripe v-loading="loading" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="42" />
        <el-table-column prop="instanceId" :label="t('instanceId')" min-width="180" />
        <el-table-column prop="instanceName" :label="t('instanceName')" min-width="150">
          <template #default="{ row }">
            <span>{{ row.instanceName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="instanceType" :label="t('instanceType')" min-width="140" />
        <el-table-column prop="cpu" :label="t('cpu')" width="80" />
        <el-table-column prop="memory" :label="t('memory')" width="100">
          <template #default="{ row }">
            <span>{{ row.memory }} GB</span>
          </template>
        </el-table-column>
        <el-table-column prop="zone" :label="t('zone')" min-width="140" />
        <el-table-column label="公网IP" min-width="150">
          <template #default="{ row }">
            <span>{{ row.publicIp || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="内网IP" min-width="150">
          <template #default="{ row }">
            <span>{{ row.privateIp || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="instanceChargeType" :label="t('chargeType')" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="row.instanceChargeType === 'POSTPAID_BY_HOUR' ? 'success' : 'warning'">
              {{ getChargeTypeLabel(row.instanceChargeType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="instanceState" :label="t('state')" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getInstanceStateTag(row.instanceState)">
              {{ getInstanceStateLabel(row.instanceState) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('actions')" width="280" fixed="right">
          <template #default="{ row }">
            <el-button-group size="small">
              <el-button v-if="row.instanceState === 'STOPPED'" type="success" @click="startInstance(row)">
                {{ t('start') }}
              </el-button>
              <el-button v-if="row.instanceState === 'RUNNING'" type="warning" @click="stopInstance(row)">
                {{ t('stop') }}
              </el-button>
              <el-button v-if="row.instanceState === 'RUNNING'" @click="rebootInstance(row)">
                {{ t('reboot') }}
              </el-button>
              <el-button @click="showModifyDialog(row)">
                {{ t('modify') }}
              </el-button>
              <el-button type="danger" @click="destroyInstance(row)">
                {{ t('destroy') }}
              </el-button>
              <el-button v-if="row.instanceChargeType === 'PREPAID'" type="primary" @click="openRenewDialog(row)">
                {{ t('renew') }}
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="instanceList.totalCount || 0"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadInstances"
          @current-change="loadInstances"
        />
      </div>
    </el-card>

    <el-dialog v-model="showCreateDialog" :title="t('createInstance')" width="700px" @closed="resetCreateForm">
      <el-form :model="createForm" label-width="140px" :rules="createRules" ref="createFormRef">
        <el-form-item :label="t('region')" prop="region">
          <el-select v-model="createForm.region" :placeholder="t('placeholderSelect')" @change="onCreateRegionChange">
            <el-option v-for="region in regions" :key="region" :label="region" :value="region" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('zone')" prop="zone">
          <el-select v-model="createForm.zone" :placeholder="t('placeholderSelect')">
            <el-option v-for="zone in zones" :key="zone" :label="zone" :value="zone" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('instanceType')" prop="instanceType">
          <el-select v-model="createForm.instanceType" :placeholder="t('placeholderSelect')" filterable>
            <el-option v-for="type in instanceTypes" :key="type.instanceType" :label="formatInstanceLabel(type)" :value="type.instanceType" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('image')" prop="imageId">
          <el-select v-model="createForm.imageId" :placeholder="t('placeholderSelect')" filterable>
            <el-option v-for="image in images" :key="image.imageId" :label="formatImageLabel(image)" :value="image.imageId" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('instanceName')" prop="instanceName"><el-input v-model="createForm.instanceName" /></el-form-item>
        <el-form-item :label="t('systemDisk')" prop="systemDiskGb"><el-input-number v-model="createForm.systemDiskGb" :min="20" :max="1024" /><span class="unit">GB</span></el-form-item>
        <el-form-item :label="t('dataDisk')" prop="dataDiskGb"><el-input-number v-model="createForm.dataDiskGb" :min="0" :max="10240" /><span class="unit">GB</span></el-form-item>
        <el-form-item :label="t('bandwidth')" prop="bandwidthMbps"><el-input-number v-model="createForm.bandwidthMbps" :min="0" :max="10000" /><span class="unit">Mbps</span></el-form-item>
        <el-form-item :label="t('bandwidthChargeType')" prop="bandwidthChargeType">
          <el-select v-model="createForm.bandwidthChargeType" :placeholder="t('placeholderSelect')">
            <el-option :label="t('bandwidthByBandwidth')" value="BANDWIDTH_POSTPAID_BY_HOUR" />
            <el-option :label="t('bandwidthByTraffic')" value="TRAFFIC_POSTPAID_BY_HOUR" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('instanceChargeType')" prop="instanceChargeType">
          <el-select v-model="createForm.instanceChargeType" :placeholder="t('placeholderSelect')">
            <el-option :label="t('instanceChargePostpaid')" value="POSTPAID_BY_HOUR" />
            <el-option label="包年包月" value="PREPAID" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="createForm.password" type="password" /></el-form-item>
        <el-form-item :label="t('userData')" prop="userData"><el-input v-model="createForm.userData" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">{{ t('cancel') }}</el-button>
        <el-button type="primary" @click="submitCreateForm" :loading="creating">{{ t('create') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="modifyDialogVisible" :title="t('modifyInstance')" width="500px">
      <el-form :model="modifyForm" label-width="100px">
        <el-form-item :label="t('instanceName')"><el-input v-model="modifyForm.instanceName" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modifyDialogVisible = false">{{ t('cancel') }}</el-button>
        <el-button type="primary" @click="submitModifyForm" :loading="modifying">{{ t('save') }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="renewDialogVisible" :title="t('renewInstance')" width="400px">
      <el-form :model="renewForm" label-width="100px">
        <el-form-item :label="t('periodMonths')"><el-input-number v-model="renewForm.periodMonths" :min="1" :max="36" /><span class="unit">{{ t('months') }}</span></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renewDialogVisible = false">{{ t('cancel') }}</el-button>
        <el-button type="primary" @click="submitRenewForm" :loading="renewing">{{ t('renew') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getJson, getInstances, createInstance, startInstance as apiStartInstance, stopInstance as apiStopInstance, rebootInstance as apiRebootInstance, modifyInstance, destroyInstance as apiDestroyInstance, renewInstance } from './api'

const props = defineProps({
  t: { type: Function, required: true },
  regions: { type: Array, default: () => [] },
  selectedRegion: { type: String, default: '' },
  regionStats: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['change-region'])

const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const selectedRows = ref([])
const filters = reactive({ keyword: '', state: '' })
const localRegion = ref(props.selectedRegion || '')
const effectiveRegion = computed(() => localRegion.value || props.selectedRegion || '')

const instanceList = reactive({ totalCount: 0, items: [] })
const zones = ref([])
const instanceTypes = ref([])
const images = ref([])

const showCreateDialog = ref(false)
const modifyDialogVisible = ref(false)
const renewDialogVisible = ref(false)
const creating = ref(false)
const modifying = ref(false)
const renewing = ref(false)

const createForm = reactive({
  region: '', zone: '', instanceType: '', imageId: '', instanceName: '', systemDiskGb: 20, dataDiskGb: 0,
  bandwidthMbps: 1, bandwidthChargeType: 'BANDWIDTH_POSTPAID_BY_HOUR', instanceChargeType: 'POSTPAID_BY_HOUR',
  password: '', keyIds: [], securityGroupIds: [], userData: ''
})
const createFormRef = ref()
const createRules = {
  region: [{ required: true, message: '地域必选', trigger: 'blur' }],
  zone: [{ required: true, message: '可用区必选', trigger: 'blur' }],
  instanceType: [{ required: true, message: '实例规格必选', trigger: 'blur' }],
  imageId: [{ required: true, message: '镜像必选', trigger: 'blur' }]
}

const modifyForm = reactive({ instanceId: '', instanceName: '' })
const renewForm = reactive({ instanceId: '', periodMonths: 1 })

const filteredItems = computed(() => {
  const kw = (filters.keyword || '').toLowerCase().trim()
  return (instanceList.items || []).filter(item => {
    if (filters.state && item.instanceState !== filters.state) return false
    if (!kw) return true
    return [item.instanceId, item.instanceName, item.instanceType].filter(Boolean).join(' ').toLowerCase().includes(kw)
  })
})

const stats = computed(() => {
  const items = instanceList.items || []
  const running = items.filter(i => i.instanceState === 'RUNNING').length
  const stopped = items.filter(i => i.instanceState === 'STOPPED').length
  return { total: items.length, running, stopped, other: Math.max(items.length - running - stopped, 0) }
})

function regionOptionLabel(region) {
  const count = props.regionStats?.[region]
  if (typeof count === 'number') {
    return `${region} (${count}台)`
  }
  return region
}

function onRegionChange(region) {
  emit('change-region', region)
}

function onSelectionChange(rows) { selectedRows.value = rows || [] }

async function loadInstances() {
  if (!effectiveRegion.value) {
    ElMessage.warning('请先选择地区')
    return
  }
  loading.value = true
  try {
    const data = await getInstances(effectiveRegion.value, currentPage.value, pageSize.value)
    instanceList.totalCount = data.totalCount
    instanceList.items = data.items || []
    selectedRows.value = []
  } catch (error) {
    ElMessage.error((props.t('errorLoadInstances') || '加载实例失败') + ': ' + error.message)
  } finally { loading.value = false }
}

async function loadRegionData(region) {
  if (!region) return
  try {
    const [zonesData, typesData, imagesData] = await Promise.all([
      getJson(`/api/zones?region=${encodeURIComponent(region)}`),
      getJson(`/api/instance-types?region=${encodeURIComponent(region)}`),
      getJson(`/api/images?region=${encodeURIComponent(region)}`)
    ])
    zones.value = zonesData
    instanceTypes.value = typesData
    images.value = imagesData
  } catch (error) {
    console.error('Failed to load region data:', error)
  }
}

function onCreateRegionChange(region) { if (region) loadRegionData(region) }
function formatInstanceLabel(item) { return `${item.instanceType || '-'} | ${item.cpu || 0} vCPU / ${item.memory || 0} GB` }
function formatImageLabel(item) { return `${item.imageName || item.imageId} (${item.imageOsName || 'OS'})` }
function getInstanceStateTag(state) { return ({ RUNNING: 'success', STOPPED: 'info', STARTING: 'warning', STOPPING: 'warning', REBOOTING: 'warning', PENDING: 'warning', SHUTDOWN: 'danger', TERMINATING: 'danger' }[state] || 'info') }
function getInstanceStateLabel(state) {
  const map = {
    RUNNING: '运行中',
    STOPPED: '已关机',
    STARTING: '开机中',
    STOPPING: '关机中',
    REBOOTING: '重启中',
    PENDING: '创建中',
    SHUTDOWN: '已隔离/下线',
    TERMINATING: '销毁中'
  }
  return map[state] || state || '-'
}
function getChargeTypeLabel(type) {
  return ({ POSTPAID_BY_HOUR: '按量计费', PREPAID: '包年包月', SPOTPAID: '竞价实例' }[type] || type || '-')
}

async function submitCreateForm() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    creating.value = true
    try {
      const req = { ...createForm }
      if (!req.password?.trim()) delete req.password
      if (!req.userData?.trim()) delete req.userData
      if (!req.keyIds?.length) delete req.keyIds
      if (!req.securityGroupIds?.length) delete req.securityGroupIds
      const result = await createInstance(req)
      ElMessage.success(`创建成功: ${result.instanceId}`)
      showCreateDialog.value = false
      loadInstances()
    } catch (error) { ElMessage.error('创建失败: ' + error.message) } finally { creating.value = false }
  })
}

function resetCreateForm() {
  createFormRef.value?.resetFields()
  Object.assign(createForm, {
    region: effectiveRegion.value || '', zone: '', instanceType: '', imageId: '', instanceName: '', systemDiskGb: 20,
    dataDiskGb: 0, bandwidthMbps: 1, bandwidthChargeType: 'BANDWIDTH_POSTPAID_BY_HOUR',
    instanceChargeType: 'POSTPAID_BY_HOUR', password: '', keyIds: [], securityGroupIds: [], userData: ''
  })
}

function showModifyDialog(row) { modifyForm.instanceId = row.instanceId; modifyForm.instanceName = row.instanceName || ''; modifyDialogVisible.value = true }
async function submitModifyForm() {
  if (!modifyForm.instanceId || !effectiveRegion.value) return
  modifying.value = true
  try { await modifyInstance(modifyForm.instanceId, effectiveRegion.value, { instanceName: modifyForm.instanceName }); ElMessage.success('修改成功'); modifyDialogVisible.value = false; loadInstances() }
  catch (error) { ElMessage.error('修改失败: ' + error.message) }
  finally { modifying.value = false }
}
function openRenewDialog(row) { renewForm.instanceId = row.instanceId; renewForm.periodMonths = 1; renewDialogVisible.value = true }
async function submitRenewForm() {
  if (!renewForm.instanceId || !effectiveRegion.value) return
  renewing.value = true
  try { await renewInstance(renewForm.instanceId, effectiveRegion.value, renewForm.periodMonths); ElMessage.success('续费成功'); renewDialogVisible.value = false; loadInstances() }
  catch (error) { ElMessage.error('续费失败: ' + error.message) }
  finally { renewing.value = false }
}

async function startInstance(row) { try { await apiStartInstance(row.instanceId, effectiveRegion.value); ElMessage.success('开机成功'); loadInstances() } catch (e) { ElMessage.error('开机失败: ' + e.message) } }
async function stopInstance(row) { try { await apiStopInstance(row.instanceId, effectiveRegion.value); ElMessage.success('关机成功'); loadInstances() } catch (e) { ElMessage.error('关机失败: ' + e.message) } }
async function rebootInstance(row) { try { await apiRebootInstance(row.instanceId, effectiveRegion.value); ElMessage.success('重启成功'); loadInstances() } catch (e) { ElMessage.error('重启失败: ' + e.message) } }
async function destroyInstance(row) {
  try {
    await ElMessageBox.confirm(`确认销毁 ${row.instanceId} ?`, '提示', { type: 'warning' })
    await apiDestroyInstance(row.instanceId, effectiveRegion.value)
    ElMessage.success('销毁成功')
    loadInstances()
  } catch (e) { if (e !== 'cancel') ElMessage.error('销毁失败: ' + e.message) }
}

async function runBatch(actionName, handler) {
  if (!selectedRows.value.length) return
  const ids = selectedRows.value.map(r => r.instanceId).join(', ')
  try {
    await ElMessageBox.confirm(`确认${actionName}选中实例？\n${ids}`, '批量操作确认', { type: 'warning' })
    for (const row of selectedRows.value) await handler(row)
    ElMessage.success(`${actionName}完成`)
    loadInstances()
  } catch (e) { if (e !== 'cancel') ElMessage.error(`${actionName}失败: ${e.message}`) }
}

function batchStart() { runBatch('批量开机', (row) => apiStartInstance(row.instanceId, effectiveRegion.value)) }
function batchStop() { runBatch('批量关机', (row) => apiStopInstance(row.instanceId, effectiveRegion.value)) }
function batchReboot() { runBatch('批量重启', (row) => apiRebootInstance(row.instanceId, effectiveRegion.value)) }
function batchDestroy() { runBatch('批量销毁', (row) => apiDestroyInstance(row.instanceId, effectiveRegion.value)) }

onMounted(() => {
  if (effectiveRegion.value) {
    createForm.region = effectiveRegion.value
  }
})

watch(
  () => props.selectedRegion,
  (region) => {
    if (!region) {
      return
    }
    currentPage.value = 1
    localRegion.value = region
    createForm.region = region
    loadRegionData(region)
    loadInstances()
  },
  { immediate: true }
)
</script>

<style scoped>
.instance-management { padding: 20px; }
.header-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.header-actions { display: flex; gap: 10px; align-items: center; }
.stats-row { margin-bottom: 12px; }
.instance-list-card { margin-bottom: 20px; }
.card-header-flex { display: flex; align-items: center; gap: 12px; }
.total-count { font-size: 14px; color: #909399; }
.filter-row { display: flex; gap: 8px; align-items: center; margin-bottom: 12px; flex-wrap: wrap; }
.pagination-wrap { margin-top: 20px; display: flex; justify-content: flex-end; }
.unit { margin-left: 5px; color: #606266; }
.el-button-group { display: flex; flex-wrap: wrap; gap: 4px; }
</style>
