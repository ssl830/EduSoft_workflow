<template>
  <div class="detail-page">
    <h1 class="title">自测详情</h1>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div v-if="items.length" class="question-list">
        <div v-for="(q, idx) in items" :key="q.questionId" class="question-item">
          <h3 class="question-title">
            <span class="question-index">{{ idx + 1 }}.</span>
            <span class="question-text">{{ q.question }}</span>
          </h3>
          <ul
            v-if="q.options && q.options.length && q.options[0]"
            class="option-list"
          >
            <li v-for="(opt, oidx) in q.options" :key="oidx" class="option-item">
              <span class="option-label">{{ String.fromCharCode(65 + oidx) }}.</span>
              <span class="option-text">{{ opt }}</span>
            </li>
          </ul>
          <div class="answer-row">
            <span class="answer-label">学生答案：</span>
            <span :class="q.correct ? 'correct' : 'wrong'">{{ q.studentAnswer }}</span>
          </div>
          <div class="answer-row">
            <span class="answer-label">正确答案：</span>
            <span class="correct-answer">{{ q.correctAnswer }}</span>
          </div>
          <div class="answer-row">
            <span class="answer-label">得分：</span>
            <span class="score">{{ q.score }}</span>
          </div>
        </div>
      </div>
      <p v-else class="empty">暂无数据</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getSelfPracticeDetail } from '@/api/ai'

const route = useRoute()
const pid = route.params.pid as string

const items = ref<any[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const res = await getSelfPracticeDetail(pid) as any
    items.value = res?.data || []
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.detail-page {
  max-width: 820px;
  margin: 0 auto;
  padding: 2.5rem 1.2rem;
  background: #fafbfc;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(60,60,60,0.06);
  font-size: 1.08rem;
}
.title {
  font-size: 2.1rem;
  font-weight: 700;
  text-align: center;
  margin-bottom: 2.2rem;
  letter-spacing: 2px;
  color: #2d3a4b;
}
.question-list {
  margin-top: 0.5rem;
}
.question-item {
  border: 1.5px solid #e3e8ee;
  background: #fff;
  padding: 1.3rem 1.2rem 1.1rem 1.2rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 1px 6px 0 rgba(60,60,60,0.04);
  transition: box-shadow 0.2s;
}
.question-item:hover {
  box-shadow: 0 4px 16px 0 rgba(60,60,60,0.10);
}
.question-title {
  font-size: 1.18rem;
  font-weight: 600;
  margin-bottom: 0.7rem;
  color: #1a2233;
  display: flex;
  align-items: flex-start;
}
.question-index {
  margin-right: 0.5em;
  color: #1976d2;
  font-weight: 700;
}
.question-text {
  flex: 1;
  word-break: break-word;
}
.option-list {
  list-style: none;
  padding-left: 0;
  margin-bottom: 1.1rem;
}
.option-item {
  display: flex;
  align-items: center;
  font-size: 1.04rem;
  margin-bottom: 0.25rem;
  padding-left: 0.2em;
}
.option-label {
  font-weight: 600;
  color: #1976d2;
  margin-right: 0.5em;
  min-width: 1.8em;
  display: inline-block;
}
.option-text {
  flex: 1;
  color: #374151;
}
.answer-row {
  margin-bottom: 0.3rem;
  font-size: 1.03rem;
  display: flex;
  align-items: center;
}
.answer-label {
  color: #6b7280;
  min-width: 5.2em;
  font-weight: 500;
}
.correct {
  color: #219653;
  font-weight: 600;
}
.wrong {
  color: #e53935;
  font-weight: 600;
}
.correct-answer {
  color: #1976d2;
  font-weight: 600;
}
.score {
  color: #f59e42;
  font-weight: 700;
}
.loading, .empty {
  text-align: center;
  font-size: 1.15rem;
  color: #888;
  margin-top: 2.5rem;
  letter-spacing: 1px;
}
</style>
