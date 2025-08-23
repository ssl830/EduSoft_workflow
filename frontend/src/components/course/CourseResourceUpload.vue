<template>
  <div class="course-resource-upload">
    <q-card class="upload-card">
      <q-card-section>
        <div class="text-h6">上传课程资料</div>
        <div class="text-subtitle2">支持PDF、Word、TXT、Markdown等格式</div>
      </q-card-section>

      <q-card-section>
        <q-file
          v-model="files"
          label="选择文件"
          filled
          multiple
          accept=".pdf,.docx,.txt,.md,.json,.csv,.xlsx,.html"
          style="max-width: 300px"
          @update:model-value="handleFileSelect"
        >
          <template v-slot:prepend>
            <q-icon name="attach_file" />
          </template>
        </q-file>

        <div class="q-mt-md">
          <q-list bordered separator>
            <q-item v-for="file in uploadQueue" :key="file.name">
              <q-item-section>
                <q-item-label>{{ file.name }}</q-item-label>
                <q-item-label caption>
                  {{ formatFileSize(file.size) }}
                </q-item-label>
              </q-item-section>

              <q-item-section side>
                <div class="row items-center">
                  <q-circular-progress
                    v-if="file.status === 'uploading'"
                    :value="file.progress"
                    size="xs"
                    color="primary"
                    class="q-mr-sm"
                  />
                  <q-icon
                    v-else-if="file.status === 'success'"
                    name="check_circle"
                    color="positive"
                  />
                  <q-icon
                    v-else-if="file.status === 'error'"
                    name="error"
                    color="negative"
                  />
                  <q-btn
                    v-if="file.status !== 'success'"
                    flat
                    round
                    dense
                    icon="close"
                    @click="removeFile(file)"
                  />
                </div>
              </q-item-section>
            </q-item>
          </q-list>
        </div>
      </q-card-section>

      <q-card-actions align="right">
        <q-btn
          label="开始上传"
          color="primary"
          :loading="uploading"
          :disabled="uploadQueue.length === 0"
          @click="startUpload"
        />
      </q-card-actions>
    </q-card>

    <!-- 上传进度 -->
    <q-dialog v-model="showProgress">
      <q-card style="min-width: 350px">
        <q-card-section>
          <div class="text-h6">上传进度</div>
        </q-card-section>

        <q-card-section class="q-pt-none">
          <div class="text-subtitle1">正在处理文件...</div>
          <div class="text-caption">{{ currentFile?.name }}</div>
          <q-linear-progress
            :value="totalProgress"
            class="q-mt-md"
          />
          <div class="text-caption q-mt-sm">
            {{ Math.round(totalProgress * 100) }}% 完成
          </div>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn
            flat
            label="取消"
            color="primary"
            @click="cancelUpload"
          />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useQuasar } from 'quasar'
import { uploadResource } from '@/api/resource'

const $q = useQuasar()

interface UploadFile extends File {
  status: 'pending' | 'uploading' | 'success' | 'error'
  progress: number
}

const files = ref<File[]>([])
const uploadQueue = ref<UploadFile[]>([])
const uploading = ref(false)
const showProgress = ref(false)
const currentFile = ref<UploadFile | null>(null)

const totalProgress = computed(() => {
  if (uploadQueue.value.length === 0) return 0
  const total = uploadQueue.value.reduce((sum, file) => sum + file.progress, 0)
  return total / uploadQueue.value.length
})

function handleFileSelect(newFiles: File[]) {
  const newUploadFiles = newFiles.map(file => ({
    ...file,
    status: 'pending',
    progress: 0
  })) as UploadFile[]

  uploadQueue.value = [...uploadQueue.value, ...newUploadFiles]
}

function removeFile(file: UploadFile) {
  const index = uploadQueue.value.indexOf(file)
  if (index > -1) {
    uploadQueue.value.splice(index, 1)
  }
}

function formatFileSize(bytes: number) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`
}

async function uploadFile(file: UploadFile) {
  try {
    file.status = 'uploading'
    currentFile.value = file

    const formData = new FormData()
    formData.append('file', file)

    await uploadResource(formData, (progressEvent) => {
      if (progressEvent.lengthComputable && progressEvent?.total) {
        file.progress = progressEvent.loaded / progressEvent?.total
      }else{
        file.progress = 0
      }
    })

    file.status = 'success'
    file.progress = 1

    $q.notify({
      type: 'positive',
      message: `${file.name} 上传成功`
    })
  } catch (error) {
    file.status = 'error'
    $q.notify({
      type: 'negative',
      message: `${file.name} 上传失败: ${error instanceof Error ? error.message : '未知错误'}`
    })
  }
}

async function startUpload() {
  if (uploadQueue.value.length === 0) return

  uploading.value = true
  showProgress.value = true

  try {
    for (const file of uploadQueue.value) {
      if (file.status === 'pending') {
        await uploadFile(file)
      }
    }
  } finally {
    uploading.value = false
    showProgress.value = false
    currentFile.value = null
  }
}

function cancelUpload() {
  // TODO: 实现取消上传功能
  showProgress.value = false
}
</script>

<style scoped>
.course-resource-upload {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.upload-card {
  width: 100%;
}
</style>
