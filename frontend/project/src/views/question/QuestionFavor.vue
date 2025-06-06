<script setup lang="ts">
import {ref, onMounted, watch} from 'vue'
import QuestionApi from '../../api/question.ts'
import {useAuthStore} from "../../stores/auth.ts";
import ExerciseApi from "../../api/exercise.ts";

const authStore = useAuthStore()

// const isTeacher = authStore.userRole === 'teacher'

const questions = ref<any[]>([])
const loading = ref(true)
const error = ref('')

// Fetch questions
const fetchQuestions = async () => {
    loading.value = true
    error.value = ''

    try {
        const response = await QuestionApi.getFavorQuestionList()
        console.log('收藏题目响应:', response)
        
        if (response.code === 200 && response.data) {
            questions.value = Array.isArray(response.data) ? response.data : []
            console.log('收藏题目数据:', questions.value)
        } else {
            error.value = response.message || '获取收藏题目失败'
            console.error('获取收藏题目失败:', response)
            questions.value = []
        }
    } catch (err) {
        error.value = '获取收藏题目失败，请稍后再试'
        console.error('获取收藏题目错误:', err)
        questions.value = []
    } finally {
        loading.value = false
    }
}

// 在onMounted中添加
onMounted(() => {
    fetchQuestions()
})

// 新增弹窗相关状态
const showDetailDialog = ref(false)
const selectedQuestion = ref<any>(null)

// 修改预览方法
const showQuestionDetail = (question: any) => {
    selectedQuestion.value = question
    showDetailDialog.value = true
}

const enFavorQuestion = async (questionId: string) => {
    try {
        const response = await ExerciseApi.enFavouriteQuestions(questionId)
        if (response.code === 200) {
            await fetchQuestions()
        } else {
            error.value = response.message || '取消收藏失败'
        }
    } catch (err) {
        error.value = '取消收藏失败，请稍后再试'
        console.error('取消收藏错误:', err)
    }
}

// 新增答案格式化方法
const formatAnswer = (question: any) => {
    if (!question) return '-'
    if (question.type === 'multiple_choice') {
        return question.answer.split(',').join(', ')
    }
    return question.answer || '-'
}

onMounted(() => {
    fetchQuestions()
})


</script>

<template>
    <div class="question-bank-container">
        <div class="resource-header">
            <h2>收藏题库</h2>
        </div>

        <!-- Resource List -->
        <div v-if="loading" class="loading-container">加载中...</div>
        <div v-else-if="error" class="error-message">{{ error }}</div>
        <div v-else-if="questions.length === 0" class="empty-state">
            暂无收藏题目
        </div>
        <div v-else class="resource-table-wrapper">
            <table class="resource-table">
                <thead>
                <tr>
                    <th>题目内容</th>
                    <th>所属课程</th>
                    <th>所属章节</th>
                    <th>所属练习</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="question in questions" :key="question.id">
                    <td>{{ question.content }}</td>
                    <td>{{ question.course_name }}</td>
                    <td>{{ question.section_title || '-' }}</td>
                    <td>{{ question.practice_title || '-' }}</td>
                    <td class="actions">
                        <button
                            class="btn-action preview"
                            @click="showQuestionDetail(question)"
                            title="查看"
                        >
                            查看
                        </button>
                        <button
                            class="btn-action download"
                            @click="enFavorQuestion(question.id)"
                            title="取消收藏"
                        >
                            取消收藏
                        </button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <!-- 题目详情弹窗 -->
        <div v-if="showDetailDialog" class="dialog-overlay" @click="showDetailDialog = false">
            <div class="dialog-content" @click.stop>
                <div class="dialog-header">
                    <h3>题目详情</h3>
                    <button class="close-btn" @click="showDetailDialog = false">&times;</button>
                </div>
                <div class="dialog-body" v-if="selectedQuestion">
                    <div class="question-info">
                        <p><strong>题目内容：</strong>{{ selectedQuestion.content }}</p>
                        <p><strong>所属课程：</strong>{{ selectedQuestion.course_name }}</p>
                        <p><strong>所属章节：</strong>{{ selectedQuestion.section_title || '-' }}</p>
                        <p><strong>所属练习：</strong>{{ selectedQuestion.practice_title || '-' }}</p>
                        <p><strong>题目类型：</strong>{{ selectedQuestion.type }}</p>
                        <p><strong>选项：</strong>{{ selectedQuestion.options }}</p>
                        <p><strong>答案：</strong>{{ formatAnswer(selectedQuestion) }}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.question-bank-container {
    padding: 20px;
}

.resource-header {
    margin-bottom: 20px;
}

.resource-header h2 {
    color: #333;
    font-size: 24px;
}

.loading-container,
.empty-state,
.error-message {
    text-align: center;
    padding: 40px;
    background: #f5f5f5;
    border-radius: 8px;
    margin: 20px 0;
    color: #666;
}

.error-message {
    color: #ff4d4f;
    background: #fff2f0;
}

.resource-table-wrapper {
    overflow-x: auto;
}

.resource-table {
    width: 100%;
    border-collapse: collapse;
    background: white;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.resource-table th,
.resource-table td {
    padding: 12px 16px;
    text-align: left;
    border-bottom: 1px solid #e8e8e8;
}

.resource-table th {
    background: #fafafa;
    font-weight: 500;
}

.actions {
    display: flex;
    gap: 8px;
}

.btn-action {
    padding: 6px 12px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s;
}

.btn-action.preview {
    background: #1890ff;
    color: white;
}

.btn-action.download {
    background: #ff4d4f;
    color: white;
}

.btn-action:hover {
    opacity: 0.8;
}

/* 弹窗样式 */
.dialog-overlay {
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

.dialog-content {
    background: white;
    border-radius: 8px;
    width: 80%;
    max-width: 600px;
    max-height: 80vh;
    overflow-y: auto;
}

.dialog-header {
    padding: 16px;
    border-bottom: 1px solid #e8e8e8;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.dialog-header h3 {
    margin: 0;
    color: #333;
}

.close-btn {
    background: none;
    border: none;
    font-size: 24px;
    cursor: pointer;
    color: #999;
}

.dialog-body {
    padding: 16px;
}

.question-info p {
    margin: 8px 0;
    line-height: 1.5;
}

.question-info strong {
    color: #333;
    margin-right: 8px;
}
</style>
