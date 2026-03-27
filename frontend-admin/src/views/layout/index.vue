<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="asideWidth" class="layout-aside" :class="{ 'is-collapse': isCollapse }">
      <div class="logo">
        <el-icon :size="28"><Box /></el-icon>
        <span v-show="!isCollapse" class="logo-text">WMS</span>
      </div>
      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="$route.path"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="aside-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <!-- 首页 - 所有人可见 -->
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <!-- 动态渲染菜单 -->
          <template v-for="menu in userStore.menus" :key="menu.id">
            <!-- 有子菜单 -->
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.permissionCode">
              <template #title>
                <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
                <span>{{ menu.permissionName }}</span>
              </template>
              <el-menu-item 
                v-for="child in menu.children" 
                :key="child.id" 
                :index="child.path"
              >
                {{ child.permissionName }}
              </el-menu-item>
            </el-sub-menu>
            <!-- 无子菜单 -->
            <el-menu-item v-else :index="menu.path">
              <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
              <template #title>{{ menu.permissionName }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <!-- 主体区域 -->
    <el-container class="layout-main">
      <!-- 头部 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <component :is="isCollapse ? 'Expand' : 'Fold'" />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb-container">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="$route.meta.title">{{ $route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="layout-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const userStore = useUserStore()
const isCollapse = ref(false)
const windowWidth = ref(window.innerWidth)

// 根据图标名称获取图标组件
const getIcon = (iconName) => {
  if (!iconName) return 'Document'
  return ElementPlusIconsVue[iconName] ? iconName : 'Document'
}

// 响应式宽度计算
const asideWidth = computed(() => {
  if (windowWidth.value <= 768) {
    return isCollapse.value ? '0px' : '220px'
  }
  return isCollapse.value ? '64px' : '220px'
})

// 监听窗口变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
  // 当窗口小于768px时自动收起菜单
  if (windowWidth.value <= 768 && !isCollapse.value) {
    isCollapse.value = true
  }
}

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

onMounted(async () => {
  window.addEventListener('resize', handleResize)
  handleResize()
  
  try {
    await userStore.fetchUserInfo()
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.doLogout()
    })
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
  flex-shrink: 0;

  &.is-collapse {
    .logo {
      padding: 0;
      
      .logo-text {
        display: none;
      }
    }
    
    .aside-menu {
      :deep(.el-sub-menu__title) {
        padding: 0 !important;
        display: flex;
        justify-content: center;
        
        span {
          display: none;
        }
        
        .el-sub-menu__icon-arrow {
          display: none;
        }
      }
      
      :deep(.el-menu-item) {
        padding: 0 !important;
        display: flex;
        justify-content: center;
        
        span {
          display: none;
        }
      }
      
      :deep(.el-icon) {
        margin-right: 0;
      }
    }
  }

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #fff;
    font-size: 20px;
    font-weight: 600;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    white-space: nowrap;
    overflow: hidden;
  }

  .menu-scrollbar {
    height: calc(100vh - 60px);
    
    :deep(.el-scrollbar__wrap) {
      overflow-x: hidden;
    }
  }

  .aside-menu {
    border-right: none;
    width: 100% !important;
  }
}

.layout-main {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex: 1;
  min-width: 0;
}

.layout-header {
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    min-width: 0;
    flex: 1;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #606266;
      transition: color 0.3s;
      flex-shrink: 0;

      &:hover {
        color: #409EFF;
      }
    }
    
    .breadcrumb-container {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .header-right {
    flex-shrink: 0;
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.3s;

      &:hover {
        background-color: #F5F7FA;
      }

      .username {
        color: #606266;
        font-size: 14px;
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.layout-content {
  background-color: #F5F7FA;
  padding: 0;
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  width: 100%;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 响应式布局
@media screen and (max-width: 768px) {
  .layout-aside {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    height: 100vh;
    
    &.is-collapse {
      width: 0 !important;
      
      .logo, .menu-scrollbar {
        display: none;
      }
    }
  }
  
  .layout-header {
    padding: 0 12px;
    
    .header-right {
      .username {
        display: none;
      }
    }
  }
}
</style>
