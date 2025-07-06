<template>
  <div class="assistant-container">
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
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { askAssistant, AssistantRequest, AssistantResponse } from '@/api/ai'
import CourseApi from '@/api/course'
import { useAuthStore } from '@/stores/auth'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  references?: { source: string; content: string }[]
  knowledgePoints?: string[]
}

const messages = ref<ChatMessage[]>([])
const input = ref('')
const loading = ref(false)

// 新增：课程相关
const courses = ref<any[]>([])
const selectedCourseId = ref<string | number>('')

// 获取课程列表
const authStore = useAuthStore()
const fetchCourses = async () => {
  if (!authStore.user?.id) return
  try {
    const resp = await CourseApi.getUserCourses(authStore.user.id)
    courses.value = Array.isArray(resp.data) ? resp.data : []
  } catch (e) {
    courses.value = []
  }
}
onMounted(fetchCourses)

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
    const payload: AssistantRequest & { course_name?: string } = {
      question,
      chat_history: messages.value.map((m) => ({ role: m.role, content: m.content }))
    }
    // 新增：带上course_name参数
    const courseName = getSelectedCourseName()
    if (courseName) payload.course_name = courseName
    console.log('发送请求:', payload)
    const resp = await askAssistant(payload)
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
  flex-direction: column;
  height: calc(100vh - 64px); /* 头部栏高度为56px时，自动适配剩余空间 */
  padding: 16px;
  box-sizing: border-box;
}
.chat-window {
  flex: 1 1 0%; /* 填满剩余空间 */
  min-height: 0; /* 允许flex容器收缩 */
  overflow-y: auto;
  margin-bottom: 12px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e0e7ef;
  /* 始终显示滚动条 */
  scrollbar-width: thin;
  scrollbar-color: #b2dfdb #f8fafc;
}
.chat-bubble {
  max-width: 70%;
  margin: 8px auto;
  padding: 8px 12px;
  border-radius: 6px;
}
.chat-bubble.user {
  align-self: flex-end;
  background: #e0f7fa;
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
  margin-top: 8px;
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
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
  margin-bottom: 0;
  height: 44px; /* 统一高度 */
}
.course-select-bar label {
  font-weight: bold;
  color: #00838f;
  font-size: 16px;
  margin-right: 4px;
  margin-bottom: 0;
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
  margin-left: 0;
  margin-bottom: 0;
  box-shadow: 0 1px 4px 0 #e0e7ef22;
  height: 40px; /* 统一高度 */
  line-height: 20px;
  box-sizing: border-box;
  display: flex;
  align-items: center;
}
.course-select-bar select:focus {
  border-color: #00bfae;
}
.input-area {
  flex: 1;
  display: flex;
  gap: 8px;
  align-items: center;
  background: transparent;
  border: none;
  box-shadow: none;
  padding: 0;
  min-width: 0;
  height: 44px; /* 统一高度 */
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
  min-width: 0;
  height: 40px; /* 统一高度 */
  box-sizing: border-box;
  display: flex;
  align-items: center;
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
  height: 40px; /* 统一高度 */
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
/* 新增：引用资料source和content分行 */
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
/* 新增：知识点与引用资料间距 */
.knowledge-list {
  margin-top: 8px;
}
</style>
