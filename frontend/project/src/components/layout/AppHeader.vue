<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import ThemeToggle from '../littlecomponents/ThemeToggle.vue'

const router = useRouter()
const authStore = useAuthStore()

const showHelpModal = ref(false)
const showFeedbackModal = ref(false)

const isLoggedIn = computed(() => authStore.isAuthenticated)
const username = computed(() => authStore.user?.username || '')

// 时钟相关
const currentTime = ref(new Date())
let timeInterval: NodeJS.Timeout | null = null

const formatTime = computed(() => {
  return currentTime.value.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
})

const formatDate = computed(() => {
  return currentTime.value.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
})

onMounted(() => {
  // 每秒更新时间
  timeInterval = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})

const handleLogout = async () => {
  await authStore.logout()
  router.push('/')
}
</script>
<script lang="ts">
export default {
  name: 'AppHeader' // 显式设置组件名
}
</script>
<template>
  <header class="app-header">
    <div class="header-container">
      <div class="logo">
        <router-link to="/">
          <h1 >EduSoft</h1>
        </router-link>
      </div>

      <nav class="main-nav">
        <router-link to="/" class="nav-link">首页</router-link>
        <router-link to="/questionBank" class="nav-link" v-if="isLoggedIn && authStore.user?.role !== 'student'">题库</router-link>
        <button class="nav-link" @click="showHelpModal = true">帮助中心</button>
        <button class="nav-link" @click="showFeedbackModal = true">意见反馈</button>
      </nav>      <div class="auth-section">
        <div class="clock-display">
          <div class="time">{{ formatTime }}</div>
          <div class="date">{{ formatDate }}</div>
        </div>
        <ThemeToggle />
        <template v-if="isLoggedIn">
          <span class="user-greeting">你好，{{ username }}</span>
          <button class="logout-btn" @click="handleLogout">登出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-auth login">登录</router-link>
          <router-link to="/register" class="btn-auth register">注册</router-link>
        </template>
      </div>
    </div>

    <!-- 帮助中心弹窗 -->
    <div v-if="showHelpModal" class="modal-overlay" @click="showHelpModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>帮助中心</h2>
          <button class="close-btn" @click="showHelpModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <h3>欢迎使用EduSoft课程平台！</h3>
          <div class="help-section">
            <h4>📚 课程学习</h4>
            <ul>
              <li>浏览课程：在首页可以查看当前加入的课程</li>
              <li>课程详情：点击课程可以查看详细的课程信息、教学大纲和学习目标</li>
              <li>学习进度：系统会记录您的学习进度，方便您随时继续学习</li>
              <li>课程资料：每门课程都提供丰富的学习资料，包括课件、视频和文档</li>
              <li>讨论区：对课程内容进行交流探讨</li>
            </ul>
          </div>

          <div class="help-section">
            <h4>📝 题库练习</h4>
            <ul>
              <li>题目分类：题库按课程和知识点进行分类，方便针对性练习</li>
              <li>练习模式：支持练习模式，完成老师设置的练习</li>
              <li>错题本：记录错题，方便复习和巩固</li>
              <li>答题解析：每道题目都配有详细的解析和知识点说明</li>
            </ul>
          </div>

          <div class="help-section">
            <h4>📊 学习记录</h4>
            <ul>
              <li>练习成绩：展示您的练习成绩和分数排名</li>
              <li>学习轨迹：查看您的学习历史记录</li>
            </ul>
          </div>

          <div class="help-section">
            <h4>👥 个人中心</h4>
            <ul>
              <li>个人信息：管理您的个人资料</li>
              <li>消息通知：接收课程更新和学习提醒</li>
            </ul>
          </div>

          <div class="help-section">
            <h4>💡 使用提示</h4>
            <ul>
              <li>定期查看学习记录，了解自己的学习进度和效果</li>
              <li>多利用题库练习功能，巩固所学知识</li>
              <li>及时关注课程更新和系统通知</li>
              <li>遇到问题可以随时通过意见反馈联系我们</li>
              <li>建议制定合理的学习计划，循序渐进地学习</li>
            </ul>
          </div>

          <div class="help-section">
            <h4>🔍 常见问题</h4>
            <ul>
              <li>如何修改个人信息？</li>
              <li>答：在个人中心页面可以修改您的个人信息</li>
              <li>如何查看学习进度？</li>
              <li>答：在学习记录页面可以查看详细的学习进度</li>
              <li>如何获取课程资料？</li>
              <li>答：在课程详情页面可以下载相关学习资料</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 意见反馈弹窗 -->
    <div v-if="showFeedbackModal" class="modal-overlay" @click="showFeedbackModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>意见反馈</h2>
          <button class="close-btn" @click="showFeedbackModal = false">&times;</button>
        </div>
        <div class="modal-body">
          <p>感谢您使用EduSoft课程平台！</p>
          <p>如果您有任何建议或遇到问题，欢迎通过以下方式联系我们：</p>
          <div class="feedback-email">
            <p>📧 邮箱地址：</p>
            <p class="email">support@edusoft.com</p>
          </div>
          <p>我们会认真对待每一条反馈，并尽快回复您。</p>
          <p>感谢您的支持与理解！</p>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background: url('@/assets/sky.png') center/cover;
  background-color: #2c6ecf; /* 备用颜色 */
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
}

.app-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(44, 110, 207, 0.3);
  z-index: 1;
}

.header-container {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1280px;
  margin: 0 auto;
  padding: 1rem;
  height: 64px;
}

.logo {
  font-size: 0.875rem;
}

.logo a {
  color: white;
  text-decoration: none;
}

.logo h1 {
  margin: 0;
  font-size: 2.25rem;
  font-family: "Pinyon Script", cursive;
  font-weight: 800;
  font-style: normal;
}

.main-nav {
  display: flex;
  gap: 1.5rem;
}

.nav-link {
  color: rgba(255, 255, 255, 0.9);
  text-decoration: none;
  font-weight: 500;
  padding: 0.5rem;
  border-radius: 4px;
  transition: all 0.2s;
}

.nav-link.router-link-active {
  color: white;
  background-color: rgba(255, 255, 255, 0.15);
}

.nav-link:hover {
  color: white;
  background-color: rgba(255, 255, 255, 0.1);
}

.auth-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-greeting {
  margin-right: 0.5rem;
  font-weight: 500;
}

.btn-auth {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
}

.login {
  color: white;
  background-color: transparent;
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.login:hover {
  background-color: rgba(255, 255, 255, 0.1);
  border-color: white;
}

.register {
  color: #2c6ecf;
  background-color: white;
}

.register:hover {
  background-color: rgba(255, 255, 255, 0.9);
}

.logout-btn {
  padding: 0.5rem 1rem;
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.clock-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 1rem;
  margin-right: 1rem;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.5rem;
  border-radius: 8px;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.time {
  font-weight: 600;
  font-size: 1rem;
  letter-spacing: 1px;
}

.date {
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.75rem;
  margin-top: 2px;
}

.clock {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-size: 0.875rem;
}

.current-time {
  font-weight: 500;
}

.current-date {
  color: rgba(255, 255, 255, 0.7);
}

@media (max-width: 768px) {
  .header-container {
    flex-wrap: wrap;
    height: auto;
    padding: 0.75rem;
  }

  .logo {
    flex: 1;
  }

  .main-nav {
    order: 3;
    width: 100%;
    margin-top: 0.75rem;
    justify-content: space-around;
  }

  .auth-section {
    flex: 0 0 auto;
  }
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  margin: 0;
  color: #2c6ecf;
  font-size: 1.5rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #666;
  cursor: pointer;
  padding: 0.5rem;
  line-height: 1;
}

.modal-body {
  padding: 1.5rem;
  color: #333;
}

.help-section {
  margin-bottom: 1.5rem;
}

.help-section h4 {
  color: #2c6ecf;
  margin-bottom: 0.75rem;
}

.help-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
  color: #333;
}

.help-section li {
  margin-bottom: 0.5rem;
  padding-left: 1.5rem;
  position: relative;
  color: #333;
}

.modal-body h3 {
  color: #2c6ecf;
  margin-bottom: 1.5rem;
}

.feedback-email {
  background-color: #f8f9fa;
  padding: 1rem;
  border-radius: 4px;
  margin: 1rem 0;
}

.email {
  color: #2c6ecf;
  font-weight: 500;
  margin: 0.5rem 0;
}

/* 修改导航链接样式 */
.nav-link {
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
}

@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
}
</style>
