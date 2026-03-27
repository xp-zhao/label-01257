import request from '@/utils/request'

// 库房管理
export function getWarehouseList(params) {
  return request.get('/warehouses', { params })
}

export function getAllWarehouses() {
  return request.get('/warehouses/all')
}

export function getWarehouseById(id) {
  return request.get(`/warehouses/${id}`)
}

export function addWarehouse(data) {
  return request.post('/warehouses', data)
}

export function updateWarehouse(id, data) {
  return request.put(`/warehouses/${id}`, data)
}

export function deleteWarehouse(id) {
  return request.delete(`/warehouses/${id}`)
}

export function getWarehouseStats(id) {
  return request.get(`/warehouses/${id}/stats`)
}

export function getWarehouseRemainingArea(id, excludeAreaId) {
  return request.get(`/warehouses/${id}/remaining-area`, { params: { excludeAreaId } })
}

// 区域管理
export function getAreaList(params) {
  return request.get('/areas', { params })
}

export function getAreasByWarehouse(warehouseId) {
  return request.get(`/areas/warehouse/${warehouseId}`)
}

export function addArea(data) {
  return request.post('/areas', data)
}

export function updateArea(id, data) {
  return request.put(`/areas/${id}`, data)
}

export function deleteArea(id) {
  return request.delete(`/areas/${id}`)
}

// 货架管理
export function getShelfList(params) {
  return request.get('/shelves', { params })
}

export function getShelvesByArea(areaId) {
  return request.get(`/shelves/area/${areaId}`)
}

export function addShelf(data) {
  return request.post('/shelves', data)
}

export function updateShelf(id, data) {
  return request.put(`/shelves/${id}`, data)
}

export function deleteShelf(id) {
  return request.delete(`/shelves/${id}`)
}
