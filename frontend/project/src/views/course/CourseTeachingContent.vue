<!-- 教学内容生成页面 -->
<template>
  <div class="course-teaching-content">
    <q-card class="content-card">
      <q-card-section>
        <div class="text-h6">生成教学内容</div>
        <div class="text-subtitle2">请输入课程大纲，AI将为您生成详细的教学内容</div>
      </q-card-section>

      <q-card-section>
        <q-form @submit="onSubmit" class="q-gutter-md">
          <q-input
            v-model="courseName"
            label="课程名称"
            :rules="[val => !!val || '请输入课程名称']"
          />
          
          <q-input
            v-model="courseOutline"
            type="textarea"
            label="课程大纲"
            :rules="[val => !!val || '请输入课程大纲']"
            rows="6"
          />
          
          <q-input
            v-model="expectedHours"
            type="number"
            label="预期课时"
            :rules="[
              val => !!val || '请输入预期课时',
              val => val > 0 || '课时必须大于0'
            ]"
          />
          
          <div class="row justify-end q-gutter-sm">
            <q-btn
              label="生成内容"
              type="submit"
              color="primary"
              :loading="loading"
            />
          </div>
        </q-form>
      </q-card-section>
    </q-card>

    <q-card v-if="generatedContent" class="content-card q-mt-md">
      <q-card-section>
        <div class="row items-center justify-between">
          <div class="text-h6">生成结果</div>
          <div class="row q-gutter-sm">
            <q-btn
              flat
              round
              color="primary"
              icon="save"
              @click="saveContent"
              :loading="saving"
            >
              <q-tooltip>保存到课程</q-tooltip>
            </q-btn>
            <q-btn-dropdown flat round color="primary" icon="download">
              <q-tooltip>导出内容</q-tooltip>
              <q-list>
                <q-item clickable v-close-popup @click="exportToWord">
                  <q-item-section>
                    <q-item-label>导出为Word</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="exportToMarkdown">
                  <q-item-section>
                    <q-item-label>导出为Markdown</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="exportToJson">
                  <q-item-section>
                    <q-item-label>导出为JSON</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-btn-dropdown>
          </div>
        </div>
      </q-card-section>

      <q-card-section>
        <div class="text-h6">总体教学安排</div>
        <div class="q-pa-md">
          <div class="row q-mb-md">
            <div class="col-3">总课时：</div>
            <div class="col">{{ generatedContent.totalHours }}</div>
          </div>
          <div class="row q-mb-md">
            <div class="col-3">时间分配建议：</div>
            <div class="col">{{ generatedContent.timeDistribution }}</div>
          </div>
          <div class="row q-mb-md">
            <div class="col-3">教学建议：</div>
            <div class="col">{{ generatedContent.teachingAdvice }}</div>
          </div>
        </div>

        <div class="text-h6 q-mt-lg">课时详细内容</div>
        <div class="q-pa-md">
          <q-expansion-item
            v-for="(lesson, index) in generatedContent.lessons"
            :key="index"
            :label="`第${index + 1}课时：${lesson.title}`"
            header-class="text-primary"
          >
            <q-card>
              <q-card-section>
                <div class="row q-mb-md">
                  <div class="col-3">建议课时：</div>
                  <div class="col">{{ lesson.suggestedHours }}</div>
                </div>
                <div class="row q-mb-md">
                  <div class="col-3">知识点讲解：</div>
                  <div class="col">{{ lesson.content }}</div>
                </div>
                <div class="row q-mb-md">
                  <div class="col-3">实训练习：</div>
                  <div class="col">{{ lesson.practiceContent }}</div>
                </div>
                <div class="row q-mb-md">
                  <div class="col-3">教学指导：</div>
                  <div class="col">{{ lesson.teachingGuidance }}</div>
                </div>
                <div class="row q-mb-md">
                  <div class="col-3">知识点来源：</div>
                  <div class="col">
                    <div v-for="(source, idx) in lesson.knowledgeSources" :key="idx">
                      - {{ source }}
                    </div>
                  </div>
                </div>
              </q-card-section>
            </q-card>
          </q-expansion-item>
        </div>
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { useQuasar } from 'quasar'
import { generateTeachingContent, saveTeachingContent } from '@/api/ai'
import { exportToWord, exportToJson, exportToMarkdown } from '@/utils/export'
import type { TeachingContentResponse } from '@/api/ai'

const route = useRoute()
const $q = useQuasar()

const courseName = ref('')
const courseOutline = ref('')
const expectedHours = ref(0)
const loading = ref(false)
const saving = ref(false)
const generatedContent = ref<TeachingContentResponse | null>(null)

async function onSubmit() {
  try {
    loading.value = true
    const response = await generateTeachingContent({
      courseName: courseName.value,
      courseOutline: courseOutline.value,
      expectedHours: expectedHours.value
    })
    generatedContent.value = response
    $q.notify({
      type: 'positive',
      message: '教学内容生成成功'
    })
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: '生成失败：' + (error instanceof Error ? error.message : '未知错误')
    })
  } finally {
    loading.value = false
  }
}

async function saveContent() {
  if (!generatedContent.value) return
  
  try {
    saving.value = true
    await saveTeachingContent({
      courseName: courseName.value,
      content: generatedContent.value
    })
    $q.notify({
      type: 'positive',
      message: '保存成功'
    })
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: '保存失败：' + (error instanceof Error ? error.message : '未知错误')
    })
  } finally {
    saving.value = false
  }
}

function exportToWord() {
  if (!generatedContent.value) return
  exportToWord(generatedContent.value, courseName.value)
}

function exportToJson() {
  if (!generatedContent.value) return
  exportToJson(generatedContent.value, courseName.value)
}

function exportToMarkdown() {
  if (!generatedContent.value) return
  exportToMarkdown(generatedContent.value, courseName.value)
}
</script>

<style scoped>
.course-teaching-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.content-card {
  width: 100%;
}
</style> 