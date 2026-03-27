<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">异常预警列表</span>
          <el-button type="primary" @click="loadData"><el-icon><Refresh /></el-icon> 刷新</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="warehouseName" label="所在库房" min-width="150" />
        <el-table-column prop="quantity" label="当前库存" width="100">
          <template #default="{ row }">
            <span class="text-danger">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expiryDate" label="过期日期" width="110" />
        <el-table-column prop="warningType" label="预警类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.warningType === '已过期' ? 'danger' : row.warningType === '即将过期' ? 'warning' : 'info'">{{ row.warningType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="建议" min-width="200">
          <template #default="{ row }">
            <span v-if="row.warningType === '已过期'" class="text-danger">商品已过期，请立即处理</span>
            <span v-else-if="row.warningType === '即将过期'" class="text-warning">请尽快处理即将过期的商品</span>
            <span v-else class="text-warning">建议及时补货</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !tableData.length" description="暂无预警信息" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWarning } from '@/api/report'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const tableData = ref([])

const loadData = async () => {
  await withLoading(async () => {
    const res = await getWarning()
    tableData.value = res.data || []
  }, loading)
}

onMounted(() => { loadData() })
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.text-danger { color: #F56C6C; }
.text-warning { color: #E6A23C; }
</style>
