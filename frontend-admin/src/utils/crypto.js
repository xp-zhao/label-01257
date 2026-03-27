/**
 * 简单的密码加密工具
 * 使用 Base64 + 简单混淆，实际生产环境建议使用更安全的加密方式
 */

/**
 * 加密密码
 * @param {string} password 原始密码
 * @returns {string} 加密后的密码
 */
export function encryptPassword(password) {
  if (!password) return ''
  // 添加时间戳混淆
  const timestamp = Date.now().toString(36)
  const mixed = `${timestamp}_${password}_wms`
  // Base64 编码
  return btoa(encodeURIComponent(mixed))
}

/**
 * 简单的 MD5 模拟（实际项目建议使用 crypto-js）
 * 这里使用简化版本，仅做示例
 */
export function simpleHash(str) {
  let hash = 0
  if (str.length === 0) return hash.toString()
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash
  }
  return Math.abs(hash).toString(16)
}
