<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="库房">
          <el-select v-model="queryParams.warehouseId" placeholder="全部库房" clearable style="width: 200px;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon> 查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="equal-height-row">
      <el-col :span="12">
        <el-card class="equal-height-card">
          <template #header><span>库存汇总</span></template>
          <el-table :data="summaryData" v-loading="loading" stripe max-height="350" style="width: 100%">
            <el-table-column prop="warehouseName" label="库房" min-width="120" />
            <el-table-column prop="productCount" label="商品种类" min-width="100" />
            <el-table-column prop="totalQuantity" label="总库存" min-width="100" />
            <el-table-column prop="availableQuantity" label="可用库存" min-width="100" />
            <el-table-column prop="lockedQuantity" label="锁定库存" min-width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="equal-height-card">
          <template #header><span>库存分布</span></template>
          <div ref="pieChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px;">
      <template #header><span>库存明细</span></template>
      <el-table :data="detailData" v-loading="detailLoading" stripe max-height="400" style="width: 100%">
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="warehouseName" label="库房" min-width="120" />
        <el-table-column prop="batchNo" label="批次号" min-width="140" />
        <el-table-column prop="quantity" label="库存数量" width="100" />
        <el-table-column prop="availableQty" label="可用数量" width="100" />
        <el-table-column prop="expiryDate" label="过期日期" width="110" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getInventorySummary, getInventoryDetail } from '@/api/report'
import { getAllWarehouses } from '@/api/warehouse'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const detailLoading = ref(false)
const summaryData = ref([])
const detailData = ref([])
const warehouseList = ref([])
const queryParams = reactive({ warehouseId: null })
const pieChartRef = ref()
let pieChart = null

const loadData = async () => {
  detailLoading.value = true
  await withLoading(async () => {
    const [sumRes, detRes] = await Promise.all([
      getInventorySummary(queryParams),
      getInventoryDetail(queryParams)
    ])
    summaryData.value = sumRes.data || []
    detailData.value = detRes.data || []
    initPieChart()
    detailLoading.value = false
  }, loading)
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)
  const data = summaryData.value.map(item => ({ name: item.warehouseName, value: item.totalQuantity }))
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{ type: 'pie', radius: ['40%', '70%'], avoidLabelOverlap: false, label: { show: false }, data }]
  })
}

onMounted(async () => {
  const res = await getAllWarehouses()
  warehouseList.value = res.data || []
  loadData()
  window.addEventListener('resize', () => pieChart?.resize())
})
onUnmounted(() => { pieChart?.dispose(); window.removeEventListener('resize', () => pieChart?.resize()) })
</script>

<style scoped>
.equal-height-row {
  display: flex;
  flex-wrap: wrap;
}
.equal-height-row > .el-col {
  display: flex;
}
.equal-height-card {
  width: 100%;
  display: flex;
  flex-direction: column;
}
.equal-height-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
