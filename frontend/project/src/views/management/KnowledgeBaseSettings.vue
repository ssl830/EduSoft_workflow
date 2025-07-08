<template>
  <div class="settings-page">
    <div class="settings-container">
      <h2 class="settings-title">
        <q-icon name="storage" class="q-mr-sm" /> 私密知识库设置
      </h2>

      <div class="current-path q-mb-lg">
        <template v-if="currentPath">
          <q-badge outline color="primary" class="text-bold" style="font-size: 1rem; padding: 8px 12px;">
            <q-icon name="folder" class="q-mr-xs" />{{ currentPath }}
          </q-badge>
          <div class="text-caption text-grey-7 q-mt-sm">当前私密知识库路径</div>
        </template>
        <template v-else>
          <q-badge color="positive" class="text-bold" style="font-size: 1rem; padding: 8px 12px;">
            <q-icon name="cloud" class="q-mr-xs" />公共知识库 (服务器)
          </q-badge>
        </template>
      </div>

      <q-card flat bordered class="info-card q-pa-md q-mb-lg">
        <p class="text-body1 q-mb-md">
          默认情况下，教学资料将统一上传至 <strong>服务器公共知识库</strong>，所有授权人员均可检索。
          如果您更关注隐私或离线场景，可切换到<strong>本地私密知识库</strong> —— 所有上传文件与生成向量仅保存在您指定的本地目录。
        </p>

        <ul class="text-body2">
          <li class="q-mb-sm">启用私密库后，<b>文件不会上传到服务器</b>；AI 只访问该本地目录。</li>
          <li class="q-mb-sm">若需要与学生/他人共享资料，请保持使用公共知识库。</li>
          <li>可随时点击"恢复默认"重新使用服务器端知识库。</li>
        </ul>
      </q-card>

      <div class="input-row q-mt-lg">
        <q-input
          v-model="localPath"
          filled
          label="本地存储根目录"
          placeholder="例如：D:/MyKnowledgeBase"
          class="col"
        />
      </div>

      <div class="actions q-mt-xl">
        <q-btn unelevated color="primary" @click="savePath" :loading="saving" label="保存设置" />
        <q-btn flat color="negative" @click="resetToDefault" :loading="saving" label="恢复默认" />
      </div>

      <!-- 上传文件到知识库 -->
      <q-separator class="q-my-lg" />
      <div class="upload-row">
        <q-btn color="primary" icon="upload" label="上传资料到当前知识库" @click="triggerFileSelect" />
        <q-linear-progress v-if="uploading" :value="uploadProgress / 100" class="q-ml-md" style="width:200px" />
      </div>
      <input type="file" ref="uploadInput" style="display:none" @change="handleFileUpload" />

      <!-- 历史路径 -->
      <q-card flat bordered class="history-card q-mt-xl">
        <div class="q-pa-md text-grey-8 text-caption">最多保存 10 条历史记录</div>
        <q-expansion-item
          icon="history"
          :label="'历史知识库路径 (' + historyPaths.length + ')'"
          expand-separator
          header-class="text-primary"
          switch-toggle-side
          default-opened
        >
          <q-list padding v-if="historyPaths.length">
            <q-item
              v-for="entry in historyPaths"
              :key="entry.path"
              class="history-item q-py-sm"
              clickable
              @click="localPath = entry.path"
            >
              <q-item-section avatar>
                <q-icon name="folder" color="grey-7" />
              </q-item-section>
              <q-item-section>
                <q-item-label class="q-mb-xs text-weight-medium">
                    {{ entry.alias || '未命名知识库' }}
                 </q-item-label>
                <q-item-label class="text-grey-6 text-caption">
                 {{ entry.path }}
                </q-item-label>
                </q-item-section>

              <q-item-section side class="row no-wrap items-center">
                <q-btn
                  flat
                  dense
                  round
                  icon="content_copy"
                  color="grey-7"
                  @click.stop="copyPath(entry.path)"
                >
                  <q-tooltip>复制路径</q-tooltip>
                </q-btn>
                <q-btn
                  flat
                  dense
                  round
                  icon="edit"
                  color="primary"
                  @click.stop="editAlias(entry)"
                />
                <q-btn
                  flat
                  dense
                  round
                  icon="delete"
                  color="negative"
                  @click.stop="deletePath(entry.path)"
                />
              </q-item-section>
            </q-item>
          </q-list>
          <div v-else class="q-pa-lg text-grey text-center">暂无历史记录</div>
        </q-expansion-item>
      </q-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useQuasar, QIcon, QInput, QBtn } from 'quasar'
import { QExpansionItem } from 'quasar'
import StorageApi from '@/api/storage'

const $q = useQuasar()
const localPath = ref('')
const saving = ref(false)

// 路径状态
const currentPath = ref(localStorage.getItem('kbCurrentPath') || '')

interface PathEntry {
  path: string
  alias: string
}

// ---- 历史路径持久化 ----
function parseHistory(raw: any): PathEntry[] {
  try {
    const arr = JSON.parse(raw)
    if (Array.isArray(arr)) {
      // 兼容旧格式（字符串数组）
      if (typeof arr[0] === 'string') {
        return (arr as string[]).map(p => ({ path: p, alias: p }))
      }
      return arr as PathEntry[]
    }
    return []
  } catch {
    return []
  }
}

const historyPaths = ref<PathEntry[]>(parseHistory(localStorage.getItem('kbHistory') || '[]'))

// 上传
const uploadInput = ref<HTMLInputElement>()
const uploading = ref(false)
const uploadProgress = ref(0)

async function savePath() {
  if (!localPath.value) {
    $q.notify({ type: 'warning', message: '请输入存储路径' })
    return
  }
  saving.value = true
  try {
    await StorageApi.setBasePath(localPath.value)
    $q.notify({ type: 'positive', message: '存储路径已更新' })

    currentPath.value = localPath.value
    if (!historyPaths.value.find(h => h.path === localPath.value)) {
      // 限制 10 条
      if (historyPaths.value.length >= 10) historyPaths.value.shift()
      historyPaths.value.push({ path: localPath.value, alias: '未命名知识库' })
      localStorage.setItem('kbHistory', JSON.stringify(historyPaths.value))
    }
    localStorage.setItem('kbCurrentPath', currentPath.value)
  } catch (e: any) {
    $q.notify({ type: 'negative', message: e.message || '更新失败' })
  } finally {
    saving.value = false
  }
}

async function resetToDefault() {
  saving.value = true
  try {
    await StorageApi.resetBasePath()
    localPath.value = ''
    $q.notify({ type: 'positive', message: '已恢复默认知识库' })

    currentPath.value = ''
    localStorage.setItem('kbCurrentPath', '')
  } catch (e: any) {
    $q.notify({ type: 'negative', message: e.message || '操作失败' })
  } finally {
    saving.value = false
  }
}

// 文件上传逻辑
function triggerFileSelect() {
  uploadInput.value?.click()
}

async function handleFileUpload(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  const formData = new FormData()
  formData.append('file', file)

  uploading.value = true
  uploadProgress.value = 0
  try {
    const ResourceApi = (await import('@/api/resource')).default
    await ResourceApi.uploadToKnowledgeBase(
      file,
      formData,
      undefined,
      (p: number) => (uploadProgress.value = p)
    )
    $q.notify({ type: 'positive', message: '文件已上传并入库' })
  } catch (err: any) {
    $q.notify({ type: 'negative', message: err.message || '上传失败' })
  } finally {
    uploading.value = false
  }
}

function deletePath(path: string) {
  historyPaths.value = historyPaths.value.filter(h => h.path !== path)
  localStorage.setItem('kbHistory', JSON.stringify(historyPaths.value))
  $q.notify({ type: 'positive', message: '记录已删除' })
}

function editAlias(entry: PathEntry) {
  $q.dialog({
    title: '设置别名',
    prompt: {
      model: entry.alias,
      type: 'text',
      isValid: val => !!val,
    },
    cancel: true,
    persistent: true,
  }).onOk((val: string) => {
    entry.alias = val
    localStorage.setItem('kbHistory', JSON.stringify(historyPaths.value))
    $q.notify({ type: 'positive', message: '别名已更新' })
  })
}

function copyPath(path: string) {
  navigator.clipboard.writeText(path)
  $q.notify({ type: 'positive', message: '路径已复制到剪贴板' })
}
</script>

<style scoped>
.settings-page {
  min-height: calc(100vh - 64px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 60px 20px;
  background: var(--bg-primary);
}

.settings-container {
  width: 100%;
  max-width: 800px;
  padding: 2.5rem;
  border-radius: 24px;
  backdrop-filter: blur(20px);
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.settings-title {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
  font-size: 1.75rem;
  color: var(--q-primary);
}

.info-card {
  background: rgba(var(--q-primary-rgb), 0.04);
  border-color: rgba(var(--q-primary-rgb), 0.2) !important;
}

.input-row {
  display: flex;
  align-items: center;
  margin: 2rem 0;
}

.input-row .q-input {
  flex: 1;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin: 2rem 0;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 1.5rem 0;
}

.history-card {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border-color: rgba(var(--q-primary-rgb), 0.15) !important;
  display: block;
  margin-top: 2rem;
}

.history-item {
  border-radius: 8px;
  transition: background-color 0.3s;
}

.history-item:hover {
  background: rgba(var(--q-primary-rgb), 0.08);
}

:deep(.q-expansion-item__container) {
  background: transparent;
  font-weight: bold;
}
:deep(.q-item) {
  display: flex !important;
}

:deep(.q-item__section--avatar) {
  min-width: 40px;
}

:deep(.history-item .q-item__section--side) {
  gap: 4px;
}
</style> 