<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期"
            end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 300px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData"><el-icon><Search /></el-icon> 查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card>
          <template #header><span>入库统计</span></template>
          <div class="chart-container" style="height: 350px;">
            <div v-show="inboundData.length > 0" ref="inboundChartRef" style="height: 100%;"></div>
            <el-empty v-show="inboundData.length === 0" description="暂无入库数据" :image-size="120" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>出库统计</span></template>
          <div class="chart-container" style="height: 350px;">
            <div v-show="outboundData.length > 0" ref="outboundChartRef" style="height: 100%;"></div>
            <el-empty v-show="outboundData.length === 0" description="暂无出库数据" :image-size="120" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="12">
        <el-card>
          <template #header><span>入库明细</span></template>
          <el-table :data="inboundData" v-loading="loading" stripe max-height="300" style="width: 100%">
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="orderCount" label="订单数" />
            <el-table-column prop="totalQuantity" label="入库数量" />
            <template #empty>
              <el-empty description="暂无入库明细数据" :image-size="80" />
            </template>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>出库明细</span></template>
          <el-table :data="outboundData" v-loading="loading" stripe max-height="300" style="width: 100%">
            <el-table-column prop="date" label="日期" />
            <el-table-column prop="orderCount" label="订单数" />
            <el-table-column prop="totalQuantity" label="出库数量" />
            <template #empty>
              <el-empty description="暂无出库明细数据" :image-size="80" />
            </template>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import * as echarts from 'echarts'
import { getInboundSummary, getOutboundSummary } from '@/api/report'
import { withLoading } from '@/utils/loading'

const dateRange = ref([])
const queryParams = computed(() => ({
  startDate: dateRange.value?.[0] || '',
  endDate: dateRange.value?.[1] || ''
}))

const loading = ref(false)
const inboundData = ref([])
const outboundData = ref([])
const inboundChartRef = ref()
const outboundChartRef = ref()
let inboundChart = null, outboundChart = null

const loadData = async () => {
  await withLoading(async () => {
    const [inRes, outRes] = await Promise.all([
      getInboundSummary(queryParams.value),
      getOutboundSummary(queryParams.value)
    ])
    inboundData.value = inRes.data || []
    outboundData.value = outRes.data || []
    initCharts()
  }, loading)
}

const initCharts = () => {
  if (inboundChartRef.value) {
    if (!inboundChart) inboundChart = echarts.init(inboundChartRef.value)
    inboundChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: inboundData.value.map(i => i.date) },
      yAxis: { type: 'value' },
      series: [{ name: '入库数量', type: 'bar', data: inboundData.value.map(i => i.totalQuantity), itemStyle: { color: '#67C23A' } }]
    })
  }
  if (outboundChartRef.value) {
    if (!outboundChart) outboundChart = echarts.init(outboundChartRef.value)
    outboundChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: outboundData.value.map(i => i.date) },
      yAxis: { type: 'value' },
      series: [{ name: '出库数量', type: 'bar', data: outboundData.value.map(i => i.totalQuantity), itemStyle: { color: '#E6A23C' } }]
    })
  }
}

onMounted(() => {
  const today = new Date()
  const lastWeek = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  dateRange.value = [lastWeek.toISOString().split('T')[0], today.toISOString().split('T')[0]]
  loadData()
  window.addEventListener('resize', () => { inboundChart?.resize(); outboundChart?.resize() })
})
onUnmounted(() => { inboundChart?.dispose(); outboundChart?.dispose() })
</script>
