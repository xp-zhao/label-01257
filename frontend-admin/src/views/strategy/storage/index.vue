<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="策略名称">
          <el-input v-model="queryParams.strategyName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="库房">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择" clearable style="width: 200px;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
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
          <span class="title">存储策略列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增策略</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="strategyCode" label="策略编码" width="120" />
        <el-table-column prop="strategyName" label="策略名称" min-width="150" />
        <el-table-column prop="warehouseName" label="适用库房" min-width="150" />
        <el-table-column prop="category" label="适用分类" min-width="100" />
        <el-table-column prop="ruleType" label="规则类型" width="100">
          <template #default="{ row }">{{ ruleTypeMap[row.ruleType] }}</template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="80" />
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
        <el-form-item label="适用库房" prop="warehouseId">
          <el-select v-model="formData.warehouseId" placeholder="全部库房" clearable style="width: 100%;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用分类" prop="category">
          <el-input v-model="formData.category" placeholder="请输入商品分类" />
        </el-form-item>
        <el-form-item label="规则类型" prop="ruleType">
          <el-select v-model="formData.ruleType" placeholder="请选择" style="width: 100%;">
            <el-option label="先进先出(FIFO)" value="FIFO" />
            <el-option label="后进先出(LIFO)" value="LIFO" />
            <el-option label="先到期先出(FEFO)" value="FEFO" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="formData.priority" :min="0" :max="100" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
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
import { getStorageStrategyList, addStorageStrategy, updateStorageStrategy, deleteStorageStrategy } from '@/api/strategy'
import { getAllWarehouses } from '@/api/warehouse'
import { withLoading } from '@/utils/loading'

const ruleTypeMap = { FIFO: '先进先出', LIFO: '后进先出', FEFO: '先到期先出' }
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouseList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, strategyName: '', warehouseId: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, strategyCode: '', strategyName: '', warehouseId: null, category: '', ruleType: 'FIFO', priority: 0, status: 1, remark: '' })
const rules = {
  strategyCode: [{ required: true, message: '请输入策略编码', trigger: 'blur' }],
  strategyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }]
}

const loadData = async () => {
  await withLoading(async () => {
    const res = await getStorageStrategyList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.strategyName = ''; queryParams.warehouseId = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增策略'
  Object.assign(formData, { id: null, strategyCode: '', strategyName: '', warehouseId: null, category: '', ruleType: 'FIFO', priority: 0, status: 1, remark: '' })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑策略'
  Object.assign(formData, row)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除策略【${row.strategyName}】吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteStorageStrategy(row.id); ElMessage.success('删除成功'); loadData()
  })
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updateStorageStrategy(formData.id, formData); ElMessage.success('更新成功') }
    else { await addStorageStrategy(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) }
  finally { submitLoading.value = false }
}

onMounted(async () => { const res = await getAllWarehouses(); warehouseList.value = res.data || []; loadData() })
</script>
