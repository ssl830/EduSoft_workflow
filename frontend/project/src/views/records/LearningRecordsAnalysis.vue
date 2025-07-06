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
              <div class="modal-container ai-modal-container">
                <!-- 固定标题 -->
                <div class="modal-header ai-modal-header fixed-header">
                  <h3>AI学情分析结果</h3>
                  <!-- 导出按钮靠右 -->
                  <button
                    class="ai-export-btn"
                    @click="exportAiAnalysisAsPDF"
                    :disabled="aiModalLoading || !aiModalResult || exportAiLoading"
                  >
                    <svg v-if="exportAiLoading" class="ai-export-icon spin" width="18" height="18" viewBox="0 0 50 50">
                      <circle cx="25" cy="25" r="20" fill="none" stroke="#ffffff" stroke-width="5" stroke-linecap="round" stroke-dasharray="31.4 31.4" />
                    </svg>
                    <svg v-else class="ai-export-icon" width="18" height="18" viewBox="0 0 24 24"><path fill="#ffffff" d="M5 20h14a1 1 0 0 0 1-1v-6h-2v5H6v-5H4v6a1 1 0 0 0 1 1zm7-2v-8.59l3.29 3.3a1 1 0 1 0 1.42-1.42l-5-5a1 1 0 0 0-1.42 0l-5 5a1 1 0 1 0 1.42 1.42L11 9.41V18a1 1 0 0 0 2 0z"/></svg>
                    <span style="margin-left:6px;">导出</span>
                  </button>
                  <button class="modal-close" @click="showAiModal = false">×</button>
                </div>
                <div class="modal-body ai-modal-body">
                  <div v-if="aiModalLoading" class="ai-modal-loading"><span class="ai-loading-spinner"></span> 分析中...</div>
                  <div v-else-if="aiModalError" class="ai-modal-error">{{ aiModalError }}</div>
                  <div v-else-if="!!aiModalResult && !aiModalLoading">
                    <div class="ai-modal-section ai-modal-beauty" id="ai-analysis-pdf-content">
                      <h4><span class="ai-icon">📊</span> <span class="ai-section-title">整体分析</span></h4>
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
                      <h4><span class="ai-icon">📚</span> <span class="ai-section-title">知识点分析</span></h4>
                      <div class="ai-modal-block">
                        <table class="ai-table ai-table-beauty" v-if="Array.isArray(aiModalResult.knowledge_points_analysis)">
                          <thead>
                            <tr>
                              <th>知识点</th>
                              <th>掌握情况</th>
                              <th class="nowrap-th">错误数</th>
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
                      <h4><span class="ai-icon">💡</span> <span class="ai-section-title">教学建议</span></h4>
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
    <div v-if="exportAiGlobalLoading" class="global-loading-mask">
        <div class="global-loading-spinner"></div>
        <div class="global-loading-text">正在导出，请稍候...</div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import AppSidebar from '@/components/layout/AppSidebar.vue';
import PracticeApi from '@/api/practice';
import { analyzeExercise } from '@/api/ai';
import { ElMessage } from 'element-plus';
// 新增：引入 jsPDF
import jsPDF from 'jspdf';

const exportAiLoading = ref(false);

interface Practice {
  id: number;
  title: string;
  start_time: string;
  end_time: string;
  course_name: string;
  class_id: number; // 班级ID，后端需返回
}

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
  await updateScoreRate(practice);
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
        // 新增：将 overall_analysis.average_score 赋值为 practice.averageScore.toFixed(2)
        if (
          aiModalResult.value.overall_analysis &&
          practice.averageScore !== null &&
          practice.averageScore !== undefined
        ) {
          aiModalResult.value.overall_analysis.average_score = practice.averageScore.toFixed(2);
        }
        console.log('[AI弹窗] 命中 result.data', result.data);
      } else if ('result' in result && typeof result.result === 'object') {
        aiModalResult.value = result.result;
        if (
          aiModalResult.value.overall_analysis &&
          practice.averageScore !== null &&
          practice.averageScore !== undefined
        ) {
          aiModalResult.value.overall_analysis.average_score = practice.averageScore.toFixed(2);
        }
        console.log('[AI弹窗] 命中 result.result', result.result);
      } else {
        aiModalResult.value = result;
        if (
          aiModalResult.value.overall_analysis &&
          practice.averageScore !== null &&
          practice.averageScore !== undefined
        ) {
          aiModalResult.value.overall_analysis.average_score = practice.averageScore.toFixed(2);
        }
        console.log('[AI弹窗] 直接赋值 result', result);
      }
    } else {
      aiModalResult.value = result;
      if (
        aiModalResult.value.overall_analysis &&
        practice.averageScore !== null &&
        practice.averageScore !== undefined
      ) {
        aiModalResult.value.overall_analysis.average_score = practice.averageScore.toFixed(2);
      }
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
    ElMessage.success('得分率统计成功！');
  } catch (e: any) {
    ElMessage.error('得分率统计失败：' + (e?.message || '未知错误'));
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
const exportAiGlobalLoading = ref(false);
// 导出AI学情分析结果为PDF
const exportAiAnalysisAsPDF = async () => {
  if (!aiModalResult.value) {
    ElMessage.error('无AI学情分析内容，无法导出');
    return;
  }
  exportAiLoading.value = true;
  exportAiGlobalLoading.value = true; // 显示全局 loading

  // 使用 setTimeout 将导出逻辑放入异步任务队列，避免阻塞主线程
  setTimeout(async () => {
    try {
      const doc = new jsPDF('p', 'mm', 'a4');
      const marginLeft = 15;
      const marginTop = 20;
      const lineHeight = 8;
      const pageWidth = doc.internal.pageSize.getWidth();
      const pageHeight = doc.internal.pageSize.getHeight();
      let cursorY = marginTop;

      // 字体
      const fontName = 'SourceHanSerifSC';
      const fontUrl = '/src/assets/fonts/SourceHanSerifSC-VF.ttf';
      if (!doc.getFontList()[fontName]) {
        const fontData = await fetch(fontUrl).then(res => res.arrayBuffer());
        doc.addFileToVFS(`${fontName}.ttf`, arrayBufferToBase64(fontData));
        doc.addFont(`${fontName}.ttf`, fontName, 'normal');
      }
      doc.setFont(fontName, 'normal');
      doc.setFontSize(12);

      const maxTextWidth = pageWidth - 2 * marginLeft;
      const addTextWithWrapping = (text: string) => {
        const lines = doc.splitTextToSize(text, maxTextWidth);
        lines.forEach((line: string) => {
          if (cursorY > pageHeight - marginTop) {
            doc.addPage();
            cursorY = marginTop;
          }
          doc.text(line, marginLeft, cursorY);
          cursorY += lineHeight;
        });
      };

      // 整体分析
      addTextWithWrapping('AI学情分析结果');
      cursorY += lineHeight;
      addTextWithWrapping('【整体分析】');
      const overall = aiModalResult.value.overall_analysis || {};
      addTextWithWrapping(`平均分：${overall.average_score ?? '-'}`);
      addTextWithWrapping(`难度分析：${overall.difficulty_analysis ?? '-'}`);
      if (overall.common_issues && overall.common_issues.length) {
        addTextWithWrapping('常见问题：');
        overall.common_issues.forEach((issue: string) => addTextWithWrapping('  - ' + issue));
      }
      cursorY += lineHeight;

      // 知识点分析
      addTextWithWrapping('【知识点分析】');
      const kpArr = Array.isArray(aiModalResult.value.knowledge_points_analysis)
        ? aiModalResult.value.knowledge_points_analysis : [];
      if (kpArr.length) {
        addTextWithWrapping('知识点 | 掌握情况 | 错误数 | 章节位置 | 提升建议');
        kpArr.forEach((kp: any) => {
          let sug = Array.isArray(kp.improvement_suggestions)
            ? kp.improvement_suggestions.join('；') : (kp.improvement_suggestions || '-');
          addTextWithWrapping(
            `${kp.point} | ${kp.mastery_level} | ${kp.error_rate} | ${kp.chapter_location} | ${sug}`
          );
        });
      } else {
        addTextWithWrapping('无知识点分析数据');
      }
      cursorY += lineHeight;

      // 教学建议
      addTextWithWrapping('【教学建议】');
      const ts = aiModalResult.value.teaching_suggestions || {};
      if (ts.key_focus_areas && ts.key_focus_areas.length) {
        addTextWithWrapping('重点关注：');
        ts.key_focus_areas.forEach((area: string) => addTextWithWrapping('  - ' + area));
      }
      if (ts.recommended_methods && ts.recommended_methods.length) {
        addTextWithWrapping('推荐方法：');
        ts.recommended_methods.forEach((m: string) => addTextWithWrapping('  - ' + m));
      }
      if (ts.resource_suggestions && ts.resource_suggestions.length) {
        addTextWithWrapping('资源建议：');
        ts.resource_suggestions.forEach((r: string) => addTextWithWrapping('  - ' + r));
      }
      doc.save('AI学情分析.pdf');
      ElMessage.success('导出成功');
    } catch (err) {
      console.error('导出PDF失败:', err);
      ElMessage.error('导出失败，请稍后再试');
    } finally {
      exportAiLoading.value = false;
      exportAiGlobalLoading.value = false; // 隐藏全局 loading
    }
  }, 0);
};

// 辅助函数：ArrayBuffer转Base64
function arrayBufferToBase64(buffer: ArrayBuffer) {
  let binary = '';
  const bytes = new Uint8Array(buffer);
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return window.btoa(binary);
}
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
.global-loading-mask {
    position: fixed;
    z-index: 2000;
    top: 0; left: 0; right: 0; bottom: 0;
    background: rgba(255,255,255,0.7);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}
.global-loading-spinner {
    border: 6px solid #e3eaf2;
    border-top: 6px solid #1976d2;
    border-radius: 50%;
    width: 54px;
    height: 54px;
    animation: spin 1s linear infinite;
    margin-bottom: 18px;
}
.global-loading-text {
    color: #1976d2;
    font-size: 1.2rem;
    font-weight: 500;
    letter-spacing: 1px;
}
@keyframes spin {
    100% { transform: rotate(360deg); }
}

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

.ai-modal-container {
  background: #fafdff;
  border-radius: 16px;
  width: 98%;
  max-width: 900px;
  max-height: 92vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(24, 144, 255, 0.18), 0 2px 8px rgba(60, 60, 60, 0.08);
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  padding-bottom: 8px;
  animation: modal-pop 0.25s;
}

@keyframes modal-pop {
  0% { transform: scale(0.95); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}

.ai-modal-header {
  padding: 22px 36px 12px 36px;
  border-bottom: 1px solid #e3eaf2;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(90deg, #e3f0fc 0%, #fafdff 100%);
  border-radius: 16px 16px 0 0;
}

.ai-modal-header h3 {
  margin: 0;
  color: #1a355b;
  font-size: 1.45rem;
  font-weight: 700;
  letter-spacing: 1px;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

.ai-modal-body {
  padding: 28px 36px 24px 36px;
  font-size: 1.08rem;
  color: #2d3a4a;
  background: #fafdff;
  border-radius: 0 0 16px 16px;
}

.ai-modal-section {
  margin-bottom: 2.2em;
}

.ai-section-title {
  font-size: 1.18rem;
  font-weight: 600;
  color: #1976d2;
  margin-left: 2px;
  letter-spacing: 0.5px;
}

.ai-modal-block {
  background: #f5f7fa;
  border-radius: 10px;
  padding: 1.1em 1.2em 1.1em 1.2em;
  margin: 0.7em 0 1.2em 0;
  box-shadow: 0 2px 8px rgba(60, 60, 60, 0.04);
  font-size: 1.05rem;
}

.ai-row {
  margin-bottom: 0.7em;
  display: flex;
  align-items: flex-start;
  gap: 0.5em;
  flex-wrap: wrap;
}

.ai-label {
  color: #1976d2;
  font-weight: 500;
  min-width: 5em;
  display: inline-block;
}

.ai-value {
  color: #333;
  font-weight: 500;
}

.ai-score {
  color: #43a047;
  font-size: 1.13em;
  font-weight: 700;
}

.ai-list-dot {
  padding-left: 1.2em;
  margin: 0.2em 0;
  color: #444;
  font-size: 1em;
}
.ai-list-dot li {
  list-style: disc;
  margin-bottom: 0.1em;
}

.ai-list-suggestion {
  padding-left: 1.2em;
  margin: 0.2em 0;
  color: #1976d2;
  font-size: 0.98em;
}
.ai-list-suggestion li {
  list-style: circle;
  margin-bottom: 0.1em;
  word-break: break-all;
}

.ai-table-beauty {
  font-size: 1.04em;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(24,144,255,0.06);
  background: #fff;
  margin-bottom: 0.5em;
}
.ai-table-beauty th, .ai-table-beauty td {
  padding: 0.6em 0.9em;
  border: 1px solid #e0e0e0;
  font-size: 1em;
  vertical-align: top;
}
.ai-table-beauty th {
  background: #e3f0fc;
  color: #1976d2;
  font-weight: 600;
  letter-spacing: 0.5px;
}
.ai-table-beauty tr:nth-child(even) {
  background: #fafdff;
}
.ai-table-beauty tr:nth-child(odd) {
  background: #f5f7fa;
}
.ai-table-beauty td ul {
  margin: 0;
  padding-left: 1.1em;
}
.ai-table-beauty td ul li {
  font-size: 0.98em;
  color: #1976d2;
  margin-bottom: 0.1em;
  word-break: break-all;
}

.ai-tag {
  display: inline-block;
  padding: 0.18em 0.7em;
  border-radius: 12px;
  font-size: 0.98em;
  font-weight: 500;
  background: #e3f0fc;
  color: #1976d2;
  margin-right: 0.2em;
  letter-spacing: 0.5px;
}
.ai-tag-高 { background: #e8f5e9; color: #43a047; }
.ai-tag-中 { background: #fffde7; color: #fbc02d; }
.ai-tag-低 { background: #ffebee; color: #e53935; }

.ai-strong {
  font-weight: 600;
  color: #1976d2;
}

.ai-icon {
  font-size: 1.15em;
  margin-right: 0.3em;
  vertical-align: middle;
}

.ai-modal-loading {
  color: #1976d2;
  font-size: 1.15em;
  text-align: center;
  padding: 2em 0;
  font-weight: 500;
  letter-spacing: 1px;
}

.ai-modal-error {
  color: #e53935;
  font-size: 1.1em;
  text-align: center;
  padding: 2em 0;
  font-weight: 500;
}

.ai-empty {
  color: #888;
  font-size: 1em;
  text-align: center;
  padding: 1.2em 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  transition: color 0.2s;
}
.modal-close:hover {
  color: #1976d2;
}

.ai-table-beauty th.nowrap-th {
  white-space: nowrap;
}

/* 固定标题样式 */
.fixed-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: linear-gradient(90deg, #e3f0fc 0%, #fafdff 100%);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 导出按钮样式调整 */
.ai-export-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(90deg, #1976d2 0%, #42a5f5 100%);
  color: #ffffff;
  border: none;
  border-radius: 20px;
  padding: 0.38em 1.2em;
  font-size: 1.02rem;
  font-weight: 500;
  margin-left: auto; /* 靠右对齐 */
  margin-right: 15px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.08);
  transition: background 0.2s, box-shadow 0.2s, transform 0.2s, color 0.2s;
  outline: none;
  gap: 0.4em;
}
.ai-export-btn:disabled {
  background: #bcd4e6;
  color: #e3eaf2;
  cursor: not-allowed;
  opacity: 0.7;
  box-shadow: none;
}
.ai-export-btn:not(:disabled):hover {
  background: linear-gradient(90deg, #42a5f5 0%, #1976d2 100%);
  color: #ffffff;
  transform: translateY(-1px) scale(1.03);
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.13);
}
.ai-export-icon {
  vertical-align: middle;
  margin-right: 6px;
}
</style>
