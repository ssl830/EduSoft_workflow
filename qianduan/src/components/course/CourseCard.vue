<script setup lang="ts">
interface Course {
  id: string;
  title: string;
  description?: string;
  teacher?: string;
}

const props = defineProps<{ id: string; title: string; description?: string; teacher?: string }>();
</script>
<script lang="ts">
export default {
  name: 'CourseCard' // 显式设置组件名
}
</script>
<template>
  <router-link :to="`/course/${props.id}`" class="course-card">
    <div class="course-header">
      <h3 class="course-title">{{ props.title }}</h3>
      <span class="course-code">{{ props.description }}</span>
    </div>
    
    <div class="course-content">
      <p class="course-teacher">教师: {{ props.teacher }}</p>
    </div>
    
    <div class="card-footer">
      <span class="view-details">查看详情</span>
    </div>
  </router-link>
</template>

<style scoped>
.course-card {
  display: block;
  background-color: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  text-decoration: none;
  color: inherit;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.course-header {
  padding: 1.25rem 1.25rem 0.75rem;
  border-bottom: 1px solid #f0f0f0;
}

.course-title {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: #333;
}

.course-code {
  font-size: 0.875rem;
  color: #757575;
}

.course-content {
  padding: 1.25rem;
  min-height: 100px;
}

.course-teacher {
  margin: 0 0 1rem;
  font-size: 0.9375rem;
}

.card-footer {
  padding: 0.75rem 1.25rem;
  background-color: #f9f9f9;
  border-top: 1px solid #f0f0f0;
  text-align: right;
}

.view-details {
  font-size: 0.875rem;
  color: #2c6ecf;
  font-weight: 500;
}
</style>