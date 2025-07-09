<template>
  <div class="settings-page">
    <div class="settings-container">
      <h2 class="settings-title">
        <q-icon name="storage" class="q-mr-sm" /> 私密知识库设置
      </h2>

      <div class="current-path q-mb-lg">
    
          <q-icon name="folder" class="q-mr-xs" />{{ currentPathDisplay }}
        
      </div>

    
  <div class="text-h6 text-primary q-mb-md flex items-center">
    <q-icon name="info" class="q-mr-sm" /> 使用说明
  </div>

  <div class="text-body1 q-mb-lg">
    默认情况下，教学资料将统一上传至
    <strong class="text-bold text-primary">服务器公共知识库</strong>，所有授权人员均可检索。<br />
    如果您更关注隐私或离线场景，可切换到
    <strong class="text-bold text-deep-orange">本地私密知识库</strong> —— 所有上传文件与生成向量仅保存在您指定的本地目录。
  </div>

  <q-separator class="q-mb-md" color="grey-3" />

  <div class="text-body2">
    <div class="bullet-item">
      <q-icon name="lock" size="18px" class="text-positive q-mr-sm" />
      启用私密库后，<b>文件不会上传到服务器</b>；AI 只访问该本地目录。
    </div>
    <div class="bullet-item">
      <q-icon name="group" size="18px" class="text-secondary q-mr-sm" />
      若需要与学生/他人共享资料，请保持使用公共知识库。
    </div>
    <div class="bullet-item">
      <q-icon name="restore" size="18px" class="text-grey-7 q-mr-sm" />
      可随时点击"恢复默认"重新使用服务器端知识库。
    </div>
  </div>



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
     
        <div class="q-pa-md text-grey-8 text-caption">最多保存 10 条历史记录</div>
        <q-expansion-item
          icon="history"
          :label="'历史知识库路径 (' + historyPaths.length + ')'"
          expand-separator
          header-class="text-primary"
          switch-toggle-side
          default-opened
          class="q-mb-xl"
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
    

      <!-- 联合知识库卡片 -->
      

  <!-- 卡片标题 -->
  <h2 class="settings-title">
    <q-icon name="hub" class="q-mr-sm" />联合知识库设置
    </h2>

  <!-- 背景介绍说明 -->
  <div class="text-body1 q-mb-md">
    <p>
      联合知识库旨在将 <strong class="text-deep-orange">本地私密知识库</strong> 与 <strong class="text-primary">服务器公共知识库</strong> 高效整合，
      实现 <b class="text-weight-medium">更具专业性与跨学科能力</b> 的教案生成、内容检索和智能问答。
    </p>
    <p class="q-mt-sm">
      用户可选取本地历史路径与服务器库中 <b class="text-weight-medium">最多五个知识库</b> 进行联合，
      构建一个更强大的数据库检索源。
    </p>
    <p class="q-mt-sm text-grey-7 text-caption">
      设置后，此账号下的所有检索、生成、上传等操作将统一作用于该联合知识库。为避免调用过慢，建议控制在 2~3 个库内。
    </p>
  </div>

  <!-- 分隔线 -->
  <q-separator class="q-my-md" color="grey-3" />

  <!-- 选择知识库列表 -->
  <q-list bordered separator class="rounded-borders">
    <q-item
      v-for="entry in selectableList"
      :key="entry.path || 'server'"
      class="selectable-kb-item"
    >
      <q-item-section avatar>
        <q-checkbox
          :model-value="selectedPaths.includes(entry.path)"
          @update:model-value="() => toggleSelect(entry.path)"
          :disable="!selectedPaths.includes(entry.path) && selectedPaths.length >= MAX_UNION"
        />
      </q-item-section>
      <q-item-section>
        <q-item-label class="text-weight-medium">{{ entry.alias || '未命名知识库' }}</q-item-label>
        <q-item-label class="text-grey-6 text-caption">{{ entry.path || '服务器' }}</q-item-label>
      </q-item-section>
    </q-item>
  </q-list>

  <!-- 操作按钮 -->
  <div class="row justify-end q-gutter-sm q-mt-md">
    <q-btn
      color="primary"
      unelevated
      label="保存设置"
      @click="saveSelected"
      :disable="selectedPaths.length === 0"
    />
    <q-btn
      flat
      color="negative"
      label="解散联合知识库"
      @click="disbandUnion"
      :disable="!isUnionActive"
    />
  </div>


    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useQuasar, QIcon, QInput, QBtn, QCheckbox, QLinearProgress, QList, QItem, QItemSection, QItemLabel,QSeparator} from 'quasar'
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

const SERVER_TOKEN = '' // sentinel for server KB
const selectedPaths = ref<string[]>([])
const MAX_UNION = 5

// 可选知识库列表：服务器库 + 历史
const selectableList = computed(() => [
  { path: SERVER_TOKEN, alias: '服务器公共知识库' },
  ...historyPaths.value
])

const isUnionActive = computed(() => selectedPaths.value.length > 1)

const currentPathDisplay = computed(() => {
  return isUnionActive.value ? '正在使用联合知识库' : (currentPath.value || '公共知识库 (服务器)')
})

onMounted(async () => {
  try {
    const { data } = await StorageApi.listKnowledgeBases()
    selectedPaths.value = data
  } catch (e) {
    console.error('Failed to fetch active KB list', e)
  }
})

function toggleSelect(path: string) {
  if (selectedPaths.value.includes(path)) {
    selectedPaths.value = selectedPaths.value.filter(p => p !== path)
  } else {
    if (selectedPaths.value.length >= MAX_UNION) {
      $q.notify({ type: 'warning', message: `最多选择 ${MAX_UNION} 个知识库` })
      return
    }
    selectedPaths.value.push(path)
  }
}

async function saveSelected() {
  try {
    await StorageApi.setSelectedKBs(selectedPaths.value)
    $q.notify({ type: 'positive', message: '已更新联合知识库' })
  } catch (e: any) {
    $q.notify({ type: 'negative', message: e.message || '更新失败' })
  }
}

async function disbandUnion() {
  selectedPaths.value = [SERVER_TOKEN]
  await saveSelected()
}

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

.union-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
}
</style> 