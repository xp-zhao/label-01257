import request from '@/utils/request'

// 用户管理
export function getUserList(params) {
  return request.get('/users', { params })
}

export function addUser(data) {
  return request.post('/users', data)
}

export function updateUser(id, data) {
  return request.put(`/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function updateUserStatus(id, status) {
  return request.put(`/users/${id}/status`, null, { params: { status } })
}

export function resetPassword(id) {
  return request.post(`/users/${id}/reset-password`)
}

// 角色管理
export function getRoleList(params) {
  return request.get('/roles', { params })
}

export function getAllRoles() {
  return request.get('/roles/all')
}

export function addRole(data) {
  return request.post('/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/roles/${id}`)
}

export function getRolePermissions(id) {
  return request.get(`/roles/${id}/permissions`)
}

export function assignPermissions(id, permissionIds) {
  return request.put(`/roles/${id}/permissions`, permissionIds)
}

// 获取权限树（用于角色权限分配）
export function getPermissionTree() {
  return request.get('/roles/permissions/tree')
}
