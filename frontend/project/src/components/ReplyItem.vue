<template>
  <div class="reply-item bg-gray-50 rounded-lg p-4 border-l-4 border-blue-500">
    <!-- 回复头部信息 -->
    <div class="flex items-center justify-between mb-3">
      <div class="flex items-center space-x-3">
        <!-- 用户头像 -->
        <div class="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-medium">
          {{ getInitials(reply.authorName) }}
        </div>
        
        <!-- 用户信息 -->
        <div>
          <span class="font-medium text-gray-900">{{ reply.authorName }}</span>
          <span v-if="reply.isTeacher" class="ml-2 px-2 py-1 bg-red-100 text-red-800 text-xs rounded-full">
            教师
          </span>
        </div>
        
        <!-- 时间信息 -->
        <span class="text-sm text-gray-500">{{ formatDate(reply.createdAt) }}</span>
      </div>
      
      <!-- 操作按钮 -->
      <div class="flex items-center space-x-2">
        <!-- 点赞按钮 -->
        <button 
          @click="$emit('like', reply.id)"
          class="flex items-center space-x-1 text-gray-500 hover:text-blue-600 transition-colors"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 10h4.764a2 2 0 011.789 2.894l-3.5 7A2 2 0 0115.263 21h-4.017c-.163 0-.326-.02-.485-.06L7 20m7-10V5a2 2 0 00-2-2h-.095c-.5 0-.905.405-.905.905 0 .714-.211 1.412-.608 2.006L9 7m5 3v10M7 20H5a2 2 0 01-2-2v-6a2 2 0 012-2h2.5" />
          </svg>
          <span class="text-sm">{{ reply.likeCount }}</span>
        </button>
        
        <!-- 回复按钮 -->
        <button 
          @click="toggleReplyForm"
          class="text-gray-500 hover:text-blue-600 transition-colors"
          title="回复"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6" />
          </svg>
        </button>
        
        <!-- 编辑按钮 (仅作者可见) -->
        <button 
          v-if="canEdit"
          @click="toggleEditForm"
          class="text-gray-500 hover:text-yellow-600 transition-colors"
          title="编辑"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
        </button>
        
        <!-- 删除按钮 (仅作者可见) -->
        <button 
          v-if="canDelete"
          @click="handleDelete"
          class="text-gray-500 hover:text-red-600 transition-colors"
          title="删除"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>
      </div>
    </div>
    
    <!-- 回复内容 -->
    <div v-if="!isEditing" class="prose max-w-none text-gray-800 mb-3" v-html="reply.content"></div>
    
    <!-- 编辑表单 -->
    <div v-if="isEditing" class="mb-3">
      <ReplyForm 
        :initial-content="reply.content"
        :is-editing="true"
        @success="handleEditSuccess"
        @cancel="toggleEditForm"
        placeholder="编辑回复内容..."
        submit-text="保存修改"
      />
    </div>
    
    <!-- 回复表单 -->
    <div v-if="showReplyForm" class="mt-4 border-t pt-4">
      <ReplyForm 
        :discussion-id="reply.discussionId"
        :parent-reply-id="reply.id"
        @success="handleReplySuccess"
        @cancel="toggleReplyForm"
        :placeholder="`回复 ${reply.authorName}...`"
        submit-text="提交回复"
      />
    </div>
    
    <!-- 子回复加载按钮 -->
    <div v-if="hasChildren && level === 0" class="mt-3">
      <button 
        @click="$emit('load-children', reply.id)"
        class="text-blue-600 hover:text-blue-800 text-sm flex items-center"
      >
        <svg class="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
        查看回复 ({{ childrenCount }})
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, inject } from 'vue';
import type { DiscussionReply } from '../api/discussion';
import ReplyForm from './ReplyForm.vue';

// Props
interface Props {
  reply: DiscussionReply;
  level: number;
}

const props = defineProps<Props>();

// Emits
const emit = defineEmits<{
  delete: [id: number];
  edit: [id: number, content: string];
  reply: [parentId: number];
  like: [id: number];
  'load-children': [parentId: number];
}>();

// 注入当前用户信息
const currentUserId = inject<string>('currentUserId', 'user123');
const userRole = inject<string>('userRole', 'student');

// 状态
const isEditing = ref(false);
const showReplyForm = ref(false);

// 计算属性
const canEdit = computed(() => {
  return props.reply.authorId.toString() === currentUserId || userRole === 'teacher';
});

const canDelete = computed(() => {
  return props.reply.authorId.toString() === currentUserId || userRole === 'teacher';
});

const hasChildren = computed(() => {
  return props.reply.children && props.reply.children.length > 0;
});

const childrenCount = computed(() => {
  return props.reply.children ? props.reply.children.length : 0;
});

// 方法
const getInitials = (name: string): string => {
  return name.charAt(0).toUpperCase();
};

const formatDate = (dateString: string): string => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));
  
  if (diffInMinutes < 1) {
    return '刚刚';
  } else if (diffInMinutes < 60) {
    return `${diffInMinutes}分钟前`;
  } else if (diffInMinutes < 1440) {
    return `${Math.floor(diffInMinutes / 60)}小时前`;
  } else {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
};

const toggleEditForm = () => {
  isEditing.value = !isEditing.value;
  if (isEditing.value) {
    showReplyForm.value = false;
  }
};

const toggleReplyForm = () => {
  showReplyForm.value = !showReplyForm.value;
  if (showReplyForm.value) {
    isEditing.value = false;
  }
};

const handleDelete = () => {
  if (confirm('确定要删除这条回复吗？')) {
    emit('delete', props.reply.id);
  }
};

const handleEditSuccess = (content: string) => {
  emit('edit', props.reply.id, content);
  isEditing.value = false;
};

const handleReplySuccess = () => {
  emit('reply', props.reply.id);
  showReplyForm.value = false;
};
</script>
<script lang="ts">
export default {
  name: 'ReplyItem' // 显式设置组件名
}
</script>
<style scoped>
.reply-item {
  transition: all 0.2s ease-in-out;
}

.reply-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.prose {
  max-width: none;
}

.prose p {
  margin-bottom: 0.5rem;
}

.prose p:last-child {
  margin-bottom: 0;
}
</style>
