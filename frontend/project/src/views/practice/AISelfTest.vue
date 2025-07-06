<template>
  <div class="ai-selftest-page" ref="pageTop">
    <h1 class="main-title">AI 自测练习生成</h1>

    <div class="form-section">
      <label class="form-label-lg">练习要求（题型、数量、难度等）</label>
      <textarea v-model="form.requirements" placeholder="例如：选择题3道，填空题1道，难度 medium" />
    </div>

    <div class="form-section">
      <label class="form-label-lg">知识点偏好</label>
      <textarea v-model="form.knowledge_preferences" placeholder="例如：进程调度、同步机制" />
    </div>

    <!-- 预留错题列表输入，可选 -->
    <!-- <div class="form-section">
      <label>错题（JSON 数组，可选）</label>
      <textarea v-model="form.wrongQuestionsJson" placeholder="[{\"type\":\"singlechoice\", ...}]" />
    </div> -->

    <div class="button-group">
      <button class="btn primary" :disabled="loading" @click="generate">
        {{ loading ? '生成中...' : '生成练习' }}
      </button>
      <button
        v-if="exercises.length"
        class="btn"
        @click="saveProgress"
        :disabled="saveLoading"
      >{{ saveLoading ? '保存中...' : '暂存进度' }}</button>
      <button
        v-if="exercises.length"
        class="btn success"
        @click="submitPractice"
        :disabled="submitLoading"
      >{{ submitLoading ? '提交中...' : '提交练习' }}</button>
    </div>

    <div v-if="loading" class="progress-wrapper">
      <div class="progress-bar"></div>
    </div>

    <div v-if="error" class="error-msg">{{ error }}</div>

    <div v-if="exercises.length" class="exercise-list">
      <h2 class="result-title">生成结果</h2>
      <div v-for="(item, idx) in exercises" :key="idx" class="exercise-item">
        <div class="exercise-question">
          <span class="type-tag" :class="'type-' + (item.type || '').toLowerCase()">
            {{ typeLabel(item.type) }}
          </span>
          <span class="question-text">{{ idx + 1 }}. {{ item.question }}</span>
        </div>
        <ul v-if="item.type && item.type.toLowerCase() === 'singlechoice' && item.options && item.options.length">
          <li v-for="(opt, oIdx) in item.options" :key="oIdx">
            {{ String.fromCharCode(65 + oIdx) + '. ' + opt }}
          </li>
        </ul>
        <ul v-else-if="item.options && item.options.length">
          <li v-for="(opt, oIdx) in item.options" :key="oIdx">{{ opt }}</li>
        </ul>
        <input
          v-model="answers[idx].answer"
          :placeholder="inputPlaceholder(item.type)"
          class="answer-input"
        />
        <div v-if="resultDetails.length" class="result-detail">
          <span :class="resultDetails[idx].correct ? 'correct' : 'wrong'">
            {{ resultDetails[idx].correct ? '✔ 正确' : '✘ 错误' }}，得分 {{ resultDetails[idx].score }}/{{ resultDetails[idx].maxScore }}
          </span>
          <p v-if="resultDetails[idx].feedback">反馈：{{ resultDetails[idx].feedback }}</p>
          <p class="correct-ans" v-if="resultDetails[idx].correctAnswer">正确答案：{{ resultDetails[idx].correctAnswer }}</p>
        </div>
      </div>
      <div v-if="result.totalScore !== undefined" class="total-score">总得分：{{ result.totalScore }}/{{ result.totalPossible }}</div>
      <button class="btn" style="margin: 1.5rem auto 0; display: block;" @click="scrollToTop">回到顶部</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { generateStudentExercise, StudentExerciseRequest, StudentExerciseResponse, ExerciseItem, submitSelfPractice } from '@/api/ai'
import { ElMessage } from 'element-plus'

const pageTop = ref<HTMLElement | null>(null)

const form = reactive<StudentExerciseRequest>({
  requirements: '',
  knowledge_preferences: '',
  wrongQuestions: []
})

// const wrongQuestionsJson = ref('')

const loading = ref(false)
const error = ref('')
const exercises = ref<ExerciseItem[]>([])

// 保存答案数组，用于暂存与提交。结构：[{questionId, answer, type}]
const answers = ref<any[]>([])
const result = reactive<any>({})
const resultDetails = ref<any[]>([])
const practiceId = ref<number | null>(null)
const saveLoading = ref(false)
const submitLoading = ref(false)

async function generate() {
  error.value = ''
  exercises.value = []
  if (!form.requirements.trim()) {
    error.value = '请填写练习要求'
    return
  }
  loading.value = true
  try {
    // 如果用户手动输入错题 JSON，可在此解析
    // if (wrongQuestionsJson.value.trim()) {
    //   form.wrongQuestions = JSON.parse(wrongQuestionsJson.value)
    // }
    const res = await generateStudentExercise(form) as unknown as StudentExerciseResponse & { practiceId?: number }
    exercises.value = res.data.exercises || []
    // 初始化答案结构
    answers.value = exercises.value.map(q => ({ questionId: q.id || 0, answer: '', type: q.type, correctAnswer: q.answer }))
    if(res.data.practiceId){
      practiceId.value = res.data.practiceId
    } else {
      practiceId.value = null
    }
  } catch (e: any) {
    error.value = e?.message || '生成失败'
  } finally {
    loading.value = false
  }
}

async function saveProgress() {
  saveLoading.value = true
  try {
    localStorage.setItem('aiSelfTestProgress', JSON.stringify(answers.value))
    ElMessage.success('进度已保存')
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function submitPractice() {
  submitLoading.value = true
  try {
    const res = await submitSelfPractice({ practiceId: practiceId.value || 0, answers: answers.value }) as unknown as any
    result.totalScore = res.data.totalScore
    result.totalPossible = res.data.totalPossible
    resultDetails.value = res.data.details || []
    ElMessage.success('提交成功')
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    submitLoading.value = false
  }
}

// 题型标签映射（仅保留指定四种类型）
function typeLabel(type: string) {
  switch ((type || '').toLowerCase()) {
    case 'singlechoice':
      return '选择题'
    case 'fillblank':
      return '填空题'
    case 'judge':
      return '判断题'
    case 'program':
      return '简答题'
    default:
      return type || '题目'
  }
}

// 回到顶部
function scrollToTop() {
  if (pageTop.value) {
    pageTop.value.scrollIntoView({ behavior: 'smooth' })
  } else {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// 输入框 placeholder 根据题型变化
function inputPlaceholder(type: string) {
  switch ((type || '').toLowerCase()) {
    case 'judge':
      return "填写‘正确’或‘错误’"
    case 'singlechoice':
      return "例：A"
    default:
      return "请输入答案"
  }
}
</script>

<style scoped>
.ai-selftest-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem 1rem;
}
.main-title {
  font-size: 2.3rem;
  font-weight: 800;
  text-align: center;
  margin-bottom: 2.2rem;
  letter-spacing: 1px;
  color: #1976d2;
}
.result-title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 1.2rem;
  color: #333;
  text-align: left;
}
.button-group {
  display: flex;
  gap: 10px;
  margin-bottom: 1.5rem;
}
.form-section {
  margin-bottom: 1.5rem;
}
.form-section label {
  display: block;
  font-weight: 500;
  margin-bottom: 0.5rem;
}
.form-label-lg {
  font-size: 1.18rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  display: block;
}
textarea {
  width: 100%;
  min-height: 96px;
  padding: 0.75rem 1rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05) inset;
  resize: vertical;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  background: var(--bg-secondary, #fafafa);
}
textarea:focus {
  outline: none;
  border-color: var(--primary, #1976d2);
  box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.15);
}
.btn {
  cursor: pointer;
  border: none;
  padding: 0.6rem 1.25rem;
  font-size: 0.95rem;
  border-radius: 6px;
  transition: background 0.15s ease, box-shadow 0.15s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}
.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn.primary {
  background: var(--primary, #1976d2);
  color: #fff;
}
.btn.primary:hover:not(:disabled) {
  background: var(--primary-dark, #1565c0);
}
.btn.success {
  background: var(--success, #28a745);
  color: #fff;
}
.btn.success:hover:not(:disabled) {
  background: #218838;
}
.exercise-list {
  margin-top: 2rem;
}
.exercise-item {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
  padding: 1.25rem 1rem;
  border-radius: 8px;
}
.exercise-question {
  font-size: 1.18rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.7em;
}
.type-tag {
  display: inline-block;
  font-size: 0.92rem;
  font-weight: 500;
  padding: 0.12em 0.8em;
  border-radius: 1em;
  margin-right: 0.5em;
  background: #e3f2fd;
  color: #1976d2;
  border: 1px solid #90caf9;
  vertical-align: middle;
  letter-spacing: 1px;
  white-space: nowrap; /* 保证type标签永远单行 */
}
.type-singlechoice {
  background: #e3f2fd;
  color: #1976d2;
  border-color: #90caf9;
}
.type-fillblank {
  background: #fffde7;
  color: #fbc02d;
  border-color: #ffe082;
}
.type-judge {
  background: #e8f5e9;
  color: #388e3c;
  border-color: #a5d6a7;
}
.type-program {
  background: #fbe9e7;
  color: #d84315;
  border-color: #ffab91;
}
.question-text {
  font-size: 1.13em;
  font-weight: 500;
  color: #222;
}
.exercise-item ul {
  padding-left: 1.25rem;
  margin-bottom: 0.75rem;
}
.answer-input {
  width: 100%;
  padding: 0.6rem 0.8rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  transition: border-color 0.2s ease;
  background: var(--bg-secondary, #fafafa);
}
.answer-input:focus {
  outline: none;
  border-color: var(--primary, #1976d2);
}
.result-detail {
  margin-top: 0.6rem;
  font-size: 0.95rem;
}
.correct {
  color: var(--success, green);
}
.wrong {
  color: var(--error, red);
}
.total-score {
  font-size: 1.4rem;
  font-weight: 600;
  margin-top: 1.5rem;
  text-align: center;
}
.progress-wrapper {
  width: 100%;
  background: var(--bg-tertiary, #e0e0e0);
  height: 6px;
  margin-top: 1rem;
  overflow: hidden;
  border-radius: 3px;
}
.progress-bar {
  width: 100%;
  height: 100%;
  background: var(--primary, #1976d2);
  animation: progress 1.2s linear infinite;
  transform-origin: left;
}
@keyframes progress {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}
.error-msg {
  color: var(--error, red);
  margin-top: 1rem;
  text-align: center;
}
.correct-ans {
  color: #888;
  font-size: 0.9rem;
  margin-top: 0.25rem;
}
</style>
