<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import ManageApi from "@/api/management.ts";
import ExerciseApi from '@/api/exercise'
import router from "@/router"
import ClassApi from "@/api/class.ts";

const teachers = ref<{id: number, username: string}[]>([])
const courses = ref<{id: number, name: string}[]>([])
const classes = ref<{id: number, courseName: string, className: string}[]>([])
const exercises = ref<any[]>([])
const selectedTeacher = ref(-1)
const selectedClass = ref('')

const loadingTeachers = ref(false)
const loadingClasses = ref(false)
const loadingExercises = ref(false)
const error = ref('')

// 获取教师列表
const fetchTeachers = async () => {
  loadingTeachers.value = true
  try {
    const res = await ManageApi.getTeachersList()
    teachers.value = res.data || []
  } catch (e) {
    error.value = '获取教师列表失败'
  } finally {
    loadingTeachers.value = false
  }
}

// 获取班级列表
const fetchClasses = async (teacherId: number) => {
  if (!teacherId) {
    classes.value = []
    return
  }
  loadingClasses.value = true
  try {
    // 假设有此API：CourseApi.getCourseClasses(courseId)
    const res = await ClassApi.getUserClasses(teacherId)
    classes.value = res.data || []
      console.log(classes.value)
  } catch (e) {
    error.value = '获取班级列表失败'
  } finally {
    loadingClasses.value = false
  }
}

// 获取练习列表
const fetchExercises = async (classId: string) => {
  if (!classId) {
    exercises.value = []
    return
  }
  loadingExercises.value = true
  try {
    // 注意：此处传入classId
    const res = await ExerciseApi.getPracticeTeachList(classId)
    exercises.value = res.data || []
  } catch (e) {
    error.value = '获取练习列表失败'
  } finally {
    loadingExercises.value = false
  }
}

onMounted(() => {
  fetchTeachers()
})

watch(selectedTeacher, (val) => {
  selectedClass.value = ''
  courses.value = []
  classes.value = []
  exercises.value = []
  if (val) fetchClasses(val)
})

watch(selectedClass, (val) => {
  exercises.value = []
  if (val) fetchExercises(val)
})

const showExerciseList = computed(() => (selectedTeacher.value != -1) && (selectedClass.value != ''))

// 操作：查看练习
const viewExercise = (exerciseId: number) => {
  router.push({ name: 'ExerciseEdit', params: { id: exerciseId } })
}

// 操作：编辑练习
const editExercise = (exerciseId: number) => {
  router.push({ name: 'ExerciseEdit', params: { id: exerciseId } })
}

const deleteExercise = async (exerciseId: string) => {
  if (!confirm('确定要删除此练习吗？')) return
  try {
    await ExerciseApi.deleteExercise(exerciseId)
    exercises.value = exercises.value.filter(e => e.id !== exerciseId)
    alert('练习已删除')
  } catch (e) {
    alert('删除练习失败，请稍后再试')
  }
}
</script>

<template>
  <div class="exercise-list-outer">
    <div class="exercise-list-page">
      <div class="filters-bar">
        <div class="filters-card">
          <div class="filter-group">
            <label>教师：</label>
            <select v-model="selectedTeacher" :disabled="loadingTeachers">
              <option value=-1>请选择教师</option>
              <option v-for="t in teachers" :key=t.id :value=t.id>{{ t.username }}</option>
            </select>
          </div>
          <div class="filter-group">
            <label>班级：</label>
            <select v-model="selectedClass" :disabled="!selectedTeacher || loadingClasses">
              <option value=''>请选择班级</option>
              <option v-for="cls in classes" :key="cls.id" :value="cls.id">{{ cls.courseName }}. {{ cls.className }}</option>
            </select>
          </div>
        </div>
      </div>
      <div v-if="!showExerciseList" class="empty-hint">
        请选择教师和班级
      </div>
      <div v-else>
        <div v-if="loadingExercises" class="loading-container">加载中...</div>
        <div v-else-if="error" class="error-message">{{ error }}</div>
        <div v-else-if="exercises.length === 0" class="empty-state">
          暂无练习
        </div>
        <div v-else class="exercise-table-wrapper">
          <table class="exercise-table">
            <thead>
              <tr>
                <th>练习ID</th>
                <th>练习名称</th>
                <th>开始时间</th>
                <th>截止时间</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="exercise in exercises" :key="exercise.id">
                <td>{{ exercise.id }}</td>
                <td>{{ exercise.title }}</td>
                <td>{{ exercise.startTime ? exercise.startTime : '-' }}</td>
                <td>{{ exercise.endTime ? exercise.endTime : '-' }}</td>
                <td>{{ exercise.createdAt }}</td>
                <td class="actions">
                  <button class="btn-action download" @click="viewExercise(exercise.id)">查看</button>
                  <button class="btn-action preview" @click="editExercise(exercise.id)">编辑</button>
                  <button class="btn-action preview" @click="deleteExercise(exercise.id)">删除</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.exercise-list-outer {
  min-height: calc(100vh - 64px);
  width: calc(100vw - 240px);
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: stretch;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.exercise-list-page {
  flex: 1;
  min-height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-top: 3rem;
  padding-bottom: 2rem;
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

.empty-state {
  padding: 2rem;
  text-align: center;
  background-color: #f5f5f5;
  border-radius: 8px;
  color: #757575;
}

.exercise-table-wrapper {
  overflow-x: auto;
  max-height: 600px;
  overflow-y: auto;
}

.exercise-table {
  width: 100%;
  border-collapse: collapse;
}

.exercise-table th,
.exercise-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.exercise-table th {
  background-color: #f5f5f5;
  font-weight: 600;
}

.exercise-table tr:hover {
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
</style>
