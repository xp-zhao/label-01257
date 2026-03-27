<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="所属库房">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择" clearable style="width: 200px;" @change="onWarehouseChange">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属区域">
          <el-select v-model="queryParams.areaId" placeholder="请选择" clearable style="width: 200px;">
            <el-option v-for="item in areaList" :key="item.id" :label="item.areaName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="货架编码">
          <el-input v-model="queryParams.shelfCode" placeholder="请输入" clearable />
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
          <span class="title">货架列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增货架</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="shelfCode" label="货架编码" width="120" />
        <el-table-column prop="shelfName" label="货架名称" min-width="150" />
        <el-table-column prop="warehouseName" label="所属库房" min-width="150" />
        <el-table-column prop="areaName" label="所属区域" min-width="120" />
        <el-table-column prop="layerCount" label="层数" width="80" />
        <el-table-column prop="columnCount" label="列数" width="80" />
        <el-table-column prop="maxWeight" label="最大承重(kg)" width="120" />
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
          <el-select v-model="formData.warehouseId" placeholder="请选择" style="width: 100%;" @change="onFormWarehouseChange">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属区域" prop="areaId">
          <el-select v-model="formData.areaId" placeholder="请选择" style="width: 100%;">
            <el-option v-for="item in formAreaList" :key="item.id" :label="item.areaName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="货架编码" prop="shelfCode">
          <el-input v-model="formData.shelfCode" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="货架名称" prop="shelfName">
          <el-input v-model="formData.shelfName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="层数" prop="layerCount">
          <el-input-number v-model="formData.layerCount" :min="1" :max="100" controls-position="right" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="列数" prop="columnCount">
          <el-input-number v-model="formData.columnCount" :min="1" :max="100" controls-position="right" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="承重(kg)" prop="maxWeight">
          <el-input-number v-model="formData.maxWeight" :min="0" :max="100000" controls-position="right" style="width: 200px;" />
        </el-form-item>
        <el-form-item label="状态">
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
import { getShelfList, addShelf, updateShelf, deleteShelf, getAllWarehouses, getAreasByWarehouse } from '@/api/warehouse'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouseList = ref([])
const areaList = ref([])
const formAreaList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, warehouseId: null, areaId: null, shelfCode: '' })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, warehouseId: null, areaId: null, shelfCode: '', shelfName: '', layerCount: 5, columnCount: 10, maxWeight: 5000, status: 1 })
const rules = {
  warehouseId: [{ required: true, message: '请选择库房', trigger: 'change' }],
  areaId: [{ required: true, message: '请选择区域', trigger: 'change' }],
  shelfCode: [{ required: true, message: '请输入货架编码', trigger: 'blur' }],
  shelfName: [{ required: true, message: '请输入货架名称', trigger: 'blur' }]
}

const loadWarehouses = async () => { const res = await getAllWarehouses(); warehouseList.value = res.data || [] }
const onWarehouseChange = async (val) => {
  queryParams.areaId = null
  if (val) { const res = await getAreasByWarehouse(val); areaList.value = res.data || [] }
  else { areaList.value = [] }
}
const onFormWarehouseChange = async (val) => {
  formData.areaId = null
  if (val) { const res = await getAreasByWarehouse(val); formAreaList.value = res.data || [] }
  else { formAreaList.value = [] }
}
const loadData = async () => {
  await withLoading(async () => {
    const res = await getShelfList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}
const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.warehouseId = null; queryParams.areaId = null; queryParams.shelfCode = ''; areaList.value = []; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增货架'
  Object.assign(formData, { id: null, warehouseId: null, areaId: null, shelfCode: '', shelfName: '', layerCount: 5, columnCount: 10, maxWeight: 5000, status: 1 })
  formAreaList.value = []
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = async (row) => {
  dialogTitle.value = '编辑货架'
  Object.assign(formData, row)
  if (row.warehouseId) { const res = await getAreasByWarehouse(row.warehouseId); formAreaList.value = res.data || [] }
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除货架【${row.shelfName}】吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteShelf(row.id); ElMessage.success('删除成功'); loadData()
  })
}
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updateShelf(formData.id, formData); ElMessage.success('更新成功') }
    else { await addShelf(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) }
  finally { submitLoading.value = false }
}

onMounted(() => { loadWarehouses(); loadData() })
</script>
