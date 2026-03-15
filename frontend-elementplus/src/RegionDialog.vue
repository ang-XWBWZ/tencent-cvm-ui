<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="520px">
    <el-form label-position="top">
      <el-form-item label="地区">
        <el-select v-model="localRegion" placeholder="请选择地区" filterable style="width: 100%">
          <el-option v-for="region in regions" :key="region" :label="regionLabel(region)" :value="region" />
        </el-select>
      </el-form-item>

      <el-form-item v-if="showZoneSelection" label="可用区">
        <el-select v-model="localZone" placeholder="请选择可用区" clearable style="width: 100%" :loading="zoneLoading">
          <el-option v-for="zone in localZones" :key="zone" :label="zone" :value="zone" />
        </el-select>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="emit('refresh')">刷新地区</el-button>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :disabled="!localRegion" @click="confirm">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getJson } from './api'

const props = defineProps({
  dialogVisible: { type: Boolean, required: true },
  regions: { type: Array, default: () => [] },
  zones: { type: Array, default: () => [] },
  selectedRegionProp: { type: String, default: '' },
  selectedZoneProp: { type: String, default: '' },
  showZoneSelection: { type: Boolean, default: false },
  regionStats: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:dialogVisible', 'confirm', 'refresh'])

const localRegion = ref('')
const localZone = ref('')
const zoneLoading = ref(false)
const localZones = ref([])

const visible = computed({
  get: () => props.dialogVisible,
  set: (value) => emit('update:dialogVisible', value)
})

const dialogTitle = computed(() => (props.showZoneSelection ? '选择地区与可用区' : '选择地区'))

watch(
  () => props.dialogVisible,
  (value) => {
    if (!value) {
      return
    }
    localRegion.value = props.selectedRegionProp || ''
    localZone.value = props.selectedZoneProp || ''
    localZones.value = Array.isArray(props.zones) ? [...props.zones] : []
  },
  { immediate: true }
)

watch(
  () => props.zones,
  (zones) => {
    if (!props.showZoneSelection) {
      return
    }
    if (localRegion.value === props.selectedRegionProp) {
      localZones.value = Array.isArray(zones) ? [...zones] : []
    }
  }
)

watch(
  () => localRegion.value,
  async (region) => {
    if (!props.showZoneSelection || !region) {
      localZones.value = []
      localZone.value = ''
      return
    }
    localZone.value = ''
    zoneLoading.value = true
    try {
      const zones = await getJson(`/api/zones?region=${encodeURIComponent(region)}`)
      localZones.value = Array.isArray(zones) ? zones : []
    } catch {
      localZones.value = []
    } finally {
      zoneLoading.value = false
    }
  }
)

function regionLabel(region) {
  const count = props.regionStats?.[region]
  if (typeof count === 'number') {
    return `${region} (${count}台)`
  }
  return region
}

function close() {
  emit('update:dialogVisible', false)
}

function confirm() {
  emit('confirm', { region: localRegion.value, zone: localZone.value })
  emit('update:dialogVisible', false)
}
</script>
