<script setup lang="ts">
import { ref, computed, watch } from 'vue'
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

// 自定义弹窗相关
const showKeyModal = ref(false)
const adminKeyInput = ref('')
const keyError = ref('')
const keySuccess = ref(false) // 新增：验证通过提示

// 监听role变化，管理员需验证密钥
watch(role, (newVal, oldVal) => {
  if (newVal === 'tutor') {
    adminKeyInput.value = ''
    keyError.value = ''
    showKeyModal.value = true
  }
})

// 弹窗确认
const handleKeyConfirm = () => {
  if (adminKeyInput.value === 'GuanLiYuanMiYao') {
    showKeyModal.value = false
    keyError.value = ''
    keySuccess.value = true
    setTimeout(() => {
      keySuccess.value = false
    }, 2000)
  } else {
    keyError.value = '请不要乱注册哦(╯°□°）╯︵ ┻━┻'
    role.value = 'student'
    showKeyModal.value = false
  }
}

// 弹窗取消
const handleKeyCancel = () => {
  showKeyModal.value = false
  role.value = 'student'
  keyError.value = ''
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
            <option value="tutor">管理员</option>
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

  <!-- 管理员密钥弹窗 -->
  <div v-if="showKeyModal" class="modal-mask">
    <div class="modal-container">
      <h3>请验证身份</h3>
      <p>输入管理员密钥</p>
      <input
        v-model="adminKeyInput"
        type="password"
        placeholder="管理员密钥"
        @keyup.enter="handleKeyConfirm"
        autofocus
      />
      <div class="modal-actions">
        <button @click="handleKeyConfirm" class="btn-primary">确定</button>
        <button @click="handleKeyCancel" class="btn-secondary">取消</button>
      </div>
    </div>
  </div>
  <div v-if="keyError" class="modal-error">{{ keyError }}</div>
  <div v-if="keySuccess" class="modal-success">验证通过</div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 64px);
  padding: 2rem;
  background-image: url('../../assets/login2.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
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

.modal-mask {
  position: fixed;
  z-index: 9999;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-container {
  background: #fff;
  padding: 2rem 2rem 1.5rem 2rem;
  border-radius: 10px;
  box-shadow: 0 4px 32px rgba(0,0,0,0.18);
  min-width: 300px;
  max-width: 90vw;
  text-align: center;
}

.modal-container h3 {
  margin-bottom: 0.5rem;
  color: #2c6ecf;
}

.modal-container input {
  width: 80%;
  margin: 1rem 0;
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.btn-secondary {
  background: #eee;
  color: #333;
  border: none;
  padding: 0.5rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #ddd;
}

.modal-error {
  position: fixed;
  top: 20%;
  left: 50%;
  transform: translate(-50%, 0);
  background: #ffebee;
  color: #c62828;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  z-index: 10000;
  font-size: 1.1rem;
  text-align: center;
  min-width: 220px;
  box-shadow: 0 2px 12px rgba(198,40,40,0.12);
  animation: fadeOut 2.5s forwards;
}

.modal-success {
  position: fixed;
  top: 20%;
  left: 50%;
  transform: translate(-50%, 0);
  background: #e8f5e9;
  color: #388e3c;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  z-index: 10000;
  font-size: 1.1rem;
  text-align: center;
  min-width: 220px;
  box-shadow: 0 2px 12px rgba(56,142,60,0.12);
  animation: fadeOut 2s forwards;
}

@keyframes fadeOut {
  0% { opacity: 1; }
  80% { opacity: 1; }
  100% { opacity: 0; }
}
</style>
