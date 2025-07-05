<script setup lang="ts">
import {ref, onMounted, watch, onBeforeMount, computed} from 'vue'
// import {useRouter} from 'vue-router'
// import { ElMessage } from 'element-plus'
import { useAuthStore } from "../../stores/auth"
import QuestionApi from '../../api/question'
import CourseApi from '../../api/course'
// import request from '../../api/axios'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const isTeacher = authStore.userRole === 'teacher'

// const isTeacher = authStore.userRole === 'teacher'

const questions = ref<any[]>([])
const loading = ref(true)
const error = ref('')

// Filters
const selectedChapter = ref('')
const selectedCourse = ref()

// Chapters and types (will be populated from questions)
const courses = ref<Course[]>([])

// File upload
const showUploadForm = ref(false)
const uploadProgress = ref(0)
const uploadError = ref('')

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
    explanation: '',
    courseId: null as number | null,
    sectionId: null as number | null,
    creatorId: authStore.user?.id,
    // 使用计算属性处理多选答案
    get answerArray(): string[] {
        if (this.type === 'multiplechoice') {
            if (typeof this.answer === 'string') {
                return this.answer ? this.answer.split('|') : [];
            }
            return [];
        }
        return [];
    },
    set answerArray(values: string[]) {
        this.answer = values.join('|');
    },
    answer: '' // 始终保持字符串类型
})

const generateForm = ref({
    course_id: 0,
    course_name: '',
    section_id: 0,
    lesson_content: '',
    difficulty: 'medium',
    choose_count: 0,
    fill_blank_count: 0,
    question_count: 0,
    custom_types: {} as Record<string, number>
});

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

// Fetch questions
const fetchQuestions = async () => {
    loading.value = true
    error.value = ''

    try {
        console.log('开始获取题目，selectedCourse:', selectedCourse.value)

        let params = {}
        // 只在选中课程时传 courseId，否则查全部
        if (selectedCourse.value) {
            const courseId = Number(selectedCourse.value)
            if (isNaN(courseId)) {
                console.log('无效的courseId:', selectedCourse.value)
                questions.value = []
                return
            }
            params = { courseId }
        }

        const response = await QuestionApi.getQuestionList(params)
        console.log('获取题目列表响应:', response)

        // 兼容所有课程和单课程的响应结构
        let questionsData = []
        if (response?.code === 200) {
            if (response.data?.questions) {
                questionsData = response.data.questions
                questionsData.forEach(q => {
                    if (q.type === 'singlechoice' && typeof q.answer === 'string' && q.answer.length > 1) {
                        q.type = 'multiplechoice'
                    }
                })
            } else if (Array.isArray(response.data)) {
                questionsData = response.data
                questionsData.forEach(q => {
                    if (q.type === 'singlechoice' && typeof q.answer === 'string' && q.answer.length > 1) {
                        q.type = 'multiplechoice'
                    }
                })
            }
        }

        if (Array.isArray(questionsData) && questionsData.length > 0) {
            questions.value = questionsData.map(q => {
                // 直接使用options数组，不需要额外处理
                return { ...q }
            })
            console.log('设置questions.value:', questions.value)
        } else {
            console.log('题目数据为空数组')
            questions.value = []
        }
    } catch (err) {
        error.value = '获取资源列表失败，请稍后再试'
        console.error('获取题目列表失败:', err)
        questions.value = []
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
watch(selectedCourse, (newCourse) => {
  console.log('课程选择变化:', newCourse)
  fetchQuestions() // 无论是否选中课程都获取题目列表
})

// 监听课程选择变化
watch(() => tempQuestion.value.courseId, async (newCourseId) => {
    if (newCourseId) {
        try {
            const response = await CourseApi.getCourseById(String(newCourseId))
            if (response?.code === 200 && response.data?.sections) {
                sections.value = response.data.sections
            } else {
                sections.value = []
            }
        } catch (err) {
            console.error('获取课程章节失败:', err)
            sections.value = []
        }
    } else {
        sections.value = []
    }
    tempQuestion.value.sectionId = 0
})
watch(() => generateForm.value.course_id, async (newCourseId) => {
    if (newCourseId) {
        try {
            const response = await CourseApi.getCourseById(String(newCourseId))
            if (response?.code === 200 && response.data?.sections) {
                sections.value = response.data.sections
            } else {
                sections.value = []
            }
        } catch (err) {
            console.error('获取课程章节失败:', err)
            sections.value = []
        }
    } else {
        sections.value = []
    }
    tempQuestion.value.sectionId = 0
})

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
    console.warn('=== 开始创建题目 ===')
    console.warn('当前题目数据:', JSON.stringify(tempQuestion.value, null, 2))

    // Validate question data
    if (!tempQuestion.value.content) {
        console.warn('题目内容为空')
        error.value = '请完成题目内容并设置分值'
        return
    }

    // 验证答案
    if (!tempQuestion.value.answer) {
        console.warn('未设置答案')
        error.value = '请设置正确答案'
        return
    }

    // Validate options for choice questions
    if (['singlechoice', 'multiplechoice'].includes(tempQuestion.value.type)) {
        const hasEmptyOption = tempQuestion.value.options.some(opt => !opt.text)
        if (hasEmptyOption) {
            console.warn('存在空选项')
            error.value = '请填写所有选项内容'
            return
        }
    }

    // 处理选项格式
    const formattedOptions = tempQuestion.value.type === 'fillblank' ? [] :
        tempQuestion.value.options
            .filter((opt: { key: string; text: string }) => opt.text.trim() !== '')  // 过滤掉空选项
            .map((opt: { key: string; text: string }) => opt.text)

    console.warn('格式化后的选项:', JSON.stringify(formattedOptions, null, 2))

    // 验证选项
    if (['singlechoice', 'multiplechoice'].includes(tempQuestion.value.type)) {
        if (formattedOptions.length === 0) {
            console.warn('没有有效选项')
            error.value = '请至少添加一个选项'
            return
        }
    }

    // 处理答案格式
    let formattedAnswer = tempQuestion.value.answer;

    console.warn('格式化后的答案:', formattedAnswer)

    const questionData = {
        type: tempQuestion.value.type === 'multiplechoice' ? 'singlechoice' : tempQuestion.value.type,
        content: tempQuestion.value.content,
        options: formattedOptions,  // 直接发送字符串数组
        answer: formattedAnswer,
        courseId: tempQuestion.value.courseId,
        sectionId: tempQuestion.value.sectionId,
        analysis: tempQuestion.value.explanation,
        creatorId: tempQuestion.value.creatorId
    }
    console.log(questionData)

    console.warn('准备发送到后端的数据:', JSON.stringify(questionData, null, 2))

    try {
        console.warn('开始发送请求')
        const res = await QuestionApi.createQuestion(questionData)
        console.warn('后端返回数据:', JSON.stringify(res, null, 2))

        // Reset form and refresh list
        resetUploadForm()
        showUploadForm.value = false
        fetchQuestions()

    } catch (err) {
        console.error('创建题目失败:', err)
        uploadError.value = '上传问题失败，请稍后再试'
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
                ? this.answer.split('|')
                : [];
        },
        set answerArray(values: string[]) {
            this.answer = values.join('|');
        },
    }
    uploadError.value = ''
    uploadProgress.value = 0
}

// 新增弹窗相关状态
const showDetailDialog = ref(false)
const selectedQuestion = ref<any>(null)

// 修改预览方法
const showQuestionDetail = (question: any) => {
    console.log('显示题目详情:', question);
    selectedQuestion.value = {
        ...question,
        // 如果是多选题，确保答案被正确解析
        answer: question.type === 'multiplechoice' ? question.answer : question.answer
    };
    showDetailDialog.value = true;
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

// 更新计算属性：将单选题和多选题统一为“选择题”
const filteredQuestions = computed(() => {
  if (!selectedType.value) return questions.value // 如果未选择类型，显示所有题目
  return questions.value.filter(question => {
    if (selectedType.value === 'choice') {
      return question.type === 'singlechoice' || question.type === 'multiplechoice'
    }
    return question.type === selectedType.value
  })
})

// 更新题目类型选项
const questionTypes = [
  { value: 'singlechoice', label: '单选题' },
  { value: 'multiplechoice', label: '多选题' },
  { value: 'fillblank', label: '填空题' },
  { value: 'program', label: '简答题' },
  { value: 'judge', label: '判断题' }
]

// 添加一个用于筛选题目类型的状态
const selectedType = ref('')

// 新增弹窗功能，用于填写生成题目的参数。
const showGenerateDialog = ref(false);

const addCustomType = () => {
    if (!generateForm.value.custom_types || typeof generateForm.value.custom_types !== 'object') {
        generateForm.value.custom_types = {};
    }
    const newKey = `custom_type_${Object.keys(generateForm.value.custom_types).length + 1}`;
    generateForm.value.custom_types[newKey] = 0;
};

const removeCustomType = (key: string) => {
    if (generateForm.value.custom_types && typeof generateForm.value.custom_types === 'object') {
        // Vue3 响应式对象安全删除
        const { [key]: _, ...rest } = generateForm.value.custom_types;
        generateForm.value.custom_types = { ...rest };
    }
};

const updateCustomTypeKey = (oldKey: string, newKey: string) => {
    if (
        newKey &&
        oldKey !== newKey &&
        generateForm.value.custom_types &&
        typeof generateForm.value.custom_types === 'object'
    ) {
        generateForm.value.custom_types[newKey] = generateForm.value.custom_types[oldKey];
        const { [oldKey]: _, ...rest } = generateForm.value.custom_types;
        generateForm.value.custom_types = { ...rest };
    }
};

const generateQuestions = async () => {
    console.warn('=== 开始生成题目 ===');
    openGenerateDialog('正在生成题目')
    try {
        // 只传递纯对象，避免循环引用
        const reqData = JSON.parse(JSON.stringify(generateForm.value));
        const response = await QuestionApi.generateExercise(reqData);
        console.log('生成题目响应:', response);
        console.log(Array.isArray(response.exercises))

        if (response && Array.isArray(response.exercises)) {
            for (const question of response.exercises) {
                const questionData = {
                    type: question.type,
                    content: question.question,
                    options: question.options || [],
                    answer: question.answer,
                    courseId: generateForm.value.course_id,
                    sectionId: generateForm.value.section_id,
                    analysis: question.explanation,
                    creatorId: authStore.user?.id
                };
                await QuestionApi.createQuestion(questionData);
            }
            closeGenerateDialog()
            ElMessage.success('题目生成并保存成功！');
            fetchQuestions();
        } else {
            closeGenerateDialog()
            ElMessage.error('生成题目失败，请稍后再试！');
        }
    } catch (err) {
        console.error('生成题目失败:', err);
        closeGenerateDialog()
        ElMessage.error('生成题目失败，请稍后再试！');
    } finally {
        showGenerateDialog.value = false;
    }
};

// 更新课程选择
const updateCourseSelection = (courseId: number) => {
    const selectedCourse = courses.value.find(course => course.id === courseId);
    generateForm.value.course_id = selectedCourse?.id || 0;
    generateForm.value.course_name = selectedCourse?.name || '';
};

// 新增：章节选择时同步更新 section_id 和 lesson_content
const updateSectionSelection = (sectionId: number) => {
    generateForm.value.section_id = sectionId;
    const selectedSection = sections.value.find(section => section.id === sectionId);
    generateForm.value.lesson_content = selectedSection ? selectedSection.title : '';
};

// 生成题目弹窗动画文案
const generateDialogMsgs = [
  '正在生成题目(。-ω-)✧',
  '正在生成题目📖_(:3 」∠)_',
  '正在生成题目(๑•̀ㅂ•́)و✧',
  '正在生成题目─=≡Σ((( つ•̀ω•́)つ',
]
let generateDialogInterval: number | null = null
const generateDialogMsgIndex = ref(0)
const showGenerateLoadingDialog = ref(false)
const generateDialogMsg = ref('')

function openGenerateDialog(msg: string) {
  generateDialogMsg.value = msg
  showGenerateLoadingDialog.value = true
  if (msg.startsWith('正在生成题目')) {
    generateDialogMsgIndex.value = 0
    generateDialogMsg.value = generateDialogMsgs[generateDialogMsgIndex.value]
    generateDialogInterval = window.setInterval(() => {
      generateDialogMsgIndex.value = (generateDialogMsgIndex.value + 1) % generateDialogMsgs.length
      generateDialogMsg.value = generateDialogMsgs[generateDialogMsgIndex.value]
    }, 2000)
  }
}
function closeGenerateDialog() {
  showGenerateLoadingDialog.value = false
  generateDialogMsg.value = ''
  if (generateDialogInterval) {
    clearInterval(generateDialogInterval)
    generateDialogInterval = null
  }
}
</script>

<template>
    <div class="question-bank-container">
        <div class="resource-header">
            <h2>题库中心</h2>
            <div class="header-btn-group">
                <button
                    v-if="isTeacher"
                    class="btn-primary"
                    @click="showUploadForm = !showUploadForm"
                >
                    {{ showUploadForm ? '取消新建' : '新建题目' }}
                </button>
                <button
                    v-if="isTeacher"
                    class="btn-secondary"
                    @click="showGenerateDialog = true"
                >
                    生成题目
                </button>
            </div>
        </div>

        <!-- Upload Form -->
        <div v-if="showUploadForm" class="upload-form">
            <h3>新建题目</h3>

            <div v-if="uploadError" class="error-message">{{ uploadError }}</div>

            <!-- Question Form -->
            <div class="question-form-byhand-section">

                <div class="form-row">
                    <div class="form-group form-group-half">
                        <label for="courseSelect">所属课程</label>
                        <select
                            id="courseSelect"
                            v-model="tempQuestion.courseId"
                            required
                        >
                            <option value="" disabled>请选择课程</option>
                            <option
                                v-for="course in courses.filter(c => c.id !== 0)"
                                :key="course.id"
                                :value="course.id"
                            >
                                {{ course.name }}
                            </option>
                        </select>
                    </div>

                    <div class="form-group form-group-half">
                        <label for="sectionSelect">所属章节</label>
                        <select
                            id="sectionSelect"
                            v-model="tempQuestion.sectionId"
                            :disabled="!tempQuestion.courseId"
                        >
                            <option value=0 disabled>请选择章节</option>
                            <template v-if="sections.length">
                                <option
                                    v-for="section in sections"
                                    :key="section.id"
                                    :value="section.id"
                                >
                                    {{ section.title }}
                                </option>
                            </template>
                            <option v-else disabled>加载中...</option>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label for="questionType">题目类型</label>
                    <select id="questionType" v-model="tempQuestion.type">
                        <option value="singlechoice">单选题</option>
                        <option value="multiplechoice">多选题</option>
                        <option value="judge">判断题</option>
                        <option value="program">简答题</option>
                        <option value="fillblank">填空题</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="questionContent">题目内容</label>
                    <textarea
                        id="questionContent"
                        v-model="tempQuestion.content"
                        rows="4"
                        placeholder="输入题目内容"
                    ></textarea>
                </div>

                <!-- Options for choice questions -->
                <div v-if="['singlechoice', 'multiplechoice'].includes(tempQuestion.type)" class="form-group">
                    <label>选项</label>
                    <div
                        v-for="(option, index) in tempQuestion.options"
                        :key="index"
                        class="option-row"
                    >
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
                            @click="removeOption(index)"
                            :disabled="tempQuestion.options.length <= 2"
                        >
                            ✕
                        </button>
                    </div>

                    <button
                        type="button"
                        class="btn-text"
                        @click="addOption"
                    >
                        + 添加选项
                    </button>
                </div>

                <!-- Answer for choice questions -->
                <div v-if="tempQuestion.type === 'singlechoice'" class="form-group">
                    <label for="singleAnswer">正确答案</label>
                    <select id="singleAnswer" v-model="tempQuestion.answer">
                        <option value="" disabled>选择正确答案</option>
                        <option
                            v-for="option in tempQuestion.options"
                            :key="option.key"
                            :value="option.key"
                        >
                            {{ option.key }}
                        </option>
                    </select>
                </div>

                <div v-else-if="tempQuestion.type === 'multiplechoice'" class="form-group">
                    <label>正确答案 (多选)</label>
                    <div class="checkbox-group">
                        <div
                            v-for="option in tempQuestion.options"
                            :key="option.key"
                            class="checkbox-item"
                        >
                            <input
                                :id="`answer-${option.key}`"
                                type="checkbox"
                                :checked="tempQuestion.answerArray.includes(option.key)"
                                @change="e => {
                              let arr = tempQuestion.answerArray.slice();
                              if(e.target.checked) {
                                  if(!arr.includes(option.key)) arr.push(option.key);
                              } else {
                                  arr = arr.filter(k => k !== option.key);
                              }
                              arr.sort();
                              tempQuestion.answer = arr.join('|');
                          }"
                            />
                            <label :for="`answer-${option.key}`">{{ option.key }}</label>
                        </div>
                    </div>
                </div>

                <div v-else-if="tempQuestion.type === 'judge'" class="form-group">
                    <label for="truefalseAnswer">正确答案</label>
                    <select id="truefalseAnswer" v-model="tempQuestion.answer">
                        <option value="" disabled>选择正确答案</option>
                        <option value="True">正确</option>
                        <option value="False">错误</option>
                    </select>
                </div>
                <div v-else class="form-group">
                    <label for="fillBlankAnswer">正确答案</label>
                    <input
                        id="fillBlankAnswer"
                        v-model="tempQuestion.answer"
                        type="text"
                        placeholder="输入正确答案"
                    />
                </div>

                <div class="form-group">
                    <label for="analysis">答案解析</label>
                    <textarea
                        id="analysis"
                        v-model="tempQuestion.explanation"
                        rows="4"
                        placeholder="输入答案解析"
                    ></textarea>
                </div>
            </div>

            <div class="form-actions">
                <button
                    type="button"
                    class="btn-secondary"
                    @click="resetUploadForm"
                >
                    重置
                </button>
                <button
                    type="button"
                    class="btn-primary"
                    @click="addQuestion"
                >
                    上传
                </button>
            </div>
        </div>

        <!-- 筛选框放在一行 -->
        <div class="resource-filters">
          <div class="filter-section-row">
            <div class="filter-item">
              <label for="courseFilter">按课程筛选:</label>
              <select id="courseFilter" v-model="selectedCourse">
                <option v-for="course in courses" :key="course.id" :value="course.id">
                  {{ course.name }}
                </option>
              </select>
            </div>
            <div class="filter-item">
              <label for="typeFilter">按题目类型筛选:</label>
              <select id="typeFilter" v-model="selectedType">
                <option value="">所有类型</option>
                <option v-for="type in questionTypes" :key="type.value" :value="type.value">
                  {{ type.label }}
                </option>
              </select>
            </div>
          </div>
        </div>

        <!-- Resource List -->
        <div v-if="loading" class="loading-container">加载中...</div>
        <div v-else-if="error" class="error-message">{{ error }}</div>
        <div v-else class="question-cards-container">
            <div v-if="filteredQuestions.length === 0" class="empty-state">
                暂无教学资料
            </div>
            <div v-else class="question-cards">
                <div v-for="question in filteredQuestions"
                     :key="question.id"
                     class="question-card"
                     @click="showQuestionDetail(question)">
                    <div class="question-card-header">
                        <span
                          class="question-type"
                          :class="{
                            'question-type-choice': question.type === 'singlechoice' || question.type === 'multiplechoice',
                            'question-type-fillblank': question.type === 'fillblank',
                            'question-type-program': question.type === 'program',
                            'question-type-judge': question.type === 'judge',
                          }"
                        >
                          {{
                            question.type === 'singlechoice' ? '单选题' :
                            question.type === 'multiplechoice' ? '多选题' :
                            question.type === 'fillblank' ? '填空题' :
                            question.type === 'program' ? '问答题' : '判断题'
                          }}
                        </span>
                        <span class="question-course">{{ question.courseName }}</span>
                    </div>
                    <div class="question-content">{{ question.name }}</div>
                    <div class="question-footer">
                        <span class="question-section">{{ question.sectionName || '未分类' }}</span>
                        <button class="btn-action preview" @click.stop="showQuestionDetail(question)">
                          查看详情
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 题目详情弹窗 -->
    <div v-if="showDetailDialog" class="modal-mask" @click="showDetailDialog = false">
        <div class="modal-container" @click.stop>
            <div class="modal-header">
                <h3>题目详情</h3>
                <button class="modal-close" @click="showDetailDialog = false">&times;</button>
            </div>

            <div class="modal-body" v-if="selectedQuestion">
                <div class="detail-row">
                    <label>课程名称:</label>
                    <span>{{ selectedQuestion.courseName || '-' }}</span>
                </div>
                <div class="detail-row">
                    <label>章节标题:</label>
                    <span>{{ selectedQuestion.sectionName || '-' }}</span>
                </div>
                <div class="detail-row">
                    <label>题目类型:</label>
                    <span>{{
                        selectedQuestion.type === 'singlechoice' ? '选择题' :
                        selectedQuestion.type === 'multiplechoice' ? '多选题' :
                        selectedQuestion.type === 'fillblank' ? '填空题' :
                        selectedQuestion.type === 'program' ? '问答题' : '判断题'
                    }}</span>
                </div>
                <div class="detail-row">
                    <label>题目内容:</label>
                    <div class="content-box">{{ selectedQuestion.name }}</div>
                </div>

                <!-- 选项展示 -->
                <div v-if="['singlechoice', 'multiplechoice'].includes(selectedQuestion.type)"
                     class="detail-row">
                    <label>题目选项:</label>
                    <div class="options-list">
                        <div v-for="opt in selectedQuestion.options"
                             :key="opt.key"
                             class="option-item">
                            <span class="option-key">{{ opt.key }}.</span>
                            <span class="option-text">{{ opt.text }}</span>
                            <template v-if="selectedQuestion.answer && selectedQuestion.answer.includes('|')">
                                <span v-if="selectedQuestion.answer.split('|').includes(opt.key)" class="correct-badge">✓</span>
                            </template>
                            <template v-else>
                                <span v-if="selectedQuestion.answer === opt.key || selectedQuestion.answer === opt.text" class="correct-badge">✓</span>
                            </template>
                        </div>
                    </div>
                </div>

                <div class="detail-row">
                    <label>正确答案:</label>
                    <span class="answer-text">{{ selectedQuestion.answer }}</span>
                </div>
            </div>
        </div>
    </div>

    <!-- 生成题目弹窗 -->
    <div v-if="showGenerateDialog" class="modal-mask" @click="showGenerateDialog = false">
        <div class="modal-container" @click.stop>
            <div class="modal-header">
                <h3>生成题目</h3>
                <button class="modal-close" @click="showGenerateDialog = false">&times;</button>
            </div>
            <div class="modal-body">
                <div class="form-row">
                    <div class="form-group form-group-half">
                        <label for="courseSelect">所属课程</label>
                        <select
                            id="courseSelect"
                            v-model="generateForm.course_id"
                            @change="updateCourseSelection(generateForm.course_id)"
                            required
                        >
                            <option value="" disabled>请选择课程</option>
                            <option
                                v-for="course in courses.filter(c => c.id !== 0)"
                                :key="course.id"
                                :value="course.id"
                            >
                                {{ course.name }}
                            </option>
                        </select>
                    </div>

                    <div class="form-group form-group-half">
                        <label for="sectionSelect">所属章节</label>
                        <select
                            id="sectionSelect"
                            v-model="generateForm.section_id"
                            @change="updateSectionSelection(Number(generateForm.section_id))"
                            :disabled="!generateForm.course_id"
                        >
                            <option value="" disabled>请选择章节</option>
                            <template v-if="sections.length">
                                <option
                                    v-for="section in sections"
                                    :key="section.id"
                                    :value="section.id"
                                >
                                    {{ section.title }}
                                </option>
                            </template>
                            <option v-else disabled>加载中...</option>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label for="difficulty">难度</label>
                    <select id="difficulty" v-model="generateForm.difficulty">
                        <option value="easy">简单</option>
                        <option value="medium">中等</option>
                        <option value="hard">困难</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="chooseCount">选择题数量</label>
                    <input
                        id="chooseCount"
                        v-model.number="generateForm.choose_count"
                        type="number"
                        min="0"
                        placeholder="输入选择题数量"
                    />
                </div>
                <div class="form-group">
                    <label for="fillBankCount">填空题数量</label>
                    <input
                        id="fillBankCount"
                        v-model.number="generateForm.fill_blank_count"
                        type="number"
                        min="0"
                        placeholder="输入填空题数量"
                    />
                </div>
                <div class="form-group">
                    <label for="programCount">简答题数量</label>
                    <input
                        id="programCount"
                        v-model.number="generateForm.question_count"
                        type="number"
                        min="0"
                        placeholder="输入简答题数量"
                    />
                </div>
                <div class="form-group">
                    <label>自定义题型</label>
                    <div v-for="(value, key) in generateForm.custom_types" :key="key" class="custom-type-row">
                        <input
                            :value="key"
                            @blur="updateCustomTypeKey(key, $event.target.value)"
                            type="text"
                            placeholder="题型名称"
                        />
                        <input
                            v-model.number="generateForm.custom_types[key]"
                            type="number"
                            min="0"
                            placeholder="数量"
                        />
                        <button type="button" class="btn-icon" @click="removeCustomType(key)">✕</button>
                    </div>
                    <button type="button" class="btn-text" @click="addCustomType">+ 添加题型</button>
                </div>
            </div>
            <div class="modal-footer">
                <button class="btn-secondary" @click="showGenerateDialog = false">取消</button>
                <span style="display:inline-block;width:16px"></span>
                <button class="btn-primary" @click="generateQuestions">生成</button>
            </div>
        </div>
    </div>

    <!-- 生成题目加载弹窗 -->
    <div v-if="showGenerateLoadingDialog" class="kb-dialog-mask">
      <div class="kb-dialog">
        <div class="kb-dialog-content">
          <div v-if="generateDialogMsg.startsWith('正在生成题目')">
            <div class="kb-dialog-spinner"></div>
          </div>
          <div class="kb-dialog-msg">{{ generateDialogMsg }}</div>
        </div>
      </div>
    </div>
</template>

<style scoped>
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

.modal-footer {
    margin-bottom: 20px;
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

/* 确保样式优先级更高 */
.question-type-singlechoice {
    background-color: #e6f7ff !important;
    color: #1890ff !important;
    border: 1px solid #91d5ff !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-multiplechoice {
    background-color: #f6ffed !important;
    color: #52c41a !important;
    border: 1px solid #b7eb8f !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-fillblank {
    background-color: #fffbe6 !important;
    color: #faad14 !important;
    border: 1px solid #ffe58f !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-program {
    background-color: #fff0f6 !important;
    color: #eb2f96 !important;
    border: 1px solid #ffadd2 !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-judge {
    background-color: #f0f5ff !important;
    color: #2f54eb !important;
    border: 1px solid #adc6ff !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-bank-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 1.5rem;
}
/* 单选框样式 */
.radio-group {
    display: flex;
    gap: 20px;
    margin-top: 8px;
}

.radio-item {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
}

.radio-item input[type="radio"] {
    cursor: pointer;
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

/* 新增按钮组样式，控制间距 */
.header-btn-group {
    display: flex;
    gap: 10px;
    align-items: center;
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

select:disabled {
    background-color: #f5f5f5;
    cursor: not-allowed;
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
    margin-left: 20px;
}

.btn-secondary:hover {
    background-color: #e0e0e0;
}

/* 添加不同题目类型的样式 */
.question-type-singlechoice {
    background-color: #e6f7ff !important;
    color: #1890ff !important;
    border: 1px solid #91d5ff !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-multiplechoice {
    background-color: #f6ffed !important;
    color: #52c41a !important;
    border: 1px solid #b7eb8f !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-fillblank {
    background-color: #fffbe6 !important;
    color: #faad14 !important;
    border: 1px solid #ffe58f !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-program {
    background-color: #fff0f6 !important;
    color: #eb2f96 !important;
    border: 1px solid #ffadd2 !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-judge {
    background-color: #f0f5ff !important;
    color: #2f54eb !important;
    border: 1px solid #adc6ff !important;
    border-radius: 4px;
    padding: 2px 8px;
}

.question-type-choice {
  background-color: #e6f7ff !important;
  color: #1890ff !important;
  border: 1px solid #91d5ff !important;
  border-radius: 4px;
  padding: 2px 8px;
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

.question-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); /* 减小卡片宽度 */
    gap: 16px; /* 减小卡片间距 */
    padding: 10px;
}
.question-cards-container {
    padding: 20px;
}

.question-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
    padding: 10px;
}

/* 调整题目卡片的样式，使其更小 */
.question-card {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    padding: 12px; /* 减小内边距 */
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    flex-direction: column;
    gap: 8px; /* 减小间距 */
}

.question-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.question-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px; /* 减小字体大小 */
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
    font-size: 14px; /* 减小字体大小 */
    color: #333;
    line-height: 1.4;
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
    font-size: 12px; /* 减小字体大小 */
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

.empty-state {
    text-align: center;
    padding: 40px;
    color: #999;
}

.loading-container {
    text-align: center;
    padding: 40px;
    color: #666;
}

.error-message {
    text-align: center;
    padding: 20px;
    color: #ff4d4f;
    background: #fff2f0;
    border-radius: 4px;
    margin: 20px;
}

/* 弹窗样式 */
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

.detail-row {
    margin-bottom: 16px;
}

.detail-row label {
    display: block;
    color: #666;
    margin-bottom: 8px;
    font-weight: 500;
}

.content-box {
    background: #f5f5f5;
    padding: 12px;
    border-radius: 4px;
    line-height: 1.6;
}

.options-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.option-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    background: #f5f5f5;
    border-radius: 4px;
}

.option-key {
    color: #1890ff;
    font-weight: 500;
}

.correct-badge {
    color: #67C23A;
    margin-left: 8px;
    font-weight: bold;
}

.answer-text {
    color: #1890ff;
    font-weight: 500;
}

/* 添加筛选框一行的样式 */
.filter-section-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  align-items: center;
}

.filter-item {
  display: flex;
  flex-direction: column;
}

.filter-item label {
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.filter-item select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 0.9375rem;
}

/* 生成题目弹窗样式，复用CourseSyllabus */
.kb-dialog-mask {
  position: fixed;
  z-index: 9999;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}
.kb-dialog {
  background: #fff;
  border-radius: 12px;
  min-width: 260px;
  min-height: 120px;
  box-shadow: 0 4px 24px rgba(44,110,207,0.13);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 2.5rem;
  animation: fadein 0.2s;
}
.kb-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.kb-dialog-msg {
  font-size: 1.1rem;
  color: #512da8;
  margin-top: 1rem;
  text-align: center;
}
.kb-dialog-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid #ede7f6;
  border-top: 4px solid #8e24aa;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 0.5rem;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
@keyframes fadein {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
