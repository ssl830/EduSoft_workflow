<template>
  <div class="history-page">
    <h1>自测历史</h1>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <table class="history-table" v-if="records.length">
        <thead>
          <tr>
            <th>#</th>
            <th>标题</th>
            <th>提交时间</th>
            <th>得分</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(rec, idx) in records" :key="rec.practiceId">
            <td>{{ idx + 1 }}</td>
            <td>{{ rec.title }}</td>
            <td>{{ formatDate(rec.submittedAt) }}</td>
            <td>{{ rec.score }}</td>
            <td>
              <router-link :to="`/selfpractice/history/${rec.practiceId}`">查看详情</router-link>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty">暂无历史记录</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSelfPracticeHistory } from '@/api/ai'

interface HistoryRecord {
  practiceId: number
  title: string
  submittedAt: string
  score: number
}

const records = ref<HistoryRecord[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const res = await getSelfPracticeHistory() as any
    records.value = res?.data || []
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
})

function formatDate(str: string) {
  return new Date(str).toLocaleString()
}
</script>

<style scoped>
.history-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}
.loading {
  text-align: center;
}
.history-page h1 {
  text-align: center;
  font-weight: 600;
  margin-bottom: 1.5rem;
}
.history-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  overflow: hidden;
}
.history-table thead {
  background: var(--primary, #1976d2);
  color: #fff;
}
.history-table th {
  padding: 0.75rem 1rem;
  font-weight: 500;
}
.history-table td {
  padding: 0.75rem 1rem;
}
.history-table tbody tr:nth-child(even) {
  background: var(--bg-secondary, #fafafa);
}
.history-table tbody tr:hover {
  background: rgba(25, 118, 210, 0.08);
  transition: background 0.2s ease;
}
.router-link,
a {
  color: var(--primary, #1976d2);
  text-decoration: none;
}
.router-link:hover,
a:hover {
  text-decoration: underline;
}
.empty {
  margin-top: 2rem;
  font-style: italic;
  font-size: 1.05rem;
}
.history-table th,
.history-table td {
  text-align: center;
}
</style> 