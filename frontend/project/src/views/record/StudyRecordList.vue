<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { StudyRecord } from '../../api/studyRecords'
import axios from '../../api/axios'

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

const records = ref<StudyRecord[]>([])
const loading = ref(true)
const error = ref('')
const selectedCourse = ref('')

const fetchRecords = async () => {
  loading.value = true;
  error.value = '';
  try {
    const response = await axios.get<ApiResponse<StudyRecord[]>>('/api/record/study');
    const result = response as unknown as ApiResponse<StudyRecord[]>;
    if (result.code === 200) {
      // 兼容后端返回数组的情况
      records.value = Array.isArray(result.data) ? result.data : [result.data];
    } else {
      error.value = result.message || '获取学习记录失败';
    }
  } catch (err) {
    error.value = '获取学习记录失败，请稍后再试';
    console.error('获取学习记录错误:', err);
  } finally {
    loading.value = false;
  }
}

// 格式化时间
const formatDateTime = (dateTimeStr: string) => {
  if (!dateTimeStr) return '-'
  const date = new Date(dateTimeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 从记录中提取所有不重复的课程
const courseOptions = computed(() => {
  const courses = new Set(records.value.map(record => record.courseName))
  return Array.from(courses)
})

// 根据选中的课程筛选记录
const filteredRecords = computed(() => {
  if (!selectedCourse.value) return records.value
  return records.value.filter(record => record.courseName === selectedCourse.value)
})

onMounted(() => {
  fetchRecords()
})
</script>

<template>
  <div class="study-record-container">    <header class="page-header">
      <h1>学习记录</h1>
      <div class="filter-section" v-if="courseOptions.length > 0">
        <select v-model="selectedCourse" class="course-filter">
          <option value="">全部课程</option>
          <option v-for="course in courseOptions" :key="course" :value="course">
            {{ course }}
          </option>
        </select>
      </div>
    </header>

    <div v-if="loading" class="loading-container">
      加载中...
    </div>
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    <div v-else-if="filteredRecords.length === 0" class="empty-state">
      暂无学习记录
    </div>
    <div v-else class="records-list">
      <div v-for="record in filteredRecords" :key="record.id" class="record-card">
        <div class="record-header">
          <h3>{{ record.courseName }}</h3>
          <span class="completion-status"
            :class="{
              completed: Number(record.progress) >= 100,
              'in-progress': Number(record.progress) > 0 && Number(record.progress) < 100,
              'not-started': Number(record.progress) === 0
            }"
          >
            {{ record.formattedProgress }}
          </span>
        </div>
        <div class="record-content">
          <p class="section-title">{{ record.sectionTitle }}</p>
          <p class="resource-title">资源：{{ record.resourceTitle }}</p>          <div class="progress-bar-container">
            <div class="progress-bar" :style="{ width: `${record.progress}%` }"></div>
          </div>
          <div class="watch-info">
            <span class="watch-count">观看次数：{{ record.watchCount }}</span>
            <span v-if="record.lastWatchTime" class="last-watch">
              最后观看：{{ formatDateTime(record.lastWatchTime) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.study-record-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h1 {
  color: #333;
  font-size: 2rem;
  margin: 0;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 3rem;
  color: #666;
}

.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 8px;
  margin: 1rem 0;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #666;
  background: #f5f5f5;
  border-radius: 8px;
}

.records-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.record-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.record-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.record-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.25rem;
}

.completion-status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
  background-color: #fff3e0;
  color: #f57c00;
}
.completion-status.completed {
  background-color: #e8f5e9;
  color: #2e7d32;
}
.completion-status.in-progress {
  background-color: #fffde7;
  color: #fbc02d;
}
.completion-status.not-started {
  background-color: #eeeeee;
  color: #888;
}

.record-content {
  color: #666;
}

.section-title {
  margin: 0 0 0.5rem;
  font-weight: 500;
}
.resource-title {
  margin: 0 0 0.5rem;
  font-size: 0.95rem;
  color: #555;
}
.progress-bar-container {
  background: #f0f0f0;
  border-radius: 6px;
  height: 8px;
  margin: 0.5rem 0 1rem;
  width: 100%;
}
.progress-bar {
  height: 100%;
  border-radius: 6px;
  background: linear-gradient(90deg, #42b983, #2e7d32);
  transition: width 0.4s;
}
.watch-info {
  margin: 0.5rem 0 0;
  font-size: 0.875rem;
  color: #888;
}
.watch-count {
  margin-right: 1.5rem;
}
.last-watch {
  color: #888;
}

.filter-section {
  display: flex;
  align-items: center;
}

.course-filter {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.95rem;
  color: #333;
  background-color: white;
  min-width: 200px;
}

.course-filter:focus {
  border-color: #42b983;
  outline: none;
  box-shadow: 0 0 0 2px rgba(66, 185, 131, 0.1);
}

@media (max-width: 768px) {
  .study-record-container {
    padding: 1rem;
  }
  .records-list {
    grid-template-columns: 1fr;
  }
  .page-header h1 {
    font-size: 1.5rem;
  }
}
</style>
