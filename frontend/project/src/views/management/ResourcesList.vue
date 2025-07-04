<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import CourseResourceList from '@/components/course/CourseResourceList.vue'
import CourseApi from '@/api/course'
import ManageApi from "@/api/management.ts";

const teachers = ref<{id: number, username: string}[]>([])
const courses = ref<{id: number, name: string}[]>([])
const selectedTeacher = ref('')
const selectedCourse = ref('')

const loadingTeachers = ref(false)
const loadingCourses = ref(false)
const error = ref('')

// 获取教师列表
const fetchTeachers = async () => {
  loadingTeachers.value = true
  try {
    const res = await ManageApi.getTeachersList()
      console.log(res)
    teachers.value = res.data || []
  } catch (e) {
    error.value = '获取教师列表失败'
  } finally {
    loadingTeachers.value = false
  }
}

// 获取课程列表
const fetchCourses = async (teacherId: string) => {
  if (!teacherId) {
    courses.value = []
    return
  }
  loadingCourses.value = true
  try {
    const res = await CourseApi.getUserCourses(teacherId)
    courses.value = res.data || []
  } catch (e) {
    error.value = '获取课程列表失败'
  } finally {
    loadingCourses.value = false
  }
}

onMounted(() => {
  fetchTeachers()
})

watch(selectedTeacher, (val) => {
  selectedCourse.value = ''
  if (val) fetchCourses(val)
  else courses.value = []
})

const showResourceList = computed(() => selectedTeacher.value && selectedCourse.value)

const selectedCourseObj = computed(() =>
  courses.value.find(c => String(c.id) === String(selectedCourse.value))
)
</script>

<template>
  <div class="resource-list-outer">
    <div class="resource-list-page">
      <div class="filters-bar">
        <div class="filters-card">
          <div class="filter-group">
            <label>教师：</label>
            <select v-model="selectedTeacher" :disabled="loadingTeachers">
              <option value="">请选择教师</option>
              <option v-for="t in teachers" :key="t.id" :value="t.id">{{ t.username }}</option>
            </select>
          </div>
          <div class="filter-group">
            <label>课程：</label>
            <select v-model="selectedCourse" :disabled="!selectedTeacher || loadingCourses">
              <option value="">请选择课程</option>
              <option v-for="c in courses" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
        </div>
      </div>
      <div v-if="!showResourceList" class="empty-hint">
        请选择教师和课程
      </div>
      <div v-else>
        <CourseResourceList
          :courseId="selectedCourse"
          :isTeacher="true"
          :course="selectedCourseObj"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.resource-list-outer {
  min-height: calc(100vh - 64px);
  width: calc(100vw - 240px); /* 减去侧栏宽度 */
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: stretch;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  /* 保证背景全屏 */
}

.resource-list-page {
  flex: 1;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 3rem;
  padding-bottom: 2rem;
  /* 保证内容不被头栏遮挡 */
}

.filters-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 2.5rem;
  width: 100%;
}

.filters-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 2px 12px 0 rgba(120, 120, 200, 0.08);
  padding: 1.5rem 2.5rem;
  display: flex;
  gap: 2.5rem;
  align-items: center;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 0.7rem;
}

.filter-group label {
  font-weight: 600;
  color: #4b5e8a;
  font-size: 1.08rem;
}

.filter-group select {
  padding: 0.45rem 1.2rem;
  border-radius: 7px;
  border: 1.5px solid #bfc8e6;
  font-size: 1rem;
  background: #f7faff;
  color: #3a466e;
  transition: border 0.2s, box-shadow 0.2s;
  outline: none;
  box-shadow: 0 1px 4px 0 rgba(120, 120, 200, 0.04);
}

.filter-group select:focus, .filter-group select:hover {
  border: 1.5px solid #7b9ffb;
  background: #f0f4ff;
}

.filter-group select:disabled {
  background: #f2f2f2;
  color: #aaa;
  cursor: not-allowed;
}

.empty-hint {
  text-align: center;
  margin: 5rem 0 0 0;
  font-size: 1rem;
  color: #7b8bb7;
  letter-spacing: 1px;
  font-weight: 500;
  background: rgba(255,255,255,0.7);
  border-radius: 12px;
  padding: 1.2rem 0;
  box-shadow: 0 2px 12px 0 rgba(120, 120, 200, 0.06);
  width: 100%;
  max-width: 400px;
  margin-left: auto;
  margin-right: auto;
}
</style>
