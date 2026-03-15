const state = {
  regions: [],
  zones: [],
  instanceTypes: [],
  instanceCatalog: [],
  images: [],
  priceTimer: null,
  lang: "en",
  connected: false,
  statusKey: "statusHint",
  instancePrices: {},
  credentialsReady: false,
  instances: [],
  instanceTotal: 0,
  instancePage: 1,
  instancePageSize: 10,
  instancePriceMap: {},
  cacheTtlMs: 10 * 60 * 1000,
};

const elements = {
  secretId: document.getElementById("secretId"),
  secretKey: document.getElementById("secretKey"),
  defaultRegion: document.getElementById("defaultRegion"),
  saveCredentials: document.getElementById("saveCredentials"),
  refreshLists: document.getElementById("refreshLists"),
  regionSelect: document.getElementById("regionSelect"),
  zoneSelect: document.getElementById("zoneSelect"),
  instanceTypeSelect: document.getElementById("instanceTypeSelect"),
  imageSelect: document.getElementById("imageSelect"),
  systemDisk: document.getElementById("systemDisk"),
  dataDisk: document.getElementById("dataDisk"),
  bandwidth: document.getElementById("bandwidth"),
  bandwidthChargeType: document.getElementById("bandwidthChargeType"),
  instanceChargeType: document.getElementById("instanceChargeType"),
  scheduledDestroy: document.getElementById("scheduledDestroy"),
  scheduledDestroyTime: document.getElementById("scheduledDestroyTime"),
  scheduledDestroyTimeWrap: document.getElementById("scheduledDestroyTimeWrap"),
  userDataEnabled: document.getElementById("userDataEnabled"),
  userDataBlock: document.getElementById("userDataBlock"),
  userData: document.getElementById("userData"),
  quotePrice: document.getElementById("quotePrice"),
  exportTemplate: document.getElementById("exportTemplate"),
  importTemplate: document.getElementById("importTemplate"),
  instanceOverview: document.getElementById("instanceOverview"),
  connectionStatus: document.getElementById("connectionStatus"),
  statusText: document.getElementById("statusText"),
  priceInstance: document.getElementById("priceInstance"),
  priceBandwidth: document.getElementById("priceBandwidth"),
  priceSystemDisk: document.getElementById("priceSystemDisk"),
  priceDataDisk: document.getElementById("priceDataDisk"),
  priceTotal: document.getElementById("priceTotal"),
  priceNote: document.getElementById("priceNote"),
  languageSelect: document.getElementById("languageSelect"),
  serverListBody: document.getElementById("serverListBody"),
  serverPrev: document.getElementById("serverPrev"),
  serverNext: document.getElementById("serverNext"),
  serverPageInfo: document.getElementById("serverPageInfo"),
  serverPageSize: document.getElementById("serverPageSize"),
  filterInstanceType: document.getElementById("filterInstanceType"),
  filterFamily: document.getElementById("filterFamily"),
  filterCpu: document.getElementById("filterCpu"),
  filterMemory: document.getElementById("filterMemory"),
  filterGpu: document.getElementById("filterGpu"),
  refreshServerPrices: document.getElementById("refreshServerPrices"),
  credentialsModal: document.getElementById("credentialsModal"),
  openCredentials: document.getElementById("openCredentials"),
  closeCredentials: document.getElementById("closeCredentials"),
  modalBackdrop: document.getElementById("modalBackdrop"),
};

const i18n = {
  en: {
    title: "Tencent CVM Configurator",
    eyebrow: "Tencent CVM Manager",
    heroTitle: "Build a pricing template in minutes",
    heroSubtitle:
      "Configure region, zone, instance type, storage, bandwidth, and user data. Export the full template for reuse.",
    connectionTitle: "Connection",
    statusHint: "Provide credentials to start.",
    statusReady: "Ready to configure pricing.",
    statusSaved: "Credentials saved. Loading regions...",
    statusExportHint: "Complete the form before exporting.",
    statusPricingError: "Pricing error",
    statusRefreshError: "Refresh error",
    statusRegionError: "Region error",
    statusImportError: "Import error",
    statusCredentialError: "Credential error",
    statusConnected: "Connected",
    statusNotConnected: "Not connected",
    yes: "Yes",
    no: "No",
    languageLabel: "Language",
    openCredentials: "Credentials",
    close: "Close",
    credentialsTitle: "Credentials",
    secretId: "Secret ID",
    secretKey: "Secret Key",
    defaultRegion: "Default Region",
    secretIdPlaceholder: "AKID...",
    secretKeyPlaceholder: "Secret key",
    defaultRegionPlaceholder: "ap-guangzhou",
    saveCredentials: "Save Credentials",
    configurationTitle: "Configuration",
    refreshLists: "Refresh Lists",
    region: "Region",
    zone: "Zone",
    instanceType: "Instance Type",
    image: "Image",
    systemDisk: "System Disk (GB)",
    systemDiskNote: "Minimum 20 GB.",
    dataDisk: "Data Disk (GB)",
    bandwidth: "Bandwidth (Mbps)",
    bandwidthChargeType: "Bandwidth Charge Type",
    bandwidthByBandwidth: "By Bandwidth",
    bandwidthByTraffic: "By Traffic",
    instanceChargeType: "Instance Charge Type",
    instanceChargePostpaid: "Postpaid",
    instanceChargeSpot: "Spot",
    scheduledDestroy: "Scheduled Destroy",
    scheduledDestroyTime: "Destroy Time",
    userDataToggle: "User Data Script",
    userData: "User Data Script (plain text)",
    userDataPlaceholder: "#!/bin/bash",
    instanceOverview: "Instance Overview",
    instanceOverviewHint: "Select an instance type to see details.",
    calculatePrice: "Calculate Price",
    exportTemplate: "Export Template",
    importTemplate: "Import Template",
    priceBreakdown: "Price Breakdown",
    priceInstance: "Instance",
    priceBandwidth: "Bandwidth",
    priceSystemDisk: "System Disk",
    priceDataDisk: "Data Disk",
    priceTotal: "Total Price",
    scheduledDestroyNote: "Scheduled destroy enabled.",
    instanceDetailInstance: "Instance Type",
    instanceDetailZone: "Zone",
    instanceDetailFamily: "Instance Family",
    instanceDetailCpu: "CPU",
    instanceDetailMemory: "Memory",
    instanceDetailGpu: "GPU",
    instanceDetailGpuCount: "GPU Count",
    instanceDetailFpga: "FPGA",
    serverList: "Instance List",
    refreshServerPrices: "Refresh Prices",
    prevPage: "Prev",
    nextPage: "Next",
    pageSize: "Page Size",
    listInstanceType: "Instance Type",
    listInstanceFamily: "Family",
    listCpu: "CPU",
    listMemory: "Memory",
    listGpu: "GPU",
    listPrice: "Price",
    serverEmpty: "No instances found.",
  },

  zh: {
    title: "腾讯云 CVM 配置器",
    eyebrow: "腾讯云 CVM 管理",
    heroTitle: "几分钟生成价格模板",
    heroSubtitle: "配置地域、可用区、实例规格、存储、带宽和用户数据，导出完整模板复用。",
    connectionTitle: "连接",
    statusHint: "请先填写凭证。",
    statusReady: "可以开始配置价格。",
    statusSaved: "凭证已保存，正在加载地域...",
    statusExportHint: "完成表单后再导出。",
    statusPricingError: "价格查询失败",
    statusRefreshError: "刷新失败",
    statusRegionError: "地域加载失败",
    statusImportError: "导入失败",
    statusCredentialError: "凭证错误",
    statusConnected: "已连接",
    statusNotConnected: "未连接",
    yes: "是",
    no: "否",
    languageLabel: "语言",
    openCredentials: "凭证",
    close: "关闭",
    credentialsTitle: "凭证",
    secretId: "Secret ID",
    secretKey: "Secret Key",
    defaultRegion: "默认地域",
    secretIdPlaceholder: "AKID...",
    secretKeyPlaceholder: "Secret key",
    defaultRegionPlaceholder: "ap-guangzhou",
    saveCredentials: "保存凭证",
    configurationTitle: "配置",
    refreshLists: "刷新列表",
    region: "地域",
    zone: "可用区",
    instanceType: "实例规格",
    image: "镜像",
    systemDisk: "系统盘 (GB)",
    systemDiskNote: "最小 20 GB。",
    dataDisk: "数据盘 (GB)",
    bandwidth: "带宽 (Mbps)",
    bandwidthChargeType: "带宽计费方式",
    bandwidthByBandwidth: "按带宽",
    bandwidthByTraffic: "按流量",
    instanceChargeType: "实例计费方式",
    instanceChargePostpaid: "按量计费",
    instanceChargeSpot: "竞价实例",
    scheduledDestroy: "定时销毁",
    scheduledDestroyTime: "销毁时间",
    userDataToggle: "开机脚本",
    userData: "用户数据脚本(纯文本)",
    userDataPlaceholder: "#!/bin/bash",
    instanceOverview: "实例总览",
    instanceOverviewHint: "请选择实例规格查看详情。",
    calculatePrice: "计算价格",
    exportTemplate: "导出模板",
    importTemplate: "导入模板",
    priceBreakdown: "价格明细",
    priceInstance: "实例",
    priceBandwidth: "带宽",
    priceSystemDisk: "系统盘",
    priceDataDisk: "数据盘",
    priceTotal: "总价",
    scheduledDestroyNote: "定时销毁将在到期时触发。",
    instanceDetailInstance: "实例",
    instanceDetailZone: "可用区",
    instanceDetailFamily: "实例族",
    instanceDetailCpu: "CPU",
    instanceDetailMemory: "内存",
    instanceDetailGpu: "GPU",
    instanceDetailGpuCount: "GPU 数量",
    instanceDetailFpga: "FPGA",
    serverList: "实例列表",
    refreshServerPrices: "刷新价格",
    prevPage: "上一页",
    nextPage: "下一页",
    pageSize: "每页数量",
    listInstanceType: "实例规格",
    listInstanceFamily: "规格族",
    listCpu: "CPU",
    listMemory: "内存",
    listGpu: "GPU",
    listPrice: "价格",
    serverEmpty: "暂无实例。",
  },
};


function t(key) {
  const dict = i18n[state.lang] || i18n.en;
  return dict[key] || key;
}

function setStatus(text, level) {
  elements.statusText.textContent = text;
  state.connected = level === "ok";
  elements.connectionStatus.textContent = state.connected ? t("statusConnected") : t("statusNotConnected");
  elements.connectionStatus.style.background = state.connected ? "#1f5f5b" : "#3b3f5c";
}

function setStatusKey(key, level) {
  state.statusKey = key;
  setStatus(t(key), level);
}

function setPriceValue(element, value, currency) {
  if (value === null || value === undefined) {
    element.textContent = "-";
    return;
  }
  const currencyText = currency ? ` ${currency}` : "";
  element.textContent = `${value}${currencyText}`;
}

function setOptions(select, items, getValue, getLabel) {
  select.innerHTML = "";
  items.forEach((item) => {
    const option = document.createElement("option");
    option.value = getValue(item);
    option.textContent = getLabel(item);
    select.appendChild(option);
  });
}

function applyI18n(lang) {
  state.lang = lang;
  localStorage.setItem("cvm.lang", lang);
  document.documentElement.lang = lang === "zh" ? "zh" : "en";
  document.title = t("title");
  document.querySelectorAll("[data-i18n]").forEach((node) => {
    const key = node.getAttribute("data-i18n");
    node.textContent = t(key);
  });
  document.querySelectorAll("[data-i18n-placeholder]").forEach((node) => {
    const key = node.getAttribute("data-i18n-placeholder");
    node.setAttribute("placeholder", t(key));
  });
  const bandwidthOptions = elements.bandwidthChargeType.options;
  if (bandwidthOptions.length >= 2) {
    bandwidthOptions[0].textContent = t("bandwidthByBandwidth");
    bandwidthOptions[1].textContent = t("bandwidthByTraffic");
  }
  const instanceOptions = elements.instanceChargeType.options;
  if (instanceOptions.length >= 2) {
    instanceOptions[0].textContent = t("instanceChargePostpaid");
    instanceOptions[1].textContent = t("instanceChargeSpot");
  }
  elements.connectionStatus.textContent = state.connected ? t("statusConnected") : t("statusNotConnected");
  elements.statusText.textContent = t(state.statusKey);
  updateInstanceOverview();
}

async function postJson(url, payload) {
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}

async function getJson(url) {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Request failed: ${response.status}`);
  }
  return response.json();
}

function cacheKey(key) {
  return `cvm.cache.${key}`;
}

function writeCache(key, value) {
  const payload = { ts: Date.now(), value };
  localStorage.setItem(cacheKey(key), JSON.stringify(payload));
}

function readCache(key) {
  const raw = localStorage.getItem(cacheKey(key));
  if (!raw) {
    return null;
  }
  try {
    const payload = JSON.parse(raw);
    if (!payload.ts || Date.now() - payload.ts > state.cacheTtlMs) {
      return null;
    }
    return payload.value;
  } catch (error) {
    return null;
  }
}

function invalidateCache(prefix) {
  Object.keys(localStorage).forEach((key) => {
    if (key.startsWith(cacheKey(prefix))) {
      localStorage.removeItem(key);
    }
  });
}

function instancePriceCacheKey(payload) {
  return [
    "price",
    payload.region,
    payload.zone,
    payload.instanceType,
    payload.instanceChargeType || "POSTPAID_BY_HOUR",
    payload.bandwidthChargeType || "BANDWIDTH_POSTPAID_BY_HOUR",
    payload.systemDiskGb || 0,
    payload.dataDiskGb || 0,
    payload.bandwidthMbps || 0,
  ].join(".");
}

async function saveCredentials() {
  const secretId = elements.secretId.value.trim();
  const secretKey = elements.secretKey.value.trim();
  const defaultRegion = elements.defaultRegion.value.trim();
  if (!secretId || !secretKey) {
    setStatus(`${t("statusCredentialError")}: SecretId/SecretKey required`, "error");
    return;
  }
  await postJson("/api/credentials", {
    secretId,
    secretKey,
    defaultRegion,
  });
  localStorage.setItem("cvm.secretId", secretId);
  localStorage.setItem("cvm.secretKey", secretKey);
  localStorage.setItem("cvm.defaultRegion", defaultRegion);
  state.credentialsReady = true;
  invalidateCache("");
  setStatusKey("statusSaved", "ok");
  closeCredentialsModal();
  await loadRegions();
}

async function loadRegions() {
  const cached = readCache("regions");
  if (cached) {
    state.regions = cached;
  } else {
    const regions = await getJson("/api/regions");
    state.regions = regions;
    writeCache("regions", regions);
  }
  const regions = state.regions;
  state.regions = regions;
  setOptions(elements.regionSelect, regions, (item) => item, (item) => item);
  if (regions.length > 0) {
    if (!elements.regionSelect.value) {
      elements.regionSelect.value = regions[0];
    }
    await loadRegionDependencies(elements.regionSelect.value);
  }
}

async function loadRegionDependencies(region) {
  await Promise.all([loadZones(region), loadInstanceTypes(region), loadImages(region)]);
  updateInstanceOverview();
  schedulePriceQuote();
}

async function loadZones(region) {
  const cacheKeyName = `zones.${region}`;
  const cached = readCache(cacheKeyName);
  if (cached) {
    state.zones = cached;
  } else {
    const zones = await getJson(`/api/zones?region=${encodeURIComponent(region)}`);
    state.zones = zones;
    writeCache(cacheKeyName, zones);
  }
  const zones = state.zones;
  setOptions(elements.zoneSelect, zones, (item) => item, (item) => item);
}

async function loadInstanceTypes(region) {
  const cacheKeyName = `instanceTypes.${region}`;
  const cached = readCache(cacheKeyName);
  if (cached) {
    state.instanceTypes = cached;
  } else {
    const types = await getJson(`/api/instance-types?region=${encodeURIComponent(region)}`);
    state.instanceTypes = types;
    writeCache(cacheKeyName, types);
  }
  const types = state.instanceTypes;
  state.instanceCatalog = types;
  setOptions(
    elements.instanceTypeSelect,
    types,
    (item) => item.instanceType,
    (item) => `${item.instanceType} | ${item.cpu || 0} vCPU / ${item.memory || 0} GB`
  );
  const selected = elements.instanceTypeSelect.value;
  if (!selected || !types.some((item) => item.instanceType === selected)) {
    elements.instanceTypeSelect.value = types[0]?.instanceType || "";
  }
  loadInstanceTypePage(1);
}

async function loadImages(region) {
  const cacheKeyName = `images.${region}`;
  const cached = readCache(cacheKeyName);
  if (cached) {
    state.images = cached;
  } else {
    const images = await getJson(`/api/images?region=${encodeURIComponent(region)}`);
    state.images = images;
    writeCache(cacheKeyName, images);
  }
  const images = state.images;
  setOptions(
    elements.imageSelect,
    images,
    (item) => item.imageId,
    (item) => `${item.imageName || item.imageId} (${item.imageOsName || "OS"})`
  );
}

function updateInstanceOverview() {
  const selected = state.instanceTypes.find((item) => item.instanceType === elements.instanceTypeSelect.value);
  if (!selected) {
    elements.instanceOverview.textContent = t("instanceOverviewHint");
    return;
  }
  const region = elements.regionSelect.value || "-";
  const zone = elements.zoneSelect.value || "-";
  const imageId = elements.imageSelect.value || "-";
  const systemDisk = Math.max(20, Number(elements.systemDisk.value || 20));
  const dataDisk = Math.max(0, Number(elements.dataDisk.value || 0));
  const bandwidth = Math.max(0, Number(elements.bandwidth.value || 0));
  const scheduledDestroy = elements.scheduledDestroy.checked ? t("yes") : t("no");
  const destroyTime = elements.scheduledDestroy.checked
    ? elements.scheduledDestroyTime.value || "-"
    : "-";
  const userDataEnabled = elements.userDataEnabled.checked ? t("yes") : t("no");
  const details = [
    `${t("region")}: ${region}`,
    `${t("zone")}: ${zone}`,
    `${t("instanceDetailInstance")}: ${selected.instanceType || "-"}`,
    `${t("instanceDetailZone")}: ${selected.zone || "-"}`,
    `${t("instanceDetailFamily")}: ${selected.instanceFamily || "-"}`,
    `${t("instanceDetailCpu")}: ${selected.cpu || "-"}`,
    `${t("instanceDetailMemory")}: ${selected.memory || "-"} GB`,
    `${t("instanceDetailGpu")}: ${selected.gpu || "-"}`,
    `${t("instanceDetailGpuCount")}: ${selected.gpuCount || "-"}`,
    `${t("instanceDetailFpga")}: ${selected.fpga || "-"}`,
    `${t("image")}: ${imageId}`,
    `${t("systemDisk")}: ${systemDisk} GB`,
    `${t("dataDisk")}: ${dataDisk} GB`,
    `${t("bandwidth")}: ${bandwidth} Mbps`,
    `${t("scheduledDestroy")}: ${scheduledDestroy}`,
    `${t("scheduledDestroyTime")}: ${destroyTime}`,
    `${t("userDataToggle")}: ${userDataEnabled}`,
  ];
  elements.instanceOverview.textContent = details.join("\n");
}

function selectInstanceType(instanceType) {
  if (!instanceType) {
    return;
  }
  elements.instanceTypeSelect.value = instanceType;
  updateInstanceOverview();
  renderServerList();
  schedulePriceQuote();
}

function syncUserDataVisibility() {
  if (!elements.userDataBlock) {
    return;
  }
  elements.userDataBlock.classList.toggle("hidden", !elements.userDataEnabled.checked);
}

function syncScheduledDestroyTime() {
  if (!elements.scheduledDestroyTimeWrap) {
    return;
  }
  const shouldShow = elements.scheduledDestroy.checked;
  elements.scheduledDestroyTimeWrap.classList.toggle("hidden", !shouldShow);
  if (!shouldShow) {
    elements.scheduledDestroyTime.value = "";
  }
}

function renderServerList() {
  if (!elements.serverListBody) {
    return;
  }
  const sorted = state.instances;
  elements.serverListBody.innerHTML = "";
  if (!sorted.length) {
    const empty = document.createElement("div");
    empty.className = "server-empty";
    empty.textContent = t("serverEmpty");
    elements.serverListBody.appendChild(empty);
    elements.serverPageInfo.textContent = "1 / 1";
    return;
  }
  const selectedInstanceType = elements.instanceTypeSelect.value;
  sorted.forEach((item) => {
    const row = document.createElement("div");
    row.className = "server-row";
    row.classList.add("list-row");
    if (item.instanceType === selectedInstanceType) {
      row.classList.add("selected");
    }
    row.dataset.instanceType = item.instanceType;
    const priceValue = state.instancePriceMap[item.instanceType];
    const familyValue = item.instanceFamily || (item.instanceType ? item.instanceType.split(".")[0] : "-");
    row.innerHTML = `
      <span>${item.instanceType || "-"}</span>
      <span>${familyValue || "-"}</span>
      <span>${item.cpu || "-"}</span>
      <span>${item.memory || "-"} GB</span>
      <span>${item.gpu || "-"}</span>
      <span class="server-price">${priceValue !== undefined ? priceValue : "-"}</span>
    `;
    row.addEventListener("click", () => {
      selectInstanceType(item.instanceType);
    });
    row.addEventListener("keydown", (event) => {
      if (event.key === "Enter" || event.key === " ") {
        event.preventDefault();
        selectInstanceType(item.instanceType);
      }
    });
    row.tabIndex = 0;
    elements.serverListBody.appendChild(row);
  });
  const totalPages = Math.max(1, Math.ceil(state.instanceTotal / state.instancePageSize));
  elements.serverPageInfo.textContent = `${state.instancePage} / ${totalPages}`;
}

function loadInstanceTypePage(page) {
  if (!state.credentialsReady) {
    return;
  }
  const size = Number(elements.serverPageSize.value || state.instancePageSize);
  const filters = getInstanceFilters();
  const filtered = state.instanceCatalog.filter((item) => matchesInstanceFilters(item, filters));
  const sorted = [...filtered].sort((a, b) => (a.memory || 0) - (b.memory || 0));
  const totalCount = sorted.length;
  const totalPages = Math.max(1, Math.ceil(totalCount / size));
  const safePage = Math.min(Math.max(1, page), totalPages);
  const start = (safePage - 1) * size;
  const pageItems = sorted.slice(start, start + size);
  state.instances = pageItems;
  state.instanceTotal = totalCount;
  state.instancePage = safePage;
  state.instancePageSize = size;
  renderServerList();
  refreshInstanceTypePrices();
}

function getInstanceFilters() {
  return {
    instanceType: (elements.filterInstanceType?.value || "").trim().toLowerCase(),
    family: (elements.filterFamily?.value || "").trim().toLowerCase(),
    cpu: Number(elements.filterCpu?.value || ""),
    memory: Number(elements.filterMemory?.value || ""),
    gpu: (elements.filterGpu?.value || "").trim().toLowerCase(),
  };
}

function matchesInstanceFilters(item, filters) {
  if (filters.instanceType) {
    const value = String(item.instanceType || "").toLowerCase();
    if (!value.includes(filters.instanceType)) {
      return false;
    }
  }
  if (filters.family) {
    const family = item.instanceFamily || (item.instanceType ? item.instanceType.split(".")[0] : "");
    if (!String(family || "").toLowerCase().includes(filters.family)) {
      return false;
    }
  }
  if (!Number.isNaN(filters.cpu) && filters.cpu > 0) {
    if ((item.cpu || 0) < filters.cpu) {
      return false;
    }
  }
  if (!Number.isNaN(filters.memory) && filters.memory > 0) {
    if ((item.memory || 0) < filters.memory) {
      return false;
    }
  }
  if (filters.gpu) {
    const value = String(item.gpu || "").toLowerCase();
    if (!value.includes(filters.gpu)) {
      return false;
    }
  }
  return true;
}

function buildPricePayload(instanceType) {
  const region = elements.regionSelect.value;
  const zone = elements.zoneSelect.value;
  if (!region || !zone || !instanceType) {
    return null;
  }
  const systemDisk = Math.max(20, Number(elements.systemDisk.value || 20));
  const dataDisk = Math.max(0, Number(elements.dataDisk.value || 0));
  const bandwidth = Math.max(0, Number(elements.bandwidth.value || 0));
  const userDataValue = elements.userDataEnabled && elements.userDataEnabled.checked ? elements.userData.value : "";
  return {
    region,
    zone,
    instanceType,
    imageId: elements.imageSelect.value,
    systemDiskGb: systemDisk,
    dataDiskGb: dataDisk,
    bandwidthMbps: bandwidth,
    bandwidthChargeType: elements.bandwidthChargeType.value,
    instanceChargeType: elements.instanceChargeType.value,
    userData: userDataValue,
    scheduledDestroy: elements.scheduledDestroy.checked,
  };
}

function refreshInstanceTypePrices() {
  if (!state.credentialsReady) {
    return;
  }
  const region = elements.regionSelect.value;
  if (!region || !state.instances.length) {
    return;
  }
  state.instances.forEach((item) => {
    delete state.instancePriceMap[item.instanceType];
  });
  for (const item of state.instances) {
    const payload = buildPricePayload(item.instanceType);
    if (!payload) {
      continue;
    }
    const cached = readCache(instancePriceCacheKey(payload));
    if (cached && cached.instancePrice !== undefined) {
      state.instancePriceMap[item.instanceType] = cached.instancePrice;
      const cachedRow = elements.serverListBody.querySelector(
        `[data-instance-type="${item.instanceType}"] .server-price`
      );
      if (cachedRow) {
        cachedRow.textContent = cached.instancePrice;
      }
      continue;
    }
    postJson("/api/price", payload)
      .then((response) => {
        state.instancePriceMap[item.instanceType] = response.instancePrice;
        writeCache(instancePriceCacheKey(payload), response);
        const row = elements.serverListBody.querySelector(
          `[data-instance-type="${item.instanceType}"] .server-price`
        );
        if (row) {
          row.textContent = response.instancePrice;
        }
      })
      .catch(() => {});
  }
}

function buildPriceRequest() {
  const instanceType = elements.instanceTypeSelect.value;
  return buildPricePayload(instanceType);
}

function buildTemplateRequest() {
  const payload = buildPriceRequest();
  if (!payload) {
    return null;
  }
  return {
    ...payload,
    scheduledDestroyTime: elements.scheduledDestroyTime.value || "",
    userDataEnabled: elements.userDataEnabled.checked,
  };
}

async function quotePrice() {
  const payload = buildPriceRequest();
  if (!payload) {
    return;
  }
  const cached = readCache(instancePriceCacheKey(payload));
  if (cached && cached.instancePrice !== undefined) {
    applyPriceResponse(cached);
    return;
  }
  const response = await postJson("/api/price", payload);
  writeCache(instancePriceCacheKey(payload), response);
  applyPriceResponse(response);
}

function applyPriceResponse(response) {
  setPriceValue(elements.priceInstance, response.instancePrice, response.currency);
  setPriceValue(elements.priceBandwidth, response.bandwidthPrice, response.currency);
  setPriceValue(elements.priceSystemDisk, response.systemDiskPrice, response.currency);
  setPriceValue(elements.priceDataDisk, response.dataDiskPrice, response.currency);
  setPriceValue(elements.priceTotal, response.totalPrice, response.currency);
  elements.priceNote.textContent = response.scheduledDestroy
    ? t("scheduledDestroyNote")
    : "";
}

function schedulePriceQuote() {
  if (!state.credentialsReady) {
    return;
  }
  updateInstanceOverview();
  refreshInstanceTypePrices();
  if (state.priceTimer) {
    clearTimeout(state.priceTimer);
  }
  state.priceTimer = setTimeout(() => {
    quotePrice().catch((error) => {
      setStatus(`Pricing error: ${error.message}`, "error");
    });
  }, 600);
}

function exportTemplate() {
  const template = buildTemplateRequest();
  if (!template) {
    setStatusKey("statusExportHint", "error");
    return;
  }
  const blob = new Blob([JSON.stringify(template, null, 2)], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `cvm-template-${Date.now()}.json`;
  link.click();
  URL.revokeObjectURL(url);
}

async function importTemplate(file) {
  const text = await file.text();
  const template = JSON.parse(text);
  elements.regionSelect.value = template.region || "";
  await loadRegionDependencies(elements.regionSelect.value);
  elements.zoneSelect.value = template.zone || "";
  elements.instanceTypeSelect.value = template.instanceType || "";
  elements.imageSelect.value = template.imageId || "";
  elements.systemDisk.value = template.systemDiskGb || 20;
  elements.dataDisk.value = template.dataDiskGb || 0;
  elements.bandwidth.value = template.bandwidthMbps || 0;
  elements.bandwidthChargeType.value = template.bandwidthChargeType || "BANDWIDTH_POSTPAID_BY_HOUR";
  elements.instanceChargeType.value = template.instanceChargeType || "POSTPAID_BY_HOUR";
  elements.userDataEnabled.checked = Boolean(template.userDataEnabled);
  elements.userData.value = template.userData || "";
  elements.scheduledDestroy.checked = Boolean(template.scheduledDestroy);
  elements.scheduledDestroyTime.value = template.scheduledDestroyTime || "";
  syncScheduledDestroyTime();
  syncUserDataVisibility();
  updateInstanceOverview();
  schedulePriceQuote();
}

function openCredentialsModal() {
  if (!elements.credentialsModal) {
    return;
  }
  elements.credentialsModal.classList.remove("hidden");
}

function closeCredentialsModal() {
  if (!elements.credentialsModal) {
    return;
  }
  elements.credentialsModal.classList.add("hidden");
}

elements.saveCredentials.addEventListener("click", () => {
  saveCredentials()
    .then(() => setStatusKey("statusReady", "ok"))
    .catch((error) => setStatus(`${t("statusCredentialError")}: ${error.message}`, "error"));
});

elements.refreshLists.addEventListener("click", () => {
  invalidateCache("");
  loadRegions().catch((error) => setStatus(`${t("statusRefreshError")}: ${error.message}`, "error"));
});

elements.openCredentials.addEventListener("click", openCredentialsModal);
elements.closeCredentials.addEventListener("click", closeCredentialsModal);
elements.modalBackdrop.addEventListener("click", closeCredentialsModal);

elements.regionSelect.addEventListener("change", () => {
  // 地域切换时先清空可用区，避免旧地域的 zone 残留
  elements.zoneSelect.value = "";
  loadRegionDependencies(elements.regionSelect.value).catch((error) =>
    setStatus(`${t("statusRegionError")}: ${error.message}`, "error")
  );
});

elements.instanceTypeSelect.addEventListener("change", () => {
  updateInstanceOverview();
  renderServerList();
  schedulePriceQuote();
});

[
  elements.zoneSelect,
  elements.imageSelect,
  elements.systemDisk,
  elements.dataDisk,
  elements.bandwidth,
  elements.bandwidthChargeType,
  elements.instanceChargeType,
].forEach((element) => {
  element.addEventListener("change", schedulePriceQuote);
});

elements.userDataEnabled.addEventListener("change", () => {
  syncUserDataVisibility();
  schedulePriceQuote();
});

elements.scheduledDestroy.addEventListener("change", () => {
  syncScheduledDestroyTime();
  schedulePriceQuote();
});

elements.scheduledDestroyTime.addEventListener("change", schedulePriceQuote);
elements.userData.addEventListener("input", schedulePriceQuote);

elements.quotePrice.addEventListener("click", () => {
  quotePrice().catch((error) => setStatus(`${t("statusPricingError")}: ${error.message}`, "error"));
});

elements.exportTemplate.addEventListener("click", exportTemplate);

elements.importTemplate.addEventListener("change", (event) => {
  if (!event.target.files.length) {
    return;
  }
  importTemplate(event.target.files[0]).catch((error) =>
    setStatus(`${t("statusImportError")}: ${error.message}`, "error")
  );
});

elements.instanceChargeType.addEventListener("change", () => {
  state.instancePrices = {};
  state.instancePriceMap = {};
  invalidateCache("price");
  schedulePriceQuote();
  refreshInstanceTypePrices();
});
elements.bandwidthChargeType.addEventListener("change", () => {
  state.instancePriceMap = {};
  invalidateCache("price");
  schedulePriceQuote();
  refreshInstanceTypePrices();
});

elements.refreshServerPrices.addEventListener("click", () => {
  invalidateCache("price");
  refreshInstanceTypePrices();
});

elements.serverPrev.addEventListener("click", () => {
  const prev = Math.max(1, state.instancePage - 1);
  loadInstanceTypePage(prev);
});

elements.serverNext.addEventListener("click", () => {
  const totalPages = Math.max(1, Math.ceil(state.instanceTotal / state.instancePageSize));
  const next = Math.min(totalPages, state.instancePage + 1);
  loadInstanceTypePage(next);
});

elements.serverPageSize.addEventListener("change", () => {
  loadInstanceTypePage(1);
});

[
  elements.filterInstanceType,
  elements.filterFamily,
  elements.filterCpu,
  elements.filterMemory,
  elements.filterGpu,
].forEach((element) => {
  if (!element) {
    return;
  }
  element.addEventListener("input", () => {
    loadInstanceTypePage(1);
  });
});

elements.languageSelect.addEventListener("change", () => {
  applyI18n(elements.languageSelect.value);
});

const initialLang =
  localStorage.getItem("cvm.lang") ||
  (navigator.language && navigator.language.toLowerCase().startsWith("zh") ? "zh" : "en");
elements.languageSelect.value = initialLang;
applyI18n(initialLang);
syncUserDataVisibility();
syncScheduledDestroyTime();

const storedSecretId = localStorage.getItem("cvm.secretId") || "";
const storedSecretKey = localStorage.getItem("cvm.secretKey") || "";
const storedRegion = localStorage.getItem("cvm.defaultRegion") || "";
if (storedSecretId && storedSecretKey) {
  elements.secretId.value = storedSecretId;
  elements.secretKey.value = storedSecretKey;
  elements.defaultRegion.value = storedRegion;
  saveCredentials().catch(() => {});
} else {
  setStatusKey("statusHint", "error");
}

