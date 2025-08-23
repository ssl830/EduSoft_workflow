<script setup lang="ts">
import { ref, defineProps, defineEmits, watch } from 'vue'
import { ElMessage } from 'element-plus'
import CourseApi from '../../api/course'

const props = defineProps<{
  modelValue: boolean
  course: {
    id: number
    name: string
    code: string
    teacherId: number
    objective: string
    outline: string
    assessment: string
  }
}>()

const emit = defineEmits(['update:modelValue', 'success'])

const formData = ref({
  objective: props.course.objective,
  outline: props.course.outline,
  assessment: props.course.assessment
})

const loading = ref(false)

// 监听 course 属性变化，更新表单数据
watch(() => props.course, (newCourse) => {
  formData.value = {
    objective: newCourse.objective,
    outline: newCourse.outline,
    assessment: newCourse.assessment
  }
}, { deep: true })

const handleSubmit = async () => {
  try {
    loading.value = true
    await CourseApi.updateCourse(props.course.id.toString(), {
      name: props.course.name,
      code: props.course.code,
      teacherId: props.course.teacherId,
      objective: formData.value.objective,
      outline: formData.value.outline,
      assessment: formData.value.assessment
    })
    ElMessage.success('更新成功')
    emit('success')
    emit('update:modelValue', false)
  } catch (error) {
    ElMessage.error('更新失败：' + (error as Error).message)
  } finally {
    loading.value = false
  }
}

const handleClose = () => {
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    title="编辑课程信息"
    width="50%"
    @close="handleClose"
  >
    <el-form :model="formData" label-width="100px">
      <el-form-item label="教学目标">
        <el-input
          v-model="formData.objective"
          type="textarea"
          :rows="4"
          placeholder="请输入教学目标"
        />
      </el-form-item>
      <el-form-item label="课程大纲">
        <el-input
          v-model="formData.outline"
          type="textarea"
          :rows="4"
          placeholder="请输入课程大纲"
        />
      </el-form-item>
      <el-form-item label="考核方式">
        <el-input
          v-model="formData.assessment"
          type="textarea"
          :rows="4"
          placeholder="请输入考核方式"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          确认
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style> 