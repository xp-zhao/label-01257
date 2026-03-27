<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="司机姓名">
          <el-input v-model="queryParams.driverName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="空闲" :value="1" />
            <el-option label="任务中" :value="2" />
            <el-option label="离职" :value="0" />
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
          <span class="title">司机列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增司机</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="driverCode" label="工号" width="100" />
        <el-table-column prop="driverName" label="姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="idCard" label="身份证号" min-width="180" />
        <el-table-column prop="licenseNo" label="驾驶证号" min-width="180" />
        <el-table-column prop="licenseType" label="准驾车型" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'primary' : 'info'">
              {{ row.status === 1 ? '空闲' : row.status === 2 ? '任务中' : '离职' }}
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
        <el-form-item label="工号" prop="driverCode">
          <el-input v-model="formData.driverCode" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="姓名" prop="driverName">
          <el-input v-model="formData.driverName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="formData.idCard" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="驾驶证号" prop="licenseNo">
          <el-input v-model="formData.licenseNo" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="准驾车型" prop="licenseType">
          <el-select v-model="formData.licenseType" placeholder="请选择" style="width: 100%;">
            <el-option label="C1" value="C1" />
            <el-option label="C2" value="C2" />
            <el-option label="B1" value="B1" />
            <el-option label="B2" value="B2" />
            <el-option label="A1" value="A1" />
            <el-option label="A2" value="A2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">空闲</el-radio>
            <el-radio :label="0">离职</el-radio>
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
import { getDriverList, addDriver, updateDriver, deleteDriver } from '@/api/transport'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({ pageNum: 1, pageSize: 10, driverName: '', status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, driverCode: '', driverName: '', phone: '', idCard: '', licenseNo: '', licenseType: 'C1', status: 1 })
const phoneValidator = (rule, value, callback) => {
  if (!value) { callback(new Error('请输入联系电话')); return }
  const phoneReg = /^1[3-9]\d{9}$/
  if (!phoneReg.test(value)) { callback(new Error('请输入正确的手机号码')) }
  else { callback() }
}
const rules = { driverCode: [{ required: true, message: '请输入', trigger: 'blur' }], driverName: [{ required: true, message: '请输入', trigger: 'blur' }], phone: [{ required: true, validator: phoneValidator, trigger: 'blur' }] }

const loadData = async () => {
  await withLoading(async () => {
    const res = await getDriverList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.driverName = ''; queryParams.status = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增司机'
  Object.assign(formData, { id: null, driverCode: '', driverName: '', phone: '', idCard: '', licenseNo: '', licenseType: 'C1', status: 1 })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑司机'
  Object.assign(formData, row)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => { ElMessageBox.confirm(`确定删除【${row.driverName}】？`, '提示', { type: 'warning' }).then(async () => { await deleteDriver(row.id); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updateDriver(formData.id, formData); ElMessage.success('更新成功') }
    else { await addDriver(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) } finally { submitLoading.value = false }
}

onMounted(() => { loadData() })
</script>
