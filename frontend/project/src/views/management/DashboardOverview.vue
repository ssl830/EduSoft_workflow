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
const todayStats = computed(() => {
  return overview.value?.today || initialOverviewState.today;
});
const weekStats = computed(() => {
  return overview.value?.week || initialOverviewState.week;
});

const loadData = async () => {
  try {
    loading.value = true
    const resp = await fetchDashboardOverview()

    if (resp && resp.data) {
      const rawData = resp.data;

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
      <div v-if="overview">
        <div>
          <section class="stats-section">
            <h3>今日概览</h3>
            <div class="stats-grid">
              <div class="stat-card" v-for="(value, key) in todayStats.teacher" :key="'today-teacher-' + key">
                <h4>教师 {{ translateKey(key) }}</h4>
                <p>{{ value }}</p>
              </div>
              <div class="stat-card" v-for="(value, key) in todayStats.student" :key="'today-student-' + key">
                <h4>学生 {{ translateKey(key) }}</h4>
                <p>{{ value }}</p>
              </div>
            </div>
          </section>

          <section class="stats-section">
            <h3>今日教学效率指数</h3>
            <div class="stats-grid">
              <div class="stat-card" v-for="(value, key) in todayStats.efficiency" :key="'today-efficiency-' + key">
                <h4>{{ translateKey(key) }}</h4>
                <p>{{ value }}</p>
              </div>
            </div>
          </section>

          <section class="stats-section">
            <h3>今日学生学习效果</h3>
            <div class="learning-effect-section">
              <h4>平均正确率趋势 (按课程-章节)</h4>
              <div class="data-list">
                <div v-for="item in todayStats.learningEffect.correctnessBySection" :key="`today-correctness-${item.course_id}-${item.section_id}`">
                  {{ getCourseSectionName(item.course_id, item.section_id, 'today') }}: {{ (item.average_score || 0).toFixed(2) }} 分
                </div>
                <div v-if="!todayStats.learningEffect.correctnessBySection?.length">暂无数据</div>
              </div>

              <h4>高频错误知识点</h4>
              <div class="data-list">
                <div v-for="item in todayStats.learningEffect.topWrongKnowledgePoints" :key="`today-wrong-${item.question_id}`">
                  [{{ getCourseSectionName(item.course_id, item.section_id, 'today') }}] {{ item.content }} (错误 {{ item.wrong_count }} 次)
                </div>
                <div v-if="!todayStats.learningEffect.topWrongKnowledgePoints?.length">暂无数据</div>
              </div>
            </div>
          </section>

          <section class="stats-section">
            <h3>近7日概览</h3>
            <div class="stats-grid">
              <div class="stat-card" v-for="(value, key) in weekStats.teacher" :key="'week-teacher-' + key">
                <h4>教师 {{ translateKey(key) }}</h4>
                <p>{{ value }}</p>
              </div>
              <div class="stat-card" v-for="(value, key) in weekStats.student" :key="'week-student-' + key">
                <h4>学生 {{ translateKey(key) }}</h4>
                <p>{{ value }}</p>
              </div>
            </div>
          </section>

          <section class="stats-section">
            <h3>近7日教学效率指数</h3>
            <div class="stats-grid">
              <div class="stat-card" v-for="(value, key) in weekStats.efficiency" :key="'week-efficiency-' + key">
                <h4>{{ translateKey(key) }}</h4>
                <p>{{ value }}</p>
              </div>
            </div>
          </section>

          <section class="stats-section">
            <h3>近7日学生学习效果</h3>
            <div class="learning-effect-section">
              <h4>平均正确率趋势 (按课程-章节)</h4>
              <div class="data-list">
                <div v-for="item in weekStats.learningEffect.correctnessBySection" :key="`week-correctness-${item.course_id}-${item.section_id}`">
                  {{ getCourseSectionName(item.course_id, item.section_id, 'week') }}: {{ (item.average_score || 0).toFixed(2) }} 分
                </div>
                <div v-if="!weekStats.learningEffect.correctnessBySection?.length">暂无数据</div>
              </div>

              <h4>高频错误知识点</h4>
              <div class="data-list">
                <div v-for="item in weekStats.learningEffect.topWrongKnowledgePoints" :key="`week-wrong-${item.question_id}`">
                  [{{ getCourseSectionName(item.course_id, item.section_id, 'week') }}] {{ item.content }} (错误 {{ item.wrong_count }} 次)
                </div>
                <div v-if="!weekStats.learningEffect.topWrongKnowledgePoints?.length">暂无数据</div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard-overview {
  padding: 20px;
}

.loading {
  text-align: center;
  padding: 20px;
  font-size: 16px;
  color: #666;
}

.stats-section {
  margin-bottom: 30px;
}

.stats-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-card h4 {
  margin: 0 0 10px;
  color: #666;
  font-size: 14px;
}

.stat-card p {
  margin: 0;
  font-size: 24px;
  color: #333;
  font-weight: bold;
}

.learning-effect-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.learning-effect-section h4 {
  margin: 0 0 15px;
  color: #333;
  font-size: 16px;
}

.data-list {
  margin-bottom: 20px;
}

.data-list > div {
  padding: 10px;
  border-bottom: 1px solid #eee;
  color: #666;
}

.data-list > div:last-child {
  border-bottom: none;
}
</style> 