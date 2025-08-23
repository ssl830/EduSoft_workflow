<script setup lang="ts">
import { defineProps } from 'vue'

interface ClassInfo {
    id: string;
    name: string;
    classCode: string;
    studentCount: number;
}

interface Course {
    id: number;
    teacherID: string;
    teacherName: string;
    name: string;
    code: string;
    outline: string;
    objective: string;
    assessment: string;
    created_at: string;
    sections: Section[];
    classes: ClassInfo[];
}

interface Section {
    id: string;
    title: string;
    sortOrder: string;
}

const props = defineProps<{
  course: Course;
}>()
</script>

<template>
  <router-link :to="`/course/${props.course.id}`" class="course-card">
    <div class="course-header">
      <h3 class="course-title">{{ course.name }} {{ course.code }}</h3>
      <span class="course-code">{{ course.teacherName }}</span>
    </div>

    <div class="course-content">
      <div class="course-info">
        <div class="info-item">
          <span class="label">班级数:</span>
          <span class="value">{{ course.classes?.length || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="label">章节数:</span>
          <span class="value">{{ course.sections?.length || 0 }}</span>
        </div>
      </div>
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

.course-info {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.info-item .label {
  color: #666;
  font-size: 0.875rem;
}

.info-item .value {
  color: #333;
  font-weight: 500;
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
