import request from '@/utils/request'

// 运输任务
export function getTaskList(params) {
  return request.get('/transport/tasks', { params })
}

export function getTaskById(id) {
  return request.get(`/transport/tasks/${id}`)
}

export function createTask(data) {
  return request.post('/transport/tasks', data)
}

export function updateTask(id, data) {
  return request.put(`/transport/tasks/${id}`, data)
}

export function assignTask(id, data) {
  return request.post(`/transport/tasks/${id}/assign`, data)
}

export function startTask(id) {
  return request.post(`/transport/tasks/${id}/start`)
}

export function completeTask(id) {
  return request.post(`/transport/tasks/${id}/complete`)
}

export function cancelTask(id) {
  return request.post(`/transport/tasks/${id}/cancel`)
}

// 司机管理
export function getDriverList(params) {
  return request.get('/transport/drivers', { params })
}

export function getAvailableDrivers() {
  return request.get('/transport/drivers/available')
}

export function addDriver(data) {
  return request.post('/transport/drivers', data)
}

export function updateDriver(id, data) {
  return request.put(`/transport/drivers/${id}`, data)
}

export function deleteDriver(id) {
  return request.delete(`/transport/drivers/${id}`)
}

// 车辆管理
export function getVehicleList(params) {
  return request.get('/transport/vehicles', { params })
}

export function getAvailableVehicles() {
  return request.get('/transport/vehicles/available')
}

export function addVehicle(data) {
  return request.post('/transport/vehicles', data)
}

export function updateVehicle(id, data) {
  return request.put(`/transport/vehicles/${id}`, data)
}

export function deleteVehicle(id) {
  return request.delete(`/transport/vehicles/${id}`)
}
