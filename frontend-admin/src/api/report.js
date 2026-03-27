import request from '@/utils/request'

// 仪表盘
export function getDashboard() {
  return request.get('/reports/dashboard')
}

// 库存汇总
export function getInventorySummary(params) {
  return request.get('/reports/inventory/summary', { params })
}

// 库存明细
export function getInventoryDetail(params) {
  return request.get('/reports/inventory/detail', { params })
}

// 入库汇总
export function getInboundSummary(params) {
  return request.get('/reports/inbound/summary', { params })
}

// 出库汇总
export function getOutboundSummary(params) {
  return request.get('/reports/outbound/summary', { params })
}

// 周转分析
export function getTurnover(params) {
  return request.get('/reports/turnover', { params })
}

// 异常预警
export function getWarning() {
  return request.get('/reports/warning')
}
