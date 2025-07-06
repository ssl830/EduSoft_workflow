<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import ClassApi from '../../api/class'
import CourseApi from '../../api/course'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const error = ref('')

const classForm = reactive({
  courseId: 0,
  classTime: '',
  classCode: ''
})

const courses = ref(<any>[])

const resetClassForm = () => {
  classForm.courseId = 0
  classForm.classTime = ''
  classForm.classCode = ''
}

const isValidTime = (time: string) => {
  const hours = parseInt(time.slice(0, 2))
  const minutes = parseInt(time.slice(2))
  return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59
}

const submitClass = async () => {
  if (!authStore.user?.id) {
    error.value = '请先登录'
    return
  }

  if (!classForm.courseId || !classForm.classTime || !classForm.classCode) {
    error.value = '请填写所有必填字段'
    return
  }
  const timeFormat = /^\d{4}-\d{4}$/
  if (!timeFormat.test(classForm.classTime)) {
    error.value = '时间格式应为 0800-0935 格式'
    return
  }
  const [startTime, endTime] = classForm.classTime.split('-')
  if (!isValidTime(startTime) || !isValidTime(endTime)) {
    error.value = '无效的时间值（时间范围应为0000-2359）'
    return
  }
  const toMinutes = (time: string) => {
    const hours = parseInt(time.slice(0, 2))
    const minutes = parseInt(time.slice(2))
    return hours * 60 + minutes
  }
  if (toMinutes(startTime) >= toMinutes(endTime)) {
    error.value = '开始时间必须早于结束时间'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const classData = {
      courseId: classForm.courseId,
      name: classForm.classTime,
      classCode: classForm.classCode,
      creatorId: authStore.user.id
    }
    console.log("classData:", classData)
    console.log()
    const resp:any = await ClassApi.createClass(classData)
    if (resp.code !== 200) {
      error.value = resp.message || '创建班级失败，请稍后再试'
    } else {
      await router.push('/class')
      resetClassForm()
    }
  } catch (err) {
    error.value = '创建班级失败，请稍后再试'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const fetchCourses = async () => {
  try {
    const response = await CourseApi.getUserCourses(authStore.user?.id)
    courses.value = response.data || []
  } catch (err) {
    courses.value = []
  }
}

onMounted(() => {
  if (!authStore.user?.id) {
    error.value = '请先登录'
    router.push('/login')
    return
  }
  fetchCourses()
})
</script>

<template>
  <div class="class-create-container">
    <h1 class="page-title">创建班级</h1>
    <div v-if="error" class="error-message">{{ error }}</div>
    <div class="form-step">
      <div class="form-row">
        <div class="form-group form-group-half">
          <label>所属课程</label>
          <select v-model="classForm.courseId">
            <option value="0" disabled>请选择课程</option>
            <option v-for="course in courses" :key="course.id" :value="course.id">
              {{ course.name }} ({{ course.code }})
            </option>
          </select>
        </div>
      </div>
      <div class="form-group">
        <label>授课时间</label>
        <input
          v-model="classForm.classTime"
          type="text"
          placeholder="例如：0800-0935"
        />
      </div>
      <div class="form-group">
        <label>班级代码</label>
        <input
          v-model="classForm.classCode"
          type="text"
          placeholder="输入班级唯一代码"
        />
      </div>
    </div>
    <div class="step-navigation">
      <button
        type="button"
        class="btn-primary"
        @click="submitClass"
        :disabled="loading"
      >
        创建班级
        <span v-if="loading">...</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.class-create-container {
    max-width: 1000px;
    margin: 0 auto;
    padding: 1.5rem;
}
.page-title {
  margin-bottom: 1.5rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #e0e0e0;
}
.form-step {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 1.5rem;
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
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}
.form-group input:focus,
.form-group select:focus {
  border-color: #2c6ecf;
  outline: none;
}
.form-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}
.form-group-half {
  flex: 1;
}
.step-navigation {
    display: flex;
    justify-content: space-between;
    margin-top: 1.5rem;
}
.btn-primary {
  background-color: #2c6ecf;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 4px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: background-color 0.2s;
}
.btn-primary:hover {
  background-color: #215bb4;
}
.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 0.75rem;
  border-radius: 4px;
  margin-bottom: 1.25rem;
}
</style>
