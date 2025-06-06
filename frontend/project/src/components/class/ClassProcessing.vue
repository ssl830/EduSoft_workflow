<script setup lang="ts">
import {ref, onMounted, watch, computed} from 'vue'
import { defineProps } from 'vue'
import ExerciseApi from '../../api/exercise'
import {useAuthStore} from "../../stores/auth.ts";
import router from "../../router";

const props = defineProps<{
    classId: string;
    isTeacher: boolean;
}>()

const practices = ref<any[]>([])
const loading = ref(true)
const error = ref('')

// Filters
const selectedChapter = ref('')
const selectedType = ref('')
const selectedExer = ref(-1)

// Chapters and types (will be populated from resources)
const names = ref([])
const practicesTypes = ref([
    { value: 'unFinished', label: '未完成' },
    { value: 'unScored', label: '待批改' },
    { value: 'Scored', label: '已批改' },
    { value: 'overdue', label: '已过期' },
])
const practicesName = ref([])

const authStore = useAuthStore()
const isTutor = computed(() => authStore.userRole === 'tutor')
const isStudent = computed(() => authStore.userRole === 'student')

// Fetch practices
const fetchPractices = async () => {
    loading.value = true
    error.value = ''
    const studentName = ref('')
    if(isStudent.value){
        studentName.value = authStore.user?.username;
    }

    try {
        const response = await ExerciseApi.getClassExercises(props.classId, {
            practice_id: selectedExer.value,
            type: selectedType.value,
            name: studentName.value,
        })
        console.log('练习列表响应:', response)
        
        if (response.code === 200 && response.data) {
            // 更新数据结构处理
            practices.value = Array.isArray(response.data) ? response.data : []
            console.log('练习列表数据:', practices.value)

            // 提取唯一值（显示名称用exercise_name，值用exercise_id）
            const practiceSet = new Map()
            practices.value.forEach((ex: any) => {
                if (ex.id && ex.title) {
                    practiceSet.set(ex.id, ex.title)
                }
            })

            // 生成包含id和name的数组
            practicesName.value = Array.from(practiceSet, ([id, title]) => ({id, title}))

            const namesSet = new Set(practices.value.map((r: any) => r.name).filter(Boolean))
            names.value = Array.from(namesSet)
        } else {
            error.value = response.message || '获取练习列表失败'
            console.error('获取练习列表失败:', response)
            practices.value = []
        }
    } catch (err) {
        error.value = '获取练习列表失败，请稍后再试'
        console.error('获取练习列表错误:', err)
        practices.value = []
    } finally {
        loading.value = false
    }
}

const getPracticeTypeLabel = (type) => {
    switch (type) {
        case 'unFinished':
            return '未完成';
        case 'unScored':
            return '待批改';
        case 'scored':
            return '已批改';
        case 'overdue':
            return '已过期';
        default:
            return '-';
    }
};

const downloadPracticeReport = async () => {
// TODO:
}

// Preview resource
const doPractice = (practiceId) => {
    router.push({
        name: 'TakeExercise',
        params: { id: practiceId }
    })
}

const checkPractice = (practiceId, submitId) => {
    router.push({
        name: 'CheckExercise',
        params: { practiceId: practiceId, submissionId: submitId }
    })
}

// Download resource
const getPracticeReport = async (practiceId, submissionId) => {
    router.push({
        name: 'ExerciseFeedback',
        params: { practiceId: practiceId, submissionId: submissionId }
    })
}

const exportReport = async() => {
    // TODO:
}

// Filter resources when criteria change
watch([selectedChapter, selectedType], () => {
    fetchPractices()
})

const formatDate = (dateString: string) => {
    if (!dateString) return '-'
    try {
        const date = new Date(dateString)
        return date.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit'
        })
    } catch (err) {
        console.error('日期格式化错误:', err)
        return dateString
    }
}

onMounted(() => {
    fetchPractices()
})
</script>

<template>
    <div class="resource-list-container">
        <div class="resource-header">
            <h2>学习进度</h2>
            <button
                v-if="isStudent"
                class="btn-primary"
                @click="exportReport"
            >
                {{ '导出报告' }}
            </button>
        </div>

        <!-- Filter Section -->
        <div class="resource-filters">
            <div class="filter-section">
                <label for="typeFilter">按练习筛选:</label>
                <select
                    id="typeFilter"
                    v-model="selectedExer"
                >
                    <option value=-1>所有练习</option>
                    <option
                        v-for="practice in practicesName"
                        :key="practice.id"
                        :value="practice.id"
                    >
                        {{ practice.title }}
                    </option>
                </select>
            </div>

            <div v-if="isTeacher || isTutor" class="filter-section">
                <label for="chapterFilter">按学生筛选:</label>
                <select
                    id="chapterFilter"
                    v-model="selectedChapter"
                >
                    <option value="">所有学生</option>
                    <option v-for="name in names" :key="name" :value="name">
                        {{ name }}
                    </option>
                </select>
            </div>

            <div class="filter-section">
                <label for="typeFilter">按完成状态筛选:</label>
                <select
                    id="typeFilter"
                    v-model="selectedType"
                >
                    <option value="">所有完成状态</option>
                    <option v-for="type in practicesTypes" :key="type.value" :value="type.value">
                        {{ type.label }}
                    </option>
                </select>
            </div>
        </div>

        <!-- Resource List -->
        <div v-if="loading" class="loading-container">加载中...</div>
        <div v-else-if="error" class="error-message">{{ error }}</div>
        <div v-else-if="practices.length === 0" class="empty-state">
            暂无练习
        </div>
        <div v-else class="resource-table-wrapper">
            <table class="resource-table">
                <thead>
                <tr>
                    <th>练习名称</th>
                    <th>姓名</th>
                    <th>开始时间</th>
                    <th>截止时间</th>
                    <th>提交id</th>
                    <th>成绩</th>
                    <th>完成状态</th>
                    <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="practice in practices" :key="practice.id">
                    <td>{{ practice.title }}</td>
                    <td>{{ practice.name || '-' }}</td>
                    <td>{{ formatDate(practice.startTime) }}</td>
                    <td>{{ formatDate(practice.endTime) }}</td>
                    <td>{{ practice.submitId || '-' }}</td>
                    <td>{{ practice.score || '-' }}</td>
                    <td>{{ getPracticeTypeLabel(practice.type) || '-' }}</td>
                    <td class="actions">
                        <button
                            class="btn-action preview"
                            @click="doPractice(practice.id)"
                            title="练习"
                            :disabled="(practice.allowMultipleSubmission == false && practice.type != 'unFinished') || practice.type == 'overdue'"
                        >
                            练习
                        </button>
                        <button
                            class="btn-action download"
                            @click="getPracticeReport(practice.id, practice.submitId)"
                            title="查看"
                            :disabled="practice.type == 'unFinished' || practice.type == 'overdue'"
                        >
                            查看
                        </button>
                        <button
                            v-if="isTeacher"
                            class="btn-action history"
                            @click="checkPractice(practice.id, practice.submitId)"
                            title="批改"
                            :disabled="practice.type != 'unScored'"
                        >
                            批改
                        </button>
                        <button
                            class="btn-action renew"
                            @click="downloadPracticeReport(practice.id)"
                            title="下载报告"
                            :disabled="!practice.submitId"
                        >
                            下载报告
                        </button>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<style scoped>
/* 单选框样式 */
.radio-group {
    display: flex;
    gap: 1rem;
    margin-top: 0.5rem;
}

.radio-group label {
    display: flex;
    align-items: center;
    cursor: pointer;
}

.radio-label {
    margin-left: 0.25rem;
}

input[type="radio"] {
    accent-color: #409eff; /* 与Element Plus主色一致 */
}

.resource-list-container {
    max-width: 1400px;
    margin: 0 auto;
    padding: 1.5rem;
}

.resource-table-wrapper {
    overflow-x: auto;
}

.resource-table {
    width: 100%;
    border-collapse: collapse;
}

.resource-table th,
.resource-table td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid #e0e0e0;
}

.resource-table th {
    background-color: #f5f5f5;
    font-weight: 600;
}

.resource-table tr:hover {
    background-color: #f9f9f9;
}

.actions {
    display: flex;
    gap: 0.5rem;
}

.btn-action {
    padding: 0.375rem 0.75rem;
    border-radius: 4px;
    border: none;
    font-size: 0.875rem;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-action.preview {
    background-color: #e3f2fd;
    color: #1976d2;
}

.btn-action.preview:hover {
    background-color: #bbdefb;
}

.btn-action:disabled {
    cursor: not-allowed; /* Change cursor to indicate the button is not clickable */
}


.resource-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
}

.resource-header h2 {
    margin: 0;
}

.upload-form {
    background-color: #f9f9f9;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 1.5rem;
    border: 1px solid #e0e0e0;
}

.upload-form h3 {
    margin-top: 0;
    margin-bottom: 1rem;
}

.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
}

.form-row {
    display: flex;
    gap: 1rem;
}

.form-group-half {
    flex: 1;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1.5rem;
}

.upload-progress {
    margin-top: 1rem;
}

.progress-bar {
    height: 8px;
    background-color: #e0e0e0;
    border-radius: 4px;
    overflow: hidden;
    margin-bottom: 0.5rem;
}

.progress-value {
    height: 100%;
    background-color: #2c6ecf;
    border-radius: 4px;
}

.progress-text {
    text-align: right;
    font-size: 0.875rem;
    color: #616161;
}

.resource-filters {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 1.5rem;
    gap: 1rem;
    align-items: center;
}

.filter-section {
    display: flex;
    align-items: center;
}

.filter-section label {
    margin-right: 0.5rem;
    white-space: nowrap;
}

.filter-section select {
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 0.9375rem;
}

.filter-section.search {
    flex-grow: 1;
}

.search-input {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 0.9375rem;
}

.resource-table-wrapper {
    overflow-x: auto;
}

.resource-table {
    width: 100%;
    border-collapse: collapse;
}

.resource-table th,
.resource-table td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid #e0e0e0;
}

.resource-table th {
    background-color: #f5f5f5;
    font-weight: 600;
}

.resource-table tr:hover {
    background-color: #f9f9f9;
}

.actions {
    display: flex;
    gap: 0.5rem;
}

.btn-action {
    padding: 0.375rem 0.75rem;
    border-radius: 4px;
    border: none;
    font-size: 0.875rem;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-action.preview {
    background-color: #e3f2fd;
    color: #1976d2;
}

.btn-action.preview:hover {
    background-color: #bbdefb;
}

.btn-action.download {
    background-color: #e8f5e9;
    color: #2e7d32;
}

.btn-action.download:hover {
    background-color: #c8e6c9;
}

/* 黄色系按钮 */
.btn-action.history {
    background-color: #fff3e0;
    color: #ffa000;
}

.btn-action.history:hover {
    background-color: #ffe0b2;
}

.btn-action.renew {
    background-color: #ede7f6;
    color: #673ab7;
}

.btn-action.renew:hover {
    background-color: #d1c4e9;
}

.empty-state {
    padding: 2rem;
    text-align: center;
    background-color: #f5f5f5;
    border-radius: 8px;
    color: #757575;
}

.loading-container {
    display: flex;
    justify-content: center;
    padding: 2rem;
    color: #616161;
}

.error-message {
    background-color: #ffebee;
    color: #c62828;
    padding: 1rem;
    border-radius: 4px;
    margin-bottom: 1rem;
}

.btn-primary, .btn-secondary {
    padding: 0.5rem 1rem;
    border-radius: 4px;
    font-weight: 500;
    cursor: pointer;
    border: none;
    transition: background-color 0.2s;
}

.btn-primary {
    background-color: #2c6ecf;
    color: white;
}

.btn-primary:hover {
    background-color: #215bb4;
}

.btn-secondary {
    background-color: #f5f5f5;
    color: #424242;
    border: 1px solid #ddd;
}

.btn-secondary:hover {
    background-color: #e0e0e0;
}

@media (max-width: 768px) {
    .form-row {
        flex-direction: column;
        gap: 0;
    }

    .resource-filters {
        flex-direction: column;
        align-items: stretch;
    }

    .filter-section {
        width: 100%;
    }

    .filter-section select,
    .search-input {
        flex-grow: 1;
    }
}
</style>
