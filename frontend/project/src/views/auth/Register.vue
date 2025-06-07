<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const username = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const role = ref('student')
const userId = ref('')
const loading = ref(false)
const error = ref('')

// 添加验证状态
const userIdError = computed(() => {
  if (!userId.value) return ''
  if (userId.value.length < 3 || userId.value.length > 15) {
    return '用户ID长度必须在3-15个字符之间'
  }
  return ''
})

const usernameError = computed(() => {
  if (!username.value) return ''
  if (username.value.length < 2 || username.value.length > 50) {
    return '用户名长度必须在2-50个字符之间'
  }
  return ''
})

const passwordError = computed(() => {
  if (!password.value) return ''
  if (password.value.length < 6) {
    return '密码长度不能少于6个字符'
  }
  return ''
})

const emailError = computed(() => {
  if (!email.value) return ''
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email.value)) {
    return '请输入有效的电子邮箱'
  }
  if (email.value.length > 100) {
    return '邮箱长度不能超过100个字符'
  }
  return ''
})

const confirmPasswordError = computed(() => {
  if (!confirmPassword.value) return ''
  if (password.value !== confirmPassword.value) {
    return '两次输入的密码不一致'
  }
  return ''
})

const isFormValid = computed(() => {
  return !userIdError.value && 
         !usernameError.value && 
         !passwordError.value && 
         !emailError.value && 
         !confirmPasswordError.value &&
         userId.value &&
         username.value &&
         password.value &&
         email.value &&
         confirmPassword.value
})

const handleRegister = async () => {
  if (!isFormValid.value) {
    error.value = '请检查所有字段的输入是否正确'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await authStore.register({
      username: username.value,
      email: email.value,
      password: password.value,
      role: role.value,
      userId: userId.value
    })
    router.push('/')
  } catch (err: any) {
    error.value = err.message || '注册失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h1 class="auth-title">注册账号</h1>

      <div v-if="error" class="error-message">{{ error }}</div>

      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label for="userId">用户ID</label>
          <input
              id="userId"
              v-model="userId"
              type="text"
              placeholder="请输入用户ID（3-15个字符）"
              :disabled="loading"
              required
          />
          <div v-if="userIdError" class="field-error">{{ userIdError }}</div>
        </div>

        <div class="form-group">
          <label for="username">用户名</label>
          <input
              id="username"
              v-model="username"
              type="text"
              placeholder="请输入用户名（2-50个字符）"
              :disabled="loading"
              required
          />
          <div v-if="usernameError" class="field-error">{{ usernameError }}</div>
        </div>

        <div class="form-group">
          <label for="email">电子邮箱</label>
          <input
              id="email"
              v-model="email"
              type="email"
              placeholder="请输入电子邮箱"
              :disabled="loading"
              required
          />
          <div v-if="emailError" class="field-error">{{ emailError }}</div>
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
              id="password"
              v-model="password"
              type="password"
              placeholder="请输入密码（至少6个字符）"
              :disabled="loading"
              required
              autocomplete="new-password"
          />
          <div v-if="passwordError" class="field-error">{{ passwordError }}</div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <input
              id="confirmPassword"
              v-model="confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :disabled="loading"
              required
              autocomplete="new-password"
          />
          <div v-if="confirmPasswordError" class="field-error">{{ confirmPasswordError }}</div>
        </div>

        <div class="form-group">
          <label for="role">角色</label>
          <select
              id="role"
              v-model="role"
              :disabled="loading"
          >
            <option value="student">学生</option>
            <option value="teacher">教师</option>
            <option value="tutor">助教</option>
          </select>
        </div>

        <button
            type="submit"
            class="btn-primary btn-full"
            :disabled="loading || !isFormValid"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="auth-actions">
        <p>
          已有账号？
          <router-link to="/login">立即登录</router-link>
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
  min-height: calc(100vh - 64px);
  padding: 2rem;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border-radius: 8px;
  background-color: white;
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

.form-group input,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus {
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

.field-error {
  color: #c62828;
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.form-group input:invalid,
.form-group input.error {
  border-color: #c62828;
}

.form-group input:focus:invalid {
  border-color: #c62828;
  box-shadow: 0 0 0 2px rgba(198, 40, 40, 0.1);
}
</style>
