<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="入库单号">
          <el-input v-model="queryParams.orderNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="库房">
          <el-select v-model="queryParams.warehouseId" placeholder="请选择" clearable style="width: 200px;">
            <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="待入库" :value="1" />
            <el-option label="入库中" :value="2" />
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
          <span class="title">入库单列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增入库单</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="入库单号" width="180" />
        <el-table-column prop="warehouseName" label="入库仓库" min-width="150" />
        <el-table-column prop="supplier" label="供应商" min-width="150" />
        <el-table-column prop="inboundType" label="入库类型" width="100">
          <template #default="{ row }">{{ inboundTypeMap[row.inboundType] }}</template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="计划数量" width="90" />
        <el-table-column prop="actualQuantity" label="实际数量" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 1" type="success" link @click="handleConfirm(row)">确认入库</el-button>
            <el-button v-if="row.status < 3" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination :current-page="queryParams.pageNum" :page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]" :total="total" layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange" @current-change="handlePageChange" />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="900px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px" class="dialog-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="入库仓库" prop="warehouseId">
              <el-select v-model="formData.warehouseId" placeholder="请选择" style="width: 100%;">
                <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="入库类型" prop="inboundType">
              <el-select v-model="formData.inboundType" placeholder="请选择" style="width: 100%;">
                <el-option label="采购入库" value="PURCHASE" />
                <el-option label="退货入库" value="RETURN" />
                <el-option label="调拨入库" value="TRANSFER" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="供应商" prop="supplier">
              <el-input v-model="formData.supplier" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入" />
        </el-form-item>

        <el-divider content-position="left">商品明细</el-divider>
        <div style="margin-bottom: 12px; display: flex; gap: 10px; align-items: center;">
          <el-button type="primary" size="small" @click="addItem">
            <el-icon><Plus /></el-icon> 添加商品
          </el-button>
          <el-button type="success" size="small" @click="openScanDialog">
            <el-icon><VideoCameraFilled /></el-icon> 扫码录入
          </el-button>
        </div>
        <el-table :data="formData.items" border size="small">
          <el-table-column label="商品" min-width="200">
            <template #default="{ row }">
              <el-select v-model="row.productId" placeholder="请选择" style="width: 100%;" @change="(val) => onProductChange(row, val)">
                <el-option v-for="item in productList" :key="item.id" :label="`${item.productCode} - ${item.productName}`" :value="item.id" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="批次号" width="140">
            <template #default="{ row }">
              <el-input v-model="row.batchNo" placeholder="批次号" />
            </template>
          </el-table-column>
          <el-table-column label="计划数量" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.planQuantity" :min="1" size="small" style="width: 100%;" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button type="danger" link @click="removeItem($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 扫码弹窗 -->
    <el-dialog v-model="scanDialogVisible" title="扫码录入商品" width="500px" @opened="focusScanInput">
      <div class="scan-dialog-content">
        <el-alert type="info" :closable="false" style="margin-bottom: 16px;">
          <template #title>
            <span>请使用扫码枪扫描商品条码，或手动输入商品编码后按回车确认</span>
          </template>
        </el-alert>
        <el-form @submit.prevent="handleScanSubmit" label-width="80px" label-position="right">
          <el-form-item label="商品编码">
            <div class="scan-input-row">
              <el-input ref="scanInputRef" v-model="scanCode" placeholder="扫描或输入商品编码" 
                @keyup.enter="handleScanSubmit" clearable>
                <template #prefix><el-icon><Search /></el-icon></template>
              </el-input>
              <el-button type="warning" @click="simulateScan" style="margin-left: 8px;">
                <el-icon><Cpu /></el-icon> 模拟扫码
              </el-button>
            </div>
          </el-form-item>
          <el-form-item label="批次号">
            <el-input v-model="scanBatchNo" placeholder="请输入批次号" clearable />
          </el-form-item>
          <el-form-item label="数量">
            <el-input-number v-model="scanQuantity" :min="1" controls-position="right" class="scan-input-number" />
          </el-form-item>
        </el-form>
        <div v-if="scanResult" class="scan-result">
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="商品编码">{{ scanResult.productCode }}</el-descriptions-item>
            <el-descriptions-item label="商品名称">{{ scanResult.productName }}</el-descriptions-item>
            <el-descriptions-item label="规格">{{ scanResult.specification || '-' }}</el-descriptions-item>
            <el-descriptions-item label="单位">{{ scanResult.unit || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="scanDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleScanSubmit" :disabled="!scanCode">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 查看弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="入库单详情" width="800px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="入库单号">{{ viewData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="入库仓库">{{ viewData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="入库类型">{{ inboundTypeMap[viewData.inboundType] }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ viewData.supplier }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[viewData.status] }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">商品明细</el-divider>
      <el-table :data="viewData.items || []" border size="small">
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="batchNo" label="批次号" width="140" />
        <el-table-column prop="planQuantity" label="计划数量" width="100" />
        <el-table-column prop="actualQuantity" label="实际数量" width="100" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Cpu } from '@element-plus/icons-vue'
import { getInboundList, getInboundDetail, createInbound, updateInbound, submitInbound, confirmInbound, cancelInbound } from '@/api/stock'
import { getAllWarehouses } from '@/api/warehouse'
import { getAllProducts } from '@/api/stock'
import { withLoading } from '@/utils/loading'

const inboundTypeMap = { PURCHASE: '采购入库', RETURN: '退货入库', TRANSFER: '调拨入库', OTHER: '其他' }
const statusMap = { 0: '草稿', 1: '待入库', 2: '入库中', 3: '已完成', 4: '已取消' }
const statusTypeMap = { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success', 4: 'danger' }

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouseList = ref([])
const productList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, orderNo: '', warehouseId: null, status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, warehouseId: null, inboundType: 'PURCHASE', supplier: '', remark: '', items: [] })
const rules = { warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }] }

const viewDialogVisible = ref(false)
const viewData = ref({})

// 扫码相关
const scanDialogVisible = ref(false)
const scanInputRef = ref()
const scanCode = ref('')
const scanBatchNo = ref('')
const scanQuantity = ref(1)
const scanResult = ref(null)

const loadData = async () => {
  await withLoading(async () => {
    const res = await getInboundList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handlePageChange = (page) => { queryParams.pageNum = page; loadData() }
const handleSizeChange = (size) => { queryParams.pageSize = size; queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.orderNo = ''; queryParams.warehouseId = null; queryParams.status = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增入库单'
  Object.assign(formData, { id: null, warehouseId: null, inboundType: 'PURCHASE', supplier: '', remark: '', items: [] })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = async (row) => {
  dialogTitle.value = '编辑入库单'
  const res = await getInboundDetail(row.id)
  Object.assign(formData, res.data)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleView = async (row) => {
  const res = await getInboundDetail(row.id)
  viewData.value = res.data
  viewDialogVisible.value = true
}
const handleSubmit = (row) => {
  ElMessageBox.confirm('确定要提交该入库单吗？', '提示', { type: 'warning' }).then(async () => {
    await submitInbound(row.id); ElMessage.success('提交成功'); loadData()
  })
}
const handleConfirm = (row) => {
  ElMessageBox.confirm('确定要确认入库吗？', '提示', { type: 'warning' }).then(async () => {
    await confirmInbound(row.id); ElMessage.success('入库成功'); loadData()
  })
}
const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该入库单吗？', '提示', { type: 'warning' }).then(async () => {
    await cancelInbound(row.id); ElMessage.success('已取消'); loadData()
  })
}
const addItem = () => { formData.items.push({ productId: null, productCode: '', productName: '', batchNo: '', planQuantity: 1 }) }
const removeItem = (index) => { formData.items.splice(index, 1) }
const onProductChange = (row, val) => {
  const product = productList.value.find(p => p.id === val)
  if (product) { row.productCode = product.productCode; row.productName = product.productName }
}

// 扫码功能
const openScanDialog = () => {
  scanCode.value = ''
  scanBatchNo.value = ''
  scanQuantity.value = 1
  scanResult.value = null
  scanDialogVisible.value = true
}
const focusScanInput = () => {
  nextTick(() => { scanInputRef.value?.focus() })
}
const simulateScan = () => {
  // 模拟扫码枪：随机选择一个商品的编码填入
  if (productList.value.length === 0) {
    ElMessage.warning('暂无商品数据')
    return
  }
  const randomIndex = Math.floor(Math.random() * productList.value.length)
  const product = productList.value[randomIndex]
  scanCode.value = product.productCode
  scanResult.value = product
  // 自动生成批次号
  const now = new Date()
  scanBatchNo.value = `B${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}`
  ElMessage.success(`模拟扫码成功：${product.productCode}`)
}
const handleScanSubmit = () => {
  if (!scanCode.value.trim()) { ElMessage.warning('请输入或扫描商品编码'); return }
  // 根据商品编码查找商品
  const product = productList.value.find(p => p.productCode === scanCode.value.trim())
  if (!product) { ElMessage.error('未找到该商品编码对应的商品'); scanResult.value = null; return }
  scanResult.value = product
  // 检查批次号
  if (!scanBatchNo.value.trim()) { ElMessage.warning('请输入批次号'); return }
  // 检查是否已存在相同商品和批次号
  const existItem = formData.items.find(item => item.productId === product.id && item.batchNo === scanBatchNo.value.trim())
  if (existItem) {
    existItem.planQuantity += scanQuantity.value
    ElMessage.success(`商品【${product.productName}】数量已累加`)
  } else {
    formData.items.push({
      productId: product.id,
      productCode: product.productCode,
      productName: product.productName,
      batchNo: scanBatchNo.value.trim(),
      planQuantity: scanQuantity.value
    })
    ElMessage.success(`商品【${product.productName}】已添加`)
  }
  // 添加成功后关闭弹窗
  scanCode.value = ''
  scanBatchNo.value = ''
  scanQuantity.value = 1
  scanResult.value = null
  scanDialogVisible.value = false
}
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!formData.items.length) { ElMessage.warning('请添加商品明细'); return }
  // 校验商品明细必填项
  for (let i = 0; i < formData.items.length; i++) {
    const item = formData.items[i]
    if (!item.productId) { ElMessage.warning(`第${i + 1}行商品未选择`); return }
    if (!item.batchNo || !item.batchNo.trim()) { ElMessage.warning(`第${i + 1}行批次号未填写`); return }
    if (!item.planQuantity || item.planQuantity < 1) { ElMessage.warning(`第${i + 1}行计划数量必须大于0`); return }
  }
  submitLoading.value = true
  try {
    if (formData.id) { await updateInbound(formData.id, formData); ElMessage.success('更新成功') }
    else { await createInbound(formData); ElMessage.success('创建成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) }
  finally { submitLoading.value = false }
}

onMounted(async () => {
  const [wRes, pRes] = await Promise.all([getAllWarehouses(), getAllProducts()])
  warehouseList.value = wRes.data || []
  productList.value = pRes.data || []
  loadData()
})
</script>

<style scoped>
.scan-dialog-content {
  padding: 0 10px;
}
.scan-input-row {
  display: flex;
  align-items: center;
  width: 100%;
}
.scan-input-row .el-input {
  flex: 1;
}
.scan-input-number {
  width: 100%;
}
.scan-result {
  margin-top: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
