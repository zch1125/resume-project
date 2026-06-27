<template>
  <div class="login-container">
    <div class="login-card">
      <div class="card-accent"></div>
      <div class="login-header">
        <div class="header-icon">
          <svg width="36" height="36" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" fill="currentColor"/>
          </svg>
        </div>
        <h1 class="login-title">简历分析平台</h1>
        <p class="login-subtitle">{{ isRegister ? '创建账号，解锁AI简历分析' : '登录后使用AI简历分析与模拟面试' }}</p>
      </div>

      <div class="tab-bar">
        <button class="tab-btn" :class="{ active: !isRegister }" @click="isRegister = false">登录</button>
        <button class="tab-btn" :class="{ active: isRegister }" @click="isRegister = true">注册</button>
      </div>

      <form class="login-form" @submit.prevent="handleSubmit">
        <div class="input-group">
          <div class="input-icon">👤</div>
          <input
            v-model="form.username"
            type="text"
            placeholder="用户名"
            class="premium-input"
            @input="clearError"
          />
        </div>

        <div class="input-group">
          <div class="input-icon">🔒</div>
          <input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            placeholder="密码"
            class="premium-input"
            @input="clearError"
          />
          <button type="button" class="password-toggle" @click="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁️' }}
          </button>
        </div>

        <div class="input-group" v-if="isRegister">
          <div class="input-icon">✏️</div>
          <input
            v-model="form.nickname"
            type="text"
            placeholder="昵称（选填）"
            class="premium-input"
          />
        </div>

        <div v-if="error" class="error-msg">{{ error }}</div>

        <button type="submit" class="submit-btn" :class="{ loading: loading }" :disabled="loading">
          <span v-if="loading" class="btn-spinner"></span>
          <span v-else class="btn-icon">{{ isRegister ? '✨' : '🚀' }}</span>
          <span>{{ loading ? '处理中...' : isRegister ? '创建账号' : '登录' }}</span>
        </button>
      </form>

      <p v-if="!isRegister && !loading" class="register-hint">
        还没账号？
        <a href="#" @click.prevent="isRegister = true; clearError()">立即注册</a>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginApi, registerApi } from '../api/auth'
import { useAuth } from '../stores/auth'

const router = useRouter()
const { login } = useAuth()

const isRegister = ref(false)
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const form = reactive({
  username: '',
  password: '',
  nickname: '',
})

function clearError() {
  error.value = ''
}

async function handleSubmit() {
  if (!form.username.trim() || !form.password.trim()) {
    error.value = '请填写用户名和密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    let res
    if (isRegister.value) {
      if (form.password.length < 6) {
        error.value = '密码至少6位'
        return
      }
      res = await registerApi({
        username: form.username.trim(),
        password: form.password,
        nickname: form.nickname.trim() || undefined,
      })
      ElMessage.success({ message: '注册成功！', duration: 2000 })
    } else {
      res = await loginApi({
        username: form.username.trim(),
        password: form.password,
      })
    }

    login(res.token, {
      userId: res.userId,
      username: res.username,
      nickname: res.nickname,
    })
    ElMessage.success({ message: isRegister.value ? '欢迎加入！' : '登录成功！', duration: 1500 })
    router.push('/')
  } catch (e) {
    error.value = e.message || '操作失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 20px;
  padding: 40px 36px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.2);
}

.card-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #6366f1, #818cf8, #a5b4fc);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.header-icon {
  display: inline-flex;
  color: #818cf8;
  margin-bottom: 12px;
  animation: pulse 3s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.8; }
}

.login-title {
  font-size: 26px;
  font-weight: 800;
  background: linear-gradient(135deg, #e0e7ff 0%, #a5b4fc 50%, #c4b5fd 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
}

/* Tabs */
.tab-bar {
  display: flex;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 28px;
}

.tab-btn {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  font-family: inherit;
  color: rgba(255, 255, 255, 0.4);
  background: transparent;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background: rgba(99, 102, 241, 0.2);
  color: #c7d2fe;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

/* Form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  z-index: 1;
  pointer-events: none;
}

.password-toggle {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  opacity: 0.6;
  transition: opacity 0.2s;
}

.password-toggle:hover { opacity: 1; }

.premium-input {
  width: 100%;
  padding: 14px 16px 14px 46px;
  border: 1.5px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  font-size: 15px;
  font-family: inherit;
  color: #e2e8f0;
  background: rgba(255, 255, 255, 0.05);
  transition: all 0.25s ease;
  outline: none;
}

.premium-input::placeholder {
  color: rgba(255, 255, 255, 0.25);
}

.premium-input:focus {
  border-color: rgba(99, 102, 241, 0.5);
  background: rgba(255, 255, 255, 0.08);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.error-msg {
  font-size: 13px;
  color: #f87171;
  background: rgba(239, 68, 68, 0.1);
  padding: 8px 12px;
  border-radius: 8px;
  text-align: center;
}

.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  font-family: inherit;
  color: #fff;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  cursor: pointer;
  transition: all 0.35s ease;
  box-shadow: 0 4px 20px rgba(99, 102, 241, 0.3);
  margin-top: 4px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(99, 102, 241, 0.4);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon { font-size: 18px; }

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.loading { pointer-events: none; }

.register-hint {
  text-align: center;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
  margin-top: 24px;
}

.register-hint a {
  color: #818cf8;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}

.register-hint a:hover {
  color: #a5b4fc;
  text-decoration: underline;
}
</style>
