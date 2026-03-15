<template>
  <div v-if="!accessReady" class="auth-shell">
    <el-card class="auth-card" shadow="hover">
      <template #header>
        <div class="auth-title">访问授权</div>
      </template>
      <el-form label-position="top">
        <el-form-item label="Secret ID">
          <el-input v-model="credentials.secretId" type="password" show-password />
        </el-form-item>
        <el-form-item label="Secret Key">
          <el-input v-model="credentials.secretKey" type="password" show-password />
        </el-form-item>
        <el-button type="primary" style="width: 100%" @click="loginWithCredentials">登录</el-button>
      </el-form>
    </el-card>
  </div>

  <div v-else class="app-shell">
    <header class="app-header">
      <div>
        <div class="brand">Tencent CVM</div>
        <div class="status-row">
          <span>{{ t("connection") }}</span>
          <span class="status-chip">{{ connected ? t("statusConnected") : t("statusNotConnected") }}</span>
          <span>{{ statusText }}</span>
        </div>
      </div>
      <div class="header-actions">
        <el-select v-model="lang" size="small" class="lang-select">
          <el-option v-for="option in languageOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-button type="primary" @click="openCredentials">{{ t("credentials") }}</el-button>
      </div>
    </header>

    <div class="app-body">
      <aside class="app-sidebar">
        <el-menu :default-active="activeView" @select="handleMenuSelect" class="side-menu">
          <el-menu-item index="purchase">{{ t('menuPurchase') }}</el-menu-item>
          <el-menu-item index="instances">{{ t('menuInstances') }}</el-menu-item>
          <el-menu-item index="billing">{{ t('menuBilling') }}</el-menu-item>
        </el-menu>
      </aside>

      <main class="app-main">
      <div v-if="activeView === 'purchase'">
        <el-steps class="purchase-steps" :active="activeStep" align-center finish-status="success">
        <el-step :title="t('stepRegionTitle')" :description="t('stepRegionDesc')" />
        <el-step :title="t('stepInstanceTitle')" :description="t('stepInstanceDesc')" />
        <el-step :title="t('stepStorageTitle')" :description="t('stepStorageDesc')" />
        <el-step :title="t('stepReviewTitle')" :description="t('stepReviewDesc')" />
      </el-steps>

      <div class="flow">
        <el-card class="section-card" shadow="hover">
          <div class="card-title">
            <h3>{{ t("step1Title") }}</h3>
            <el-button @click="refreshAll" plain>{{ t("refreshLists") }}</el-button>
          </div>

          <el-form label-position="top" :model="form" class="config-form">
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item :label="t('region')">
                  <el-select v-model="form.region" :placeholder="t('placeholderSelect')">
                    <el-option v-for="item in regions" :key="item" :label="item" :value="item" />
                  </el-select>
                  <div class="region-hint" v-if="selectedRegion && form.region && form.region !== selectedRegion">
                    与全局地区不一致，切换后将更新全局地区
                  </div>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="t('instanceChargeType')">
                  <el-select v-model="form.instanceChargeType" :placeholder="t('placeholderSelect')">
                    <el-option :label="t('instanceChargePostpaid')" value="POSTPAID_BY_HOUR" />
                    <el-option :label="t('instanceChargeSpot')" value="SPOTPAID" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="t('pricingZoneAuto')">
                  <el-select v-model="form.zone" :placeholder="t('placeholderSelect')">
                    <el-option v-for="item in zones" :key="item" :label="item" :value="item" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-card class="section-card instance-card" shadow="hover">
          <div class="card-title">
            <h3>{{ t("step2Title") }}</h3>
            <el-button @click="refreshPrices" plain>{{ t("refreshPrices") }}</el-button>
          </div>

          <el-form label-position="top" :model="filters" class="instance-form">
            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item :label="t('selectedType')">
                  <el-select v-model="form.instanceType" :placeholder="t('placeholderPickFromList')">
                    <el-option
                      v-for="item in instanceTypes"
                      :key="item.instanceType"
                      :label="formatInstanceLabel(item)"
                      :value="item.instanceType"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="t('typeKeyword')">
                  <el-input v-model="filters.instanceType" :placeholder="t('placeholderInstanceKeyword')" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="t('matched')">
                  <el-input :model-value="t('matchedCount', { count: filteredItems.length })" disabled />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item :label="t('family')">
                  <el-input v-model="filters.family" :placeholder="t('placeholderFamily')" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="t('gpu')">
                  <el-input v-model="filters.gpu" :placeholder="t('placeholderGpu')" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="CPU 范围">
                  <el-space>
                    <el-input-number v-model="filters.cpuMin" :min="0" :max="filters.cpuMax" />
                    <span>~</span>
                    <el-input-number v-model="filters.cpuMax" :min="filters.cpuMin" :max="512" />
                  </el-space>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="内存范围(GB)">
                  <el-space>
                    <el-input-number v-model="filters.memoryMin" :min="0" :max="filters.memoryMax" />
                    <span>~</span>
                    <el-input-number v-model="filters.memoryMax" :min="filters.memoryMin" :max="2048" />
                  </el-space>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="库存筛选">
                  <el-switch v-model="filters.inStockOnly" active-text="仅显示有库存" inactive-text="显示全部" />
                </el-form-item>
              </el-col>
            </el-row>

            <div class="table-wrap">
              <el-table
                :data="pagedItems"
                stripe
                height="520"
                highlight-current-row
                row-key="instanceType"
                :current-row-key="form.instanceType"
                @row-click="selectInstance"
              >
                <el-table-column prop="instanceType" :label="t('instanceType')" min-width="170" />
                <el-table-column prop="instanceFamily" :label="t('family')" min-width="120" />
                <el-table-column prop="cpu" :label="t('cpu')" min-width="80" />
                <el-table-column prop="memory" :label="t('memory')" min-width="110" />
                <el-table-column prop="gpu" :label="t('gpu')" min-width="80" />
                <el-table-column label="库存" min-width="140">
                  <template #default="{ row }">
                    <el-tag :type="row.hasStock ? 'success' : 'danger'" size="small">
                      {{ row.hasStock ? '有库存' : '空库存' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="t('price')" min-width="120">
                  <template #default="{ row }">
                    <span>{{ priceMap[row.instanceType] ?? "-" }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <el-form-item :label="t('pagination')" class="pagination-item">
              <el-pagination
                v-model:current-page="page"
                v-model:page-size="pageSize"
                :total="filteredItems.length"
                layout="prev, pager, next, sizes"
                :page-sizes="[5, 10, 20]"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="section-card" shadow="hover">
          <div class="card-title">
            <h3>{{ t("step3Title") }}</h3>
          </div>

          <el-form label-position="top" :model="form" class="config-form">
            <el-row :gutter="12">
              <el-col :span="6">
                <el-form-item :label="t('image')">
                  <el-select v-model="form.imageId" :placeholder="t('placeholderSelect')">
                    <el-option
                      v-for="item in displayedImages"
                      :key="item.imageId"
                      :label="formatImageLabel(item)"
                      :value="item.imageId"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="t('recommendImagesOnly')">
                  <el-switch v-model="onlyRecommendedImages" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="t('bandwidthChargeType')">
                  <el-select v-model="form.bandwidthChargeType" :placeholder="t('placeholderSelect')">
                    <el-option :label="t('bandwidthByBandwidth')" value="BANDWIDTH_POSTPAID_BY_HOUR" />
                    <el-option :label="t('bandwidthByTraffic')" value="TRAFFIC_POSTPAID_BY_HOUR" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="t('bandwidth')">
                  <el-input-number v-model="form.bandwidthMbps" :min="0" :max="10000" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item :label="t('systemDisk')">
                  <el-input-number v-model="form.systemDiskGb" :min="20" :max="1024" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="t('dataDisk')">
                  <el-input-number v-model="form.dataDiskGb" :min="0" :max="10240" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-card class="section-card" shadow="hover">
          <div class="card-title">
            <h3>{{ t("step4Title") }}</h3>
          </div>

          <el-form label-position="top" :model="form" class="config-form">
            <el-row :gutter="12">
              <el-col :span="6">
                <el-form-item :label="t('scheduledDestroy')">
                  <el-switch v-model="form.scheduledDestroy" />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="快速分钟">
                  <el-input-number
                    v-model="form.scheduledDestroyMinutes"
                    :min="10"
                    :step="5"
                    :disabled="!form.scheduledDestroy"
                    @change="updateScheduledDestroyTimeFromMinutes"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="t('destroyTime')">
                  <el-date-picker
                    v-model="form.scheduledDestroyTime"
                    type="datetime"
                    :placeholder="t('placeholderDatetime')"
                    :disabled="!form.scheduledDestroy"
                    @change="updateMinutesFromScheduledDestroyTime"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="t('userDataScript')">
                  <el-switch v-model="form.userDataEnabled" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="12">
              <el-col :span="8">
                <el-form-item label="实例名称（购买用）">
                  <el-input v-model="form.instanceName" placeholder="可选，不填则自动生成" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="购买数量">
                  <el-input-number v-model="form.instanceCount" :min="1" :max="100" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="实例密码（购买必填）">
                  <el-input v-model="form.password" type="password" show-password placeholder="请输入实例登录密码" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item :label="t('userData')">
              <el-input
                v-model="form.userData"
                type="textarea"
                :rows="4"
                :placeholder="t('placeholderUserData')"
                :disabled="!form.userDataEnabled"
              />
            </el-form-item>
          </el-form>

          <div class="review-stack">
            <el-card shadow="never" class="overview-card">
              <template #header>
                <span>{{ t("overviewTitle") }}</span>
              </template>
              <pre style="margin: 0">{{ overviewText }}</pre>
            </el-card>

            <div class="price-summary">
              <div class="items">
                <span>{{ t("priceInstance") }}: {{ price.instancePrice || "-" }}</span>
                <span>{{ t("priceBandwidth") }}: {{ price.bandwidthPrice || "-" }}</span>
                <span>{{ t("priceSystemDisk") }}: {{ price.systemDiskPrice || "-" }}</span>
                <span>{{ t("priceDataDisk") }}: {{ price.dataDiskPrice || "-" }}</span>
              </div>
              <div class="total">{{ price.totalPrice || "-" }}</div>
            </div>
          </div>

          <el-space>
            <el-button type="primary" @click="quotePrice" :disabled="!form.instanceType">
              {{ t("calculatePrice") }}
            </el-button>
            <el-button type="success" @click="quickBuy" :loading="creatingInstance" :disabled="!form.instanceType || !form.imageId || !form.zone">
              立即购买
            </el-button>
            <el-button @click="exportTemplate">{{ t("exportTemplate") }}</el-button>
            <el-upload
              :show-file-list="false"
              accept="application/json"
              :before-upload="importTemplate"
            >
              <el-button>{{ t("importTemplate") }}</el-button>
            </el-upload>
          </el-space>
        </el-card>
      </div>
      </div>
      <InstanceManagement
        v-if="activeView === 'instances'"
        :t="t"
        :regions="regions"
        :selected-region="selectedRegion"
        :region-stats="regionStats"
        @change-region="handleInstanceRegionChange"
      />
      <BillingManagement v-if="activeView === 'billing'" :t="t" />
      </main>
    </div>

    <el-dialog v-model="credentialsVisible" :title="t('credentialsTitle')" width="420px">
      <el-form label-position="top">
        <el-form-item :label="t('secretId')">
          <el-input v-model="credentials.secretId" type="password" />
        </el-form-item>
        <el-form-item :label="t('secretKey')">
          <el-input v-model="credentials.secretKey" type="password" />
        </el-form-item>
        <!-- 默认地域改为登录后自动取地域列表首项 -->
      </el-form>
      <template #footer>
        <el-button @click="credentialsVisible = false">{{ t("cancel") }}</el-button>
        <el-button type="primary" @click="saveCredentials">{{ t("save") }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import { getJson, postJson, getRegionInstanceCounts, getInstances } from "./api";
import { readCache, writeCache, invalidateCache, loadRegionData, saveRegionData } from "./cache";
import InstanceManagement from "./InstanceManagement.vue";
import BillingManagement from "./BillingManagement.vue";

// 视图状态
const activeView = ref('purchase');
function handleMenuSelect(key) {
  activeView.value = key;
}

const regions = ref([]);
const zones = ref([]);
const instanceTypes = ref([]);
const images = ref([]);
const onlyRecommendedImages = ref(true);
const connected = ref(false);
const accessReady = ref(false);

// 添加地区相关状态
const selectedRegion = ref('');
const regionStats = ref({});
const regionZoneMap = ref({});

const languageOptions = [
  { label: "中文", value: "zh" },
  { label: "English", value: "en" },
];

const translations = {
  en: {
    connection: "Connection",
    statusConnected: "Connected",
    statusNotConnected: "Not connected",
    statusHint: "Provide credentials to start.",
    statusSaved: "Credentials saved. Loading regions...",
    credentials: "Credentials",
    stepRegionTitle: "Region",
    stepRegionDesc: "Pick region and billing",
    stepInstanceTitle: "Instance",
    stepInstanceDesc: "Filter and choose type",
    stepStorageTitle: "Storage & Network",
    stepStorageDesc: "Disks and bandwidth",
    stepReviewTitle: "Review",
    stepReviewDesc: "Advanced and pricing",
    step1Title: "Step 1 - Region",
    step2Title: "Step 2 - Instance List",
    step3Title: "Step 3 - Storage & Network",
    step4Title: "Step 4 - Review & Price",
    refreshLists: "Refresh Lists",
    refreshPrices: "Refresh Prices",
    menuPurchase: "Purchase",
    menuInstances: "Instances",
    menuBilling: "Billing",
    region: "Region",
    instanceChargeType: "Instance Charge Type",
    instanceChargePostpaid: "Postpaid",
    instanceChargeSpot: "Spot",
    pricingZoneAuto: "Pricing Zone (Auto)",
    auto: "Auto",
    placeholderSelect: "Select",
    selectedType: "Selected Type",
    placeholderPickFromList: "Pick from list",
    typeKeyword: "Type Keyword",
    placeholderInstanceKeyword: "Instance type keyword",
    matched: "Matched",
    matchedCount: "{count} items",
    family: "Family",
    gpu: "GPU",
    placeholderFamily: "Family",
    placeholderGpu: "GPU",
    minCpu: "Min CPU",
    minMemory: "Min Memory (GB)",
    pagination: "Pagination",
    instanceType: "Instance Type",
    cpu: "CPU",
    memory: "Memory (GB)",
    price: "Price",
    image: "Image",
    recommendImagesOnly: "Recommended images only",
    bandwidthChargeType: "Bandwidth Charge Type",
    bandwidthByBandwidth: "By Bandwidth",
    bandwidthByTraffic: "By Traffic",
    bandwidth: "Bandwidth (Mbps)",
    systemDisk: "System Disk (GB)",
    dataDisk: "Data Disk (GB)",
    scheduledDestroy: "Scheduled Destroy",
    destroyTime: "Destroy Time",
    placeholderDatetime: "Select datetime",
    userDataScript: "User Data Script",
    userData: "User Data",
    placeholderUserData: "#!/bin/bash",
    overviewTitle: "Instance Overview",
    overviewHint: "Select an instance type to see details.",
    overviewRegion: "Region",
    overviewZone: "Zone",
    overviewInstanceType: "Instance Type",
    overviewFamily: "Family",
    overviewCpu: "CPU",
    overviewMemory: "Memory",
    overviewGpu: "GPU",
    overviewImage: "Image",
    overviewSystemDisk: "System Disk",
    overviewDataDisk: "Data Disk",
    overviewBandwidth: "Bandwidth",
    overviewScheduledDestroy: "Scheduled Destroy",
    overviewUserDataEnabled: "User Data Enabled",
    yes: "Yes",
    no: "No",
    priceInstance: "Instance",
    priceBandwidth: "Bandwidth",
    priceSystemDisk: "System Disk",
    priceDataDisk: "Data Disk",
    calculatePrice: "Calculate Price",
    exportTemplate: "Export Template",
    importTemplate: "Import Template",
    credentialsTitle: "Credentials",
    secretId: "Secret ID",
    secretKey: "Secret Key",
    defaultRegion: "Default Region",
    cancel: "Cancel",
    save: "Save",
    errorCredentialRequired: "SecretId/SecretKey required",
    errorExportHint: "Complete the form before exporting.",
    errorPricing: "Pricing error: {message}",
    errorInstanceUnavailable: "Instance type {instanceType} is not available in region {region}.",
    errorPriceQueryFailed: "Price query failed.",
    errorUnknown: "Unknown error.",
    // Billing management
    billingManagementTitle: "Billing Management",
    billingSdkUnavailable: "Billing SDK is unavailable, using mock data.",
    availableBalance: "Available Balance",
    frozenBalance: "Frozen Balance",
    totalBalance: "Total Balance",
    billList: "Bill List",
    placeholderBillingCycle: "Select billing cycle",
    refresh: "Refresh",
    billId: "Bill ID",
    productName: "Product Name",
    billingCycle: "Billing Cycle",
    billDate: "Bill Date",
    totalCost: "Total Cost",
    cashPayAmount: "Cash Payment",
    voucherPayAmount: "Voucher Payment",
    taxAmount: "Tax Amount",
    currency: "Currency",
    actions: "Actions",
    view: "View",
    billDetail: "Bill Detail",
    noData: "No data",
    close: "Close",
    errorLoadBills: "Failed to load bills",
    errorLoadBalance: "Failed to load balance",
  },
  zh: {
    connection: "连接状态",
    statusConnected: "已连接",
    statusNotConnected: "未连接",
    statusHint: "请先填写凭证。",
    statusSaved: "凭证已保存，正在加载地域...",
    credentials: "凭证",
    stepRegionTitle: "地域",
    stepRegionDesc: "选择地域与计费方式",
    stepInstanceTitle: "实例",
    stepInstanceDesc: "筛选并选择规格",
    stepStorageTitle: "存储与网络",
    stepStorageDesc: "磁盘与带宽",
    stepReviewTitle: "确认",
    stepReviewDesc: "高级选项与价格",
    step1Title: "步骤 1 - 地域",
    step2Title: "步骤 2 - 实例列表",
    step3Title: "步骤 3 - 存储与网络",
    step4Title: "步骤 4 - 确认与价格",
    refreshLists: "刷新列表",
    refreshPrices: "刷新价格",
    menuPurchase: "购买",
    menuInstances: "服务器管理",
    menuBilling: "账单管理",
    region: "地域",
    instanceChargeType: "实例计费方式",
    instanceChargePostpaid: "按量计费",
    instanceChargeSpot: "竞价",
    pricingZoneAuto: "计费可用区（自动）",
    auto: "自动",
    placeholderSelect: "请选择",
    selectedType: "已选规格",
    placeholderPickFromList: "从列表选择",
    typeKeyword: "规格关键词",
    placeholderInstanceKeyword: "输入规格关键词",
    matched: "匹配数量",
    matchedCount: "{count} 项",
    family: "规格族",
    gpu: "GPU",
    placeholderFamily: "规格族",
    placeholderGpu: "GPU",
    minCpu: "最小 CPU",
    minMemory: "最小内存 (GB)",
    pagination: "分页",
    instanceType: "实例规格",
    cpu: "CPU",
    memory: "内存 (GB)",
    price: "价格",
    image: "镜像",
    recommendImagesOnly: "仅看推荐镜像",
    bandwidthChargeType: "带宽计费方式",
    bandwidthByBandwidth: "按带宽",
    bandwidthByTraffic: "按流量",
    bandwidth: "带宽 (Mbps)",
    systemDisk: "系统盘 (GB)",
    dataDisk: "数据盘 (GB)",
    scheduledDestroy: "定时销毁",
    destroyTime: "销毁时间",
    placeholderDatetime: "请选择时间",
    userDataScript: "用户数据脚本",
    userData: "用户数据",
    placeholderUserData: "#!/bin/bash",
    overviewTitle: "实例概览",
    overviewHint: "请选择实例规格查看详情。",
    overviewRegion: "地域",
    overviewZone: "可用区",
    overviewInstanceType: "实例规格",
    overviewFamily: "规格族",
    overviewCpu: "CPU",
    overviewMemory: "内存",
    overviewGpu: "GPU",
    overviewImage: "镜像",
    overviewSystemDisk: "系统盘",
    overviewDataDisk: "数据盘",
    overviewBandwidth: "带宽",
    overviewScheduledDestroy: "定时销毁",
    overviewUserDataEnabled: "启用用户数据",
    yes: "是",
    no: "否",
    priceInstance: "实例",
    priceBandwidth: "带宽",
    priceSystemDisk: "系统盘",
    priceDataDisk: "数据盘",
    calculatePrice: "计算价格",
    exportTemplate: "导出模板",
    importTemplate: "导入模板",
    credentialsTitle: "凭证",
    secretId: "Secret ID",
    secretKey: "Secret Key",
    defaultRegion: "默认地域",
    cancel: "取消",
    save: "保存",
    errorCredentialRequired: "需要填写 SecretId/SecretKey",
    errorExportHint: "请先完成配置后再导出。",
    errorPricing: "价格查询失败：{message}",
    errorInstanceUnavailable: "实例规格 {instanceType} 在地域 {region} 不可用。",
    errorPriceQueryFailed: "价格查询失败。",
    errorUnknown: "未知错误。",
    // 账单管理
    billingManagementTitle: "账单管理",
    billingSdkUnavailable: "账单 SDK 不可用，使用模拟数据。",
    availableBalance: "可用余额",
    frozenBalance: "冻结余额",
    totalBalance: "总余额",
    billList: "账单列表",
    placeholderBillingCycle: "选择账单周期",
    refresh: "刷新",
    billId: "账单 ID",
    productName: "产品名称",
    billingCycle: "账单周期",
    billDate: "账单日期",
    totalCost: "总费用",
    cashPayAmount: "现金支付",
    voucherPayAmount: "代金券支付",
    taxAmount: "税额",
    currency: "货币",
    actions: "操作",
    view: "查看",
    billDetail: "账单详情",
    noData: "无数据",
    close: "关闭",
    errorLoadBills: "加载账单失败",
    errorLoadBalance: "加载余额失败",
  },
};

const lang = ref(localStorage.getItem("cvm.lang") || "zh");
const statusKey = ref("statusHint");
const statusArgs = ref({});
const statusText = computed(() => t(statusKey.value, statusArgs.value));

function t(key, params = {}) {
  const dictionary = translations[lang.value] || translations.en;
  const template = dictionary[key] || translations.en[key] || key;
  return template.replace(/\{(\w+)\}/g, (match, token) => {
    if (params[token] === undefined || params[token] === null) {
      return match;
    }
    return String(params[token]);
  });
}

const credentialsVisible = ref(false);
const credentials = reactive({
  secretId: localStorage.getItem("cvm.secretId") || "",
  secretKey: localStorage.getItem("cvm.secretKey") || "",
});

const form = reactive({
  region: "",
  zone: "",
  instanceChargeType: "POSTPAID_BY_HOUR",
  instanceType: "",
  imageId: "",
  systemDiskGb: 20,
  dataDiskGb: 0,
  bandwidthMbps: 1,
  bandwidthChargeType: "BANDWIDTH_POSTPAID_BY_HOUR",
  scheduledDestroy: false,
  scheduledDestroyTime: "",
  scheduledDestroyMinutes: 10,
  userDataEnabled: false,
  userData: "",
  instanceName: "",
  password: "",
  instanceCount: 1,
});

const filters = reactive({
  instanceType: "",
  family: "",
  cpuMin: 0,
  cpuMax: 512,
  memoryMin: 0,
  memoryMax: 2048,
  gpu: "",
  inStockOnly: true,
});

const price = reactive({
  instancePrice: null,
  bandwidthPrice: null,
  systemDiskPrice: null,
  dataDiskPrice: null,
  totalPrice: null,
  currency: null,
});

const page = ref(1);
const pageSize = ref(10);
const priceMap = reactive({});
const creatingInstance = ref(false);
let priceTimer = null;

const activeStep = computed(() => {
  if (!form.region) {
    return 0;
  }
  if (!form.instanceType) {
    return 1;
  }
  if (!price.totalPrice) {
    return 2;
  }
  return 3;
});

function formatInstanceLabel(item) {
  return `${item.instanceType || "-"} | ${item.cpu || 0} vCPU / ${item.memory || 0} GB`;
}

function formatImageLabel(item) {
  return `${item.imageName || item.imageId} (${item.imageOsName || "OS"})`;
}

function formatErrorMessage(error) {
  const raw = error?.message || t("errorUnknown");

  if (error && error.code === "INSTANCE_TYPE_UNAVAILABLE") {
    const instanceType = error.payload?.instanceType || form.instanceType || "-";
    const region = error.payload?.region || form.region || "-";
    return t("errorInstanceUnavailable", { instanceType, region });
  }
  if (error && error.code === "PRICE_QUERY_FAILED") {
    return t("errorPriceQueryFailed");
  }

  const lower = String(raw).toLowerCase();
  if (lower.includes("understocked") || lower.includes("out of stock")) {
    return "该规格当前库存不足，建议切换可用区或降低配置后重试。";
  }
  if (lower.includes("balance of account is not enough") || lower.includes("balance is not enough")) {
    return "账户余额不足，请先充值或降低配置后再购买。";
  }
  if (lower.includes("image") && lower.includes("not available")) {
    return "当前镜像与实例规格不兼容，请更换镜像或规格。";
  }
  if (lower.includes("credentials are not configured") || lower.includes("authfailure")) {
    return "凭证无效或未配置，请先在右上角更新腾讯云凭证。";
  }

  return raw;
}

function selectInstance(row) {
  if (row && row.instanceType) {
    form.instanceType = row.instanceType;
  }
}

async function loginWithCredentials() {
  try {
    await saveCredentials();
    accessReady.value = true;
  } catch (error) {
    ElMessage.error(`登录失败: ${error?.message || error}`);
  }
}

function openCredentials() {
  credentialsVisible.value = true;
}

async function saveCredentials({ loadAfter = true } = {}) {
  if (!credentials.secretId || !credentials.secretKey) {
    ElMessage.error(t("errorCredentialRequired"));
    return;
  }
  await postJson("/api/credentials", {
    secretId: credentials.secretId,
    secretKey: credentials.secretKey,
    // 默认地域由登录后获取到的地域列表首项自动决定
    defaultRegion: "",
  });
  localStorage.setItem("cvm.secretId", credentials.secretId);
  localStorage.setItem("cvm.secretKey", credentials.secretKey);
  credentialsVisible.value = false;
  connected.value = true;
  statusKey.value = "statusSaved";
  // 保留地区记忆（cvm.cache.region.data），只清理接口缓存
  invalidateCache("regions");
  invalidateCache("zones.");
  invalidateCache("instanceTypes.");
  invalidateCache("images.");
  if (loadAfter) {
    await loadRegions();
  }
}

async function loadRegions() {
  const cached = readCache("regions");
  const data = cached || (await getJson("/api/regions"));
  regions.value = data;
  if (!cached) {
    writeCache("regions", data);
  }

  // 保存到地区专用缓存
  if (data.length > 0) {
    // 如果没有选中地区，默认第一个
    if (!selectedRegion.value) {
      selectedRegion.value = data[0] || "";
    }
    await loadRegionZoneMap();
  }

  if (!form.region) {
    form.region = selectedRegion.value || data[0] || "";
  }
  await loadRegionDependencies();
}

async function loadRegionState() {
  const cached = loadRegionData();
  if (cached.fromCache && cached.regions.length > 0) {
    regions.value = cached.regions;
    selectedRegion.value = cached.selectedRegion || cached.regions[0] || "";
    regionZoneMap.value = cached.regionZoneMap || {};
    // 如果有缓存的区域映射，加载当前地区的区域
    if (!form.region) {
      form.region = selectedRegion.value;
    }
    if (connected.value && form.region) {
      await loadRegionDependencies();
    }
  } else if (connected.value) {
    // 如果没有缓存或缓存过期，从API加载
    await loadRegions();
  }
}

async function loadRegionZoneMap() {
  if (!regions.value.length) return;

  const zoneMap = { ...regionZoneMap.value };
  // 并行加载所有地区的区域列表（可优化为按需加载）
  const promises = regions.value.slice(0, 5).map(async region => {
    if (zoneMap[region]?.length) {
      return;
    }
    try {
      const zones = await getJson(`/api/zones?region=${encodeURIComponent(region)}`);
      zoneMap[region] = zones;
    } catch (error) {
      console.warn(`Failed to load zones for ${region}:`, error);
      zoneMap[region] = [];
    }
  });

  await Promise.all(promises);
  regionZoneMap.value = zoneMap;

  // 保存到缓存
  saveRegionData(regions.value, selectedRegion.value, regionZoneMap.value);
}

async function loadRegionStats() {
  if (activeView.value !== "instances") {
    return;
  }
  try {
    const stats = await getRegionInstanceCounts();
    regionStats.value = stats || {};
    return;
  } catch (error) {
    // 后端暂未实现 /api/regions/instance-counts 时，前端遍历所有地区统计
    const nextStats = {};
    const regionList = [...(regions.value || [])];
    await Promise.all(
      regionList.map(async (region) => {
        try {
          const data = await getInstances(region, 1, 1);
          nextStats[region] = Number(data?.totalCount || 0);
        } catch {
          nextStats[region] = 0;
        }
      })
    );
    regionStats.value = nextStats;
  }
}

function handleInstanceRegionChange(region) {
  if (!region) return;
  selectedRegion.value = region;
  if (activeView.value === "purchase") {
    form.region = region;
  }
}


async function loadRegionDependencies() {
  await Promise.all([loadZones(), loadInstanceTypes(), loadImages()]);
  schedulePriceQuote();
  refreshPrices();
}

async function loadZones() {
  if (!form.region) {
    return;
  }
  const key = `zones.${form.region}`;
  const cached = readCache(key);
  const data = cached || (await getJson(`/api/zones?region=${encodeURIComponent(form.region)}`));
  zones.value = data;
  if (!cached) {
    writeCache(key, data);
  }
  // 地域切换后，如果当前可用区不在新地域可用区列表内，自动切换到首个可用区
  if (!form.zone || !data.includes(form.zone)) {
    form.zone = data[0] || "";
  }
}

async function loadInstanceTypes() {
  if (!form.region) {
    return;
  }
  const key = `instanceTypes.${form.region}`;
  const cached = readCache(key);
  const data = cached || (await getJson(`/api/instance-types?region=${encodeURIComponent(form.region)}`));
  instanceTypes.value = data;
  if (!cached) {
    writeCache(key, data);
  }
  if (!form.instanceType && data.length) {
    form.instanceType = data[0].instanceType;
  }
}

async function loadImages() {
  if (!form.region) {
    return;
  }
  const key = `images.${form.region}`;
  const cached = readCache(key);
  const data = cached || (await getJson(`/api/images?region=${encodeURIComponent(form.region)}`));
  images.value = data;
  if (!cached) {
    writeCache(key, data);
  }
  if (!form.imageId && data.length) {
    form.imageId = data[0].imageId;
  }
}

function buildPricePayload(instanceType) {
  if (!form.region || !form.zone || !instanceType) {
    return null;
  }
  return {
    region: form.region,
    zone: form.zone,
    instanceType,
    imageId: form.imageId,
    systemDiskGb: form.systemDiskGb,
    dataDiskGb: form.dataDiskGb,
    bandwidthMbps: form.bandwidthMbps,
    bandwidthChargeType: form.bandwidthChargeType,
    instanceChargeType: form.instanceChargeType,
    userData: form.userDataEnabled ? form.userData : "",
    scheduledDestroy: form.scheduledDestroy,
  };
}

function priceCacheKey(payload) {
  return [
    "price",
    payload.region,
    payload.zone,
    payload.instanceType,
    payload.instanceChargeType,
    payload.bandwidthChargeType,
    payload.systemDiskGb,
    payload.dataDiskGb,
    payload.bandwidthMbps,
  ].join(".");
}

async function quotePrice() {
  const payload = buildPricePayload(form.instanceType);
  if (!payload) {
    return;
  }
  const cached = readCache(priceCacheKey(payload));
  const response = cached || (await postJson("/api/price", payload));
  if (!cached) {
    writeCache(priceCacheKey(payload), response);
    if (response && response.zone && response.zone !== payload.zone) {
      writeCache(priceCacheKey({ ...payload, zone: response.zone }), response);
    }
  }
  if (response && response.zone) {
    const previousZone = form.zone;
    if (response.zone !== previousZone) {
      form.zone = response.zone;
      refreshPrices();
    }
  }
  Object.assign(price, response);
}

function schedulePriceQuote() {
  if (priceTimer) {
    clearTimeout(priceTimer);
  }
  priceTimer = setTimeout(() => {
    quotePrice().catch((error) => {
      ElMessage.error(t("errorPricing", { message: formatErrorMessage(error) }));
    });
  }, 500);
}

const filteredItems = computed(() => {
  return instanceTypes.value
    .filter((item) => {
      const type = String(item.instanceType || "").toLowerCase();
      const family = String(item.instanceFamily || "").toLowerCase();
      const gpu = String(item.gpu || "").toLowerCase();

      if (form.zone && item.zone && item.zone !== form.zone) {
        return false;
      }
      if (filters.inStockOnly && item.hasStock === false) {
        return false;
      }
      if (filters.instanceType && !type.includes(filters.instanceType.toLowerCase())) {
        return false;
      }
      if (filters.family && !family.includes(filters.family.toLowerCase())) {
        return false;
      }
      if (filters.gpu && !gpu.includes(filters.gpu.toLowerCase())) {
        return false;
      }
      if ((item.cpu || 0) < filters.cpuMin || (item.cpu || 0) > filters.cpuMax) {
        return false;
      }
      if ((item.memory || 0) < filters.memoryMin || (item.memory || 0) > filters.memoryMax) {
        return false;
      }
      return true;
    })
    .sort((a, b) => (a.memory || 0) - (b.memory || 0));
});

const displayedImages = computed(() => {
  if (!onlyRecommendedImages.value) {
    return images.value;
  }
  const recommended = images.value.filter((item) => item.recommended !== false);
  return recommended.length ? recommended : images.value;
});

const pagedItems = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  return filteredItems.value.slice(start, start + pageSize.value);
});

const overviewText = computed(() => {
  const selected = instanceTypes.value.find((item) => item.instanceType === form.instanceType);
  if (!selected) {
    return t("overviewHint");
  }
  return [
    `${t("overviewRegion")}: ${form.region || "-"}`,
    `${t("overviewZone")}: ${form.zone || "-"}`,
    `${t("overviewInstanceType")}: ${selected.instanceType || "-"}`,
    `${t("overviewFamily")}: ${selected.instanceFamily || "-"}`,
    `${t("overviewCpu")}: ${selected.cpu || "-"}`,
    `${t("overviewMemory")}: ${selected.memory || "-"} GB`,
    `${t("overviewGpu")}: ${selected.gpu || "-"}`,
    `${t("overviewImage")}: ${form.imageId || "-"}`,
    `${t("overviewSystemDisk")}: ${form.systemDiskGb} GB`,
    `${t("overviewDataDisk")}: ${form.dataDiskGb} GB`,
    `${t("overviewBandwidth")}: ${form.bandwidthMbps} Mbps`,
    `${t("overviewScheduledDestroy")}: ${form.scheduledDestroy ? t("yes") : t("no")}`,
    `销毁时间(本地): ${form.scheduledDestroy ? formatScheduledDestroyTimeDisplay(form.scheduledDestroyTime) : "-"}`,
    `${t("overviewUserDataEnabled")}: ${form.userDataEnabled ? t("yes") : t("no")}`,
  ].join("\n");
});

async function refreshPrices() {
  priceMapClear();
  for (const item of pagedItems.value) {
    const payload = buildPricePayload(item.instanceType);
    if (!payload) {
      continue;
    }
    const cached = readCache(priceCacheKey(payload));
    if (cached && cached.instancePrice !== undefined) {
      priceMap[item.instanceType] = cached.instancePrice;
      continue;
    }
    postJson("/api/price", payload)
      .then((response) => {
        priceMap[item.instanceType] = response.instancePrice;
        writeCache(priceCacheKey(payload), response);
      })
      .catch(() => {});
  }
}

function priceMapClear() {
  Object.keys(priceMap).forEach((key) => {
    delete priceMap[key];
  });
}

function normalizeScheduledDestroyTime(value) {
  if (!value) {
    return "";
  }
  if (value instanceof Date) {
    return value.toISOString().replace(/\.\d{3}Z$/, "Z");
  }
  return String(value).replace(/\.\d{3}Z$/, "Z");
}

function formatScheduledDestroyTimeDisplay(value) {
  if (!value) {
    return "-";
  }
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) {
    return String(value);
  }
  return d.toLocaleString("zh-CN", { hour12: false });
}

function addMinutes(base, minutes) {
  const d = new Date(base);
  d.setMinutes(d.getMinutes() + minutes);
  return d;
}

function updateScheduledDestroyTimeFromMinutes() {
  const mins = Number(form.scheduledDestroyMinutes || 10);
  const normalized = Math.max(10, Math.floor(mins));
  form.scheduledDestroyMinutes = normalized;
  if (form.scheduledDestroy) {
    form.scheduledDestroyTime = addMinutes(new Date(), normalized);
  }
}

function updateMinutesFromScheduledDestroyTime() {
  if (!form.scheduledDestroy || !form.scheduledDestroyTime) {
    return;
  }
  const target = new Date(form.scheduledDestroyTime);
  const diff = Math.ceil((target.getTime() - Date.now()) / 60000);
  form.scheduledDestroyMinutes = Math.max(10, diff || 10);
}

async function quickBuy() {
  if (!form.password || !form.password.trim()) {
    ElMessage.error("请先填写实例密码再购买");
    return;
  }
  if (form.scheduledDestroy && !form.scheduledDestroyTime) {
    ElMessage.error("已开启定时销毁，请选择销毁时间");
    return;
  }
  const request = {
    region: form.region,
    zone: form.zone,
    instanceType: form.instanceType,
    imageId: form.imageId,
    instanceName: form.instanceName?.trim() || `cvm-${Date.now()}`,
    systemDiskGb: form.systemDiskGb,
    dataDiskGb: form.dataDiskGb,
    bandwidthMbps: form.bandwidthMbps,
    bandwidthChargeType: form.bandwidthChargeType,
    instanceChargeType: form.instanceChargeType,
    instanceCount: Math.max(1, Number(form.instanceCount || 1)),
    password: form.password,
    userData: form.userDataEnabled ? form.userData : "",
    scheduledDestroy: form.scheduledDestroy,
    scheduledDestroyTime: normalizeScheduledDestroyTime(form.scheduledDestroyTime),
  };

  creatingInstance.value = true;
  try {
    const result = await postJson('/api/instances', request);
    ElMessage.success(`购买成功，实例ID：${result.instanceId}`);
    activeView.value = 'instances';
  } catch (error) {
    ElMessage.error(`购买失败：${formatErrorMessage(error)}`);
  } finally {
    creatingInstance.value = false;
  }
}

function exportTemplate() {
  const payload = buildPricePayload(form.instanceType);
  if (!payload) {
    ElMessage.error(t("errorExportHint"));
    return;
  }
  const template = {
    ...payload,
    scheduledDestroyTime: form.scheduledDestroyTime,
    userDataEnabled: form.userDataEnabled,
    instanceCount: form.instanceCount,
  };
  const blob = new Blob([JSON.stringify(template, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `cvm-template-${Date.now()}.json`;
  link.click();
  URL.revokeObjectURL(url);
}

function importTemplate(file) {
  const reader = new FileReader();
  reader.onload = () => {
    const template = JSON.parse(reader.result);
    Object.assign(form, {
      region: template.region || "",
      zone: template.zone || "",
      instanceType: template.instanceType || "",
      imageId: template.imageId || "",
      systemDiskGb: template.systemDiskGb || 20,
      dataDiskGb: template.dataDiskGb || 0,
      bandwidthMbps: template.bandwidthMbps || 0,
      bandwidthChargeType: template.bandwidthChargeType || "BANDWIDTH_POSTPAID_BY_HOUR",
      instanceChargeType: template.instanceChargeType || "POSTPAID_BY_HOUR",
      userData: template.userData || "",
      userDataEnabled: Boolean(template.userDataEnabled),
      scheduledDestroy: Boolean(template.scheduledDestroy),
      scheduledDestroyTime: template.scheduledDestroyTime || "",
      instanceCount: Math.max(1, Number(template.instanceCount || 1)),
    });
    schedulePriceQuote();
    refreshPrices();
  };
  reader.readAsText(file);
  return false;
}

async function refreshAll() {
  invalidateCache("");
  await loadRegions();
}

watch(
  () => form.region,
  async (region, prevRegion) => {
    if (!region) {
      return;
    }

    // 地域变化时先清空可用区，避免残留旧地域的 zone
    if (prevRegion && prevRegion !== region) {
      form.zone = "";
    }

    if (activeView.value === "purchase" && region !== selectedRegion.value) {
      selectedRegion.value = region;
    }
    if (!connected.value) {
      return;
    }
    await loadRegionDependencies();
  }
);

watch(
  () => selectedRegion.value,
  async (region) => {
    if (!region) {
      return;
    }
    if (activeView.value === "purchase" && form.region !== region) {
      form.region = region;
      return;
    }
    saveRegionData(regions.value, region, regionZoneMap.value);
    if (activeView.value === "instances") {
      await loadRegionStats();
    }
  }
);

watch(
  () => activeView.value,
  async (view) => {
    if (view === "instances") {
      await loadRegionStats();
    }
  }
);

watch(
  () => [form.instanceChargeType, form.bandwidthChargeType, form.systemDiskGb, form.dataDiskGb, form.bandwidthMbps],
  () => {
    schedulePriceQuote();
    refreshPrices();
  }
);

watch(
  () => [form.instanceType, form.imageId, form.scheduledDestroy, form.userDataEnabled, form.userData],
  () => {
    schedulePriceQuote();
  }
);

watch(
  () => form.scheduledDestroy,
  (enabled) => {
    if (enabled && !form.scheduledDestroyTime) {
      updateScheduledDestroyTimeFromMinutes();
    }
  }
);

watch(
  () => [filters.instanceType, filters.family, filters.cpuMin, filters.cpuMax, filters.memoryMin, filters.memoryMax, filters.gpu, filters.inStockOnly],
  () => {
    page.value = 1;
    refreshPrices();
  }
);

watch(
  () => [page.value, pageSize.value],
  () => {
    refreshPrices();
  }
);

watch(lang, (value) => {
  localStorage.setItem("cvm.lang", value);
});

onMounted(async () => {
  if (credentials.secretId && credentials.secretKey) {
    try {
      connected.value = true;
      await saveCredentials({ loadAfter: false });
      accessReady.value = true;
    } catch (error) {
      accessReady.value = false;
    }
  }
  if (accessReady.value) {
    await loadRegionState();
  }
});
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f5f7fa;
}

.auth-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.auth-card {
  width: 420px;
}

.auth-title {
  font-weight: 600;
  font-size: 16px;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: #fff;
  padding: 12px 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.app-body {
  display: flex;
  min-height: calc(100vh - 64px);
  align-items: flex-start;
}

.app-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #ebeef5;
  padding-top: 8px;
  position: sticky;
  top: 64px;
  height: calc(100vh - 64px);
  overflow-y: auto;
  flex-shrink: 0;
}

.side-menu {
  border-right: none;
}

.app-main {
  flex: 1;
  padding: 16px;
  overflow-x: auto;
}

.region-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #e6a23c;
}
</style>
