<template>
  <div class="page-container">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="角色名称">
          <el-input v-model="queryParams.roleName" placeholder="请输入" clearable />
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
          <span class="title">角色列表</span>
          <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 新增角色</el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="roleCode" label="角色编码" min-width="120" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handlePermission(row)">分配权限</el-button>
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

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px" class="dialog-form">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="500px" destroy-on-close>
      <div class="perm-dialog-content">
        <div class="perm-header">
          <span>当前角色：<strong>{{ currentRole.roleName }}</strong></span>
          <div class="perm-actions">
            <el-button link type="primary" @click="handleCheckAll">全选</el-button>
            <el-button link type="info" @click="handleUncheckAll">取消全选</el-button>
            <el-button link type="success" @click="handleExpandAll">展开全部</el-button>
            <el-button link type="warning" @click="handleCollapseAll">收起全部</el-button>
          </div>
        </div>
        <el-tree
          ref="permTreeRef"
          :data="permissionTree"
          :props="treeProps"
          show-checkbox
          check-strictly
          node-key="id"
          default-expand-all
          :default-checked-keys="checkedPermIds"
          v-loading="permLoading"
          class="perm-tree"
        />
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitLoading" @click="handlePermSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, addRole, updateRole, deleteRole, getPermissionTree, getRolePermissions, assignPermissions } from '@/api/system'
import { withLoading } from '@/utils/loading'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({ pageNum: 1, pageSize: 10, roleName: '', status: null })
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = reactive({ id: null, roleCode: '', roleName: '', status: 1, remark: '' })
const rules = { roleCode: [{ required: true, message: '请输入', trigger: 'blur' }], roleName: [{ required: true, message: '请输入', trigger: 'blur' }] }

// 权限分配相关
const permDialogVisible = ref(false)
const permLoading = ref(false)
const permSubmitLoading = ref(false)
const permTreeRef = ref()
const permissionTree = ref([])
const checkedPermIds = ref([])
const currentRole = reactive({ id: null, roleName: '' })
const treeProps = { children: 'children', label: 'permissionName' }

const loadData = async () => {
  await withLoading(async () => {
    const res = await getRoleList(queryParams)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  }, loading)
}

const handleSearch = () => { queryParams.pageNum = 1; loadData() }
const handleReset = () => { queryParams.roleName = ''; handleSearch() }
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  Object.assign(formData, { id: null, roleCode: '', roleName: '', status: 1, remark: '' })
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  Object.assign(formData, row)
  dialogVisible.value = true
  nextTick(() => { formRef.value?.clearValidate() })
}
const handleDelete = (row) => { ElMessageBox.confirm(`确定删除【${row.roleName}】？`, '提示', { type: 'warning' }).then(async () => { await deleteRole(row.id); ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false); if (!valid) return
  submitLoading.value = true
  try {
    if (formData.id) { await updateRole(formData.id, formData); ElMessage.success('更新成功') }
    else { await addRole(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadData()
  } catch (error) { console.error(error) } finally { submitLoading.value = false }
}

// 分配权限
const handlePermission = async (row) => {
  currentRole.id = row.id
  currentRole.roleName = row.roleName
  checkedPermIds.value = []  // 先清空，避免显示旧数据
  permDialogVisible.value = true
  permLoading.value = true
  try {
    // 获取权限树和角色已有权限
    const [treeRes, permRes] = await Promise.all([
      getPermissionTree(),
      getRolePermissions(row.id)
    ])
    permissionTree.value = treeRes.data || []
    // 父子不关联模式，直接使用所有权限ID
    checkedPermIds.value = permRes.data || []
    // 数据加载完成后，手动设置选中的节点（default-checked-keys 只在首次渲染时生效）
    await nextTick()
    permTreeRef.value?.setCheckedKeys(checkedPermIds.value)
  } catch (error) {
    console.error(error)
  } finally {
    permLoading.value = false
  }
}

// 获取所有节点ID
const getAllNodeIds = (tree) => {
  const ids = []
  const traverse = (nodes) => {
    nodes.forEach(node => {
      ids.push(node.id)
      if (node.children && node.children.length > 0) {
        traverse(node.children)
      }
    })
  }
  traverse(tree)
  return ids
}

// 全选
const handleCheckAll = () => {
  const allIds = getAllNodeIds(permissionTree.value)
  permTreeRef.value?.setCheckedKeys(allIds)
}

// 取消全选
const handleUncheckAll = () => {
  permTreeRef.value?.setCheckedKeys([])
}

// 展开全部
const handleExpandAll = () => {
  const nodes = permTreeRef.value?.store?.nodesMap || {}
  Object.values(nodes).forEach(node => {
    node.expanded = true
  })
}

// 收起全部
const handleCollapseAll = () => {
  const nodes = permTreeRef.value?.store?.nodesMap || {}
  Object.values(nodes).forEach(node => {
    node.expanded = false
  })
}

// 提交权限分配
const handlePermSubmit = async () => {
  permSubmitLoading.value = true
  try {
    // 父子不关联模式，直接获取选中的节点
    const checkedKeys = permTreeRef.value?.getCheckedKeys() || []
    await assignPermissions(currentRole.id, checkedKeys)
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
  } catch (error) {
    console.error(error)
  } finally {
    permSubmitLoading.value = false
  }
}

onMounted(() => { loadData() })
</script>

<style scoped>
.perm-dialog-content {
  max-height: 400px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.perm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.perm-actions {
  display: flex;
  gap: 8px;
}

.perm-tree {
  flex: 1;
  overflow: auto;
  max-height: 340px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 8px;
}
</style>
