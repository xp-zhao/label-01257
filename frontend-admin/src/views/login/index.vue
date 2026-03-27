<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>物流仓库管理系统</h1>
        <p>Warehouse Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
            autocomplete="username"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            autocomplete="current-password"
            @keyup.enter="handleLogin"
          >
            <template #suffix>
              <el-icon class="password-icon" @click="showPassword = !showPassword">
                <component :is="showPassword ? 'View' : 'Hide'" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            class="login-btn"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <div class="login-footer">
      <p>Copyright © 2024 WMS. All Rights Reserved.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
// encryptPassword removed - backend uses BCrypt, no frontend encryption needed

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const showPassword = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, max: 20, message: '密码长度在 4 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 加密密码后发送
    const loginData = {
      username: form.username,
      password: form.password // 后端使用 BCrypt，这里直接传原始密码
    }
    await userStore.doLogin(loginData)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
    // 错误提示已在request拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    animation: rotate 30s linear infinite;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.login-box {
  width: 420px;
  max-width: calc(100vw - 40px);
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  h1 {
    font-size: 28px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
  }

  p {
    font-size: 14px;
    color: #909399;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }

  .el-input {
    :deep(.el-input__wrapper) {
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    }
  }
  
  .password-icon {
    cursor: pointer;
    color: #909399;
    transition: color 0.3s;
    
    &:hover {
      color: #409EFF;
    }
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;

  &:hover {
    background: linear-gradient(135deg, #5a6fd6 0%, #6a4190 100%);
  }
}

.login-footer {
  position: absolute;
  bottom: 20px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

@media screen and (max-width: 480px) {
  .login-box {
    padding: 30px 20px;
  }
  
  .login-header {
    margin-bottom: 30px;
    
    h1 {
      font-size: 22px;
    }
  }
}
</style>
