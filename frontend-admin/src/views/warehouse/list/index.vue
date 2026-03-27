<template>
  <div class="page-container">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="库房编码">
          <el-input v-model="queryParams.warehouseCode" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="库房名称">
          <el-input v-model="queryParams.warehouseName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="title">库房列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon> 新增库房
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="warehouseCode" label="库房编码" width="120" />
        <el-table-column prop="warehouseName" label="库房名称" min-width="150" />
        <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
        <el-table-column prop="totalArea" label="总面积(㎡)" width="110" />
        <el-table-column prop="usedArea" label="已用面积(㎡)" width="120" />
        <el-table-column prop="managerName" label="负责人" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '启用' : '停用' }}
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
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="dialog-form">
        <el-form-item label="库房编码" prop="warehouseCode">
          <el-input v-model="formData.warehouseCode" placeholder="请输入库房编码" />
        </el-form-item>
        <el-form-item label="库房名称" prop="warehouseName">
          <el-input v-model="formData.warehouseName" placeholder="请输入库房名称" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总面积" prop="totalArea" label-width="80px">
              <el-input-number v-model="formData.totalArea" :min="0" :max="9999999" :precision="2" controls-position="right" style="width: 100%;" @change="handleTotalAreaChange" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="已用面积" prop="usedArea" label-width="80px">
              <el-input-number v-model="formData.usedArea" :min="0" :max="9999999" :precision="2" controls-position="right" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="managerName" label-width="80px">
              <el-input v-model="formData.managerName" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone" label-width="80px">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import { getWarehouseList, addWarehouse, updateWarehouse, deleteWarehouse } from '@/api/warehouse'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  warehouseCode: '',
  warehouseName: '',
  status: null
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({
  id: null,
  warehouseCode: '',
  warehouseName: '',
  address: '',
  totalArea: 0,
  usedArea: 0,
  managerName: '',
  phone: '',
  status: 1,
  remark: ''
})

// 自定义验证：已用面积必须小于等于总面积
const validateUsedArea = (rule, value, callback) => {
  if (value > formData.totalArea) {
    callback(new Error('已用面积不能大于总面积'))
  } else {
    callback()
  }
}

const rules = {
  warehouseCode: [{ required: true, message: '请输入库房编码', trigger: 'blur' }],
  warehouseName: [{ required: true, message: '请输入库房名称', trigger: 'blur' }],
  totalArea: [
    { required: true, message: '请输入总面积', trigger: 'change' },
    { type: 'number', min: 0, max: 9999999, message: '总面积范围为0-9999999', trigger: 'change' }
  ],
  usedArea: [
    { required: true, message: '请输入已用面积', trigger: 'change' },
    { type: 'number', min: 0, max: 9999999, message: '已用面积范围为0-9999999', trigger: 'change' },
    { validator: validateUsedArea, trigger: 'change' }
  ],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }]
}

// 总面积变化时重新验证已用面积
const handleTotalAreaChange = () => {
  if (formRef.value) {
    formRef.value.validateField('usedArea')
  }
}

const loadData = async () => {
  await withLoading(async () => {
    const res = await getWarehouseList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  queryParams.warehouseCode = ''
  queryParams.warehouseName = ''
  queryParams.status = null
  handleSearch()
}

const resetForm = () => {
  Object.assign(formData, {
    id: null,
    warehouseCode: '',
    warehouseName: '',
    address: '',
    totalArea: 0,
    usedArea: 0,
    managerName: '',
    phone: '',
    status: 1,
    remark: ''
  })
  // 清除校验状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleAdd = () => {
  dialogTitle.value = '新增库房'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑库房'
  Object.assign(formData, row)
  dialogVisible.value = true
  // 编辑时也清除校验状态
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除库房【${row.warehouseName}】吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteWarehouse(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
    }
  })
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (formData.id) {
      await updateWarehouse(formData.id, formData)
      ElMessage.success('更新成功')
    } else {
      await addWarehouse(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.dialog-form {
  overflow: hidden;
}
</style>
