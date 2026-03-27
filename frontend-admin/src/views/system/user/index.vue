<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryParams.realName" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
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
          <span class="title">用户列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增用户</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="realName" label="姓名" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="danger" link @click="handleDelete(row)" :disabled="row.username === 'admin'">删除</el-button>
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
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px" class="dialog-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item v-if="!formData.id" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" placeholder="请输入，默认123456" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="formData.roleIds" multiple placeholder="请选择角色" style="width: 100%;">
            <el-option v-for="item in roleList" :key="item.id" :label="item.roleName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">正常</el-radio>
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
import { getUserList, addUser, updateUser, deleteUser, resetPassword, getAllRoles } from '@/api/system'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const roleList = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, username: '', realName: '', status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, username: '', password: '', realName: '', phone: '', email: '', roleIds: [], status: 1 })
// 手机号校验
const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号格式'))
  } else {
    callback()
  }
}
// 邮箱校验
const validateEmail = (rule, value, callback) => {
  if (value && !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
    callback(new Error('请输入正确的邮箱格式'))
  } else {
    callback()
  }
}
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  email: [{ validator: validateEmail, trigger: 'blur' }]
}

const loadData = async () => {
  await withLoading(async () => {
    const res = await getUserList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.username = ''; queryParams.realName = ''; queryParams.status = null; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(formData, { id: null, username: '', password: '', realName: '', phone: '', email: '', roleIds: [], status: 1 })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(formData, { ...row, roleIds: row.roleIds || [] })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => { ElMessageBox.confirm(`确定删除【${row.username}】？`, '提示', { type: 'warning' }).then(async () => { await deleteUser(row.id); ElMessage.success('删除成功'); loadData() }) }
const handleResetPwd = (row) => { ElMessageBox.confirm(`确定重置【${row.username}】的密码为123456？`, '提示', { type: 'warning' }).then(async () => { await resetPassword(row.id); ElMessage.success('重置成功') }) }
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updateUser(formData.id, formData); ElMessage.success('更新成功') }
    else { await addUser(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) } finally { submitLoading.value = false }
}

onMounted(async () => { const res = await getAllRoles(); roleList.value = res.data || []; loadData() })
</script>
