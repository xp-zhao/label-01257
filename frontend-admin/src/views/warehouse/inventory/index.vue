<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="所属库房">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择" clearable style="width: 200px;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品">
          <el-select v-model="queryParams.productId" placeholder="请选择" clearable filterable style="width: 200px;">
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="批次号">
          <el-input v-model="queryParams.batchNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch"><el-icon><Search /></el-icon> 查询</el-button>
          <el-button @click="handleReset"><el-icon><Refresh /></el-icon> 重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="title">库存列表</span>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="warehouseName" label="库房" min-width="120" />
        <el-table-column prop="locationCode" label="库位" min-width="120" />
        <el-table-column prop="batchNo" label="批次号" width="140" />
        <el-table-column prop="quantity" label="库存数量" width="100">
          <template #default="{ row }">
            <span :class="{ 'text-danger': row.quantity <= 10 }">{{ row.quantity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="availableQty" label="可用数量" width="100" />
        <el-table-column prop="lockedQty" label="锁定数量" width="100" />
        <el-table-column prop="expiryDate" label="过期日期" width="110" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
      </el-table>
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getInventoryList } from '@/api/stock'
import { getAllWarehouses } from '@/api/warehouse'
import { getAllProducts } from '@/api/stock'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouseList = ref([])
const productList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, warehouseId: null, productId: null, batchNo: '' })

const loadData = async () => {
  await withLoading(async () => {
    const res = await getInventoryList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.warehouseId = null; queryParams.productId = null; queryParams.batchNo = ''; handleSearch() }

onMounted(async () => {
  const [wRes, pRes] = await Promise.all([getAllWarehouses(), getAllProducts()])
  warehouseList.value = wRes.data || []
  productList.value = pRes.data || []
  loadData()
})
</script>

<style scoped>
.text-danger { color: #F56C6C; font-weight: 600; }
</style>
