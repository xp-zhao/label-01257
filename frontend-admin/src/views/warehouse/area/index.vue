<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="所属库房">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择" clearable style="width: 200px;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域类型">
          <el-select v-model="queryParams.areaType" placeholder="请选择" clearable>
            <el-option label="存储区" value="STORAGE" />
            <el-option label="拣货区" value="PICKING" />
            <el-option label="收货区" value="RECEIVING" />
            <el-option label="发货区" value="SHIPPING" />
            <el-option label="暂存区" value="TEMPORARY" />
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
          <span class="title">区域列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增区域</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="areaCode" label="区域编码" width="120" />
        <el-table-column prop="areaName" label="区域名称" min-width="150" />
        <el-table-column prop="warehouseName" label="所属库房" min-width="150" />
        <el-table-column prop="areaType" label="区域类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ areaTypeMap[row.areaType] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="areaSize" label="面积(㎡)" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
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
        <el-form-item label="所属库房" prop="warehouseId">
          <el-select v-model="formData.warehouseId" placeholder="请选择库房" style="width: 100%;" @change="handleWarehouseChange">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域编码" prop="areaCode">
          <el-input v-model="formData.areaCode" placeholder="请输入区域编码" />
        </el-form-item>
        <el-form-item label="区域名称" prop="areaName">
          <el-input v-model="formData.areaName" placeholder="请输入区域名称" />
        </el-form-item>
        <el-form-item label="区域类型" prop="areaType">
          <el-select v-model="formData.areaType" placeholder="请选择" style="width: 100%;">
            <el-option label="存储区" value="STORAGE" />
            <el-option label="拣货区" value="PICKING" />
            <el-option label="收货区" value="RECEIVING" />
            <el-option label="发货区" value="SHIPPING" />
            <el-option label="暂存区" value="TEMPORARY" />
          </el-select>
        </el-form-item>
        <el-form-item label="面积(㎡)" prop="areaSize">
          <el-input-number v-model="formData.areaSize" :min="0" :max="maxAreaSize" :precision="2" style="width: 100%;" />
          <div v-if="remainingAreaInfo.totalArea > 0" class="area-hint">
            库房总面积: {{ remainingAreaInfo.totalArea }} ㎡，最大可填: {{ maxAreaSize }} ㎡
          </div>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
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
import { getAreaList, addArea, updateArea, deleteArea, getAllWarehouses, getWarehouseRemainingArea } from '@/api/warehouse'
import { withLoading } from '@/utils/loading'

const areaTypeMap = { STORAGE: '存储区', PICKING: '拣货区', RECEIVING: '收货区', SHIPPING: '发货区', TEMPORARY: '暂存区' }
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouseList = ref([])
const remainingAreaInfo = reactive({ totalArea: 0, usedArea: 0, remainingArea: 0 })
const maxAreaSize = ref(999999)

const queryParams = reactive({ pageNum: 1, pageSize: 10, warehouseId: null, areaType: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, warehouseId: null, areaCode: '', areaName: '', areaType: 'STORAGE', areaSize: 0, status: 1 })
const rules = {
  warehouseId: [{ required: true, message: '请选择库房', trigger: 'change' }],
  areaCode: [{ required: true, message: '请输入区域编码', trigger: 'blur' }],
  areaName: [{ required: true, message: '请输入区域名称', trigger: 'blur' }]
}

const loadWarehouses = async () => {
  try {
    const res = await getAllWarehouses()
    warehouseList.value = res.data || []
  } catch (error) { console.error(error) }
}

const loadData = async () => {
  await withLoading(async () => {
    const res = await getAreaList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.warehouseId = null; queryParams.areaType = ''; handleSearch() }

const resetRemainingArea = () => {
  Object.assign(remainingAreaInfo, { totalArea: 0, usedArea: 0, remainingArea: 0 })
  maxAreaSize.value = 999999
}

const loadRemainingArea = async (warehouseId, excludeAreaId = null, currentAreaSize = 0) => {
  if (!warehouseId) {
    resetRemainingArea()
    return
  }
  try {
    const res = await getWarehouseRemainingArea(warehouseId, excludeAreaId)
    Object.assign(remainingAreaInfo, res.data)
    // 编辑时，可用面积 = 剩余面积 + 当前区域原有面积
    const availableArea = Number(res.data.remainingArea) + Number(currentAreaSize)
    maxAreaSize.value = availableArea > 0 ? availableArea : 999999
  } catch (error) {
    console.error('获取剩余面积失败:', error)
    maxAreaSize.value = 999999
  }
}

const handleWarehouseChange = (warehouseId) => {
  // 新增时切换库房，不需要考虑当前区域面积
  loadRemainingArea(warehouseId, formData.id, formData.id ? formData.areaSize : 0)
}

const handleAdd = () => {
  dialogTitle.value = '新增区域'
  Object.assign(formData, { id: null, warehouseId: null, areaCode: '', areaName: '', areaType: 'STORAGE', areaSize: 0, status: 1 })
  resetRemainingArea()
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑区域'
  Object.assign(formData, row)
  // 编辑时传入当前区域面积，可用面积 = 剩余面积 + 当前区域面积
  loadRemainingArea(row.warehouseId, row.id, row.areaSize || 0)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除区域【${row.areaName}】吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteArea(row.id); ElMessage.success('删除成功'); loadData()
  })
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updateArea(formData.id, formData); ElMessage.success('更新成功') }
    else { await addArea(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) }
  finally { submitLoading.value = false }
}

onMounted(() => { loadWarehouses(); loadData() })
</script>

<style lang="scss" scoped>
.dialog-form {
  overflow: hidden;
}
.area-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
