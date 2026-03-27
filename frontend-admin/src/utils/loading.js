/**
 * 带最小显示时间的 loading 控制工具
 * 解决接口响应过快导致 loading 一闪而过的问题
 */

const MIN_LOADING_TIME = 300 // 最小 loading 显示时间（毫秒）

/**
 * 执行带 loading 的异步操作
 * @param {Function} asyncFn - 异步函数
 * @param {Object} loadingRef - loading 状态的 ref 对象
 * @returns {Promise} 异步操作结果
 */
export async function withLoading(asyncFn, loadingRef) {
  const startTime = Date.now()
  loadingRef.value = true
  
  try {
    const result = await asyncFn()
    const elapsed = Date.now() - startTime
    
    // 如果执行时间小于最小显示时间，等待剩余时间
    if (elapsed < MIN_LOADING_TIME) {
      await new Promise(resolve => setTimeout(resolve, MIN_LOADING_TIME - elapsed))
    }
    
    return result
  } finally {
    loadingRef.value = false
  }
}

/**
 * 创建带最小显示时间的 loading 控制器
 * @param {Object} loadingRef - loading 状态的 ref 对象
 * @returns {Object} loading 控制器
 */
export function createLoadingController(loadingRef) {
  let startTime = 0
  
  return {
    start() {
      startTime = Date.now()
      loadingRef.value = true
    },
    async end() {
      const elapsed = Date.now() - startTime
      if (elapsed < MIN_LOADING_TIME) {
        await new Promise(resolve => setTimeout(resolve, MIN_LOADING_TIME - elapsed))
      }
      loadingRef.value = false
    }
  }
}
