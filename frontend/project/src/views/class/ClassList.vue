<script setup lang="ts">
import {ref, onMounted} from 'vue'
import { useAuthStore } from '../../stores/auth'
import ClassCard from '../../components/class/ClassCard.vue'
import ClassApi from '../../api/class'
import CourseApi from '../../api/course'  // 新增课程API引入

const authStore = useAuthStore()
const classes = ref([])
const courses = ref([])
const loading = ref(true)
const error = ref('')
const errorDialog = ref('')

// 弹窗相关状态
const showCreateClassDialog = ref(false)
const newClass = ref({
  classTime: '',
  classCode: '',
  courseId: 0  // 新增课程ID字段
})

const showJoinClassDialog = ref(false)
const joinClassCode = ref('')

// 加入班级功能
const joinClass = async () => {
  if (!joinClassCode.value.trim()) {
    errorDialog.value = '请输入班级代码'
    return
  }

  try {
    await ClassApi.joinClass(String(authStore.user?.id), joinClassCode.value)

    // 重置表单并关闭弹窗
    resetJoinForm()

    // 刷新班级列表
    await fetchClasses()
  } catch (err) {
    errorDialog.value = '加入班级失败: ' + (err.response?.data?.message || '请检查代码是否正确')
    console.error(err)
  }
}

// 重置加入表单
const resetJoinForm = () => {
  showJoinClassDialog.value = false
  joinClassCode.value = ''
  errorDialog.value = ''
}

// 获取班级列表（根据身份适配接口）
const fetchClasses = async () => {
  try {
    let response
    if (authStore.userRole === 'teacher') {
      // 老师用专用接口，避免查到自己是成员
      response = await ClassApi.getTeacherClasses(authStore.user?.id)
    } else {
      // 学生用原有接口
      response = await ClassApi.getUserClasses(authStore.user?.id)
    }
    console.log('获取班级列表响应:', response)

    if (response.code === 200 && Array.isArray(response.data)) {
      classes.value = response.data.map(classItem => ({
        ...classItem,
        name: classItem.className || classItem.name || '未命名班级',
        code: classItem.classCode || classItem.code || '无代码',
        courseName: classItem.courseName || '未知课程',
        createdAt: classItem.createdAt || classItem.joinedAt || new Date().toISOString()
      }))
      console.log('处理后的班级数据:', classes.value)
    } else {
      classes.value = []
      error.value = response.message || '获取班级列表失败'
    }
  } catch (err) {
    classes.value = []
    error.value = '获取班级列表失败，请稍后再试'
    console.error('获取班级列表错误:', err)
  }
}

// 获取用户课程列表
const fetchCourses = async () => {
  try {
    const response = await CourseApi.getUserCourses(authStore.user?.id)
      courses.value = response.data
  } catch (err) {
    console.error('获取课程列表失败:', err)
  }
}

const resetUploadForm = () => {
  showCreateClassDialog.value = false
  newClass.value.classTime = ''
  newClass.value.classCode = ''
  newClass.value.courseId = 0
}

// 可以添加更严格的时间有效性验证（可选）
const isValidTime = (time: string) => {
  const hours = parseInt(time.slice(0, 2))
  const minutes = parseInt(time.slice(2))
  return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59
}

// 创建班级
const createClass = async () => {
  if (!newClass.value.classTime || !newClass.value.classCode || newClass.value.courseId == 0) {
    errorDialog.value = '请填写所有必填字段'
    return
  }

  const timeFormat = /^\d{4}-\d{4}$/
  if (!timeFormat.test(newClass.value.classTime)) {
    errorDialog.value = '时间格式应为 0800-0935 格式'
    return
  }

  // 在格式验证后添加时间有效性检查
  const [startTime, endTime] = newClass.value.classTime.split('-')
  if (!isValidTime(startTime) || !isValidTime(endTime)) {
    errorDialog.value = '无效的时间值（时间范围应为0000-2359）'
    return
  }

  const toMinutes = (time: string) => {
    const hours = parseInt(time.slice(0, 2))
    const minutes = parseInt(time.slice(2))
    return hours * 60 + minutes
  }

  const startMinutes = toMinutes(startTime)
  const endMinutes = toMinutes(endTime)

  // 验证时间顺序
  if (startMinutes >= endMinutes) {
    errorDialog.value = '开始时间必须早于结束时间'
    return
  }

  try {
    await ClassApi.createClass({
      courseId: newClass.value.courseId,
      name: newClass.value.classTime,
      code: newClass.value.classCode,
    })

    // 重置表单
    resetUploadForm()

    // 刷新列表
    await fetchClasses()
  } catch (err) {
    errorDialog.value = '创建班级失败: ' + (err.response?.data?.message || '请稍后再试')
    console.error(err)
  }
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '未知时间'
  try {
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return '未知时间'
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    console.error('日期格式化错误:', e)
    return '未知时间'
  }
}

onMounted(async () => {
  if (authStore.isAuthenticated) {
    try {
      await Promise.all([fetchClasses(), fetchCourses()])
    } finally {
      loading.value = false
    }
  } else {
    loading.value = false
  }
})
</script>

<template>
  <div class="home-container">
    <section v-if="authStore.isAuthenticated" class="courses-section">
      <div class="section-header">
        <h2>我的班级</h2>
        <button
            v-if="authStore.userRole === 'student'"
            class="btn-primary"
            @click="showJoinClassDialog = true"
        >
          加入班级
        </button>
        <button
            v-if="authStore.userRole === 'teacher'"
            class="btn-primary"
            @click="showCreateClassDialog = true"
        >
          创建班级
        </button>
      </div>

      <!-- 班级列表 -->
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="error" class="error-message">{{ error }}</div>
      <div v-else-if="classes.length === 0" class="empty-state">
        <p>您还没有参与任何班级</p>
      </div>
      <div v-else class="class-grid">
        <div v-for="classItem in classes" :key="classItem.id" class="class-card">
          <div class="class-info">
            <h3>{{ classItem.name }}</h3>
            <p>班级代码: {{ classItem.code }}</p>
            <p>课程: {{ classItem.courseName }}</p>
            <p>创建时间: {{ formatDate(classItem.createdAt) }}</p>
          </div>
          <div class="class-actions">
            <router-link
                :to="`/class/${classItem.id}`"
                class="btn-primary"
            >
              进入班级
            </router-link>
          </div>
        </div>
      </div>

      <!-- 加入班级弹窗 -->
      <div v-if="showJoinClassDialog" class="dialog-overlay">
        <div class="create-class-dialog">
          <h3>加入班级</h3>
          <div class="form-group">
            <label>班级代码</label>
            <input
                v-model="joinClassCode"
                type="text"
                placeholder="输入班级代码"
            >
          </div>
          <div class="dialog-actions">
            <button class="btn-secondary" @click="resetJoinForm()">
              取消
            </button>
            <button class="btn-primary" @click="joinClass">
              确定
            </button>
          </div>
          <div v-if="errorDialog" class="error-message">{{ errorDialog }}</div>
        </div>
      </div>

      <!-- 创建班级弹窗 -->
      <div v-if="showCreateClassDialog" class="dialog-overlay">
        <div class="create-class-dialog">
          <h3>新建班级</h3>
          <div class="form-group">
            <label>所属课程</label>
            <select v-model="newClass.courseId">
              <option value="" disabled>请选择课程</option>
              <option
                  v-for="course in courses"
                  :key="course.id"
                  :value="course.id"
              >
                {{ course.name }} ({{ course.code }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>授课时间</label>
            <input
                v-model="newClass.classTime"
                type="text"
                placeholder="例如：0800-0935"
            >
          </div>
          <div class="form-group">
            <label>班级代码</label>
            <input
                v-model="newClass.classCode"
                type="text"
                placeholder="输入班级唯一代码"
            >
          </div>
          <div class="dialog-actions">
            <button class="btn-secondary" @click="resetUploadForm">
              取消
            </button>
            <button class="btn-primary" @click="createClass">
              确定
            </button>
          </div>
          <div v-if="errorDialog" class="error-message">{{ errorDialog }}</div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1.5rem;
}

.courses-section {
  margin-bottom: 2rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h2 {
  margin: 0;
  color: #333;
}

.class-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-top: 1rem;
}

.class-card {
  background: #fff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.class-info h3 {
  margin: 0 0 0.5rem 0;
  color: #333;
}

.class-info p {
  margin: 0.25rem 0;
  color: #666;
  font-size: 0.9rem;
}

.class-actions {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
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

.empty-state {
  text-align: center;
  padding: 2rem;
  color: #666;
  background: #f5f5f5;
  border-radius: 8px;
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

.create-class-dialog {
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

.form-group input,
.form-group select {
  width: 100%;
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
  text-decoration: none;
  display: inline-block;
}

.btn-secondary {
  background: #757575;
  color: #fff;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #1565c0;
}

.btn-secondary:hover {
  background: #616161;
}
</style>
