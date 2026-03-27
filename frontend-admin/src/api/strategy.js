import request from '@/utils/request'

// 存储策略
export function getStorageStrategyList(params) {
  return request.get('/strategies/storage', { params })
}

export function addStorageStrategy(data) {
  return request.post('/strategies/storage', data)
}

export function updateStorageStrategy(id, data) {
  return request.put(`/strategies/storage/${id}`, data)
}

export function deleteStorageStrategy(id) {
  return request.delete(`/strategies/storage/${id}`)
}

// 拣货策略
export function getPickingStrategyList(params) {
  return request.get('/strategies/picking', { params })
}

export function addPickingStrategy(data) {
  return request.post('/strategies/picking', data)
}

export function updatePickingStrategy(id, data) {
  return request.put(`/strategies/picking/${id}`, data)
}

export function deletePickingStrategy(id) {
  return request.delete(`/strategies/picking/${id}`)
}

// 补货策略
export function getReplenishStrategyList(params) {
  return request.get('/strategies/replenish', { params })
}

export function addReplenishStrategy(data) {
  return request.post('/strategies/replenish', data)
}

export function updateReplenishStrategy(id, data) {
  return request.put(`/strategies/replenish/${id}`, data)
}

export function deleteReplenishStrategy(id) {
  return request.delete(`/strategies/replenish/${id}`)
}
