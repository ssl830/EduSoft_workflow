<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import CourseApi from '../../api/course'

import CourseSyllabus from '../../components/course/CourseSyllabus.vue'
import CourseResourceList from '../../components/course/CourseResourceList.vue'
import CourseVideoList from '../../components/course/CourseVideoList.vue'

// 类型定义
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface Course {
  id: number
  name: string
  code: string
  teacherName: string
  studentCount: number
  practiceCount: number
  homeworkCount: number
  objective: string
  outline: string
  assessment: string
}

const route = useRoute()
const authStore = useAuthStore()
const courseId = computed(() => route.params.id as string)

const loading = ref(true)
const error = ref('')
const course = ref<Course | null>(null)
const activeTab = ref('syllabus') // 'syllabus', 'resources', 'video'

const isTeacherOrTutor = computed(() => {
  return ['teacher', 'tutor'].includes(authStore.userRole)
})

onMounted(async () => {
  try {
    // 兼容类型
    const response = await CourseApi.getCourseById(courseId.value) as unknown as ApiResponse<Course>
    if (response && response.code === 200 && response.data) {
      course.value = response.data
    } else {
      error.value = '获取课程详情失败'
      console.error('获取课程详情失败: 响应数据格式不正确', response)
    }
  } catch (err) {
    error.value = '获取课程详情失败，请稍后再试'
    console.error('获取课程详情错误:', err)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="course-detail-container">
    <div v-if="loading" class="loading-container">加载中...</div>
    <div v-else-if="error" class="error-message">{{ error }}</div>
    <template v-else-if="course">
      <header class="course-header">
        <h1>{{ course.name }} {{ course.code }}</h1>
        <div class="course-info" v-if="activeTab === 'syllabus'">
          <p class="info-item">课程代码: {{ course.code }}</p>
          <p class="info-item">教师: {{ course.teacherName }}</p>
          <p class="info-item">学生人数: {{ course.studentCount }}人</p>
          <p class="info-item">练习题数: {{ course.practiceCount }}题</p>
          <p class="info-item">作业数量: {{ course.homeworkCount }}个</p>
          <p class="info-item">课程目标: {{ course.objective }}</p>
          <p class="info-item">课程大纲: {{ course.outline }}</p>
          <p class="info-item">考核方式: {{ course.assessment }}</p>
        </div>
      </header>

      <div class="course-content">
        <!-- 左侧 Tab -->
        <div class="course-tabs">
          <button
            :class="['tab-button', { active: activeTab === 'syllabus' }]"
            @click="activeTab = 'syllabus'"
          >
            课程概况
          </button>
          <button
            :class="['tab-button', { active: activeTab === 'resources' }]"
            @click="activeTab = 'resources'"
          >
            教学资料
          </button>
          <button
            :class="['tab-button', { active: activeTab === 'video' }]"
            @click="activeTab = 'video'"
          >
            视频学习
          </button>
        </div>

        <!-- 右侧内容区 -->
        <div class="course-main-content">
          <CourseSyllabus
            v-if="activeTab === 'syllabus'"
            :course="course"
          />
          <CourseResourceList
            v-else-if="activeTab === 'resources'"
            :course-id="courseId"
            :is-teacher="isTeacherOrTutor"
          />
          <CourseVideoList
            v-else-if="activeTab === 'video'"
            :course-id="courseId"
            :is-teacher="isTeacherOrTutor"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.course-detail-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 1.5rem;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 3rem 0;
}

.error-message {
  color: #e53935;
  padding: 1rem;
  text-align: center;
  background-color: #ffebee;
  border-radius: 8px;
  margin: 1rem 0;
}

.course-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

.course-code {
  color: #616161;
  margin-top: 0.5rem;
}

.course-content {
  display: flex;
  margin-bottom: 2rem;
  gap: 1.5rem;
}

.course-tabs {
  width: 200px;
  flex-shrink: 0;
}

.tab-button {
  display: block;
  width: 100%;
  padding: 1rem;
  margin-bottom: 0.5rem;
  text-align: left;
  background: none;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}

.tab-button:hover {
  background-color: #f5f5f5;
}

.tab-button.active {
  background-color: #e3f2fd;
  color: #2c6ecf;
  border-left: 3px solid #2c6ecf;
}

.course-main-content {
  flex: 1;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.course-discussion-section {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e0e0e0;
}

@media (max-width: 768px) {
  .course-content {
    flex-direction: column;
  }

  .course-tabs {
    width: 100%;
    display: flex;
    overflow-x: auto;
    margin-bottom: 1rem;
  }

  .tab-button {
    flex: 0 0 auto;
    margin-right: 0.5rem;
    margin-bottom: 0;
    white-space: nowrap;
  }
}
</style>
