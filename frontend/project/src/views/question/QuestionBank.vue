<script setup lang="ts">
import {ref, onMounted, watch, onBeforeMount, reactive} from 'vue'
import {useRouter} from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from "../../stores/auth"
import QuestionApi from '../../api/question'
import CourseApi from '../../api/course'
import request from '../../api/axios'

const authStore = useAuthStore()
const isTeacher = authStore.userRole === 'teacher'

// const isTeacher = authStore.userRole === 'teacher'

const questions = ref<any[]>([])
const loading = ref(true)
const error = ref('')

// Filters
const selectedChapter = ref('')
const selectedCourse = ref<number | null>(null)
const selectedSection = ref<number | null>(null)
const selectedType = ref<string>('')

// Chapters and types (will be populated from questions)
const chapters = ref([])
const courses = ref<Course[]>([])

// File upload
const showUploadForm = ref(false)
const uploadForm = ref({
    courseId: 0,
    sectionId: 0,
    uploaderId: 0,
    title: '',
    type: '',
    file: null as File | null,
    visibility: '',
})
const uploadProgress = ref(0)
const uploadError = ref('')

// Temporary question data for editing
interface Question {
    type: string;
    content: string;
    options: { key: string; text: string; }[];
    answer: string;
    courseId: number;
    sectionId: number;
    analysis: string;
    creatorId: number | undefined;
    answerArray: string[];
    range?: string;
}

// 临时存储题目数据
const tempQuestion = ref({
    type: 'singlechoice',
    content: '',
    options: [
        { key: 'A', text: '' },
        { key: 'B', text: '' },
        { key: 'C', text: '' },
        { key: 'D', text: '' }
    ],
    analysis: '',
    courseId: null as number | null,
    sectionId: null as number | null,
    creatorId: authStore.user?.id,
    // 使用计算属性处理多选答案
    get answerArray(): string[] {
        return this.type === 'multiplechoice' && this.answer
            ? this.answer.split(',')
            : [];
    },
    set answerArray(values: string[]) {
        this.answer = values.join(',');
    },
    answer: '' // 始终保持字符串类型
})

// Add an option for choice questions
const addOption = () => {
    console.warn('=== 添加选项 ===')
    const nextKey = String.fromCharCode(65 + tempQuestion.value.options.length)
    tempQuestion.value.options.push({ key: nextKey, text: '' })
    console.warn('当前选项列表:', JSON.stringify(tempQuestion.value.options, null, 2))
}

// Remove an option
const removeOption = (index: number) => {
    console.warn('=== 删除选项 ===')
    console.warn('要删除的选项索引:', index)
    if (tempQuestion.value.options.length > 2) {
        tempQuestion.value.options.splice(index, 1)

        // Re-key the options
        tempQuestion.value.options.forEach((opt, i) => {
            opt.key = String.fromCharCode(65 + i)
        })
        console.warn('删除后的选项列表:', JSON.stringify(tempQuestion.value.options, null, 2))
    }
}

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 添加必要的响应式变量
const currentQuestion = ref<any>(null)
const formattedOptions = ref<Array<{key: string, text: string}>>([])
const detailVisible = ref(false)

// Fetch questions
const fetchQuestions = async () => {
  loading.value = true
  try {
    const response = await QuestionApi.getQuestionList({
      courseId: selectedCourse.value || undefined
    })
    console.log('获取题目列表响应:', response)
    
    if (response?.code === 200) {
      questions.value = response.data.questions.map((question: any) => ({
        ...question,
        options: question.options || []
      }))
      total.value = response.data.total
    } else {
      questions.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    questions.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ---
// 在接口定义部分添加类型
interface Course {
    id: number
    name: string
    sections: Section[]
}

interface Section {
    id: number
    course_id: number
    title: string
    sort_order: number
}

// 添加课程和章节数据
const sections = ref<Section[]>([])

// 获取教师课程列表
const fetchCourses = async () => {
  try {
    loading.value = true
    const response = await CourseApi.getAllCourses()
    if (response?.code === 200) {
      // 添加"所有课程"选项
      const courseList = [
        { id: 0, name: '所有课程', code: 'all' },
        ...response.data
      ]
      courses.value = courseList
      console.log('成功获取课程列表:', courseList)
    } else {
      courses.value = []
      console.error('获取课程列表失败:', response)
    }
  } catch (error) {
    courses.value = []
    console.error('获取课程列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 监听课程选择变化
watch(selectedCourse, (newValue) => {
  fetchQuestions()
})

// watch(() => tempQuestion.value.courseId, (newCourseId) => {
//     const selectedCourse = courses2.value.find(c => c.id === newCourseId)
//
//     // 新增数据校验
//     const safeSections = Array.isArray(selectedCourse?.sections)
//         ? [...selectedCourse.sections]
//         : []
//     console.log(selectedCourse?.sections)
//     console.log(Array.isArray(selectedCourse?.sections))
//     console.log(safeSections)
//     sections.value = safeSections.filter(s => s?.id && s?.title) // 过滤无效数据
//     console.log("处理后的章节数据:", sections.value)
//
//     tempQuestion.value.sectionId = 0
// })

// 在组件挂载时获取课程列表
onMounted(() => {
    console.log('组件挂载')
    fetchCourses()
    fetchQuestions()
})

// 添加生命周期钩子
onBeforeMount(() => {
    console.warn('组件即将挂载 - 测试控制台输出')
})

const addQuestion = async () => {
    try {
        // 处理选项格式
        let formattedOptions = []
        if (['singlechoice', 'multiplechoice'].includes(tempQuestion.value.type)) {
            formattedOptions = tempQuestion.value.options.map(opt => ({
                key: opt.key,
                text: opt.text
            }))
        }

        const questionData = {
            type: tempQuestion.value.type,
            content: tempQuestion.value.content,
            analysis: tempQuestion.value.analysis,
            options: JSON.stringify(formattedOptions), // 确保转换为JSON字符串
            answer: tempQuestion.value.type === 'multiplechoice' 
                ? JSON.stringify(tempQuestion.value.answerArray) 
                : tempQuestion.value.answer,
            courseId: tempQuestion.value.courseId,
            sectionId: tempQuestion.value.sectionId
        }

        console.log('发送的题目数据:', questionData)
        const response = await QuestionApi.createQuestion(questionData)
        
        if (response?.data?.code === 200) {
            ElMessage.success('题目创建成功')
            resetUploadForm()
            fetchQuestions()
        } else {
            ElMessage.error(response?.data?.message || '创建题目失败')
        }
    } catch (error) {
        console.error('创建题目失败:', error)
        ElMessage.error('创建题目失败，请重试')
    }
}

// Reset upload form
const resetUploadForm = () => {
    tempQuestion.value = {
        type: 'singlechoice',
        content: '',
        options: [
            { key: 'A', text: '' },
            { key: 'B', text: '' },
            { key: 'C', text: '' },
            { key: 'D', text: '' }
        ],
        answer: '',
        courseId: 0,
        sectionId: 0,
        analysis: '',
        creatorId: authStore.user?.id,
        get answerArray(): string[] {
            return this.type === 'multiplechoice' && this.answer
                ? this.answer.split(',')
                : [];
        },
        set answerArray(values: string[]) {
            this.answer = values.join(',');
        },
    }
    uploadError.value = ''
    uploadProgress.value = 0
}

// 新增弹窗相关状态
const showDetailDialog = ref(false)
const selectedQuestion = ref<any>(null)

// 显示题目详情
const showQuestionDetail = (question: any) => {
  console.log('显示题目详情:', question)
  currentQuestion.value = {
    ...question,
    analysis: question.analysis || '暂无解析'
  }
  // 处理选项
  if (question.options && Array.isArray(question.options)) {
    formattedOptions.value = question.options.map((opt: any) => {
      if (opt.text) {
        // 如果选项文本包含分隔符，则分割成数组
        const texts = opt.text.split('|')
        return texts.map((text: string, index: number) => ({
          text,
          key: opt.key // 使用后端提供的key
        }))
      }
      return opt
    }).flat()
  } else {
    formattedOptions.value = []
  }
  detailVisible.value = true
}

// 新增答案格式化方法
const formatAnswer = (question: any) => {
    if (question.type === 'multiplechoice') {
        return question.answer.split(',').join(', ')
    }
    return question.answer
}

// Filter questions when criteria change
watch([selectedChapter, selectedCourse], () => {
    fetchQuestions()
})

onMounted(() => {
    fetchQuestions()
})

// 获取课程相关的题目列表
const fetchCourseQuestions = async () => {
  try {
    loading.value = true
    const response = await QuestionApi.getQuestionList({
      courseId: selectedCourse.value
    })
    console.log('获取到的题目列表:', response)
    if (response.data.code === 200 && response.data.data) {
      // 直接使用返回的questions数组
      questions.value = response.data.data.questions.map((q: any) => {
        // 解析选项JSON字符串
        let optionsArray = []
        try {
          if (q.options && q.options[0] && q.options[0].text) {
            optionsArray = JSON.parse(q.options[0].text)
          }
        } catch (e) {
          console.error('解析选项失败:', e)
        }

        return {
          id: q.id,
          type: q.type,
          content: q.name, // 使用name作为content
          options: optionsArray.map((text: string, index: number) => ({
            key: String.fromCharCode(65 + index), // A, B, C, D...
            text: text
          })),
          answer: q.answer,
          courseId: q.courseId,
          courseName: q.courseName,
          sectionId: q.sectionId,
          sectionName: q.sectionName,
          teacherId: q.teacherId
        }
      })
      console.log('处理后的题目列表:', questions.value)
    } else {
      console.error('获取题目列表失败:', response.data.message)
      error.value = response.data.message || '获取题目列表失败'
    }
  } catch (err: any) {
    console.error('获取题目列表出错:', err)
    error.value = err.message || '获取题目列表失败'
  } finally {
    loading.value = false
  }
}

// 获取题目类型标签样式
const getQuestionTypeTag = (type: string) => {
  const typeMap: { [key: string]: string } = {
    'singlechoice': 'primary',
    'multiplechoice': 'success',
    'truefalse': 'warning',
    'fillblank': 'info',
    'shortanswer': 'danger'
  }
  return typeMap[type] || ''
}

// 获取题目类型名称
const getQuestionTypeName = (type: string) => {
  const typeMap: { [key: string]: string } = {
    'singlechoice': '选择题',
    'multiplechoice': '多选题',
    'truefalse': '判断题',
    'fillblank': '填空题',
    'shortanswer': '简答题'
  }
  return typeMap[type] || type
}

const showCreateDialog = ref(false)
const newQuestion = reactive({
  type: 'singlechoice',
  content: '',
  options: [
    { key: 'A', text: '' },
    { key: 'B', text: '' },
    { key: 'C', text: '' },
    { key: 'D', text: '' }
  ],
  explanation: '',
  points: 5,
  answer: ''
})

// 重置题目表单
const resetQuestionForm = () => {
  newQuestion.type = 'singlechoice'
  newQuestion.content = ''
  newQuestion.options = [
    { key: 'A', text: '' },
    { key: 'B', text: '' },
    { key: 'C', text: '' },
    { key: 'D', text: '' }
  ]
  newQuestion.answer = ''
  newQuestion.explanation = ''
  newQuestion.points = 5
}

// 添加新选项
const addNewOption = () => {
  const nextKey = String.fromCharCode(65 + newQuestion.options.length)
  newQuestion.options.push({ key: nextKey, text: '' })
}

// 删除选项
const removeNewOption = (index: number) => {
  if (newQuestion.options.length > 2) {
    newQuestion.options.splice(index, 1)
    // 重新生成选项key
    newQuestion.options.forEach((opt, i) => {
      opt.key = String.fromCharCode(65 + i)
    })
  }
}

// 创建题目
const createQuestion = async () => {
  if (!newQuestion.content) {
    ElMessage.error('请填写题目内容')
    return
  }
  if (['singlechoice', 'multiplechoice'].includes(newQuestion.type)) {
    const hasEmptyOption = newQuestion.options.some(opt => !opt.text)
    if (hasEmptyOption) {
      ElMessage.error('请填写所有选项内容')
      return
    }
    if (!newQuestion.answer) {
      ElMessage.error('请设置正确答案')
      return
    }
  }
  if (!selectedCourse.value) {
    ElMessage.error('请选择所属课程')
    return
  }
  if (!selectedSection.value) {
    ElMessage.error('请选择所属章节')
    return
  }

  try {
    const questionData = {
      type: newQuestion.type,
      content: newQuestion.content,
      options: newQuestion.options.map(opt => opt.text),
      answer: newQuestion.answer,
      analysis: newQuestion.explanation,
      courseId: selectedCourse.value,
      sectionId: selectedSection.value,
      creatorId: authStore.user?.id
    }
    await QuestionApi.createQuestion(questionData)
    ElMessage.success('题目创建成功')
    showCreateDialog.value = false
    resetQuestionForm()
    await fetchQuestions()
  } catch (err) {
    console.error('创建题目失败:', err)
    ElMessage.error('创建题目失败，请稍后再试')
  }
}

// 处理课程选择变化
const handleCourseChange = async (courseId: number) => {
  selectedCourse.value = courseId
  selectedSection.value = null
  if (courseId) {
    try {
      const response = await CourseApi.getCourseById(String(courseId))
      if (response.data && response.data.sections) {
        sections.value = response.data.sections
      }
    } catch (err) {
      console.error('获取课程章节失败:', err)
      sections.value = []
    }
  } else {
    sections.value = []
  }
  await fetchQuestions()
}

// 处理章节选择变化
const handleSectionChange = async () => {
  await fetchQuestions()
}

</script>

<template>
  <div class="question-bank-container">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-group">
        <select v-model="selectedCourse" @change="(e: Event) => handleCourseChange(Number((e.target as HTMLSelectElement).value))">
          <option value="">所有课程</option>
          <option v-for="course in courses" :key="course.id" :value="course.id">
            {{ course.name }}
          </option>
        </select>
        <select v-model="selectedSection" @change="handleSectionChange">
          <option value="">所有章节</option>
          <option v-for="section in sections" :key="section.id" :value="section.id">
            {{ section.title }}
          </option>
        </select>
        <select v-model="selectedType">
          <option value="">所有类型</option>
          <option value="singlechoice">选择题</option>
          <option value="multiplechoice">多选题</option>
          <option value="truefalse">判断题</option>
          <option value="fillblank">填空题</option>
          <option value="shortanswer">简答题</option>
        </select>
      </div>
      <button class="btn-primary" @click="showCreateDialog = true">
        创建题目
      </button>
    </div>

    <!-- 题目列表 -->
    <div class="question-cards-container">
      <div class="question-cards">
        <div v-for="question in questions" :key="question.id" class="question-card" @click="showQuestionDetail(question)">
          <div class="question-card-header">
            <span class="question-type">{{ getQuestionTypeName(question.type) }}</span>
            <span class="question-course">{{ question.courseName }}</span>
          </div>
          <div class="question-content">{{ question.name }}</div>
          <div class="question-footer">
            <span class="question-section">{{ question.sectionName }}</span>
            <button class="btn-action">查看详情</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 题目详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="题目详情"
      width="60%"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      class="question-detail-dialog"
    >
      <div v-if="currentQuestion" class="question-detail">
        <div class="detail-header">
          <el-tag :type="getQuestionTypeTag(currentQuestion.type)" class="question-type">
            {{ getQuestionTypeName(currentQuestion.type) }}
          </el-tag>
          <h3 class="detail-title">{{ currentQuestion.name }}</h3>
        </div>

        <div class="detail-info">
          <div class="info-item">
            <i class="el-icon-reading"></i>
            <span>课程：{{ currentQuestion.courseName }}</span>
          </div>
          <div class="info-item">
            <i class="el-icon-collection"></i>
            <span>章节：{{ currentQuestion.sectionName }}</span>
          </div>
        </div>
        
        <!-- 选项 -->
        <div v-if="['singlechoice', 'multiplechoice'].includes(currentQuestion.type)" class="options">
          <div v-for="(opt, index) in currentQuestion.options" :key="`${currentQuestion.id}-${index}-${opt.key}`" class="option">
            <span class="option-key">{{ opt.key }}.</span>
            <div class="option-text">
              <div v-for="(text, textIndex) in opt.text.split('|')" :key="textIndex" class="option-text-item">
                {{ text }}
              </div>
            </div>
          </div>
        </div>

        <!-- 答案 -->
        <div class="answer-section">
          <h4 class="section-title">答案：</h4>
          <div class="answer-content">{{ currentQuestion.answer }}</div>
        </div>

        <!-- 解析 -->
        <div class="analysis-section" v-if="currentQuestion.analysis">
          <h4 class="section-title">解析：</h4>
          <div class="analysis-content">{{ currentQuestion.analysis }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 创建题目弹窗 -->
    <div v-if="showCreateDialog" class="modal-mask">
      <div class="modal-container">
        <div class="modal-header">
          <h3>创建题目</h3>
          <button class="modal-close" @click="showCreateDialog = false">&times;</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label>题目类型</label>
            <select v-model="newQuestion.type">
              <option value="singlechoice">单选题</option>
              <option value="multiplechoice">多选题</option>
              <option value="truefalse">判断题</option>
              <option value="fillblank">填空题</option>
              <option value="shortanswer">简答题</option>
            </select>
          </div>

          <div class="form-group">
            <label>题目内容</label>
            <textarea
              v-model="newQuestion.content"
              rows="4"
              placeholder="输入题目内容"
            ></textarea>
          </div>

          <!-- 选项 -->
          <div v-if="['singlechoice', 'multiplechoice'].includes(newQuestion.type)" class="form-group">
            <label>选项</label>
            <div v-for="(option, index) in newQuestion.options" :key="index" class="option-row">
              <div class="option-key">{{ option.key }}</div>
              <input
                v-model="option.text"
                type="text"
                :placeholder="`选项${option.key}内容`"
                class="option-input"
              />
              <button
                type="button"
                class="btn-icon"
                @click="removeNewOption(index)"
                :disabled="newQuestion.options.length <= 2"
              >
                ✕
              </button>
            </div>
            <button type="button" class="btn-text" @click="addNewOption">
              + 添加选项
            </button>
          </div>

          <!-- 答案 -->
          <div v-if="newQuestion.type === 'singlechoice'" class="form-group">
            <label>正确答案</label>
            <select v-model="newQuestion.answer">
              <option value="" disabled>选择正确答案</option>
              <option v-for="option in newQuestion.options" :key="option.key" :value="option.key">
                {{ option.key }}
              </option>
            </select>
          </div>

          <div v-if="newQuestion.type === 'multiplechoice'" class="form-group">
            <label>正确答案 (多选)</label>
            <div class="checkbox-group">
              <div v-for="option in newQuestion.options" :key="option.key" class="checkbox-item">
                <input
                  :id="`answer-${option.key}`"
                  type="checkbox"
                  v-model="newQuestion.answer"
                  :value="option.key"
                />
                <label :for="`answer-${option.key}`">{{ option.key }}</label>
              </div>
            </div>
          </div>

          <div v-if="newQuestion.type === 'truefalse'" class="form-group">
            <label>正确答案</label>
            <select v-model="newQuestion.answer">
              <option value="" disabled>选择正确答案</option>
              <option value="True">正确</option>
              <option value="False">错误</option>
            </select>
          </div>

          <div v-if="newQuestion.type === 'fillblank'" class="form-group">
            <label>正确答案</label>
            <input
              v-model="newQuestion.answer"
              type="text"
              placeholder="输入正确答案"
            />
          </div>

          <div class="form-group">
            <label>答案解析</label>
            <textarea
              v-model="newQuestion.explanation"
              rows="4"
              placeholder="输入答案解析"
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="showCreateDialog = false">
              取消
            </button>
            <button type="button" class="btn-primary" @click="createQuestion">
              创建
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.question-bank {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-title {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 20px 0;
}

.filter-section {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
}

.course-select {
  width: 200px;
}

.question-cards-container {
  padding: 20px;
}

.question-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 10px;
}

.question-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.question-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.question-type {
  background: #e6f7ff;
  color: #1890ff;
  padding: 4px 8px;
  border-radius: 4px;
}

.question-course {
  color: #666;
}

.question-content {
  font-size: 16px;
  color: #333;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.question-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.question-section {
  font-size: 14px;
  color: #999;
}

.btn-action {
  background: #1890ff;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-action:hover {
  background: #40a9ff;
}

/* 保留原有的详情对话框样式 */
.question-detail {
  padding: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.detail-title {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.detail-info {
  background-color: #f5f7fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 16px 0;
}

.options {
  margin: 20px 0;
}

.option {
  margin: 10px 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.option-key {
  font-weight: bold;
  margin-right: 10px;
}

.option-text {
  flex: 1;
  color: #333;
  line-height: 1.6;
}

.option-text-item {
  margin: 4px 0;
  padding: 4px 0;
}

.option-text-item:not(:last-child) {
  border-bottom: 1px dashed #e0e0e0;
}

.answer-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f9eb;
  border-radius: 4px;
}

.answer-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.analysis-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f4f4f5;
  border-radius: 4px;
}

.analysis-content {
  color: #606266;
  line-height: 1.6;
  white-space: pre-wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.info-item i {
  color: #909399;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.filter-group {
  display: flex;
  gap: 1rem;
}

.filter-group select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 150px;
}

.btn-primary {
  padding: 0.5rem 1rem;
  background-color: #2c6ecf;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary:hover {
  background-color: #215bb4;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  border-radius: 8px;
  padding: 20px;
  width: 600px;
  max-width: 90%;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 15px;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.form-group {
  margin-bottom: 1.25rem;
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

.option-row {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.option-key {
  flex: 0 0 2rem;
  text-align: center;
  font-weight: 500;
  background-color: #f5f5f5;
  border-radius: 4px;
  padding: 0.5rem;
  margin-right: 0.5rem;
}

.option-input {
  flex: 1;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  color: #757575;
  font-size: 1rem;
  padding: 0.5rem;
}

.btn-icon:hover {
  color: #d32f2f;
}

.btn-icon:disabled {
  color: #bdbdbd;
  cursor: not-allowed;
}

.btn-text {
  background: none;
  border: none;
  color: #2c6ecf;
  padding: 0;
  font-weight: 500;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  margin-top: 0.5rem;
}

.btn-text:hover {
  text-decoration: underline;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}

.checkbox-item {
  display: flex;
  align-items: center;
  margin-right: 1rem;
}

.checkbox-item input {
  margin-right: 0.5rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background-color: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary:hover {
  background-color: #e0e0e0;
}
</style>
