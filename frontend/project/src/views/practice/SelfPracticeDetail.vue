<template>
  <div class="detail-page">
    <h1>自测详情</h1>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div v-if="items.length" class="question-list">
        <div v-for="(q, idx) in items" :key="q.questionId" class="question-item">
          <h3>{{ idx + 1 }}. {{ q.question }}</h3>
          <ul v-if="q.options && q.options.length">
            <li v-for="(opt, oidx) in q.options" :key="oidx">{{ opt }}</li>
          </ul>
          <p>学生答案：<span :class="q.correct ? 'correct' : 'wrong'">{{ q.studentAnswer }}</span></p>
          <p>正确答案：{{ q.correctAnswer }}</p>
          <p>得分：{{ q.score }}</p>
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
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}
.question-item {
  border: 1px solid var(--border-color);
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 4px;
}
.correct {
  color: green;
}
.wrong {
  color: red;
}
.loading, .empty {
  text-align: center;
}
</style> 