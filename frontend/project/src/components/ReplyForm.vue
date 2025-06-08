<template>
  <div class="reply-form bg-white border border-gray-200 rounded-lg p-4">
    <div class="mb-4">
      <!-- 富文本编辑器或简单文本框 -->
      <div class="relative">
        <!-- 工具栏 -->
        <div class="wb-toolbar" style="margin-bottom:6px;">
          <button type="button" class="wb-btn" @click="insertFormat('**','**')" title="粗体"><b>B</b></button>
          <button type="button" class="wb-btn" @click="insertFormat('*','*')" title="斜体"><i>I</i></button>
          <button type="button" class="wb-btn" @click="insertFormat('~~','~~')" title="删除线"><span style="text-decoration:line-through;">S</span></button>
          <span class="wb-divider"></span>
          <button type="button" class="wb-btn" @click="insertFormat('`','`')" title="行内代码"><span style="font-family:monospace;">&lt;/&gt;</span></button>
          <button type="button" class="wb-btn" @click="insertFormat('\n````\n','\n````\n')" title="代码块"><span style="font-family:monospace;">[code]</span></button>
          <span class="wb-divider"></span>
          <button type="button" class="wb-btn" @click="toggleEmojiPicker" title="表情">😀</button>
        </div>
        <textarea
          ref="textareaRef"
          v-model="content"
          :placeholder="placeholder"
          class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
          :class="{ 'border-red-500': hasError }"
          rows="4"
          @input="handleInput"
          @keydown="handleKeydown"
        ></textarea>
        <!-- 字符计数 -->
        <div class="absolute bottom-2 right-2 text-xs text-gray-400">
          {{ content.length }}/{{ maxLength }}
        </div>
      </div>
      <!-- 错误提示 -->
      <div v-if="hasError" class="mt-2 text-sm text-red-600">
        {{ errorMessage }}
      </div>
    </div>
    <!-- 表情选择器 -->
    <div v-if="showEmojiPicker" class="mt-3 p-3 border border-gray-200 rounded-md bg-gray-50">
      <div class="grid grid-cols-8 gap-2">
        <button
          v-for="emoji in commonEmojis"
          :key="emoji"
          type="button"
          @click="insertEmoji(emoji)"
          class="p-2 text-lg hover:bg-gray-200 rounded text-center"
        >
          {{ emoji }}
        </button>
      </div>
    </div>
    <!-- 工具栏和表情区结束 -->
    <div class="flex items-center justify-between mt-2">
      <!-- 操作按钮 -->
      <div class="flex items-center space-x-3">
        <button
          v-if="showCancel"
          class="cancel-button"
          @click="handleCancel"
        >
          取消
        </button>
        <button
          class="submit-button"
          @click="handleSubmit"
          style="margin-left: 10px"
          :disabled="!canSubmit || isSubmitting"
        >
          <span v-if="isSubmitting" class="flex items-center">
            <svg class="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
            </svg>
            提交中...
          </span>
          <span v-else>{{ submitText }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue';
import discussionApi, { type CreateReplyRequest, type UpdateReplyRequest } from '../api/discussion';

// Props
interface Props {
  discussionId?: number;
  parentReplyId?: number | null;
  initialContent?: string;
  isEditing?: boolean;
  placeholder?: string;
  submitText?: string;
  showCancel?: boolean;
  maxLength?: number;
}

const props = withDefaults(defineProps<Props>(), {
  initialContent: '',
  isEditing: false,
  placeholder: '输入你的回复...',
  submitText: '提交',
  showCancel: true,
  maxLength: 2000
});

// Emits
const emit = defineEmits<{
  success: [content?: string];
  cancel: [];
  error: [message: string];
}>();

// Refs
const textareaRef = ref<HTMLTextAreaElement>();

// State
const content = ref(props.initialContent);
const isSubmitting = ref(false);
const hasError = ref(false);
const errorMessage = ref('');
const showEmojiPicker = ref(false);

// 常用表情
const commonEmojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣',
  '😊', '😇', '🙂', '🙃', '😉', '😌', '😍', '🥰',
  '😘', '😗', '😙', '😚', '😋', '😛', '😝', '😜',
  '🤪', '🤨', '🧐', '🤓', '😎', '🤩', '🥳', '😏',
  '👍', '👎', '👌', '🤝', '🙏', '💪', '👏', '🎉'
];

// Computed
const canSubmit = computed(() => {
  return content.value.trim().length > 0 && 
         content.value.length <= props.maxLength && 
         !isSubmitting.value;
});

// Watch
watch(() => props.initialContent, (newValue) => {
  content.value = newValue;
});

// Methods
const handleInput = () => {
  hasError.value = false;
  errorMessage.value = '';
  
  if (content.value.length > props.maxLength) {
    hasError.value = true;
    errorMessage.value = `内容长度不能超过 ${props.maxLength} 个字符`;
  }
  
  autoResize();
};

const autoResize = () => {
  if (textareaRef.value) {
    textareaRef.value.style.height = 'auto';
    textareaRef.value.style.height = textareaRef.value.scrollHeight + 'px';
  }
};

const handleKeydown = (event: KeyboardEvent) => {
  // Ctrl/Cmd + Enter 快速提交
  if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
    event.preventDefault();
    handleSubmit();
  }
};

const insertFormat = (prefix: string, suffix: string) => {
  const textarea = textareaRef.value;
  if (!textarea) return;
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const value = content.value;
  const selected = value.substring(start, end);
  const newValue = value.substring(0, start) + prefix + selected + suffix + value.substring(end);
  content.value = newValue;
  nextTick(() => {
    textarea.focus();
    textarea.setSelectionRange(start + prefix.length, end + prefix.length);
  });
};

const insertEmoji = (emoji: string) => {
  const textarea = textareaRef.value;
  if (!textarea) return;
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const value = content.value;
  const newValue = value.substring(0, start) + emoji + value.substring(end);
  content.value = newValue;
  nextTick(() => {
    textarea.focus();
    textarea.setSelectionRange(start + emoji.length, start + emoji.length);
  });
};

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value;
};

const handleSubmit = async () => {
  if (!canSubmit.value) return;
  
  const trimmedContent = content.value.trim();
  if (!trimmedContent) {
    hasError.value = true;
    errorMessage.value = '回复内容不能为空';
    return;
  }
  
  isSubmitting.value = true;
  hasError.value = false;
  errorMessage.value = '';
  
  try {
    if (props.isEditing) {
      // 编辑模式 - 这里应该调用更新API，但需要回复ID
      // 由于组件设计限制，我们通过emit传递内容，让父组件处理
      emit('success', trimmedContent);
    } else {
      // 创建新回复
      if (!props.discussionId) {
        throw new Error('讨论ID不能为空');
      }
      
      const replyData: CreateReplyRequest = {
        content: trimmedContent,
        parentReplyId: props.parentReplyId || undefined
      };
      
      await discussionApi.createReply(props.discussionId, replyData);
      emit('success');
      
      // 重置表单
      content.value = '';
      showEmojiPicker.value = false;
    }
  } catch (error: any) {
    hasError.value = true;
    errorMessage.value = error.response?.data?.message || error.message || '提交失败，请重试';
    emit('error', errorMessage.value);
  } finally {
    isSubmitting.value = false;
  }
};

const handleCancel = () => {
  content.value = props.initialContent;
  hasError.value = false;
  errorMessage.value = '';
  showEmojiPicker.value = false;
  emit('cancel');
};

// 组件挂载后自动调整高度
nextTick(() => {
  autoResize();
});
</script>
<script lang="ts">
export default {
  name: 'ReplyForm' // 显式设置组件名
}
</script>
<style scoped>
.reply-form {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(226, 232, 240, 0.8);
  padding: 1.5rem;
  transition: all 0.3s ease;
}

.reply-form:focus-within {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #3b82f6;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.form-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: #1e293b;
}

.editor-container {
  margin-bottom: 1rem;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
}

.editor-container:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1.2rem;
  margin-top: 1.5rem;
}

.submit-button, .cancel-button {
  background: linear-gradient(90deg, #2563eb 0%, #3b82f6 100%);
  color: #fff !important;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  font-size: 0.95rem;
  min-width: 120px;
  padding: 0.75rem 1.5rem;
  box-shadow: 0 2px 8px rgba(59,130,246,0.08);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  transition: none !important;
}
.submit-button:active, .cancel-button.cancel-button:active {
  background: linear-gradient(90deg, #2563eb 0%, #3b82f6 100%) !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(59,130,246,0.08);
}
.submit-button:disabled, .cancel-button:disabled {
  background: #e5e7eb !important;
  color: #cbd5e1 !important;
  cursor: not-allowed;
  box-shadow: none;
}

.character-count {
  font-size: 0.875rem;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 0.25rem;
  transition: all 0.2s ease;
}

.character-count.warning {
  color: #f59e0b;
}

.character-count.error {
  color: #ef4444;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  backdrop-filter: blur(4px);
}

.loading-spinner {
  width: 2rem;
  height: 2rem;
  border: 3px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 640px) {
  .reply-form {
    padding: 1rem;
  }

  .form-actions {
    flex-direction: column-reverse;
    gap: 0.5rem;
  }

  .submit-button,
  .cancel-button {
    width: 100%;
    justify-content: center;
  }
}

/* WriteBoard编辑器样式覆盖 */
:deep(.q-editor) {
  border: none !important;
  border-radius: 8px;
}

:deep(.q-editor__content) {
  min-height: 150px;
  padding: 1rem;
  font-size: 0.95rem;
  line-height: 1.7;
}

:deep(.q-editor__toolbar) {
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  padding: 0.5rem;
}

:deep(.q-btn) {
  padding: 0.25rem;
  border-radius: 4px;
}

:deep(.q-btn:hover) {
  background: rgba(226, 232, 240, 0.5);
}

/* 错误提示样式 */
.error-message {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.error-message i {
  font-size: 1rem;
}

.wb-toolbar {
  display: flex;
  gap: 0.5rem;
  padding: 0.5rem;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 8px;
  border: 1px solid rgba(226, 232, 240, 0.6);
  flex-wrap: wrap;
  margin-bottom: 0.5rem;
}

.wb-btn {
  padding: 0.5rem 0.75rem;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.9);
  color: #4B5563;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
}

.wb-btn:hover {
  background: rgba(59, 130, 246, 0.1);
  border-color: rgba(59, 130, 246, 0.3);
  color: #2563EB;
  transform: translateY(-1px);
}

.wb-btn:active {
  transform: translateY(0);
}

.wb-divider {
  width: 1px;
  height: 24px;
  background: rgba(226, 232, 240, 0.8);
  margin: 0 0.25rem;
}

textarea, .wb-textarea {
  width: 100% !important;
  max-width: 100%;
  min-width: 0;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
  /* 表情选择器样式 */
  .mt-3.p-3 {
    background: rgba(255, 255, 255, 0.95);
    border: 1px solid rgba(226, 232, 240, 0.8);
    border-radius: 12px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  }

  .grid.grid-cols-8 button {
    transition: all 0.2s ease;
    border-radius: 8px;
  }

  .grid.grid-cols-8 button:hover {
    background: rgba(59, 130, 246, 0.1);
    transform: scale(1.1);
  }

  /* 提交按钮样式 */
  button[type="button"]:not(.wb-btn) {
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
  }

  button[type="button"]:not(.wb-btn):hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.2);
  }

  button[type="button"]:not(.wb-btn):active {
    transform: translateY(0);
  }

  /* 错误提示样式 */
  .text-red-600 {
    background: rgba(239, 68, 68, 0.1);
    padding: 0.75rem;
    border-radius: 8px;
    font-size: 0.875rem;
  display: flex;
  align-items: center;
    gap: 0.5rem;
  }

  .text-red-600::before {
    content: '⚠️';
  }

  /* 字数统计样式 */
  .text-xs.text-gray-400 {
    background: rgba(255, 255, 255, 0.9);
    padding: 0.25rem 0.5rem;
    border-radius: 4px;
    border: 1px solid rgba(226, 232, 240, 0.8);
    font-weight: 500;
  }

  /* 提交按钮渐变背景 */
  button[type="button"]:not(.wb-btn):not([disabled]) {
    background: linear-gradient(135deg, #3B82F6, #2563EB);
    border: none;
    color: white;
    font-weight: 600;
    letter-spacing: 0.025em;
  }

  /* 取消按钮样式 */
  button[type="button"].text-gray-600 {
    background: rgba(75, 85, 99, 0.1);
    border: 1px solid rgba(75, 85, 99, 0.2);
  }

  button[type="button"].text-gray-600:hover {
    background: rgba(75, 85, 99, 0.15);
    border-color: rgba(75, 85, 99, 0.3);
    color: #374151;
}
</style>
