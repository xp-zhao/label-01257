import { useUserStore } from '@/stores/user'

/**
 * 权限指令
 * 使用方式：v-permission="'system:user:add'" 或 v-permission="['system:user:add', 'system:user:edit']"
 */
export const permission = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const { value } = binding
    
    if (value) {
      const permissions = userStore.permissions
      let hasPermission = false
      
      if (Array.isArray(value)) {
        // 数组形式，满足其中一个即可
        hasPermission = value.some(p => permissions.includes(p))
      } else {
        // 字符串形式
        hasPermission = permissions.includes(value)
      }
      
      if (!hasPermission) {
        // 没有权限，移除元素
        el.parentNode && el.parentNode.removeChild(el)
      }
    }
  }
}

export default permission
