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

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header><span>商品周转率分析</span></template>
          <div ref="chartRef" style="height: 400px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>周转率排名</span></template>
          <el-table :data="tableData" v-loading="loading" stripe max-height="350" style="width: 100%">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="productName" label="商品" min-width="100" show-overflow-tooltip />
            <el-table-column prop="currentStock" label="库存" width="70" />
            <el-table-column prop="turnoverRate" label="周转率" width="80">
              <template #default="{ row }">
                <span :class="row.turnoverRate > 1 ? 'text-success' : 'text-warning'">{{ row.turnoverRate }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getTurnover } from '@/api/report'
import { getAllWarehouses } from '@/api/warehouse'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const queryParams = reactive({ warehouseId: null })
const warehouseList = ref([])
const tableData = ref([])
const chartRef = ref()
let chart = null

const loadData = async () => {
  await withLoading(async () => {
    const res = await getTurnover(queryParams)
    tableData.value = (res.data || []).sort((a, b) => b.turnoverRate - a.turnoverRate)
    initChart()
  }, loading)
}

const initChart = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const data = tableData.value.slice(0, 10)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', name: '周转率' },
    yAxis: { type: 'category', data: data.map(i => i.productName), inverse: true },
    series: [{
      type: 'bar',
      data: data.map(i => i.turnoverRate),
      itemStyle: { color: (params) => params.value > 1 ? '#67C23A' : '#E6A23C' },
      label: { show: true, position: 'right', formatter: '{c}' }
    }]
  })
}

onMounted(async () => {
  const res = await getAllWarehouses()
  warehouseList.value = res.data || []
  loadData()
  window.addEventListener('resize', () => chart?.resize())
})
onUnmounted(() => { chart?.dispose() })
</script>

<style scoped>
.text-success { color: #67C23A; font-weight: 600; }
.text-warning { color: #E6A23C; font-weight: 600; }
</style>
