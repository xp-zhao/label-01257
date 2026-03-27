import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      // 库房管理
      {
        path: 'warehouse/list',
        name: 'WarehouseList',
        component: () => import('@/views/warehouse/list/index.vue'),
        meta: { title: '库房列表', icon: 'OfficeBuilding', permission: 'warehouse:list' }
      },
      {
        path: 'warehouse/area',
        name: 'WarehouseArea',
        component: () => import('@/views/warehouse/area/index.vue'),
        meta: { title: '区域管理', icon: 'Grid', permission: 'warehouse:area' }
      },
      {
        path: 'warehouse/shelf',
        name: 'WarehouseShelf',
        component: () => import('@/views/warehouse/shelf/index.vue'),
        meta: { title: '货架管理', icon: 'Box', permission: 'warehouse:shelf' }
      },
      {
        path: 'warehouse/inventory',
        name: 'WarehouseInventory',
        component: () => import('@/views/warehouse/inventory/index.vue'),
        meta: { title: '库存监控', icon: 'DataAnalysis', permission: 'warehouse:inventory' }
      },
      // 出入库管理
      {
        path: 'stock/product',
        name: 'Product',
        component: () => import('@/views/stock/product/index.vue'),
        meta: { title: '商品管理', icon: 'Goods', permission: 'stock:product' }
      },
      {
        path: 'stock/inbound',
        name: 'Inbound',
        component: () => import('@/views/stock/inbound/index.vue'),
        meta: { title: '入库管理', icon: 'Download', permission: 'stock:inbound' }
      },
      {
        path: 'stock/outbound',
        name: 'Outbound',
        component: () => import('@/views/stock/outbound/index.vue'),
        meta: { title: '出库管理', icon: 'Upload', permission: 'stock:outbound' }
      },
      {
        path: 'stock/adjust',
        name: 'InventoryAdjust',
        component: () => import('@/views/stock/adjust/index.vue'),
        meta: { title: '库存调整', icon: 'Edit', permission: 'stock:adjust' }
      },
      // 策略管理
      {
        path: 'strategy/storage',
        name: 'StorageStrategy',
        component: () => import('@/views/strategy/storage/index.vue'),
        meta: { title: '存储策略', icon: 'FolderOpened', permission: 'strategy:storage' }
      },
      {
        path: 'strategy/picking',
        name: 'PickingStrategy',
        component: () => import('@/views/strategy/picking/index.vue'),
        meta: { title: '拣货策略', icon: 'Select', permission: 'strategy:picking' }
      },
      {
        path: 'strategy/replenish',
        name: 'ReplenishStrategy',
        component: () => import('@/views/strategy/replenish/index.vue'),
        meta: { title: '补货策略', icon: 'Refresh', permission: 'strategy:replenish' }
      },
      // 运输调度
      {
        path: 'transport/task',
        name: 'TransportTask',
        component: () => import('@/views/transport/task/index.vue'),
        meta: { title: '运输任务', icon: 'List', permission: 'transport:task' }
      },
      {
        path: 'transport/driver',
        name: 'Driver',
        component: () => import('@/views/transport/driver/index.vue'),
        meta: { title: '司机管理', icon: 'Avatar', permission: 'transport:driver' }
      },
      {
        path: 'transport/vehicle',
        name: 'Vehicle',
        component: () => import('@/views/transport/vehicle/index.vue'),
        meta: { title: '车辆管理', icon: 'Van', permission: 'transport:vehicle' }
      },
      // 报表管理
      {
        path: 'report/inventory',
        name: 'InventoryReport',
        component: () => import('@/views/report/inventory/index.vue'),
        meta: { title: '库存报表', icon: 'PieChart', permission: 'report:inventory' }
      },
      {
        path: 'report/stock',
        name: 'StockReport',
        component: () => import('@/views/report/stock/index.vue'),
        meta: { title: '出入库报表', icon: 'TrendCharts', permission: 'report:stock' }
      },
      {
        path: 'report/turnover',
        name: 'TurnoverReport',
        component: () => import('@/views/report/turnover/index.vue'),
        meta: { title: '周转分析', icon: 'DataAnalysis', permission: 'report:turnover' }
      },
      {
        path: 'report/warning',
        name: 'WarningReport',
        component: () => import('@/views/report/warning/index.vue'),
        meta: { title: '异常预警', icon: 'WarnTriangleFilled', permission: 'report:warning' }
      },
      // 系统管理
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User', permission: 'system:user' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled', permission: 'system:role' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 物流仓库管理系统` : '物流仓库管理系统'
  
  const token = localStorage.getItem('token')
  
  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }
  
  // 未登录跳转登录页
  if (!token) {
    next('/login')
    return
  }
  
  // 首页和dashboard不需要权限检查，直接放行
  if (to.path === '/' || to.path === '/dashboard') {
    next()
    return
  }
  
  // 获取用户权限（如果还没有的话）
  const userStore = useUserStore()
  if (userStore.permissions.length === 0) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      next('/login')
      return
    }
  }
  
  // 检查路由权限
  const permission = to.meta.permission
  if (permission) {
    const hasAccess = userStore.permissions.includes(permission)
    if (!hasAccess) {
      // 没有权限，跳转到首页
      console.warn(`无权限访问: ${to.path}, 需要权限: ${permission}`)
      next('/dashboard')
      return
    }
  }
  
  next()
})

export default router
