<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import { defineProps } from 'vue'
import ClassApi from '../../api/class'
import {useAuthStore} from "../../stores/auth.ts";

import Papa from 'papaparse'  // CSV解析库

const props = defineProps<{
    classId: string;
    isTeacher: boolean;
}>()

const students = ref<any[]>([])
const loading = ref(true)
const error = ref('')

// File upload
const showUploadFileForm = ref(false)
const showUploadAloneForm = ref(false)

const uploadFileForm = ref({
    classId: props.classId,
    file: null as File | null,
    parsedStudents: []
})
let uploadAloneForm = reactive({
    classId: props.classId,
    file: null as File | null,
    students: [
        { id: 23373999, name: "士小信" }
    ]
})
const uploadProgress = ref(0)
const uploadError = ref('')
const uploadAloneError = ref('')

const authStore = useAuthStore()

// Fetch students
const fetchStudents = async () => {
    loading.value = true
    error.value = ''

    try {
        const response = await ClassApi.getClassStudents(props.classId)
        console.log('获取学生列表响应:', response)
        
        if (response.code === 200 && Array.isArray(response.data)) {
            students.value = response.data
            console.log('学生列表数据:', students.value)
        } else {
            students.value = []
            error.value = response.message || '获取学生列表失败'
            console.error('获取学生列表失败:', response)
        }
    } catch (err) {
        students.value = []
        error.value = '获取学生列表失败，请稍后再试'
        console.error('获取学生列表错误:', err)
    } finally {
        loading.value = false
    }
}

// 新增CSV解析相关状态
const isParsing = ref(false)
const parsingError = ref('')

// 修改文件处理函数
const handleFileChange = async (event: Event) => {
    const input = event.target as HTMLInputElement
    if (!input.files || input.files.length === 0) return

    const file = input.files[0]

    // 验证文件类型
    if (!validateCSVFile(file)) {
        uploadError.value = '仅支持CSV格式文件'
        return
    }

    // 重置状态
    parsingError.value = ''
    isParsing.value = true

    try {
        // 解析CSV为JSON
        const parsedData = await parseCSV(file)

        // 验证数据格式
        if (!validateStudentData(parsedData)) {
            throw new Error('CSV格式错误：必须包含"name"和"id"列')
        }

        // 存储解析结果
        uploadFileForm.value.parsedStudents = parsedData
        uploadError.value = ''
    } catch (err) {
        parsingError.value = err instanceof Error ? err.message : '文件解析失败'
        uploadFileForm.value.parsedStudents = []
    } finally {
        isParsing.value = false
    }
}

// CSV文件验证
const validateCSVFile = (file: File) => {
    const validTypes = ['text/csv', 'application/vnd.ms-excel']
    const validExtension = file.name.endsWith('.csv')
    return validTypes.includes(file.type) || validExtension
}

// CSV解析函数
const parseCSV = (file: File): Promise<any[]> => {
    return new Promise((resolve, reject) => {
        Papa.parse(file, {
            header: true,        // 使用第一行为header
            skipEmptyLines: true,
            delimiter: ',', // 明确指定分隔符为逗号
            complete: (results) => {
                if (results.errors.length > 0) {
                    console.log(results.errors)
                    const firstError = results.errors[0]
                    reject(`解析错误（行 ${firstError.row}）: ${firstError.message}`)
                } else {
                    resolve(results.data)
                }
            },
            error: (err) => {
                reject(err)
            }
        })
    })
}

// 数据验证
const validateStudentData = (data: any[]) => {
    return data.every(item =>
        Object.keys(item).includes('name') &&
        Object.keys(item).includes('id')
    )
}

// 修改上传函数
const uploadFileStudents = async () => {
    if (!uploadFileForm.value.parsedStudents?.length) {
        uploadError.value = '请先上传并解析有效文件'
        return
    }

    uploadProgress.value = 0
    uploadError.value = ''

    try {
        // 改为发送JSON数据
        await ClassApi.uploadStudents(
            {
                classId: props.classId,
                operatorId: authStore.user?.id,
                students: uploadFileForm.value.parsedStudents
            }
        )

        showUploadFileForm.value = false
        resetUploadFileForm()
        fetchStudents()  // 刷新列表
    } catch (err) {
        uploadError.value = '上传失败，请稍后再试'
        console.error(err)
    }
}

const uploadAloneStudents = async () => {
    // 检查是否存在空姓名或学号
    const hasEmptyField = uploadAloneForm.students.some(student =>
        student.name.trim() === '' || String(student.id).trim() === ''
    )

    if (hasEmptyField) {
        uploadAloneError.value = '请补全所有学生的姓名和学号'
        return
    }

    uploadProgress.value = 0
    uploadAloneError.value = ''

    try {
        // 改为发送JSON数据
        await ClassApi.uploadStudents(
            {
                classId: props.classId,
                operatorId: authStore.user?.id,
                students: uploadAloneForm.students.map(s => ({
                    // 转换字段命名风格（根据后端需要）
                    student_name: s.name,
                    student_id: s.id
                }))
            }
        )

        showUploadAloneForm.value = false
        resetUploadAloneForm()
        fetchStudents()  // 刷新列表
    } catch (err) {
        uploadAloneError.value = '上传失败，请稍后再试'
        console.error(err)
    }
}

const resetUploadAloneForm = () => {
    uploadAloneForm.students = [{ id: 23373999, name: '士小信' }] // 保留一个空表单
    uploadProgress.value = 0
}

// 修改重置函数
const resetUploadFileForm = () => {
    uploadFileForm.value = {
        classId: props.classId,
        file: null,
        parsedStudents: []  // 新增解析结果存储
    }
    uploadProgress.value = 0
    uploadError.value = ''
    parsingError.value = ''
    // 清除文件输入框的 DOM 值
    const fileInput = document.getElementById('file') as HTMLInputElement
    if (fileInput) {
        fileInput.value = '' // 关键：清空 DOM 元素的值
    }
}

const toggleUploadForm = () => {
    showUploadFileForm.value = !showUploadFileForm.value
    if (!showUploadFileForm.value) {
        resetUploadFileForm()
    }
}

// Add an option for choice questions
const addStudent = () => {
    uploadAloneForm.students.push({ id: 0, name: '' })
}

// Remove an option
const deleteStudentByIndex = (index: number) => {
    if (uploadAloneForm.students.length > 1) {
        uploadAloneForm.students.splice(index, 1)
    }
}

// Preview resource
const deleteStudent = async(studentId: string) => {
    // /api/classes/{classId}/leave/{userId}

    try {
        // 改为发送JSON数据
        const response = await ClassApi.deleteStudents(props.classId, studentId)
        students.value = students.value.filter(s => s.student_id !== studentId);
        console.log(response)
        fetchStudents()  // 刷新列表
    } catch (err) {
        uploadAloneError.value = '删除学生失败，请稍后再试'
        console.error(err)
    }
}

onMounted(() => {
    fetchStudents()
})
</script>

<template>
    <div class="class-student-container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="error" class="error-message">{{ error }}</div>
        <div v-else>
            <!-- 学生列表 -->
            <div class="student-list">
                <h3>学生列表</h3>
                <div v-if="students.length === 0" class="empty-state">
                    暂无学生
                </div>
                <div v-else class="student-grid">
                    <div v-for="student in students" :key="student.userId" class="student-card">
                        <div class="student-info">
                            <h4>{{ student.studentName }}</h4>
                            <p>学号: {{ student.studentId }}</p>
                            <p>加入时间: {{ new Date(student.joinedAt).toLocaleString() }}</p>
                        </div>
                        <div v-if="isTeacher" class="student-actions">
                            <button 
                                class="btn-danger" 
                                @click="deleteStudent(student.userId)"
                            >
                                移除
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 教师操作按钮 -->
            <div v-if="isTeacher" class="teacher-actions">
                <button class="btn-primary" @click="toggleUploadForm">
                    批量导入学生
                </button>
                <button class="btn-primary" @click="showUploadAloneForm = true">
                    单个添加学生
                </button>
            </div>

            <!-- 批量导入弹窗 -->
            <div v-if="showUploadFileForm" class="dialog-overlay">
                <div class="upload-dialog">
                    <h3>批量导入学生</h3>
                    <div class="form-group">
                        <label>选择CSV文件</label>
                        <input
                            id="file"
                            type="file"
                            accept=".csv"
                            @change="handleFileChange"
                        >
                    </div>
                    <div v-if="parsingError" class="error-message">
                        {{ parsingError }}
                    </div>
                    <div v-if="uploadError" class="error-message">
                        {{ uploadError }}
                    </div>
                    <div class="dialog-actions">
                        <button class="btn-secondary" @click="resetUploadFileForm">
                            取消
                        </button>
                        <button 
                            class="btn-primary" 
                            @click="uploadFileStudents"
                            :disabled="!uploadFileForm.parsedStudents.length"
                        >
                            上传
                        </button>
                    </div>
                </div>
            </div>

            <!-- 单个添加弹窗 -->
            <div v-if="showUploadAloneForm" class="dialog-overlay">
                <div class="upload-dialog">
                    <h3>添加学生</h3>
                    <div v-for="(student, index) in uploadAloneForm.students" :key="index" class="form-group">
                        <div class="student-input-group">
                            <input
                                v-model="student.name"
                                type="text"
                                placeholder="学生姓名"
                            >
                            <input
                                v-model="student.id"
                                type="text"
                                placeholder="学号"
                            >
                            <button 
                                class="btn-danger" 
                                @click="deleteStudentByIndex(index)"
                                :disabled="uploadAloneForm.students.length === 1"
                            >
                                删除
                            </button>
                        </div>
                    </div>
                    <div v-if="uploadAloneError" class="error-message">
                        {{ uploadAloneError }}
                    </div>
                    <div class="dialog-actions">
                        <button class="btn-secondary" @click="resetUploadAloneForm">
                            取消
                        </button>
                        <button class="btn-primary" @click="addStudent">
                            添加一行
                        </button>
                        <button class="btn-primary" @click="uploadAloneStudents">
                            保存
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.class-student-container {
    padding: 1rem;
}

.loading {
    text-align: center;
    padding: 2rem;
    color: #666;
}

.error-message {
    color: #e53935;
    padding: 1rem;
    background-color: #ffebee;
    border-radius: 4px;
    margin: 1rem 0;
}

.student-list {
    margin-bottom: 2rem;
}

.student-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 1rem;
    margin-top: 1rem;
}

.student-card {
    background: #fff;
    border-radius: 8px;
    padding: 1rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.student-info h4 {
    margin: 0 0 0.5rem 0;
    color: #333;
}

.student-info p {
    margin: 0.25rem 0;
    color: #666;
    font-size: 0.9rem;
}

.student-actions {
    display: flex;
    gap: 0.5rem;
}

.teacher-actions {
    display: flex;
    gap: 1rem;
    margin-top: 1rem;
}

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

.upload-dialog {
    background: #fff;
    padding: 2rem;
    border-radius: 8px;
    width: 90%;
    max-width: 500px;
}

.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    color: #333;
}

.student-input-group {
    display: flex;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
}

.student-input-group input {
    flex: 1;
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.dialog-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
    margin-top: 1rem;
}

.btn-primary {
    background: #1976d2;
    color: #fff;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
}

.btn-secondary {
    background: #757575;
    color: #fff;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
}

.btn-danger {
    background: #e53935;
    color: #fff;
    border: none;
    padding: 0.5rem 1rem;
    border-radius: 4px;
    cursor: pointer;
}

.empty-state {
    text-align: center;
    padding: 2rem;
    color: #666;
    background: #f5f5f5;
    border-radius: 8px;
}
</style>
