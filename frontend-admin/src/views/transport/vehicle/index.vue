<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="车牌号">
          <el-input v-model="queryParams.plateNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="空闲" :value="1" />
            <el-option label="使用中" :value="2" />
            <el-option label="维修中" :value="3" />
            <el-option label="报废" :value="0" />
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
          <span class="title">车辆列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增车辆</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="plateNo" label="车牌号" width="120" />
        <el-table-column prop="vehicleType" label="车辆类型" min-width="100" />
        <el-table-column prop="brand" label="品牌" min-width="100" />
        <el-table-column prop="model" label="型号" min-width="100" />
        <el-table-column prop="loadCapacity" label="载重(kg)" width="100" />
        <el-table-column prop="volume" label="容积(m³)" width="100" />
        <el-table-column prop="purchaseDate" label="购买日期" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'primary' : row.status === 3 ? 'warning' : 'info'">
              {{ row.status === 1 ? '空闲' : row.status === 2 ? '使用中' : row.status === 3 ? '维修中' : '报废' }}
            </el-tag>
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
        <el-form-item label="车牌号" prop="plateNo">
          <el-input v-model="formData.plateNo" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="车辆类型" prop="vehicleType">
          <el-select v-model="formData.vehicleType" placeholder="请选择" style="width: 100%;">
            <el-option label="小型货车" value="小型货车" />
            <el-option label="中型货车" value="中型货车" />
            <el-option label="大型货车" value="大型货车" />
            <el-option label="厢式货车" value="厢式货车" />
            <el-option label="冷藏车" value="冷藏车" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="品牌"><el-input v-model="formData.brand" placeholder="请输入" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="型号"><el-input v-model="formData.model" placeholder="请输入" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="载重(kg)" label-width="80px"><el-input-number v-model="formData.loadCapacity" :min="0" controls-position="right" style="width: 100%;" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="容积(m³)" label-width="80px"><el-input-number v-model="formData.volume" :min="0" :precision="2" controls-position="right" style="width: 100%;" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="购买日期">
          <el-date-picker v-model="formData.purchaseDate" type="date" placeholder="选择日期" style="width: 100%;" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">空闲</el-radio>
            <el-radio :label="3">维修中</el-radio>
            <el-radio :label="0">报废</el-radio>
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
import { getVehicleList, addVehicle, updateVehicle, deleteVehicle } from '@/api/transport'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({ pageNum: 1, pageSize: 10, plateNo: '', status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, plateNo: '', vehicleType: '小型货车', brand: '', model: '', loadCapacity: 0, volume: 0, purchaseDate: '', status: 1 })
const rules = { plateNo: [{ required: true, message: '请输入', trigger: 'blur' }], vehicleType: [{ required: true, message: '请选择', trigger: 'change' }] }

const loadData = async () => {
  await withLoading(async () => {
    const res = await getVehicleList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.plateNo = ''; queryParams.status = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增车辆'
  Object.assign(formData, { id: null, plateNo: '', vehicleType: '小型货车', brand: '', model: '', loadCapacity: 0, volume: 0, purchaseDate: '', status: 1 })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑车辆'
  Object.assign(formData, row)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => { ElMessageBox.confirm(`确定删除【${row.plateNo}】？`, '提示', { type: 'warning' }).then(async () => { await deleteVehicle(row.id); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updateVehicle(formData.id, formData); ElMessage.success('更新成功') }
    else { await addVehicle(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) } finally { submitLoading.value = false }
}

onMounted(() => { loadData() })
</script>
