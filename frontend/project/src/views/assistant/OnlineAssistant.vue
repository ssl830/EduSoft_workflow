<template>
  <div class="assistant-container">
    <!-- 左侧会话列表 -->
    <div class="session-sidebar">
      <div class="session-header">
        <h3>聊天记录</h3>
        <button @click="createNewSession" class="new-chat-btn">新建聊天</button>
      </div>
      <div class="session-list">
        <div 
          v-for="session in sessions" 
          :key="session.id"
          :class="['session-item', { active: session.id === currentSessionId }]"
          @click="switchSession(session.id)"
        >
          <div class="session-title">{{ session.title }}</div>
          <div class="session-preview">{{ session.lastMessage || '暂无消息' }}</div>
          <div class="session-time">{{ formatTime(session.updatedAt) }}</div>
          <button @click.stop="deleteSession(session.id)" class="delete-btn">×</button>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-area">
      <div class="chat-window">
        <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-bubble', msg.role]">
          <div class="bubble-content">
            <h2>
              {{ msg.role }}
              <!-- 修改：AI思考时颜文字为(*´･д･)?，思考完成后为(ゝ∀･) -->
              <span v-if="msg.role === 'user'">(*´･д･)?</span>
              <span v-else-if="msg.role === 'assistant'">
                <template v-if="isThinkingMsg(idx)">
                  (*´･д･)?
                </template>
                <template v-else>
                  (ゝ∀･)
                </template>
              </span>
            </h2>
            <p>{{ msg.content }}</p>
            <!-- 修改引用资料展示方式，避免黑点 -->
            <template v-if="msg.role === 'assistant' && msg.references">
              <details ref="refsDetails" @toggle="onToggle('refs', idx)">
                <summary class="summary-toggle">
                  <span class="toggle-arrow">{{ refsOpen[idx] ? '▼' : '▶' }}</span>
                  <strong>引用资料</strong>
                </summary>
                <div class="references-list">
                  <div v-for="(ref, i) in msg.references" :key="i" class="reference-item">
                    <span class="big-dot">•</span>
                    <div class="reference-lines">
                      <div class="reference-source"><strong>{{ ref.source }}</strong></div>
                      <div class="reference-content">{{ ref.content }}</div>
                    </div>
                  </div>
                </div>
              </details>
            </template>
            <!-- 新增知识点展示 -->
            <template v-if="msg.role === 'assistant' && msg.knowledgePoints && msg.knowledgePoints.length">
              <div style="margin-top: 18px;"></div>
              <details ref="kpDetails" @toggle="onToggle('kp', idx)">
                <summary class="summary-toggle">
                  <span class="toggle-arrow">{{ kpOpen[idx] ? '▼' : '▶' }}</span>
                  <strong>知识点</strong>
                </summary>
                <div class="knowledge-list">
                  <div v-for="(kp, i) in msg.knowledgePoints" :key="i" class="knowledge-item">
                    <span class="big-dot">•</span>
                    {{ kp }}
                  </div>
                </div>
              </details>
            </template>
          </div>
        </div>
      </div>

      <!-- 合并背景的输入区域 -->
      <div class="input-bar-combined">
        <div class="course-select-bar" v-if="courses.length > 0">
          <label>@课程：</label>
          <select v-model="selectedCourseId">
            <option value="">不指定课程</option>
            <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}</option>
          </select>
        </div>
        <div class="input-area">
          <input v-model="input" @keyup.enter="send" placeholder="请输入你的问题..." />
          <button @click="send" :disabled="loading || !input">发送</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { askAssistant } from '@/api/ai'
import CourseApi from '@/api/course'
import { useAuthStore } from '@/stores/auth'
import http from '@/api/axios'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  references?: { source: string; content: string }[]
  knowledgePoints?: string[]
}

interface ChatSession {
  id: number
  title: string
  courseName?: string
  updatedAt: string
  lastMessage?: string
  lastMessageRole?: string
}

const messages = ref<ChatMessage[]>([])
const input = ref('')
const loading = ref(false)

// 会话管理
const sessions = ref<ChatSession[]>([])
const currentSessionId = ref<number | null>(null)

// 新增：课程相关
const courses = ref<any[]>([])
const selectedCourseId = ref<string | number>('')

// 获取课程列表
const authStore = useAuthStore()
const fetchCourses = async () => {
  if (!authStore.user?.id) return
  try {
    const resp = await CourseApi.getUserCourses(authStore.user?.id)
    courses.value = Array.isArray(resp.data) ? resp.data : []
  } catch (e) {
    courses.value = []
  }
}

// 获取用户的聊天会话列表
const fetchSessions = async () => {
  try {
    const resp: any = await http.get('/api/chat/sessions')
    console.log('获取会话列表响应:', resp)
    if (resp.code === 200) {
      sessions.value = resp.data
    }
  } catch (e) {
    console.error('获取会话列表失败:', e)
  }
}

// 创建新会话
const createNewSession = async () => {
  try {
    const courseName = getSelectedCourseName()
    const resp: any = await http.post('/api/chat/sessions', {
      courseName,
      courseId: selectedCourseId.value || null
    })
    console.log('创建会话完整响应:', resp)
    console.log('响应的code:', resp.code)
    console.log('响应的data:', resp.data)
    
    if (resp.code === 200) {
      const sessionId = resp.data.sessionId
      console.log('获取到sessionId:', sessionId)
      currentSessionId.value = sessionId
      messages.value = []
      await fetchSessions() // 刷新会话列表
    } else {
      console.error('创建会话失败:', resp.message)
    }
  } catch (e) {
    console.error('创建会话失败:', e)
  }
}

// 切换会话
const switchSession = async (sessionId: number) => {
  try {
    currentSessionId.value = sessionId
    const resp: any = await http.get(`/api/chat/sessions/${sessionId}`)
    if (resp.code === 200) {
      const sessionData = resp.data
      messages.value = sessionData.messages || []
      
      // 更新当前课程选择
      if (sessionData.session.courseId) {
        selectedCourseId.value = sessionData.session.courseId
      }
    }
  } catch (e) {
    console.error('切换会话失败:', e)
  }
}

// 删除会话
const deleteSession = async (sessionId: number) => {
  if (!confirm('确定要删除这个会话吗？')) return
  
  try {
    await http.delete(`/api/chat/sessions/${sessionId}`)
    
    // 如果删除的是当前会话，清空消息
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
    }
    
    await fetchSessions() // 刷新会话列表
  } catch (e) {
    console.error('删除会话失败:', e)
  }
}

// 格式化时间
const formatTime = (timeStr: string) => {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  
  return date.toLocaleDateString()
}

onMounted(() => {
  fetchCourses()
  fetchSessions()
})

function getSelectedCourseName() {
  if (!selectedCourseId.value) return ''
  const course = courses.value.find(c => String(c.id) === String(selectedCourseId.value))
  return course?.name || ''
}

// 替换为自定义AI思考动画文案
const thinkingMsgFrames = [
  'AI思考中.(。-ω-)✧',
  'AI思考中..📖_(:3 」∠)_',
  'AI思考中...（　´_ゝ）旦~☕️',
  'AI思考中....📚✍️(˘ω˘ )ｽﾔｧ…',
  'AI思考中.....(๑•̀ㅂ•́)و✧',
  'AI思考中......─=≡Σ((( つ•̀ω•́)つ'
]
let thinkingInterval: number | null = null
const thinkingFrameIdx = ref(0)
let thinkingMsgIdx: number | null = null // 记录当前AI思考消息的索引

// 判断当前消息是否为AI思考中动画
function isThinkingMsg(idx: number) {
  if (thinkingMsgIdx === null) return false
  return idx === thinkingMsgIdx && loading.value
}

async function send() {
  if (!input.value.trim()) return
  
  // 如果没有当前会话，先创建一个
  if (!currentSessionId.value) {
    await createNewSession()
    if (!currentSessionId.value) {
      console.error('创建会话失败')
      return
    }
  }
  
  // 1. push user msg
  messages.value.push({ role: 'user', content: input.value })
  const question = input.value
  input.value = ''
  loading.value = true

  // 2. 插入AI思考中动画消息
  thinkingMsgIdx = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: thinkingMsgFrames[0]
  })
  thinkingFrameIdx.value = 0
  if (thinkingInterval) clearInterval(thinkingInterval)
  thinkingInterval = window.setInterval(() => {
    thinkingFrameIdx.value = (thinkingFrameIdx.value + 1) % thinkingMsgFrames.length
    if (
      typeof thinkingMsgIdx === 'number' &&
      messages.value[thinkingMsgIdx] &&
      messages.value[thinkingMsgIdx].role === 'assistant'
    ) {
      messages.value[thinkingMsgIdx].content = thinkingMsgFrames[thinkingFrameIdx.value]
    }
  }, 1000)

  try {
    const payload = {
      question,
      sessionId: currentSessionId.value,
      course_name: getSelectedCourseName(),
      courseId: selectedCourseId.value || null
    } as any
    
    console.log('发送请求:', payload)
    const resp:any = await askAssistant(payload)
    console.log('收到响应:', resp)
    
    if (thinkingInterval) {
      clearInterval(thinkingInterval)
      thinkingInterval = null
    }
    loading.value = false
    
    // 替换AI思考中为真实内容
    if (resp.code == 200) {
      messages.value[thinkingMsgIdx] = {
        role: 'assistant',
        content: resp.data.answer,
        references: resp.data.references,
        knowledgePoints: resp.data.knowledgePoints
      }
      
      // 更新当前会话ID（如果从响应中返回了新的sessionId）
      if (resp.sessionId) {
        currentSessionId.value = resp.sessionId
      }
      
      // 刷新会话列表
      await fetchSessions()
    } else {
      messages.value[thinkingMsgIdx] = {
        role: 'assistant',
        content: '抱歉，无法处理您的请求。'
      }
    }
    thinkingMsgIdx = null
  } catch (err) {
    if (thinkingInterval) {
      clearInterval(thinkingInterval)
      thinkingInterval = null
    }
    loading.value = false
    if (typeof thinkingMsgIdx === 'number') {
      messages.value[thinkingMsgIdx] = { role: 'assistant', content: '抱歉，服务暂时不可用。' }
    }
    thinkingMsgIdx = null
  }
}

// 控制折叠/展开状态
const refsOpen = reactive<{ [k: number]: boolean }>({})
const kpOpen = reactive<{ [k: number]: boolean }>({})

function onToggle(type: 'refs' | 'kp', idx: number) {
  if (type === 'refs') {
    refsOpen[idx] = !refsOpen[idx]
  } else {
    kpOpen[idx] = !kpOpen[idx]
  }
}
</script>

<style scoped>
.assistant-container {
  display: flex;
  height: calc(100vh - 64px);
  padding: 16px;
  box-sizing: border-box;
  gap: 16px;
}

/* 左侧会话列表 */
.session-sidebar {
  width: 300px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e0e7ef;
  overflow: hidden;
}

.session-header {
  padding: 16px;
  border-bottom: 1px solid #e0e7ef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.session-header h3 {
  margin: 0;
  color: #00838f;
  font-size: 18px;
}

.new-chat-btn {
  padding: 8px 16px;
  background: linear-gradient(90deg, #26c6da 0%, #00bfae 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  height: auto;
}

.new-chat-btn:hover {
  background: linear-gradient(90deg, #00bfae 0%, #26c6da 100%);
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  padding: 12px;
  margin-bottom: 8px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
  position: relative;
}

.session-item:hover {
  background: #f0f9ff;
  border-color: #26c6da;
}

.session-item.active {
  background: #e0f7fa;
  border-color: #00bfae;
}

.session-title {
  font-weight: bold;
  color: #00838f;
  margin-bottom: 4px;
  font-size: 14px;
}

.session-preview {
  color: #666;
  font-size: 12px;
  margin-bottom: 4px;
  line-height: 1.4;
  max-height: 2.8em;
  overflow: hidden;
}

.session-time {
  color: #999;
  font-size: 11px;
}

.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 20px;
  height: 20px;
  border: none;
  background: #ff5757;
  color: white;
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
}

.session-item:hover .delete-btn {
  opacity: 1;
}

/* 右侧聊天区域 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-window {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e0e7ef;
  padding: 16px;
  scrollbar-width: thin;
  scrollbar-color: #b2dfdb #f8fafc;
}

.chat-bubble {
  max-width: 70%;
  margin: 8px 0;
  padding: 8px 12px;
  border-radius: 6px;
}

.chat-bubble.user {
  align-self: flex-end;
  background: #e0f7fa;
  margin-left: auto;
}

.chat-bubble.assistant {
  align-self: flex-start;
  background: #f1f8e9;
}

/* 合并背景的输入区域样式 */
.input-bar-combined {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #f8fafc;
  border-radius: 14px;
  box-shadow: 0 2px 8px 0 #e0e7ef33;
  padding: 12px 18px;
  border: 1.5px solid #e0f7fa;
}

.course-select-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  min-width: 220px;
  flex-shrink: 0;
}

.course-select-bar label {
  font-weight: bold;
  color: #00838f;
  font-size: 16px;
  margin-right: 4px;
}

.course-select-bar select {
  padding: 10px 16px;
  border-radius: 8px;
  border: 1.5px solid #26c6da;
  background: #fff;
  font-size: 15px;
  color: #00838f;
  outline: none;
  transition: border 0.2s;
  box-shadow: 0 1px 4px 0 #e0e7ef22;
  height: 40px;
  box-sizing: border-box;
}

.course-select-bar select:focus {
  border-color: #00bfae;
}

.input-area {
  flex: 1;
  display: flex;
  gap: 8px;
  align-items: center;
}

input {
  flex: 1;
  padding: 10px 14px;
  border: 1.5px solid #26c6da;
  border-radius: 8px;
  outline: none;
  font-size: 16px;
  background: #fff;
  box-shadow: 0 1px 4px 0 #e0e7ef22;
  transition: border 0.2s;
  height: 40px;
  box-sizing: border-box;
}

input:focus {
  border-color: #00bfae;
}

button {
  padding: 10px 22px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(90deg, #26c6da 0%, #00bfae 100%);
  color: #fff;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 2px 8px 0 #26c6da22;
  transition: background 0.2s, box-shadow 0.2s;
  height: 40px;
  display: flex;
  align-items: center;
}

button:disabled {
  background: #b2dfdb;
  cursor: not-allowed;
  color: #fff;
}

button:not(:disabled):hover {
  background: linear-gradient(90deg, #00bfae 0%, #26c6da 100%);
  box-shadow: 0 4px 12px 0 #26c6da33;
}

.references-list, .knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin: 8px 0 0 0;
}

.reference-item, .knowledge-item {
  padding-left: 4px;
  display: flex;
  align-items: flex-start;
}

.big-dot {
  font-size: 1.5em;
  line-height: 1;
  margin-right: 8px;
  color: #26c6da;
  font-weight: bold;
  display: inline-block;
  width: 1em;
}

.reference-lines {
  display: flex;
  flex-direction: column;
  line-height: 1.6;
}

.reference-source {
  color: #00838f;
  font-weight: bold;
  margin-bottom: 2px;
}

.reference-content {
  color: #333;
  word-break: break-all;
}

.summary-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  user-select: none;
  font-size: 1em;
}

.toggle-arrow {
  font-size: 1.1em;
  width: 1.2em;
  display: inline-block;
  color: #26c6da;
  font-weight: bold;
}

.knowledge-list {
  margin-top: 8px;
}
</style>
