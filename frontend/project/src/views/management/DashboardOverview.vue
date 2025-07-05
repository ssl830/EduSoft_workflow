<script setup lang="ts">
import { onMounted, ref, computed, nextTick, watch } from 'vue'
import { fetchDashboardOverview } from '@/api/dashboard'
import { getCourseOptimization } from '@/api/optimization'
import * as echarts from 'echarts'
import { useQuasar } from 'quasar'
import { QBtn, QTooltip } from 'quasar'

interface Stats {
  uploadResources: number
  createHomework: number
  createPractice: number
  correctPractice: number
  createQuestions: number
  downloadResources: number
  submitHomework: number
  submitPractice: number
  assistantQuestions: number
}

interface EfficiencyStats {
  lessonPrepDuration: number
  lessonPrepCallCount: number
  exerciseDesignDuration: number
  exerciseDesignCallCount: number
  courseOptimizationDirection?: string[]
  courseSectionNames?: Record<string, string>
  classNames?: Record<string, string>
}

interface LearningEffectStats {
  correctnessBySection: Array<{ course_id: number; section_id: number; correctness: number }>
  topWrongKnowledgePoints: Array<{ question_id: number; wrong_count: number; course_id: number; section_id: number; content: string }>
  courseSectionNames: Record<string, string>
}

interface DashboardData {
  teacher: Stats;
  student: Stats;
  efficiency: EfficiencyStats;
  learningEffect: LearningEffectStats;
  averageScores: Record<string, number>;
  errorRates: Record<string, number>;
  studentCounts: Record<string, number>;
}

interface Overview {
  today: DashboardData;
  week: DashboardData;
}

const initialOverviewState: Overview = {
  today: {
    teacher: {
      uploadResources: 0,
      createHomework: 0,
      createPractice: 0,
      correctPractice: 0,
      createQuestions: 0,
      downloadResources: 0,
      submitHomework: 0,
      submitPractice: 0,
      assistantQuestions: 0
    },
    student: {
      uploadResources: 0,
      createHomework: 0,
      createPractice: 0,
      correctPractice: 0,
      createQuestions: 0,
      downloadResources: 0,
      submitHomework: 0,
      submitPractice: 0,
      assistantQuestions: 0
    },
    efficiency: {
      lessonPrepDuration: 0,
      lessonPrepCallCount: 0,
      exerciseDesignDuration: 0,
      exerciseDesignCallCount: 0,
      courseOptimizationDirection: [],
      courseSectionNames: {},
      classNames: {}
    },
    learningEffect: {
      correctnessBySection: [],
      topWrongKnowledgePoints: [],
      courseSectionNames: {}
    },
    averageScores: {},
    errorRates: {},
    studentCounts: {}
  },
  week: {
    teacher: {
      uploadResources: 0,
      createHomework: 0,
      createPractice: 0,
      correctPractice: 0,
      createQuestions: 0,
      downloadResources: 0,
      submitHomework: 0,
      submitPractice: 0,
      assistantQuestions: 0
    },
    student: {
      uploadResources: 0,
      createPractice: 0,
      correctPractice: 0,
      createQuestions: 0,
      downloadResources: 0,
      submitHomework: 0,
      submitPractice: 0,
      assistantQuestions: 0
    },
    efficiency: {
      lessonPrepDuration: 0,
      lessonPrepCallCount: 0,
      exerciseDesignDuration: 0,
      exerciseDesignCallCount: 0,
      courseOptimizationDirection: [],
      courseSectionNames: {},
      classNames: {}
    },
    learningEffect: {
      correctnessBySection: [],
      topWrongKnowledgePoints: [],
      courseSectionNames: {}
    },
    averageScores: {},
    errorRates: {},
    studentCounts: {}
  }
};

const $q = useQuasar()
const loading = ref(true)
const overview = ref<Overview>(initialOverviewState)
const viewMode = ref<'day' | 'week'>('day')
const chartRef = ref<HTMLElement | null>(null)

// Computed properties for safe access to today and week stats
const todayStats = computed(() => {
  return overview.value?.today || initialOverviewState.today;
});
const weekStats = computed(() => {
  return overview.value?.week || initialOverviewState.week;
});

// 过滤后的高频错误知识点与正确率数据，排除学生自建题目（course_id = -1）
const todayWrongList = computed(() => ((todayStats.value.learningEffect as any)?.topWrongKnowledgePoints || []).filter((item: any) => item.course_id !== -1 && item.section_id !== -1));
const weekWrongList = computed(() => ((weekStats.value.learningEffect as any)?.topWrongKnowledgePoints || []).filter((item: any) => item.course_id !== -1 && item.section_id !== -1));
const todayCorrectnessList = computed(() => ((todayStats.value.learningEffect as any)?.correctnessBySection || []).filter((item: any) => item.course_id !== -1 && item.section_id !== -1));
const weekCorrectnessList = computed(() => ((weekStats.value.learningEffect as any)?.correctnessBySection || []).filter((item: any) => item.course_id !== -1 && item.section_id !== -1));

// 课程章节名称映射 (必须在 getCourseSectionName 前定义)
const todayCourseSectionNames = computed<Record<string, string>>(() => ((todayStats.value.learningEffect as any)?.courseSectionNames) || {});
const weekCourseSectionNames = computed<Record<string, string>>(() => ((weekStats.value.learningEffect as any)?.courseSectionNames) || {});

const getCourseSectionName = (courseId: number, sectionId: number, period: 'today' | 'week') => {
  const key = `${courseId}-${sectionId}`;
  const namesMap = period === 'today' ? todayCourseSectionNames.value : weekCourseSectionNames.value;
  return namesMap[key] || `课程 ${courseId} - 章节 ${sectionId}`;
};

// 建议文本拆分
const getAdviceLines = (value: any) => {
  if (typeof value === 'string') {
    return value.split('\n');
  }
  return [];
};

// 额外映射：教学效率中的课程/班级名称
const todayEfficiencyCourseSections = computed<Record<string, string>>(() => ((todayStats.value.efficiency as any)?.courseSectionNames) || {});
const weekEfficiencyCourseSections = computed<Record<string, string>>(() => ((weekStats.value.efficiency as any)?.courseSectionNames) || {});
const todayClassNames = computed<Record<string, string>>(() => ((todayStats.value.efficiency as any)?.classNames) || {});
const weekClassNames = computed<Record<string, string>>(() => ((weekStats.value.efficiency as any)?.classNames) || {});

// 监听视图模式或数据变化重新渲染图表
watch([viewMode, overview], () => nextTick(renderChart));

const renderChart = () => {
  if (!chartRef.value) return;
  const chart = echarts.init(chartRef.value);
  // 根据视图模式选择数据
  const teacherData = viewMode.value === 'day' ? todayStats.value.teacher : weekStats.value.teacher;
  const studentData = viewMode.value === 'day' ? todayStats.value.student : weekStats.value.student;
  const categories = Object.keys({ ...teacherData, ...studentData }).map(k => translateKey(k));
  const teacherSeries = categories.map((name, idx) => {
    const key = Object.keys({ ...teacherData, ...studentData })[idx];
    return (teacherData as any)[key] || 0;
  });
  const studentSeries = categories.map((name, idx) => {
    const key = Object.keys({ ...teacherData, ...studentData })[idx];
    return (studentData as any)[key] || 0;
  });

  const option = {
    tooltip: { trigger: 'axis' },
    legend: { data: ['教师', '学生'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: categories, axisLabel: { rotate: 30 } },
    yAxis: { type: 'value' },
    series: [
      { name: '教师', type: 'bar', data: teacherSeries, itemStyle: { color: '#409EFF' } },
      { name: '学生', type: 'bar', data: studentSeries, itemStyle: { color: '#67C23A' } }
    ]
  };
  chart.setOption(option);
};

const translateKey = (key: string) => {
  const map: Record<string, string> = {
    // 教师统计
    uploadResources: '上传资源次数',
    createHomework: '创建作业次数',
    createPractice: '创建练习次数',
    correctPractice: '批改练习次数',
    createQuestions: '生成题目数',
    uploadResource: '上传资源次数',  // 兼容旧字段
    createQuestion: '生成题目数',    // 兼容旧字段

    // 学生统计
    downloadResources: '下载/观看资源次数',
    downloadResource: '下载/观看资源次数',  // 兼容旧字段
    submitHomework: '提交作业次数',
    submitPractice: '提交练习次数',
    assistantQuestions: 'AI助手提问数',

    // 效率统计
    lessonPrepDuration: '教案备课耗时',
    lessonPrepCallCount: '教案备课调用次数',
    lessonPrepAvgDuration: '教案备课平均耗时',  // 兼容旧字段
    exerciseDesignDuration: '练习设计耗时',
    exerciseDesignCallCount: '练习设计调用次数',
    exerciseDesignAvgDuration: '练习设计平均耗时',  // 兼容旧字段
    courseOptimizationDirection: '课程优化方向',
    courseSectionNames: '关联课程-章节',
    classNames: '关联班级'
  }
  return map[key] || key
}

type EfficiencyKey = 'lessonPrepDuration' | 'lessonPrepCallCount' | 'exerciseDesignDuration' | 'exerciseDesignCallCount' | 'courseOptimizationDirection' | 'courseSectionNames' | 'classNames';

const isEfficiencyKey = (key: string): key is EfficiencyKey => {
  return [
    'lessonPrepDuration',
    'lessonPrepCallCount',
    'exerciseDesignDuration',
    'exerciseDesignCallCount',
    'courseOptimizationDirection',
    'courseSectionNames',
    'classNames'
  ].includes(key)
}

const handleOptimize = async (courseId: number, sectionId: number) => {
  try {
    $q.loading.show({ message: '正在获取优化建议...' })
    const response = await getCourseOptimization({
      courseId,
      sectionId,
      averageScore: overview.value.today.averageScores?.[`${courseId}-${sectionId}`] || 0,
      errorRate: overview.value.today.errorRates?.[`${courseId}-${sectionId}`] || 0,
      studentCount: overview.value.today.studentCounts?.[`${courseId}-${sectionId}`] || 0
    })

    $q.dialog({
      title: 'AI 优化建议',
      message: `
        <div class="optimization-dialog">
          <h6>建议措施：</h6>
          <ul>${response.suggestions.map(s => `<li>${s}</li>`).join('')}</ul>
          <h6>相关知识点：</h6>
          <ul>${response.relatedKnowledgePoints.map(k => `<li>${k}</li>`).join('')}</ul>
          <h6>推荐行动：</h6>
          <ul>${response.recommendedActions.map(a => `<li>${a}</li>`).join('')}</ul>
        </div>
      `,
      html: true,
      ok: '确定'
    })
  } catch (error) {
    console.error('获取优化建议失败:', error)
    $q.notify({
      type: 'negative',
      message: '获取优化建议失败'
    })
  } finally {
    $q.loading.hide()
  }
}

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
          },
          averageScores: rawData.today?.averageScores || {},
          errorRates: rawData.today?.errorRates || {},
          studentCounts: rawData.today?.studentCounts || {}
        },
        week: {
          teacher: rawData.week?.teacher || {},
          student: rawData.week?.student || {},
          efficiency: rawData.weekEfficiency || {},
          learningEffect: {
            correctnessBySection: rawData.weekLearningEffect?.correctnessBySection || [],
            topWrongKnowledgePoints: rawData.weekLearningEffect?.topWrongKnowledgePoints || [],
            courseSectionNames: rawData.weekLearningEffect?.courseSectionNames || {}
          },
          averageScores: rawData.week?.averageScores || {},
          errorRates: rawData.week?.errorRates || {},
          studentCounts: rawData.week?.studentCounts || {}
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

onMounted(() => { loadData(); nextTick(renderChart); });

// Add type guard for array values
const isArrayValue = (value: unknown): value is string[] => {
  return Array.isArray(value)
}

// Add type guard for number values
const isNumberValue = (value: unknown): value is number => {
  return typeof value === 'number'
}
</script>

<template>
  <div class="dashboard-overview">
    <div class="header-bar">
      <h2>系统概览</h2>
      <div class="view-toggle">
        <button :class="{ active: viewMode === 'day' }" @click="viewMode = 'day'">日视图</button>
        <button :class="{ active: viewMode === 'week' }" @click="viewMode = 'week'">周视图</button>
      </div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div v-if="overview">
        <div v-if="viewMode === 'day'">
          <!-- 日视图 -->
          <section class="stats-section">
            <h3>今日概览</h3>
            <div class="stats-grid">
              <div class="stat-card" v-for="(value, key) in todayStats.teacher" :key="'today-teacher-' + key">
                <h4>教师 {{ translateKey(key) }}</h4>
                <p>{{ value }}</p>
                <!-- 课程章节映射 & 班级映射 在上方 stat-card 中直接渲染 -->
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
                <template v-if="key === 'courseOptimizationDirection'">
                  <ul class="advice-list">
                    <li v-for="line in getAdviceLines(value)" :key="line">{{ line }}</li>
                  </ul>
                </template>
                <template v-else-if="key === 'courseSectionNames'">
                  <div class="course-section-tags">
                    <span v-for="(label, skey) in value" :key="skey" class="cs-tag">
                      {{ label }}
                      <q-btn
                        flat
                        round
                        size="sm"
                        icon="tune"
                        @click="handleOptimize(
                          Number(skey.split('-')[0]),
                          Number(skey.split('-')[1])
                        )"
                      >
                        <q-tooltip>获取AI优化建议</q-tooltip>
                      </q-btn>
                    </span>
                  </div>
                </template>
                <template v-else-if="key === 'classNames'">
                  <div class="course-section-tags">
                    <span class="cs-tag secondary" v-for="(label, skey) in value" :key="'cl-day-' + skey">{{ label }}</span>
                  </div>
                </template>
                <template v-else>
                  <div class="data-value">{{ value }}</div>
                </template>
              </div>
            </div>
          </section>

          <section class="stats-section">
            <h3>今日学生学习效果</h3>
            <div class="learning-effect-section">
              <h4>平均正确率趋势 (按课程-章节)</h4>
              <div class="data-list">
                <div v-for="item in todayCorrectnessList" :key="`today-correctness-${item.course_id}-${item.section_id}`">
                  {{ getCourseSectionName(item.course_id, item.section_id, 'today') }}: {{ (item.correctness || 0).toFixed(2) }} 分
                </div>
                <div v-if="!todayCorrectnessList?.length">暂无数据</div>
              </div>

              <h4>高频错误知识点</h4>
              <div class="data-list">
                <div v-for="item in todayWrongList" :key="`today-wrong-${item.question_id}`">
                  [{{ getCourseSectionName(item.course_id, item.section_id, 'today') }}] {{ item.content }} (错误 {{ item.wrong_count }} 次)
                </div>
                <div v-if="!todayWrongList?.length">暂无数据</div>
              </div>

              <h4>关联课程章节</h4>
              <div class="course-section-tags">
                <span class="cs-tag" v-for="(label, key) in todayCourseSectionNames" :key="'today-cs-' + key">
                  {{ label }}
                  <q-btn
                    flat
                    round
                    size="sm"
                    icon="tune"
                    @click="handleOptimize(
                      Number(key.split('-')[0]),
                      Number(key.split('-')[1])
                    )"
                  >
                    <q-tooltip>获取AI优化建议</q-tooltip>
                  </q-btn>
                </span>
                <div v-if="Object.keys(todayCourseSectionNames).length === 0">暂无数据</div>
              </div>
            </div>
          </section>
        </div>

        <div v-else>
          <!-- 周视图 -->
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
                <template v-if="key === 'courseOptimizationDirection'">
                  <ul class="advice-list">
                    <li v-for="line in getAdviceLines(value)" :key="line">{{ line }}</li>
                  </ul>
                </template>
                <template v-else-if="key === 'courseSectionNames'">
                  <div class="course-section-tags">
                    <span v-for="(label, skey) in value" :key="skey" class="cs-tag">
                      {{ label }}
                      <q-btn
                        flat
                        round
                        size="sm"
                        icon="tune"
                        @click="handleOptimize(
                          Number(skey.split('-')[0]),
                          Number(skey.split('-')[1])
                        )"
                      >
                        <q-tooltip>获取AI优化建议</q-tooltip>
                      </q-btn>
                    </span>
                  </div>
                </template>
                <template v-else-if="key === 'classNames'">
                  <div class="course-section-tags">
                    <span class="cs-tag secondary" v-for="(label, skey) in value" :key="'cl-week-' + skey">{{ label }}</span>
                  </div>
                </template>
                <template v-else>
                  <div class="data-value">{{ value }}</div>
                </template>
              </div>
            </div>
          </section>

          <section class="stats-section">
            <h3>近7日学生学习效果</h3>
            <div class="learning-effect-section">
              <h4>平均正确率趋势 (按课程-章节)</h4>
              <div class="data-list">
                <div v-for="item in weekCorrectnessList" :key="`week-correctness-${item.course_id}-${item.section_id}`">
                  {{ getCourseSectionName(item.course_id, item.section_id, 'week') }}: {{ (item.correctness || 0).toFixed(2) }} 分
                </div>
                <div v-if="!weekCorrectnessList?.length">暂无数据</div>
              </div>

              <h4>高频错误知识点</h4>
              <div class="data-list">
                <div v-for="item in weekWrongList" :key="`week-wrong-${item.question_id}`">
                  [{{ getCourseSectionName(item.course_id, item.section_id, 'week') }}] {{ item.content }} (错误 {{ item.wrong_count }} 次)
                </div>
                <div v-if="!weekWrongList?.length">暂无数据</div>
              </div>

              <h4>关联课程章节</h4>
              <div class="course-section-tags">
                <span class="cs-tag" v-for="(label, key) in weekCourseSectionNames" :key="'week-cs-' + key">{{ label }}</span>
                <div v-if="Object.keys(weekCourseSectionNames).length === 0">暂无数据</div>
              </div>
            </div>
          </section>
        </div>
        <div ref="chartRef" style="width: 100%; height: 320px; margin-bottom: 30px;"></div>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
.dashboard-overview {
  padding: 20px;
}

.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h2 {
    margin: 0;
    color: #100202;
    font-weight: bold;
    font-size: 30px;
  }
}

.view-toggle {
  display: flex;
  gap: 8px;

  button {
    padding: 6px 12px;
    border: 1px solid #dcdfe6;
    background: white;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;

    &.active {
      background: #409eff;
      color: white;
      border-color: #409eff;
    }
  }
}

.cs-tag {
  display: inline-flex;
  align-items: center;
  padding: 4px 8px;
  background-color: var(--q-primary);
  color: white;
  border-radius: 16px;
  font-size: 14px;


  &.secondary {
    background-color: var(--q-secondary);
  }

  .q-btn {
    margin-left: 4px;
    opacity: 0.8;
    transition: opacity 0.2s;

    &:hover {
      opacity: 1;
    }
  }
}

.optimization-dialog {
  h6 {
    margin: 16px 0 8px;
    color: #1976d2;
    font-size: 16px;
  }

  ul {
    margin: 8px 0;
    padding-left: 20px;
  }

  li {
    margin: 4px 0;
  }
}

.optimize-btn {
  margin-left: 8px;
  height: 32px;
}

.stats-section {
  margin-bottom: 30px;
}

.stats-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
  font-weight: bold;
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
  font-weight: bold;
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
  font-weight: bold;
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

.course-section-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.advice-list {
  padding-left: 18px;
  margin: 0;
  color: #2d6a4f;
  font-size: 14px;
  line-height: 1.7;
}

.advice-list li {
  list-style: disc;
}

.mapping-section {
  margin-top: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.mapping-section h4 {
  margin-bottom: 15px;
  color: #333;
  font-size: 16px;
}

.secondary {
  background: #fff7e6;
  color: #f1efed;
}

.loading {
  text-align: center;
  padding: 20px;
  font-size: 16px;
  color: #666;
}

.metrics-section {
  margin-top: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.metrics-section h3 {
  margin-bottom: 20px;
  color: #333;
  font-size: 18px;
  font-weight: bold;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.metric-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.metric-title {
  margin-bottom: 10px;
  color: #666;
  font-size: 14px;
}

.metric-value {
  font-size: 24px;
  color: #333;
  font-weight: bold;
}
</style> 