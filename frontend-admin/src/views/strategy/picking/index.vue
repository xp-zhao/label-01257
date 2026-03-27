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
          <span class="title">拣货策略列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增策略</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="strategyCode" label="策略编码" width="120" />
        <el-table-column prop="strategyName" label="策略名称" min-width="150" />
        <el-table-column prop="warehouseName" label="适用库房" min-width="120" />
        <el-table-column prop="pickingType" label="拣货方式" width="100">
          <template #default="{ row }">{{ pickingTypeMap[row.pickingType] }}</template>
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
        <el-form-item label="适用库房">
          <el-select v-model="formData.warehouseId" placeholder="全部库房" clearable style="width: 100%;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="拣货方式" prop="pickingType">
          <el-select v-model="formData.pickingType" placeholder="请选择" style="width: 100%;">
            <el-option label="单品拣货" value="SINGLE" />
            <el-option label="批量拣货" value="BATCH" />
            <el-option label="波次拣货" value="WAVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-input-number v-model="formData.priority" :min="0" :max="100" style="width: 100%;" />
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
import { getPickingStrategyList, addPickingStrategy, updatePickingStrategy, deletePickingStrategy } from '@/api/strategy'
import { getAllWarehouses } from '@/api/warehouse'
import { withLoading } from '@/utils/loading'

const pickingTypeMap = { SINGLE: '单品拣货', BATCH: '批量拣货', WAVE: '波次拣货' }
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouseList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, strategyName: '', warehouseId: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, strategyCode: '', strategyName: '', warehouseId: null, pickingType: 'SINGLE', priority: 0, status: 1 })
const rules = { strategyCode: [{ required: true, message: '请输入', trigger: 'blur' }], strategyName: [{ required: true, message: '请输入', trigger: 'blur' }] }

const loadData = async () => {
  await withLoading(async () => {
    const res = await getPickingStrategyList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.strategyName = ''; queryParams.warehouseId = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增策略'
  Object.assign(formData, { id: null, strategyCode: '', strategyName: '', warehouseId: null, pickingType: 'SINGLE', priority: 0, status: 1 })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑策略'
  Object.assign(formData, row)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => { ElMessageBox.confirm(`确定删除【${row.strategyName}】？`, '提示', { type: 'warning' }).then(async () => { await deletePickingStrategy(row.id); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updatePickingStrategy(formData.id, formData); ElMessage.success('更新成功') }
    else { await addPickingStrategy(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) } finally { submitLoading.value = false }
}

onMounted(async () => { const res = await getAllWarehouses(); warehouseList.value = res.data || []; loadData() })
</script>
