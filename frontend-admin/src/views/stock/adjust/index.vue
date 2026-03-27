<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="商品">
          <el-select v-model="queryParams.productId" placeholder="请选择" clearable filterable style="width: 200px;">
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整类型">
          <el-select v-model="queryParams.adjustType" placeholder="请选择" clearable>
            <el-option label="盘盈" value="GAIN" />
            <el-option label="盘亏" value="LOSS" />
            <el-option label="报损" value="DAMAGE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
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
          <span class="title">调整记录</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 库存调整</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="adjustNo" label="调整单号" width="180" />
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="150" />
        <el-table-column prop="adjustType" label="调整类型" width="100">
          <template #default="{ row }">
            <el-tag :type="adjustTypeMap[row.adjustType]?.type">{{ adjustTypeMap[row.adjustType]?.label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="beforeQuantity" label="调整前" width="90" />
        <el-table-column prop="adjustQuantity" label="调整数量" width="90">
          <template #default="{ row }">
            <span :class="row.adjustQuantity > 0 ? 'text-success' : 'text-danger'">
              {{ row.adjustQuantity > 0 ? '+' : '' }}{{ row.adjustQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="afterQuantity" label="调整后" width="90" />
        <el-table-column prop="reason" label="原因" min-width="150" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createTime" label="调整时间" width="160" />
      </el-table>
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="库存调整" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="dialog-form">
        <el-form-item label="选择库存" prop="inventoryId">
          <el-select v-model="formData.inventoryId" placeholder="请选择库存" style="width: 100%;" filterable>
            <el-option v-for="item in inventoryList" :key="item.id"
              :label="`${item.productName} - ${item.warehouseName} (${item.quantity})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整类型" prop="adjustType">
          <el-select v-model="formData.adjustType" placeholder="请选择" style="width: 100%;">
            <el-option label="盘盈" value="GAIN" />
            <el-option label="盘亏" value="LOSS" />
            <el-option label="报损" value="DAMAGE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整数量" prop="adjustQuantity">
          <el-input-number v-model="formData.adjustQuantity" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="调整原因" prop="reason">
          <el-input v-model="formData.reason" type="textarea" :rows="3" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdjustList, adjustInventory, getInventoryList, getAllProducts } from '@/api/stock'
import { withLoading } from '@/utils/loading'

const adjustTypeMap = {
  GAIN: { label: '盘盈', type: 'success' },
  LOSS: { label: '盘亏', type: 'warning' },
  DAMAGE: { label: '报损', type: 'danger' },
  OTHER: { label: '其他', type: 'info' }
}

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const productList = ref([])
const inventoryList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, productId: null, adjustType: '' })
const dialogVisible = ref(false)
const formRef = ref()
const formData = reactive({ inventoryId: null, adjustType: 'GAIN', adjustQuantity: 0, reason: '' })
const rules = {
  inventoryId: [{ required: true, message: '请选择库存', trigger: 'change' }],
  adjustType: [{ required: true, message: '请选择调整类型', trigger: 'change' }],
  adjustQuantity: [{ required: true, message: '请输入调整数量', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入调整原因', trigger: 'blur' }]
}

const loadData = async () => {
  await withLoading(async () => {
    const res = await getAdjustList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.productId = null; queryParams.adjustType = ''; handleSearch() }

const handleAdd = async () => {
  const res = await getInventoryList({ pageNum: 1, pageSize: 1000 })
  inventoryList.value = res.data.records || []
  Object.assign(formData, { inventoryId: null, adjustType: 'GAIN', adjustQuantity: 0, reason: '' })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await adjustInventory(formData)
    ElMessage.success('调整成功')
    dialogVisible.value = false
    loadData()
  } catch (error) { console.error(error) }
  finally { submitLoading.value = false }
}

onMounted(async () => {
  const res = await getAllProducts()
  productList.value = res.data || []
  loadData()
})
</script>

<style scoped>
.text-success { color: #67C23A; }
.text-danger { color: #F56C6C; }
</style>
