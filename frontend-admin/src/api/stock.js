import request from '@/utils/request'
import axios from 'axios'

// 商品管理
export function getProductList(params) {
  return request.get('/products', { params })
}

export function getAllProducts() {
  return request.get('/products/all')
}

export function addProduct(data) {
  return request.post('/products', data)
}

export function updateProduct(id, data) {
  return request.put(`/products/${id}`, data)
}

export function deleteProduct(id) {
  return request.delete(`/products/${id}`)
}

export function exportProducts(params) {
  const token = localStorage.getItem('token')
  return axios.get('/api/products/export', {
    baseURL: '',
    params,
    responseType: 'blob',
    headers: {
      'Authorization': token ? `Bearer ${token}` : ''
    }
  })
}

// 库存管理
export function getInventoryList(params) {
  return request.get('/inventory', { params })
}

export function getInventoryWarning() {
  return request.get('/inventory/warning')
}

export function adjustInventory(data) {
  return request.post('/inventory/adjust', data)
}

export function getAdjustList(params) {
  return request.get('/inventory/adjust', { params })
}

// 入库管理
export function getInboundList(params) {
  return request.get('/inbound', { params })
}

export function getInboundDetail(id) {
  return request.get(`/inbound/${id}`)
}

export function createInbound(data) {
  return request.post('/inbound', data)
}

export function updateInbound(id, data) {
  return request.put(`/inbound/${id}`, data)
}

export function submitInbound(id) {
  return request.post(`/inbound/${id}/submit`)
}

export function confirmInbound(id) {
  return request.post(`/inbound/${id}/confirm`)
}

export function cancelInbound(id) {
  return request.post(`/inbound/${id}/cancel`)
}

// 出库管理
export function getOutboundList(params) {
  return request.get('/outbound', { params })
}

export function getOutboundDetail(id) {
  return request.get(`/outbound/${id}`)
}

export function createOutbound(data) {
  return request.post('/outbound', data)
}

export function updateOutbound(id, data) {
  return request.put(`/outbound/${id}`, data)
}

export function submitOutbound(id) {
  return request.post(`/outbound/${id}/submit`)
}

export function auditOutbound(id, data) {
  return request.post(`/outbound/${id}/audit`, data)
}

export function confirmOutbound(id) {
  return request.post(`/outbound/${id}/confirm`)
}

export function cancelOutbound(id) {
  return request.post(`/outbound/${id}/cancel`)
}
