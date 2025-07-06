<template>
  <div class="page-container">
    <AppSidebar />
    <div class="content-container">
      <div class="learning-records-analysis">
        <h1>教师练习统计</h1>
        <div class="filter-section">
          <label for="courseFilter">按课程筛选：</label>
          <select id="courseFilter" v-model="selectedCourse">
            <option value="">所有课程</option>
            <option v-for="course in uniqueCourses" :key="course" :value="course">
              {{ course }}
            </option>
          </select>
        </div>
        <div v-if="loading" class="loading-indicator">加载中...</div>
        <div v-else-if="error" class="error-message">{{ error }}</div>
        <div v-else>
          <div class="card-container">
            <div class="practice-card" v-for="practice in filteredPractices" :key="practice.id">
              <h3>{{ practice.title }}</h3>
              <p><strong>课程名称：</strong>{{ practice.course_name }}</p>
              <p><strong>开始时间：</strong>{{ practice.start_time }}</p>
              <p><strong>结束时间：</strong>{{ practice.end_time }}</p>
              <p><strong>已提交人数：</strong>
                <span v-if="practice.classTotal !== null">{{ practice.submissionCount }}/{{ practice.classTotal }}</span>
                <span v-else>{{ practice.submissionCount }}</span>
              </p>
              <p><strong>平均分：</strong>{{ practice.averageScore !== null ? practice.averageScore.toFixed(2) : '-' }}</p>
              <!-- 手动统计得分率按钮 -->
              <button class="score-rate-btn" @click="updateScoreRate(practice)" :disabled="scoreRateLoadingMap[practice.id]" style="margin-bottom: 0.5rem;">
                <span v-if="scoreRateLoadingMap[practice.id]">统计中...</span>
                <span v-else>手动统计得分率</span>
              </button>
              <!-- AI分析按钮 -->
              <button class="ai-analyze-btn" @click="analyzePractice(practice)" :disabled="aiLoadingMap[practice.id]">
                <svg v-if="aiLoadingMap[practice.id]" class="ai-btn-icon spin" width="18" height="18" viewBox="0 0 50 50"><circle cx="25" cy="25" r="20" fill="none" stroke="#1890ff" stroke-width="5" stroke-linecap="round" stroke-dasharray="31.4 31.4"/></svg>
                <svg v-else class="ai-btn-icon" width="18" height="18" viewBox="0 0 24 24"><path fill="#fff" d="M12 2a10 10 0 1 0 10 10A10.011 10.011 0 0 0 12 2Zm1 17.93V20a1 1 0 0 1-2 0v-.07A8.06 8.06 0 0 1 4.07 13H4a1 1 0 0 1 0-2h.07A8.06 8.06 0 0 1 11 4.07V4a1 1 0 0 1 2 0v.07A8.06 8.06 0 0 1 19.93 11H20a1 1 0 0 1 0 2h-.07A8.06 8.06 0 0 1 13 19.93Z"/></svg>
                <span v-if="aiLoadingMap[practice.id]" style="margin-left:8px;">AI分析中...</span>
                <span v-else style="margin-left:8px;">AI分析得分情况</span>
              </button>
            </div>
            <!-- AI分析结果弹窗 -->
            <div v-if="showAiModal" class="modal-mask">
              <div class="modal-container">
                <div class="modal-header">
                  <h3>AI学情分析结果</h3>
                  <button class="modal-close" @click="showAiModal = false">×</button>
                </div>
                <div class="modal-body">
                  <div v-if="aiModalLoading" class="ai-modal-loading"><span class="ai-loading-spinner"></span> 分析中...</div>
                  <div v-else-if="aiModalError" class="ai-modal-error">{{ aiModalError }}</div>
                  <div v-else-if="!!aiModalResult && !aiModalLoading">
                    <div class="ai-modal-section ai-modal-beauty">
                      <h4><span class="ai-icon">📊</span> 整体分析</h4>
                      <div class="ai-modal-block">
                        <div class="ai-row"><span class="ai-label">平均分：</span><span class="ai-value ai-score">{{ aiModalResult.overall_analysis?.average_score ?? '-' }}</span></div>
                        <div class="ai-row"><span class="ai-label">难度分析：</span><span class="ai-value">{{ aiModalResult.overall_analysis?.difficulty_analysis ?? '-' }}</span></div>
                        <div v-if="aiModalResult.overall_analysis?.common_issues && aiModalResult.overall_analysis.common_issues.length">
                          <span class="ai-label">常见问题：</span>
                          <ul class="ai-list ai-list-dot">
                            <li v-for="(issue, idx) in aiModalResult.overall_analysis.common_issues" :key="idx">{{ issue }}</li>
                          </ul>
                        </div>
                      </div>
                      <h4><span class="ai-icon">📚</span> 知识点分析</h4>
                      <div class="ai-modal-block">
                        <table class="ai-table ai-table-beauty" v-if="Array.isArray(aiModalResult.knowledge_points_analysis)">
                          <thead>
                            <tr>
                              <th>知识点</th>
                              <th>掌握情况</th>
                              <th>错误数</th>
                              <th>章节位置</th>
                              <th>提升建议</th>
                            </tr>
                          </thead>
                          <tbody>
                            <tr v-for="(kp, idx) in aiModalResult.knowledge_points_analysis" :key="idx">
                              <td><span class="ai-strong">{{ kp.point }}</span></td>
                              <td><span :class="['ai-tag', 'ai-tag-' + kp.mastery_level]">{{ kp.mastery_level }}</span></td>
                              <td>{{ kp.error_rate }}</td>
                              <td>{{ kp.chapter_location }}</td>
                              <td>
                                <ul class="ai-list ai-list-suggestion">
                                  <li v-for="(sug, i) in kp.improvement_suggestions" :key="i">{{ sug }}</li>
                                </ul>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                        <div v-else class="ai-empty">无知识点分析数据</div>
                      </div>
                      <h4><span class="ai-icon">💡</span> 教学建议</h4>
                      <div class="ai-modal-block">
                        <div v-if="aiModalResult.teaching_suggestions">
                          <div class="ai-row"><span class="ai-label">重点关注：</span>
                            <ul class="ai-list ai-list-dot">
                              <li v-for="(area, idx) in aiModalResult.teaching_suggestions.key_focus_areas" :key="idx">{{ area }}</li>
                            </ul>
                          </div>
                          <div class="ai-row"><span class="ai-label">推荐方法：</span>
                            <ul class="ai-list ai-list-dot">
                              <li v-for="(method, idx) in aiModalResult.teaching_suggestions.recommended_methods" :key="idx">{{ method }}</li>
                            </ul>
                          </div>
                          <div class="ai-row"><span class="ai-label">资源建议：</span>
                            <ul class="ai-list ai-list-dot">
                              <li v-for="(res, idx) in aiModalResult.teaching_suggestions.resource_suggestions" :key="idx">{{ res }}</li>
                            </ul>
                          </div>
                        </div>
                        <div v-else class="ai-empty">无教学建议数据</div>
                      </div>
                    </div>
                  </div>
                  <div v-else style="color:#c0392b;">无数据分支被命中（aiModalLoading/aiModalError/aiModalResult 均为假值）</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import AppSidebar from '@/components/layout/AppSidebar.vue';
import PracticeApi from '@/api/practice';
import { analyzeExercise } from '@/api/ai';

// Define the type for a practice object
interface Practice {
  id: number;
  title: string;
  start_time: string;
  end_time: string;
  course_name: string;
  class_id: number; // 班级ID，后端需返回
}
/*未使用
interface PracticeStats {
  submissionCount: number;
  averageScore: number | null;
}*/

interface PracticeWithStats extends Practice {
  submissionCount: number;
  averageScore: number | null;
  classTotal: number | null;
}

const loading = ref(false);
const error = ref<string | null>(null);
const practices = ref<Array<PracticeWithStats>>([]);
const selectedCourse = ref('');

// AI分析相关
const aiLoadingMap = ref<Record<number, boolean>>({});
const showAiModal = ref(false);
const aiModalLoading = ref(false);
const aiModalError = ref<string | null>(null);
const aiModalResult = ref<any>(null);

const analyzePractice = async (practice: PracticeWithStats) => {
  console.log('[AI弹窗] analyzePractice called', practice);
  aiLoadingMap.value[practice.id] = true;
  showAiModal.value = true;
  aiModalLoading.value = true;
  aiModalError.value = null;
  aiModalResult.value = null;
  try {
    const res = await analyzeExercise(practice.id);
    console.log('[AI弹窗] analyzeExercise 返回', res);
    // 兼容后端返回格式（修正：res 本身就是数据对象）
    let result = res;
    console.log('[AI弹窗] result', result);
    if (result && typeof result === 'object') {
      if ('data' in result && typeof result.data === 'object') {
        aiModalResult.value = result.data;
        console.log('[AI弹窗] 命中 result.data', result.data);
      } else if ('result' in result && typeof result.result === 'object') {
        aiModalResult.value = result.result;
        console.log('[AI弹窗] 命中 result.result', result.result);
      } else {
        aiModalResult.value = result;
        console.log('[AI弹窗] 直接赋值 result', result);
      }
    } else {
      aiModalResult.value = result;
      console.log('[AI弹窗] result 非对象，直接赋值', result);
    }
    console.log('[AI弹窗] 最终 aiModalResult.value', aiModalResult.value);
  } catch (e: any) {
    aiModalError.value = e?.message || 'AI分析失败';
    console.log('[AI弹窗] catch error', e);
  } finally {
    aiModalLoading.value = false;
    aiLoadingMap.value[practice.id] = false;
    console.log('[AI弹窗] finally aiModalLoading', aiModalLoading.value, 'aiModalError', aiModalError.value, 'aiModalResult', aiModalResult.value);
  }
};

const scoreRateLoadingMap = ref<Record<number, boolean>>({});
const updateScoreRate = async (practice: any) => {
  scoreRateLoadingMap.value[practice.id] = true;
  try {
    await PracticeApi.updateScoreRate(practice.id);
    alert('得分率统计成功！');
  } catch (e: any) {
    alert('得分率统计失败：' + (e?.message || '未知错误'));
  } finally {
    scoreRateLoadingMap.value[practice.id] = false;
  }
};

const uniqueCourses = computed(() => {
  const courses = practices.value.map(practice => practice.course_name);
  return Array.from(new Set(courses));
});

const filteredPractices = computed(() => {
  if (!selectedCourse.value) {
    return practices.value;
  }
  return practices.value.filter(practice => practice.course_name === selectedCourse.value);
});

const fetchTeacherPractices = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await PracticeApi.getTeacherPractices();
    const practiceList: Practice[] = response.data;
    // 并发获取所有练习的统计数据和班级总人数
    const statsList = await Promise.all(
      practiceList.map(async (practice) => {
        let submissionCount = 0;
        let averageScore = null;
        let classTotal = null;
        try {
          const statsRes = await PracticeApi.getPracticeStats(practice.id);
          const data = statsRes.data || {};
          submissionCount = data.total_submissions ?? 0;
          averageScore = data.average_score ?? null;
        } catch {}
        try {
          // 需要后端提供此API：/api/class/{classId}/student-count
          const classRes = await PracticeApi.getClassStudentCount(practice.class_id);
          classTotal = classRes.data?.total ?? null;
        } catch {}
        return {
          ...practice,
          submissionCount,
          averageScore,
          classTotal,
        };
      })
    );
    practices.value = statsList;
  } catch (err: any) {
    error.value = err.message || '加载练习信息失败';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchTeacherPractices();
});
</script>

<script lang="ts">
// json美化过滤器（可选）
import { defineComponent } from 'vue';
export default defineComponent({
  filters: {
    json(value: any) {
      try {
        return JSON.stringify(value, null, 2);
      } catch {
        return value;
      }
    }
  }
});
</script>

<style scoped>
.ai-table {
  width: 100%;
  border-collapse: collapse;
  margin: 0.5em 0 1em 0;
  font-size: 1em;
}
.ai-table th, .ai-table td {
  border: 1px solid #e0e0e0;
  padding: 0.4em 0.7em;
  text-align: left;
}
.ai-table th {
  background: #f5f7fa;
  color: #2d3a4a;
  font-weight: 600;
}
.ai-table tr:nth-child(even) {
  background: #fafbfc;
}
/* Updated styles for better aesthetics and added color changes */
.page-container {
  display: flex;
  height: 100vh;
  background-color: #f0f4f8; /* Changed background color for better contrast */
}

.content-container {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background-color: #ffffff;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  margin: 1rem;
}

.learning-records-analysis {
  font-size: 1rem;
  color: #333;
}

.filter-section {
  margin-bottom: 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.filter-section label {
  font-weight: bold;
  color: #555;
}

.filter-section select {
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 1rem;
  background-color: #fff;
  transition: border-color 0.3s;
}

.filter-section select:hover {
  border-color: #888;
}

.card-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(270px, 1fr));
  gap: 1.2rem;
  padding: 10px 0;
}

.practice-card {
  background: #fafbfc;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(60, 60, 60, 0.06);
  padding: 1.1rem 1rem 1rem 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
  transition: box-shadow 0.2s, transform 0.2s;
  border: 1px solid #ececec;
  position: relative;
}

.practice-card:hover {
  box-shadow: 0 6px 18px rgba(60, 60, 60, 0.13);
  transform: translateY(-2px) scale(1.01);
  border-color: #bcd4e6;
  background: #f5f7fa;
}

.practice-card h3 {
  font-size: 1.18rem;
  color: #2d3a4a;
  font-weight: 600;
  margin: 0 0 0.4rem 0;
  background: #f0f2f5;
  border-radius: 5px;
  padding: 0.4rem 0.7rem;
  border: 1px solid #e0e3e8;
  letter-spacing: 0.2px;
}

.practice-card p {
  font-size: 1.08rem;
  color: #444;
  margin: 0.08rem 0;
  line-height: 1.6;
  word-break: break-all;
  padding-left: 2px;
}

.practice-card p strong {
  color: #3b6ea5;
  font-weight: 500;
}

.loading-indicator {
  font-size: 1.2rem;
  color: #666;
  text-align: center;
  margin-top: 2rem;
}

.error-message {
  color: red;
  font-size: 1.2rem;
  text-align: center;
  margin-top: 2rem;
}

.ai-analyze-btn {
  margin-top: 1rem;
  padding: 0.48rem 1.2rem 0.48rem 1.2rem;
  background: linear-gradient(90deg, #1890ff 0%, #4fc3f7 100%);
  color: #fff;
  border: none;
  border-radius: 22px;
  cursor: pointer;
  font-size: 1.08rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(24,144,255,0.08);
  transition: background 0.3s, box-shadow 0.2s, transform 0.2s;
  outline: none;
  gap: 0.5em;
}
.ai-analyze-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  background: #bcd4e6;
  color: #fff;
}
.ai-analyze-btn:hover:not(:disabled) {
  background: linear-gradient(90deg, #40a9ff 0%, #1890ff 100%);
  box-shadow: 0 4px 16px rgba(24,144,255,0.13);
  transform: translateY(-1px) scale(1.03);
}
.ai-btn-icon {
  margin-right: 2px;
  vertical-align: middle;
}

.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  100% { transform: rotate(360deg); }
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.modal-close:hover {
  color: #666;
}

.modal-body {
  padding: 24px;
}

@media (max-width: 600px) {
  .card-container {
    grid-template-columns: 1fr;
    gap: 0.7rem;
    padding: 0;
  }
  .practice-card {
    padding: 0.7rem 0.4rem;
  }
  .practice-card h3 {
    font-size: 1rem;
    padding: 0.3rem 0.4rem;
  }
  .practice-card p {
    font-size: 0.98rem;
  }
}
</style>
