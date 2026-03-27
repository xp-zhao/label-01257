<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="出库单号">
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
            <el-option label="待审核" :value="1" />
            <el-option label="已审核" :value="2" />
            <el-option label="拣货中" :value="3" />
            <el-option label="已出库" :value="4" />
            <el-option label="已取消" :value="5" />
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
          <span class="title">出库单列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增出库单</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="出库单号" width="180" />
        <el-table-column prop="warehouseName" label="出库仓库" min-width="150" />
        <el-table-column prop="customer" label="客户" min-width="150" />
        <el-table-column prop="outboundType" label="出库类型" width="100">
          <template #default="{ row }">{{ outboundTypeMap[row.outboundType] }}</template>
        </el-table-column>
        <el-table-column prop="totalQuantity" label="计划数量" width="90" />
        <el-table-column prop="actualQuantity" label="实际数量" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTypeMap[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="handleSubmitOrder(row)">提交</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="handleAudit(row)">审核</el-button>
            <el-button v-if="row.status === 2" type="success" link @click="handleConfirm(row)">确认出库</el-button>
            <el-button v-if="row.status < 4" type="danger" link @click="handleCancel(row)">取消</el-button>
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
            <el-form-item label="出库仓库" prop="warehouseId">
              <el-select v-model="formData.warehouseId" placeholder="请选择" style="width: 100%;">
                <el-option v-for="item in warehouseList" :key="item.id" :label="item.warehouseName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="出库类型" prop="outboundType">
              <el-select v-model="formData.outboundType" placeholder="请选择" style="width: 100%;">
                <el-option label="销售出库" value="SALES" />
                <el-option label="退货出库" value="RETURN" />
                <el-option label="调拨出库" value="TRANSFER" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="客户" prop="customer">
              <el-input v-model="formData.customer" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="送货地址">
          <el-input v-model="formData.deliveryAddress" placeholder="请输入" />
        </el-form-item>
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
    <el-dialog v-model="viewDialogVisible" title="出库单详情" width="800px">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="出库单号">{{ viewData.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="出库仓库">{{ viewData.warehouseName }}</el-descriptions-item>
        <el-descriptions-item label="出库类型">{{ outboundTypeMap[viewData.outboundType] }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ viewData.customer }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusMap[viewData.status] }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <el-divider content-position="left">商品明细</el-divider>
      <el-table :data="viewData.items || []" border size="small">
        <el-table-column prop="productCode" label="商品编码" width="120" />
        <el-table-column prop="productName" label="商品名称" />
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
import { getOutboundList, getOutboundDetail, createOutbound, updateOutbound, submitOutbound, auditOutbound, confirmOutbound, cancelOutbound } from '@/api/stock'
import { getAllWarehouses } from '@/api/warehouse'
import { getAllProducts } from '@/api/stock'
import { withLoading } from '@/utils/loading'

const outboundTypeMap = { SALES: '销售出库', RETURN: '退货出库', TRANSFER: '调拨出库', OTHER: '其他' }
const statusMap = { 0: '草稿', 1: '待审核', 2: '已审核', 3: '拣货中', 4: '已出库', 5: '已取消' }
const statusTypeMap = { 0: 'info', 1: 'warning', 2: 'primary', 3: '', 4: 'success', 5: 'danger' }

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
const formData = reactive({ id: null, warehouseId: null, outboundType: 'SALES', customer: '', deliveryAddress: '', remark: '', items: [] })
const rules = { warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }] }

const viewDialogVisible = ref(false)
const viewData = ref({})

// 扫码相关
const scanDialogVisible = ref(false)
const scanInputRef = ref()
const scanCode = ref('')
const scanQuantity = ref(1)
const scanResult = ref(null)

const loadData = async () => {
  await withLoading(async () => {
    const res = await getOutboundList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handlePageChange = (page) => { queryParams.pageNum = page; loadData() }
const handleSizeChange = (size) => { queryParams.pageSize = size; queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.orderNo = ''; queryParams.warehouseId = null; queryParams.status = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增出库单'
  Object.assign(formData, { id: null, warehouseId: null, outboundType: 'SALES', customer: '', deliveryAddress: '', remark: '', items: [] })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = async (row) => {
  dialogTitle.value = '编辑出库单'
  const res = await getOutboundDetail(row.id)
  Object.assign(formData, res.data)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleView = async (row) => {
  const res = await getOutboundDetail(row.id)
  viewData.value = res.data
  viewDialogVisible.value = true
}
const handleSubmitOrder = (row) => {
  ElMessageBox.confirm('确定要提交该出库单吗？', '提示', { type: 'warning' }).then(async () => {
    await submitOutbound(row.id); ElMessage.success('提交成功'); loadData()
  })
}
const handleAudit = (row) => {
  ElMessageBox.confirm('是否审核通过该出库单？', '审核', { distinguishCancelAndClose: true, confirmButtonText: '通过', cancelButtonText: '拒绝', type: 'warning' })
    .then(async () => { await auditOutbound(row.id, { approved: true }); ElMessage.success('审核通过'); loadData() })
    .catch(async (action) => { if (action === 'cancel') { await auditOutbound(row.id, { approved: false }); ElMessage.info('已拒绝'); loadData() } })
}
const handleConfirm = (row) => {
  ElMessageBox.confirm('确定要确认出库吗？', '提示', { type: 'warning' }).then(async () => {
    await confirmOutbound(row.id); ElMessage.success('出库成功'); loadData()
  })
}
const handleCancel = (row) => {
  ElMessageBox.confirm('确定要取消该出库单吗？', '提示', { type: 'warning' }).then(async () => {
    await cancelOutbound(row.id); ElMessage.success('已取消'); loadData()
  })
}
const addItem = () => { formData.items.push({ productId: null, productCode: '', productName: '', planQuantity: 1 }) }
const removeItem = (index) => { formData.items.splice(index, 1) }
const onProductChange = (row, val) => {
  const product = productList.value.find(p => p.id === val)
  if (product) { row.productCode = product.productCode; row.productName = product.productName }
}

// 扫码功能
const openScanDialog = () => {
  scanCode.value = ''
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
  ElMessage.success(`模拟扫码成功：${product.productCode}`)
}
const handleScanSubmit = () => {
  if (!scanCode.value.trim()) { ElMessage.warning('请输入或扫描商品编码'); return }
  // 根据商品编码查找商品
  const product = productList.value.find(p => p.productCode === scanCode.value.trim())
  if (!product) { ElMessage.error('未找到该商品编码对应的商品'); scanResult.value = null; return }
  scanResult.value = product
  // 检查是否已存在相同商品
  const existItem = formData.items.find(item => item.productId === product.id)
  if (existItem) {
    existItem.planQuantity += scanQuantity.value
    ElMessage.success(`商品【${product.productName}】数量已累加`)
  } else {
    formData.items.push({
      productId: product.id,
      productCode: product.productCode,
      productName: product.productName,
      planQuantity: scanQuantity.value
    })
    ElMessage.success(`商品【${product.productName}】已添加`)
  }
  // 添加成功后关闭弹窗
  scanCode.value = ''
  scanQuantity.value = 1
  scanResult.value = null
  scanDialogVisible.value = false
}
const handleSave = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (!formData.items.length) { ElMessage.warning('请添加商品明细'); return }
  submitLoading.value = true
  try {
    if (formData.id) { await updateOutbound(formData.id, formData); ElMessage.success('更新成功') }
    else { await createOutbound(formData); ElMessage.success('创建成功') }
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
