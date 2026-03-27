<template>
  <div class="page-container dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon"><el-icon><House /></el-icon></div>
          <div class="stat-info">
            <div class="stat-title">库房数量</div>
            <div class="stat-value">{{ stats.warehouseCount || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card success">
          <div class="stat-icon"><el-icon><Goods /></el-icon></div>
          <div class="stat-info">
            <div class="stat-title">商品种类</div>
            <div class="stat-value">{{ stats.productCount || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card warning">
          <div class="stat-icon"><el-icon><Box /></el-icon></div>
          <div class="stat-info">
            <div class="stat-title">库存总量</div>
            <div class="stat-value">{{ stats.totalInventory || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card danger">
          <div class="stat-icon"><el-icon><WarnTriangleFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-title">预警数量</div>
            <div class="stat-value">{{ stats.warningCount || 0 }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 第二行 -->
    <el-row :gutter="16" class="info-row">
      <el-col :span="6">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>今日入库</span>
              <el-icon color="#67C23A"><Download /></el-icon>
            </div>
          </template>
          <div class="info-value">{{ stats.todayInbound || 0 }}</div>
          <div class="info-label">件商品</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>今日出库</span>
              <el-icon color="#E6A23C"><Upload /></el-icon>
            </div>
          </template>
          <div class="info-value">{{ stats.todayOutbound || 0 }}</div>
          <div class="info-label">件商品</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>待入库单</span>
              <el-icon color="#409EFF"><Tickets /></el-icon>
            </div>
          </template>
          <div class="info-value">{{ stats.pendingInbound || 0 }}</div>
          <div class="info-label">个订单</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span>运输中</span>
              <el-icon color="#F56C6C"><Van /></el-icon>
            </div>
          </template>
          <div class="info-value">{{ stats.activeTransport || 0 }}</div>
          <div class="info-label">个任务</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>出入库趋势</span>
          </template>
          <div ref="chartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>库存预警</span>
          </template>
          <div class="warning-list">
            <div v-for="(item, index) in warnings" :key="index" class="warning-item">
              <div class="warning-info">
                <span class="warning-name">{{ item.productName }}</span>
                <span class="warning-type" :class="item.warningType === '已过期' ? 'danger' : item.warningType === '即将过期' ? 'warning' : 'info'">
                  {{ item.warningType }}
                </span>
              </div>
              <div class="warning-detail">
                {{ item.warehouseName }} | 库存: {{ item.quantity }}
              </div>
            </div>
            <el-empty v-if="!warnings.length" description="暂无预警" :image-size="80" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getDashboard, getWarning, getInboundSummary, getOutboundSummary } from '@/api/report'

const stats = ref({})
const warnings = ref([])
const chartRef = ref()
let chart = null

const loadData = async () => {
  try {
    const [dashboardRes, warningRes] = await Promise.all([
      getDashboard(),
      getWarning()
    ])
    stats.value = dashboardRes.data || {}
    warnings.value = (warningRes.data || []).slice(0, 5)
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

const initChart = async () => {
  if (!chartRef.value) return
  
  chart = echarts.init(chartRef.value)
  
  // 获取最近7天的日期范围
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 6)
  
  const formatDate = (date) => {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
  
  // 生成最近7天的日期数组
  const dates = []
  const dateMap = {}
  for (let i = 6; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    const dateStr = formatDate(date)
    const displayDate = date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
    dates.push(displayDate)
    dateMap[dateStr] = { display: displayDate, inbound: 0, outbound: 0 }
  }
  
  try {
    // 调用真实API获取入库和出库数据
    const [inboundRes, outboundRes] = await Promise.all([
      getInboundSummary({ startDate: formatDate(startDate), endDate: formatDate(endDate) }),
      getOutboundSummary({ startDate: formatDate(startDate), endDate: formatDate(endDate) })
    ])
    
    // 填充入库数据
    if (inboundRes.data) {
      inboundRes.data.forEach(item => {
        if (dateMap[item.date]) {
          dateMap[item.date].inbound = item.totalQuantity || 0
        }
      })
    }
    
    // 填充出库数据
    if (outboundRes.data) {
      outboundRes.data.forEach(item => {
        if (dateMap[item.date]) {
          dateMap[item.date].outbound = item.totalQuantity || 0
        }
      })
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
  
  // 提取数据数组
  const inboundData = Object.values(dateMap).map(d => d.inbound)
  const outboundData = Object.values(dateMap).map(d => d.outbound)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['入库', '出库']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '入库',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#67C23A' },
        data: inboundData
      },
      {
        name: '出库',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#E6A23C' },
        data: outboundData
      }
    ]
  }

  chart.setOption(option)
}

onMounted(() => {
  loadData()
  initChart()
  window.addEventListener('resize', () => chart?.resize())
})

onUnmounted(() => {
  chart?.dispose()
  window.removeEventListener('resize', () => chart?.resize())
})
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-row {
    margin-bottom: 16px;
  }

  .stat-card {
    background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
    border-radius: 12px;
    padding: 24px;
    color: #fff;
    display: flex;
    align-items: center;
    gap: 16px;

    &.success {
      background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
    }

    &.warning {
      background: linear-gradient(135deg, #E6A23C 0%, #ebb563 100%);
    }

    &.danger {
      background: linear-gradient(135deg, #F56C6C 0%, #f78989 100%);
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
    }

    .stat-info {
      .stat-title {
        font-size: 14px;
        opacity: 0.9;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 32px;
        font-weight: 600;
      }
    }
  }

  .info-row {
    margin-bottom: 16px;
  }

  .chart-row {
    margin-top: 8px;
  }

  .info-card {
    text-align: center;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .info-value {
      font-size: 36px;
      font-weight: 600;
      color: #303133;
    }

    .info-label {
      color: #909399;
      margin-top: 8px;
    }
  }

  .warning-list {
    max-height: 310px;
    overflow-y: auto;

    .warning-item {
      padding: 12px 0;
      border-bottom: 1px solid #EBEEF5;

      &:last-child {
        border-bottom: none;
      }

      .warning-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 6px;

        .warning-name {
          font-weight: 500;
          color: #303133;
        }

        .warning-type {
          font-size: 12px;
          padding: 2px 8px;
          border-radius: 4px;

          &.warning {
            background: rgba(#E6A23C, 0.1);
            color: #E6A23C;
          }

          &.danger {
            background: rgba(#F56C6C, 0.1);
            color: #F56C6C;
          }

          &.info {
            background: rgba(#909399, 0.1);
            color: #909399;
          }
        }
      }

      .warning-detail {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>
