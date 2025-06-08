<template>
  <div class="write-board-container">
    <div class="write-board-toolbar">
      <div class="toolbar-group">
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: isBold }" 
          @click="toggleBold"
          title="粗体"
        >
          <i class="fa fa-bold"></i>
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: isItalic }" 
          @click="toggleItalic"
          title="斜体"
        >
          <i class="fa fa-italic"></i>
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="insertList"
          title="无序列表"
        >
          <i class="fa fa-list-ul"></i>
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="insertOrderedList"
          title="有序列表"
        >
          <i class="fa fa-list-ol"></i>
        </button>
      </div>
      <div class="toolbar-group">
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="insertLink"
          title="插入链接"
        >
          <i class="fa fa-link"></i>
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="insertCode"
          title="代码块"
        >
          <i class="fa fa-code"></i>
        </button>
      </div>
    </div>
    
    <div class="write-board-editor" ref="editorContainer">
      <textarea
        ref="textareaRef"
        v-model="localContent"
        class="write-board-textarea"
        :placeholder="placeholder"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
      ></textarea>
    </div>
    
    <div class="write-board-footer">
      <div class="character-count">
        <span :class="{ 'warning': localContent.length > 2000, 'error': localContent.length > 3000 }">
          {{ localContent.length }}
        </span>
        <span class="max-count">/ 3000</span>
      </div>
      <div class="write-tips">
        <i class="fa fa-lightbulb-o"></i>
        支持Markdown语法
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

interface Props {
  modelValue?: string
  placeholder?: string
  maxLength?: number
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'focus'): void
  (e: 'blur'): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '输入你的想法...',
  maxLength: 3000
})

const emit = defineEmits<Emits>()

const textareaRef = ref<HTMLTextAreaElement>()
const localContent = ref(props.modelValue)
const isBold = ref(false)
const isItalic = ref(false)

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue !== localContent.value) {
    localContent.value = newValue
  }
})

// 监听本地值变化
watch(localContent, (newValue) => {
  emit('update:modelValue', newValue)
})

const handleInput = () => {
  // 字符限制
  if (localContent.value.length > props.maxLength) {
    localContent.value = localContent.value.substring(0, props.maxLength)
  }
}

const handleFocus = () => {
  emit('focus')
}

const handleBlur = () => {
  emit('blur')
}

const handleKeydown = (event: KeyboardEvent) => {
  // Ctrl+B 粗体
  if (event.ctrlKey && event.key === 'b') {
    event.preventDefault()
    toggleBold()
  }
  // Ctrl+I 斜体
  if (event.ctrlKey && event.key === 'i') {
    event.preventDefault()
    toggleItalic()
  }
  // Tab 插入缩进
  if (event.key === 'Tab') {
    event.preventDefault()
    insertAtCursor('    ')
  }
}

const insertAtCursor = (text: string) => {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const before = localContent.value.substring(0, start)
  const after = localContent.value.substring(end)
  
  localContent.value = before + text + after
  
  nextTick(() => {
    textarea.focus()
    textarea.setSelectionRange(start + text.length, start + text.length)
  })
}

const wrapSelection = (prefix: string, suffix: string = prefix) => {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = localContent.value.substring(start, end)
  const before = localContent.value.substring(0, start)
  const after = localContent.value.substring(end)
  
  const wrappedText = prefix + selectedText + suffix
  localContent.value = before + wrappedText + after
  
  nextTick(() => {
    textarea.focus()
    if (selectedText) {
      textarea.setSelectionRange(start + prefix.length, start + prefix.length + selectedText.length)
    } else {
      textarea.setSelectionRange(start + prefix.length, start + prefix.length)
    }
  })
}

const toggleBold = () => {
  wrapSelection('**')
  isBold.value = !isBold.value
}

const toggleItalic = () => {
  wrapSelection('*')
  isItalic.value = !isItalic.value
}

const insertList = () => {
  insertAtCursor('\n- ')
}

const insertOrderedList = () => {
  insertAtCursor('\n1. ')
}

const insertLink = () => {
  const url = prompt('请输入链接地址:')
  if (url) {
    wrapSelection('[', `](${url})`)
  }
}

const insertCode = () => {
  const textarea = textareaRef.value
  if (!textarea) return

  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selectedText = localContent.value.substring(start, end)
  
  if (selectedText.includes('\n')) {
    // 多行代码块
    wrapSelection('```\n', '\n```')
  } else {
    // 行内代码
    wrapSelection('`')
  }
}

// 暴露方法给父组件
defineExpose({
  focus: () => textareaRef.value?.focus(),
  blur: () => textareaRef.value?.blur(),
  insertText: insertAtCursor
})
</script>

<style scoped>
.write-board-container {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  background: #fff;
  overflow: hidden;
  transition: border-color 0.3s ease;
}

.write-board-container:focus-within {
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
}

.write-board-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
}

.toolbar-group {
  display: flex;
  gap: 4px;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: #666;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toolbar-btn:hover {
  background: #e9ecef;
  color: #333;
}

.toolbar-btn.active {
  background: #4CAF50;
  color: white;
}

.write-board-editor {
  position: relative;
}

.write-board-textarea {
  width: 100%;
  min-height: 200px;
  padding: 16px;
  border: none;
  outline: none;
  resize: vertical;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
  background: transparent;
}

.write-board-textarea::placeholder {
  color: #999;
}

.write-board-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  background: #f8f9fa;
  border-top: 1px solid #e0e0e0;
  font-size: 12px;
}

.character-count {
  color: #666;
}

.character-count .warning {
  color: #ff9800;
}

.character-count .error {
  color: #f44336;
}

.max-count {
  color: #999;
}

.write-tips {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #999;
}

.write-tips i {
  font-size: 11px;
}
</style>
