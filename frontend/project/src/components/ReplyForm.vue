<template>
  <div class="reply-form bg-white border border-gray-200 rounded-lg p-4">
    <div class="mb-4">
      <!-- 富文本编辑器或简单文本框 -->
      <div class="relative">
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
    
    <!-- 工具栏 -->
    <div class="flex items-center justify-between">
      <!-- 格式化按钮 -->
      <div class="flex items-center space-x-2">
        <button
          type="button"
          @click="insertFormat('**', '**')"
          class="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded"
          title="粗体"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 4h8a4 4 0 014 4 4 4 0 01-4 4H6z"></path>
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 12h9a4 4 0 014 4 4 4 0 01-4 4H6z"></path>
          </svg>
        </button>
        
        <button
          type="button"
          @click="insertFormat('*', '*')"
          class="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded"
          title="斜体"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 20l-7-8 7-8M5 20l7-8L5 4"></path>
          </svg>
        </button>
        
        <button
          type="button"
          @click="insertFormat('`', '`')"
          class="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded"
          title="代码"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"></path>
          </svg>
        </button>
        
        <div class="h-4 w-px bg-gray-300"></div>
        
        <!-- 表情按钮 -->
        <button
          type="button"
          @click="toggleEmojiPicker"
          class="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-100 rounded"
          title="表情"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14.828 14.828a4 4 0 01-5.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
        </button>
      </div>
      
      <!-- 操作按钮 -->
      <div class="flex items-center space-x-3">
        <button
          v-if="showCancel"
          type="button"
          @click="handleCancel"
          class="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 transition-colors"
        >
          取消
        </button>
        
        <button
          type="button"
          @click="handleSubmit"
          :disabled="!canSubmit || isSubmitting"
          class="px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
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

const insertFormat = (startFormat: string, endFormat: string) => {
  if (!textareaRef.value) return;
  
  const textarea = textareaRef.value;
  const start = textarea.selectionStart;
  const end = textarea.selectionEnd;
  const selectedText = content.value.substring(start, end);
  
  const newText = startFormat + selectedText + endFormat;
  content.value = content.value.substring(0, start) + newText + content.value.substring(end);
  
  nextTick(() => {
    textarea.focus();
    textarea.setSelectionRange(start + startFormat.length, start + startFormat.length + selectedText.length);
  });
};

const insertEmoji = (emoji: string) => {
  if (!textareaRef.value) return;
  
  const textarea = textareaRef.value;
  const start = textarea.selectionStart;
  
  content.value = content.value.substring(0, start) + emoji + content.value.substring(start);
  
  nextTick(() => {
    textarea.focus();
    textarea.setSelectionRange(start + emoji.length, start + emoji.length);
  });
  
  showEmojiPicker.value = false;
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
  transition: all 0.2s ease-in-out;
}

textarea {
  min-height: 100px;
  max-height: 300px;
  font-family: inherit;
  line-height: 1.5;
}

textarea:focus {
  outline: none;
}

.grid-cols-8 {
  grid-template-columns: repeat(8, minmax(0, 1fr));
}

button:disabled {
  cursor: not-allowed;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
