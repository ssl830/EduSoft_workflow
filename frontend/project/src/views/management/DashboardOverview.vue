<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { fetchDashboardOverview } from '@/api/dashboard'

interface Stats {
  uploadResource?: number
  createHomework?: number
  createPractice?: number
  correctPractice?: number
  createQuestion?: number

  downloadResource?: number
  submitHomework?: number
  submitPractice?: number
  assistantQuestions?: number
}

interface EfficiencyStats {
  lessonPrepAvgDuration?: string
  lessonPrepCallCount?: number
  exerciseDesignAvgDuration?: string
  exerciseDesignCallCount?: number
  courseOptimizationDirection?: string
}

interface LearningEffectStats {
  correctnessBySection?: Array<{ course_id: number; section_id: number; average_score: number }>
  topWrongKnowledgePoints?: Array<{ question_id: number; wrong_count: number; course_id: number; section_id: number; content: string }>
  courseSectionNames?: Record<string, string>
}

const initialOverviewState = {
  today: { teacher: {}, student: {}, efficiency: {}, learningEffect: {} },
  week: { teacher: {}, student: {}, efficiency: {}, learningEffect: {} }
};

const loading = ref(true)
const overview = ref<typeof initialOverviewState>(initialOverviewState);

// Computed properties for safe access to today and week stats
const todayStats = computed(() => overview.value?.today || initialOverviewState.today);
const weekStats = computed(() => overview.value?.week || initialOverviewState.week);

const loadData = async () => {
  try {
    loading.value = true
    const resp = await fetchDashboardOverview()
    console.log('API Response:', resp);

    if (resp) {
      const rawData = resp;
      console.log('Raw Data to process:', rawData);

      const newOverviewValue = {
        today: {
          teacher: rawData.today?.teacher || {},
          student: rawData.today?.student || {},
          efficiency: rawData.todayEfficiency || {},
          learningEffect: {
            correctnessBySection: rawData.todayLearningEffect?.correctnessBySection || [],
            topWrongKnowledgePoints: rawData.todayLearningEffect?.topWrongKnowledgePoints || [],
            courseSectionNames: rawData.todayLearningEffect?.courseSectionNames || {}
          }
        },
        week: {
          teacher: rawData.week?.teacher || {},
          student: rawData.week?.student || {},
          efficiency: rawData.weekEfficiency || {},
          learningEffect: {
            correctnessBySection: rawData.weekLearningEffect?.correctnessBySection || [],
            topWrongKnowledgePoints: rawData.weekLearningEffect?.topWrongKnowledgePoints || [],
            courseSectionNames: rawData.weekLearningEffect?.courseSectionNames || {}
          }
        }
      };

      overview.value = newOverviewValue;

      console.log('Overview after assignment:', overview.value);
    }
  } catch (err) {
    console.error('加载大屏统计失败', err)
  } finally {
    loading.value = false
  }
}

const translateKey = (key: string) => {
  const map: Record<string, string> = {
    uploadResource: '上传资源次数',
    createHomework: '创建作业次数',
    createPractice: '创建练习次数',
    correctPractice: '批改练习次数',
    createQuestion: '生成题目数',
    downloadResource: '下载/观看资源次数',
    submitHomework: '提交作业次数',
    submitPractice: '提交练习次数',
    assistantQuestions: 'AI助手提问数',
    lessonPrepAvgDuration: '教案备课平均耗时 (ms)',
    lessonPrepCallCount: '教案备课调用次数',
    exerciseDesignAvgDuration: '练习设计平均耗时 (ms)',
    exerciseDesignCallCount: '练习设计调用次数',
    courseOptimizationDirection: '课程优化方向'
  }
  return map[key] || key
}

const getCourseSectionName = (courseId: number, sectionId: number, period: 'today' | 'week') => {
  console.log(`getCourseSectionName called for period: ${period}, overview.value:`, overview.value);
  const key = `${courseId}-${sectionId}`
  const targetStats = period === 'today' ? todayStats.value : weekStats.value;
  return targetStats.learningEffect?.courseSectionNames?.[key] || `课程 ${courseId} - 章节 ${sectionId}`
}

onMounted(loadData)
</script>

<template>
  <div class="dashboard-overview">
    <h2>系统概览</h2>
    <div v-if="loading" class="loading">加载中...</div>

    <div v-else>
      <div v-if="overview.value">
        <section class="stats-section">
          <h3>今日概览</h3>
          <p class="debug-info">调试：今日教师数据: {{ JSON.stringify(todayStats.value.teacher) }}</p>
          <p class="debug-info">调试：今日学生数据: {{ JSON.stringify(todayStats.value.student) }}</p>
          <div class="stats-grid">
            <div class="stat-card" v-for="(value, key) in todayStats.value.teacher || {}" :key="'today-teacher-' + key">
              <h4>教师 {{ translateKey(key) }}</h4>
              <p>{{ value }}</p>
            </div>
            <div class="stat-card" v-for="(value, key) in todayStats.value.student || {}" :key="'today-student-' + key">
              <h4>学生 {{ translateKey(key) }}</h4>
              <p>{{ value }}</p>
            </div>
          </div>
        </section>

        <section class="stats-section">
          <h3>今日教学效率指数</h3>
          <p class="debug-info">调试：今日效率数据: {{ JSON.stringify(todayStats.value.efficiency) }}</p>
          <div class="stats-grid">
            <div class="stat-card" v-for="(value, key) in todayStats.value.efficiency || {}" :key="'today-efficiency-' + key">
              <h4>{{ translateKey(key) }}</h4>
              <p>{{ value }}</p>
            </div>
          </div>
        </section>

        <section class="stats-section">
          <h3>今日学生学习效果</h3>
          <p class="debug-info">调试：今日学习效果数据: {{ JSON.stringify(todayStats.value.learningEffect) }}</p>
          <div class="learning-effect-section">
            <h4>平均正确率趋势 (按课程-章节)</h4>
            <div class="data-list">
              <div v-for="item in todayStats.value.learningEffect.correctnessBySection || []" :key="`today-correctness-${item.course_id}-${item.section_id}`">
                {{ getCourseSectionName(item.course_id, item.section_id, 'today') }}: {{ (item.average_score || 0).toFixed(2) }} 分
              </div>
              <div v-if="!todayStats.value.learningEffect.correctnessBySection?.length">暂无数据</div>
            </div>

            <h4>高频错误知识点</h4>
            <div class="data-list">
              <div v-for="item in todayStats.value.learningEffect.topWrongKnowledgePoints || []" :key="`today-wrong-${item.question_id}`">
                [{{ getCourseSectionName(item.course_id, item.section_id, 'today') }}] {{ item.content }} (错误 {{ item.wrong_count }} 次)
              </div>
              <div v-if="!todayStats.value.learningEffect.topWrongKnowledgePoints?.length">暂无数据</div>
            </div>
          </div>
        </section>

        <section class="stats-section">
          <h3>近7日概览</h3>
          <p class="debug-info">调试：本周教师数据: {{ JSON.stringify(weekStats.value.teacher) }}</p>
          <p class="debug-info">调试：本周学生数据: {{ JSON.stringify(weekStats.value.student) }}</p>
          <div class="stats-grid">
            <div class="stat-card" v-for="(value, key) in weekStats.value.teacher || {}" :key="'week-teacher-' + key">
              <h4>教师 {{ translateKey(key) }}</h4>
              <p>{{ value }}</p>
            </div>
            <div class="stat-card" v-for="(value, key) in weekStats.value.student || {}" :key="'week-student-' + key">
              <h4>学生 {{ translateKey(key) }}</h4>
              <p>{{ value }}</p>
            </div>
          </div>
        </section>

        <section class="stats-section">
          <h3>近7日教学效率指数</h3>
          <p class="debug-info">调试：本周效率数据: {{ JSON.stringify(weekStats.value.efficiency) }}</p>
          <div class="stats-grid">
            <div class="stat-card" v-for="(value, key) in weekStats.value.efficiency || {}" :key="'week-efficiency-' + key">
              <h4>{{ translateKey(key) }}</h4>
              <p>{{ value }}</p>
            </div>
          </div>
        </section>

        <section class="stats-section">
          <h3>近7日学生学习效果</h3>
          <p class="debug-info">调试：本周学习效果数据: {{ JSON.stringify(weekStats.value.learningEffect) }}</p>
          <div class="learning-effect-section">
            <h4>平均正确率趋势 (按课程-章节)</h4>
            <div class="data-list">
              <div v-for="item in weekStats.value.learningEffect.correctnessBySection || []" :key="`week-correctness-${item.course_id}-${item.section_id}`">
                {{ getCourseSectionName(item.course_id, item.section_id, 'week') }}: {{ (item.average_score || 0).toFixed(2) }} 分
              </div>
              <div v-if="!weekStats.value.learningEffect.correctnessBySection?.length">暂无数据</div>
            </div>

            <h4>高频错误知识点</h4>
            <div class="data-list">
              <div v-for="item in weekStats.value.learningEffect.topWrongKnowledgePoints || []" :key="`week-wrong-${item.question_id}`">
                [{{ getCourseSectionName(item.course_id, item.section_id, 'week') }}] {{ item.content }} (错误 {{ item.wrong_count }} 次)
              </div>
              <div v-if="!weekStats.value.learningEffect.topWrongKnowledgePoints?.length">暂无数据</div>
            </div>
          </div>
        </section>
      </div>
      <div v-else>未能加载数据或数据为空。</div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-overview {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}
.stats-section {
  margin-bottom: 2.5rem;
  background: var(--bg-card);
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: var(--shadow-small);
  transition: background-color var(--transition-normal), box-shadow var(--transition-normal);
}
.stats-section h3 {
  color: var(--text-dark);
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid var(--primary);
  padding-bottom: 0.75rem;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 1.5rem;
}
.stat-card {
  background: var(--bg-secondary);
  border-radius: 10px;
  padding: 1.2rem;
  text-align: center;
  border: 1px solid var(--border-color);
  transition: all 0.2s ease-in-out;
}
.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-medium);
}
.stat-card h4 {
  margin-bottom: 0.6rem;
  font-size: 1rem;
  color: var(--text-secondary);
  font-weight: 600;
}
.stat-card p {
  font-size: 2rem;
  font-weight: bold;
  color: var(--primary);
  margin-top: 0.5rem;
}
.loading {
  text-align: center;
  padding: 4rem;
  font-size: 1.2rem;
  color: var(--text-secondary);
}

.learning-effect-section h4 {
  font-size: 1.1rem;
  color: var(--primary-dark);
  margin-top: 1.5rem;
  margin-bottom: 0.8rem;
  padding-left: 0.5rem;
  border-left: 4px solid var(--primary-light);
}

.data-list {
  background: var(--bg-code);
  border-radius: 8px;
  padding: 1rem 1.5rem;
  border: 1px dashed var(--border-color);
  max-height: 250px; /* Limit height for scroll */
  overflow-y: auto;
}

.data-list div {
  padding: 0.5rem 0;
  border-bottom: 1px dotted var(--border-color-light);
  color: var(--text-primary);
  font-size: 0.95rem;
}

.data-list div:last-child {
  border-bottom: none;
}

.data-list div:hover {
  background-color: var(--bg-hover);
  border-radius: 4px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style> 