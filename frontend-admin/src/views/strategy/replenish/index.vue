<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="策略名称">
          <el-input v-model="queryParams.strategyName" placeholder="请输入" clearable />
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
          <span class="title">补货策略列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增策略</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="strategyCode" label="策略编码" width="120" />
        <el-table-column prop="strategyName" label="策略名称" min-width="150" />
        <el-table-column prop="warehouseName" label="适用库房" min-width="120" />
        <el-table-column prop="productName" label="适用商品" min-width="120" />
        <el-table-column prop="minQuantity" label="最小库存" width="90" />
        <el-table-column prop="maxQuantity" label="最大库存" width="90" />
        <el-table-column prop="replenishQuantity" label="补货数量" width="90" />
        <el-table-column prop="triggerType" label="触发方式" width="90">
          <template #default="{ row }">{{ row.triggerType === 'AUTO' ? '自动' : '手动' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="dialog-form">
        <el-form-item label="策略编码" prop="strategyCode">
          <el-input v-model="formData.strategyCode" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="策略名称" prop="strategyName">
          <el-input v-model="formData.strategyName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="适用库房">
          <el-select v-model="formData.warehouseId" placeholder="全部库房" clearable style="width: 100%;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用商品">
          <el-select v-model="formData.productId" placeholder="全部商品" clearable filterable style="width: 100%;">
            <el-option v-for="item in productList" :key="item.id" :label="item.productName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最小库存">
              <el-input-number v-model="formData.minQuantity" :min="0" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最大库存">
              <el-input-number v-model="formData.maxQuantity" :min="0" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="补货数量">
          <el-input-number v-model="formData.replenishQuantity" :min="0" controls-position="right" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="触发方式">
          <el-radio-group v-model="formData.triggerType">
            <el-radio label="AUTO">自动</el-radio>
            <el-radio label="MANUAL">手动</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReplenishStrategyList, addReplenishStrategy, updateReplenishStrategy, deleteReplenishStrategy } from '@/api/strategy'
import { getAllWarehouses } from '@/api/warehouse'
import { getAllProducts } from '@/api/stock'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouseList = ref([])
const productList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, strategyName: '', warehouseId: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, strategyCode: '', strategyName: '', warehouseId: null, productId: null, minQuantity: 0, maxQuantity: 0, replenishQuantity: 0, triggerType: 'AUTO', status: 1 })
const rules = { strategyCode: [{ required: true, message: '请输入', trigger: 'blur' }], strategyName: [{ required: true, message: '请输入', trigger: 'blur' }] }

const loadData = async () => {
  await withLoading(async () => {
    const res = await getReplenishStrategyList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.strategyName = ''; queryParams.warehouseId = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增策略'
  Object.assign(formData, { id: null, strategyCode: '', strategyName: '', warehouseId: null, productId: null, minQuantity: 0, maxQuantity: 0, replenishQuantity: 0, triggerType: 'AUTO', status: 1 })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑策略'
  Object.assign(formData, row)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => { ElMessageBox.confirm(`确定删除【${row.strategyName}】？`, '提示', { type: 'warning' }).then(async () => { await deleteReplenishStrategy(row.id); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  // 验证最大库存必须大于最小库存
  if (formData.maxQuantity <= formData.minQuantity) {
    ElMessage.error('最大库存必须大于最小库存')
    return
  }
  submitLoading.value = true
  try {
    if (formData.id) { await updateReplenishStrategy(formData.id, formData); ElMessage.success('更新成功') }
    else { await addReplenishStrategy(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) } finally { submitLoading.value = false }
}

onMounted(async () => {
  const [wRes, pRes] = await Promise.all([getAllWarehouses(), getAllProducts()])
  warehouseList.value = wRes.data || []
  productList.value = pRes.data || []
  loadData()
})
</script>
