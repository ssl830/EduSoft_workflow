<script setup lang="ts">
import {ref, reactive, onMounted, watch} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import ExerciseApi from '../../api/exercise'
import {useAuthStore} from "../../stores/auth.ts";
import { ElMessage } from 'element-plus';

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const practiceId = Number(route.params.id)

const exercise = reactive({
  title: '',
  courseId: 0,
  classId: 0,
  startTime: '',
  endTime: '',
  allowMultipleSubmission: true,
  questions: [] as any[]
})

const loading = ref(false)
const error = ref('')
const currentStep = ref(1)

// 拉取练习详情
const fetchPracticeDetail = async () => {
  loading.value = true
  try {
    const res = await ExerciseApi.takeExercise(practiceId)
    const data = res.data
    exercise.title = data.title
    exercise.courseId = data.courseId
    exercise.classId = data.classId
    exercise.startTime = data.startTime
    exercise.endTime = data.endTime
    exercise.allowMultipleSubmission = data.allowMultipleSubmission
    exercise.questions = data.questions || []
  } catch (err) {
    error.value = '获取练习详情失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchPracticeDetail()
})

// 保存练习基本信息
const saveBasicInfo = async () => {
  loading.value = true
  try {
    await ExerciseApi.updateExercise(practiceId, {
      title: exercise.title,
      startTime: exercise.startTime,
      endTime: exercise.endTime,
      allowMultipleSubmission: exercise.allowMultipleSubmission
    })
    ElMessage.success('基本信息保存成功')
  } catch (err) {
    error.value = '保存失败，请稍后再试'
  } finally {
    loading.value = false
  }
}

// 修改题目分值
const updateQuestionScore = async (questionId: number, score: number) => {
  try {
    await ExerciseApi.updatePracticeQuestionScore(practiceId, questionId, score)
    ElMessage.success('分值已更新')
  } catch (err) {
    ElMessage.error('分值更新失败')
  }
}

// 删除题目
const removeQuestion = async (questionId: number) => {
  try {
    await ExerciseApi.removePracticeQuestion(practiceId, questionId)
    exercise.questions = exercise.questions.filter(q => q.id !== questionId)
    ElMessage.success('题目已移除')
  } catch (err) {
    ElMessage.error('移除失败')
  }
}

// 跳转题目编辑页
const editQuestion = (questionId: number) => {
  router.push({ name: 'QuestionEdit', params: { id: questionId } })
}

// 跳转添加题目页
const addQuestion = () => {
  router.push({ name: 'QuestionBank', query: { practiceId } })
}
</script>

<template>
  <div class="exercise-edit-container">
    <div class="edit-card">
      <h2>编辑练习</h2>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else>
        <div class="form-section">
          <label>标题</label>
          <input v-model="exercise.title" type="text" class="input" />
          <div class="row">
            <div class="col">
              <label>开始时间</label>
              <input v-model="exercise.startTime" type="datetime-local" class="input" />
            </div>
            <div class="col">
              <label>截止时间</label>
              <input v-model="exercise.endTime" type="datetime-local" class="input" />
            </div>
          </div>
          <label>允许多次提交</label>
          <select v-model="exercise.allowMultipleSubmission" class="input">
            <option :value="true">是</option>
            <option :value="false">否</option>
          </select>
          <button class="btn btn-primary" @click="saveBasicInfo">保存基本信息</button>
        </div>
        <hr class="divider" />
        <div class="question-section">
          <div class="q-header">
            <h3>题目列表</h3>
            <button class="btn btn-success" @click="addQuestion">+ 添加题目</button>
          </div>
          <table class="q-table">
            <thead>
              <tr>
                <th style="width:40px;">#</th>
                <th>题目内容</th>
                <th style="width:90px;">分值</th>
                <th style="width:180px;">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(q, idx) in exercise.questions" :key="q.id">
                <td>{{ idx + 1 }}</td>
                <td class="q-content">{{ q.content }}</td>
                <td>
                  <input type="number" v-model.number="q.score" @change="updateQuestionScore(q.id, q.score)" class="score-input" min="1" />
                </td>
                <td>
                  <button class="btn btn-outline" @click="editQuestion(q.id)">编辑题目</button>
                  <button class="btn btn-danger" @click="removeQuestion(q.id)">移除</button>
                </td>
              </tr>
              <tr v-if="!exercise.questions.length">
                <td colspan="4" class="empty">暂无题目，请添加</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div v-if="error" class="error-tip">{{ error }}</div>
    </div>
  </div>
</template>

<style scoped>
.exercise-edit-container {
  min-height: 100vh;
  background: #f6f8fa;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 40px 0;
}
.edit-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0,0,0,0.07);
  padding: 2.5rem 2.5rem 2rem 2.5rem;
  width: 100%;
  max-width: 700px;
}
.form-section label {
  display: block;
  margin-top: 1.2rem;
  font-weight: 500;
  color: #333;
}
.input {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 1rem;
  background: #fafbfc;
  transition: border 0.2s;
}
.input:focus {
  border-color: #409eff;
  outline: none;
}
.row {
  display: flex;
  gap: 1.5rem;
}
.col {
  flex: 1;
}
.btn {
  display: inline-block;
  padding: 0.45rem 1.2rem;
  border-radius: 6px;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  margin-right: 0.5rem;
  margin-top: 1.2rem;
  transition: background 0.2s, color 0.2s;
}
.btn-primary {
  background: #409eff;
  color: #fff;
}
.btn-primary:hover {
  background: #1976d2;
}
.btn-success {
  background: #67c23a;
  color: #fff;
}
.btn-success:hover {
  background: #4caf50;
}
.btn-danger {
  background: #f56c6c;
  color: #fff;
}
.btn-danger:hover {
  background: #c0392b;
}
.btn-outline {
  background: #fff;
  color: #409eff;
  border: 1px solid #409eff;
}
.btn-outline:hover {
  background: #e3f2fd;
}
.divider {
  border: none;
  border-top: 1px solid #e0e0e0;
  margin: 2rem 0 1.5rem 0;
}
.q-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.q-table {
  width: 100%;
  border-collapse: collapse;
  background: #fafbfc;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0,0,0,0.03);
}
.q-table th, .q-table td {
  padding: 0.75rem 1rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}
.q-table th {
  background: #f5f7fa;
  font-weight: 600;
  color: #495057;
}
.q-table tr:last-child td {
  border-bottom: none;
}
.q-content {
  max-width: 350px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.score-input {
  width: 60px;
  padding: 0.3rem 0.5rem;
  border-radius: 4px;
  border: 1px solid #e0e0e0;
  font-size: 1rem;
}
.empty {
  color: #aaa;
  text-align: center;
  padding: 1.5rem 0;
}
.loading {
  color: #409eff;
  font-size: 1.1rem;
  text-align: center;
  margin: 2rem 0;
}
.error-tip {
  color: #fff;
  background: #f56c6c;
  border-radius: 6px;
  padding: 0.75rem 1.2rem;
  margin-top: 1.5rem;
  text-align: center;
  font-weight: 500;
  letter-spacing: 1px;
}
@media (max-width: 800px) {
  .edit-card {
    padding: 1rem;
  }
  .q-content {
    max-width: 120px;
  }
}
</style>
