<template>
  <div class="assistant-container">
    <div class="chat-window">
      <div v-for="(msg, idx) in messages" :key="idx" :class="['chat-bubble', msg.role]">
        <div class="bubble-content">
          <p>{{ msg.content }}</p>
          <template v-if="msg.role === 'assistant' && msg.references">
            <details>
              <summary>引用资料</summary>
              <ul>
                <li v-for="(ref, i) in msg.references" :key="i">
                  <strong>{{ ref.source }}:</strong> {{ ref.content }}
                </li>
              </ul>
            </details>
          </template>
        </div>
      </div>
    </div>

    <div class="input-area">
      <input v-model="input" @keyup.enter="send" placeholder="请输入你的问题..." />
      <button @click="send" :disabled="loading || !input">发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { askAssistant, AssistantRequest, AssistantResponse } from '@/api/ai'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  references?: { source: string; content: string }[]
  knowledgePoints?: string[]
}

const messages = ref<ChatMessage[]>([])
const input = ref('')
const loading = ref(false)

async function send() {
  if (!input.value.trim()) return
  // 1. push user msg
  messages.value.push({ role: 'user', content: input.value })
  const question = input.value
  input.value = ''
  loading.value = true

  try {
    const payload: AssistantRequest = {
      question,
      chatHistory: messages.value.map((m) => ({ role: m.role, content: m.content }))
    }
    const resp = await askAssistant(payload)
    const data = resp.data as AssistantResponse
    messages.value.push({
      role: 'assistant',
      content: data.answer,
      references: data.references,
      knowledgePoints: data.knowledgePoints
    })
  } catch (err) {
    console.error(err)
    messages.value.push({ role: 'assistant', content: '抱歉，服务暂时不可用。' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.assistant-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
}
.chat-window {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 12px;
}
.chat-bubble {
  max-width: 80%;
  margin: 8px 0;
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
.input-area {
  display: flex;
  gap: 8px;
}
input {
  flex: 1;
  padding: 8px;
}
button {
  padding: 8px 16px;
}
</style> 