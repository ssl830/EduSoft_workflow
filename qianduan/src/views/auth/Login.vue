<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const userId = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const loginSuccess = ref(false)
const successMessage = ref('')

const handleLogin = async () => {
  if (!userId.value || !password.value) {
    error.value = '请输入用户ID和密码'
    return
  }

  loading.value = true
  error.value = ''
    try {
    await authStore.login(userId.value, password.value)
    
    // 显示成功消息
    loginSuccess.value = true
    successMessage.value = '登录成功'
    
    // 短暂延迟后跳转，以便用户看到成功消息
    setTimeout(() => {
      const redirectPath = route.query.redirect as string || '/'
      router.push(redirectPath)
    }, 1000)
  } catch (err: any) {
    error.value = err.message || '登录失败，请检查用户ID和密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">      <h1 class="auth-title">登录</h1>
      
      <div v-if="error" class="error-message">{{ error }}</div>
      <div v-if="loginSuccess" class="success-message">{{ successMessage }}</div>
      
      <form @submit.prevent="handleLogin" class="auth-form">        <div class="form-group">
          <label for="userId">用户ID</label>
          <input 
            id="userId"
            v-model="userId"
            type="text"
            placeholder="请输入用户ID"
            :disabled="loading"
            required
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            id="password"
            v-model="password"
            type="password"
            placeholder="请输入密码"
            :disabled="loading"
            required
          />
        </div>
        
        <button 
          type="submit" 
          class="btn-primary btn-full" 
          :disabled="loading"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>
      
      <div class="auth-actions">
        <p>
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-image: url('src/assets/login2.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  width: 100%;
  position: absolute; /* Position absolute to break out of the parent container layout */
  top: 0;
  left: 0;
  margin: 0;
  padding: 0;
  z-index: 1; /* Ensure it shows above other content */
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border-radius: 8px;
  
  background-color:aliceblue;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.auth-title {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #2c6ecf;
}

.auth-form {
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus {
  border-color: #2c6ecf;
  outline: none;
}

.btn-full {
  width: 100%;
  padding: 0.75rem;
}

.auth-actions {
  text-align: center;
  margin-top: 1rem;
}

.auth-actions a {
  color: #2c6ecf;
  text-decoration: none;
}

.auth-actions a:hover {
  text-decoration: underline;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1.25rem;
  text-align: center;
}

.success-message {
  background-color: #e8f5e9;
  color: #2e7d32;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1.25rem;
  text-align: center;
}
</style>