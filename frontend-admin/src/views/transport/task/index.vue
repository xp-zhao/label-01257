<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="任务单号">
          <el-input v-model="queryParams.taskNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="待分配" :value="0" />
            <el-option label="已分配" :value="1" />
            <el-option label="运输中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
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
          <span class="title">运输任务列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增任务</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="taskNo" label="任务单号" width="180" />
        <el-table-column prop="taskType" label="任务类型" width="100">
          <template #default="{ row }">{{ taskTypeMap[row.taskType] }}</template>
        </el-table-column>
        <el-table-column prop="fromAddress" label="起点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="toAddress" label="终点" min-width="150" show-overflow-tooltip />
        <el-table-column prop="driverName" label="司机" min-width="100" />
        <el-table-column prop="plateNo" label="车牌号" min-width="100" />
        <el-table-column prop="planStartTime" label="计划发车" width="160" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="warning" link @click="handleAssign(row)">分配</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleStart(row)">开始</el-button>
            <el-button v-if="row.status === 2" type="success" link @click="handleComplete(row)">完成</el-button>
            <el-button v-if="row.status === 0" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status < 3" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="loadData" @current-change="loadData" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="dialog-form">
        <el-form-item label="任务类型" prop="taskType">
          <el-select v-model="formData.taskType" placeholder="请选择" style="width: 100%;">
            <el-option label="配送" value="DELIVERY" />
            <el-option label="调拨" value="TRANSFER" />
            <el-option label="取货" value="PICKUP" />
          </el-select>
        </el-form-item>
        <el-form-item label="起点地址" prop="fromAddress">
          <el-input v-model="formData.fromAddress" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="终点地址" prop="toAddress">
          <el-input v-model="formData.toAddress" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="计划发车" prop="planStartTime">
          <el-date-picker v-model="formData.planStartTime" type="datetime" placeholder="选择时间" style="width: 100%;" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="计划到达" prop="planEndTime">
          <el-date-picker v-model="formData.planEndTime" type="datetime" placeholder="选择时间" style="width: 100%;" value-format="YYYY-MM-DD HH:mm:ss" />
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

    <!-- 分配弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="分配任务" width="400px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="选择司机">
          <el-select v-model="assignForm.driverId" placeholder="请选择司机" style="width: 100%;">
            <el-option v-for="item in availableDrivers" :key="item.id" :label="`${item.driverName} (${item.phone})`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择车辆">
          <el-select v-model="assignForm.vehicleId" placeholder="请选择车辆" style="width: 100%;">
            <el-option v-for="item in availableVehicles" :key="item.id" :label="`${item.plateNo} (${item.vehicleType})`" :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign">确定分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTaskList, createTask, updateTask, assignTask, startTask, completeTask, cancelTask, getAvailableDrivers, getAvailableVehicles } from '@/api/transport'
import { withLoading } from '@/utils/loading'

const taskTypeMap = { DELIVERY: '配送', TRANSFER: '调拨', PICKUP: '取货' }
const statusMap = { 0: '待分配', 1: '已分配', 2: '运输中', 3: '已完成', 4: '已取消' }
const statusTypeMap = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({ pageNum: 1, pageSize: 10, taskNo: '', status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, taskType: 'DELIVERY', fromAddress: '', toAddress: '', planStartTime: '', planEndTime: '', remark: '' })
const validatePlanEndTime = (rule, value, callback) => {
  if (value && formData.planStartTime && new Date(value) <= new Date(formData.planStartTime)) {
    callback(new Error('计划到达时间必须大于计划发车时间'))
  } else {
    callback()
  }
}
const rules = { 
  taskType: [{ required: true, message: '请选择', trigger: 'change' }], 
  fromAddress: [{ required: true, message: '请输入', trigger: 'blur' }], 
  toAddress: [{ required: true, message: '请输入', trigger: 'blur' }],
  planEndTime: [{ validator: validatePlanEndTime, trigger: 'change' }]
}

const assignDialogVisible = ref(false)
const assignForm = reactive({ taskId: null, driverId: null, vehicleId: null })
const availableDrivers = ref([])
const availableVehicles = ref([])

const loadData = async () => {
  await withLoading(async () => {
    const res = await getTaskList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.taskNo = ''; queryParams.status = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增任务'
  Object.assign(formData, { id: null, taskType: 'DELIVERY', fromAddress: '', toAddress: '', planStartTime: '', planEndTime: '', remark: '' })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑任务'
  Object.assign(formData, row)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}

const handleAssign = async (row) => {
  const [dRes, vRes] = await Promise.all([getAvailableDrivers(), getAvailableVehicles()])
  availableDrivers.value = dRes.data || []
  availableVehicles.value = vRes.data || []
  assignForm.taskId = row.id
  assignForm.driverId = null
  assignForm.vehicleId = null
  assignDialogVisible.value = true
}

const submitAssign = async () => {
  if (!assignForm.driverId || !assignForm.vehicleId) { ElMessage.warning('请选择司机和车辆'); return }
  await assignTask(assignForm.taskId, { driverId: assignForm.driverId, vehicleId: assignForm.vehicleId })
  ElMessage.success('分配成功')
  assignDialogVisible.value = false
  loadData()
}

const handleStart = (row) => { ElMessageBox.confirm('确定开始该任务？', '提示', { type: 'warning' }).then(async () => { await startTask(row.id); ElMessage.success('已开始'); loadData() }) }
const handleComplete = (row) => { ElMessageBox.confirm('确定完成该任务？', '提示', { type: 'warning' }).then(async () => { await completeTask(row.id); ElMessage.success('已完成'); loadData() }) }
const handleCancel = (row) => { ElMessageBox.confirm('确定取消该任务？', '提示', { type: 'warning' }).then(async () => { await cancelTask(row.id); ElMessage.success('已取消'); loadData() }) }

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updateTask(formData.id, formData); ElMessage.success('更新成功') }
    else { await createTask(formData); ElMessage.success('创建成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) } finally { submitLoading.value = false }
}

onMounted(() => { loadData() })
</script>
