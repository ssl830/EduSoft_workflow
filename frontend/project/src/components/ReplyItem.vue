<template>
  <div class="reply-item bg-gray-50 rounded-lg p-4 border-l-4 border-blue-500">
    <!-- 回复头部信息 -->
    <div class="flex items-center justify-between mb-3">
      <div class="flex items-center space-x-3">
        <!-- 用户头像 -->
        <div class="author-avatar">
          {{ getInitials(reply.creatorNum) }}
        </div>

        <!-- 用户信息 -->
        <div class="flex flex-col">
          <div class="flex items-center">
            <span class="font-medium text-gray-900">{{ reply.creatorNum }}</span>
            <span v-if="reply.isTeacher" class="ml-2 px-2 py-0.5 bg-red-100 text-red-800 text-xs rounded-full">
            教师
          </span>
        </div>
        <span class="text-sm text-gray-500">{{ formatDate(reply.createdAt) }}</span>
        </div>
      </div>

      <!-- 回复操作按钮 -->
      <div class="flex items-center space-x-4">
        <!-- 编辑按钮 -->
        <button
          v-if="canEdit"
          @click="$emit('edit', reply.id, reply.content)"
          class="edit-btn"
        >
          <i class="fas fa-edit mr-1"></i>
          编辑
        </button>

        <!-- 删除按钮 -->
        <button
          v-if="canDelete"
          @click="$emit('delete', reply.id)"
          class="delete-btn"
        >
          <i class="fas fa-trash-alt mr-1"></i>
          删除
        </button>
      </div>
    </div>

    <!-- 回复内容 -->
    <div class="prose max-w-none text-gray-700 mb-4" v-html="renderedContent"></div>

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
        :placeholder="`回复 ${reply.creatorNum}...`"
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
// @ts-ignore
import MarkdownIt from 'markdown-it';
import { ref, computed, inject } from 'vue';
import type { DiscussionReply } from '../api/discussion';
// import discussionApi from '../api/discussion';
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
  return props.reply.creatorId.toString() === currentUserId || userRole === 'teacher';
});

const canDelete = computed(() => {
  return props.reply.creatorId.toString() === currentUserId || userRole === 'teacher';
});

const hasChildren = computed(() => {
  return props.reply.children && props.reply.children.length > 0;
});

const childrenCount = computed(() => {
  return props.reply.children ? props.reply.children.length : 0;
});

// 点赞相关
// const isLiked = ref(props.reply.likedByMe || false);
// const showLikeUsers = ref(false);
// const likeUsers = ref<{userNum: string; userId: number}[]>([]);
// const loadingLikeUsers = ref(false);

// 处理点赞
// const handleLike = async () => {
//   try {
//     await emit('like', props.reply.id);
//   } catch (err) {
//     console.error('点赞失败:', err);
//   }
// };

// 加载点赞用户列表
// const loadLikeUsers = async () => {
//   if (loadingLikeUsers.value) return;
//
//   loadingLikeUsers.value = true;
//   try {
//     const response = await discussionApi.getLikeUsers(props.reply.id);
//     likeUsers.value = response.data;
//     showLikeUsers.value = true;
//   } catch (err) {
//     console.error('Failed to load like users:', err);
//   } finally {
//     loadingLikeUsers.value = false;
//   }
// };

// 关闭点赞用户列表
// const closeLikeUsers = () => {
//   showLikeUsers.value = false;
// };

// 方法
const getInitials = (name: string): string => {
  if (!name) return '';
  return name.charAt(0).toUpperCase();
};

const formatDate = (dateString: string): string => {
  if (!dateString) return '';
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

// const handleDelete = () => {
//   if (confirm('确定要删除这条回复吗？')) {
//     emit('delete', props.reply.id);
//   }
// };

const handleEditSuccess = (content?: string) => {
  emit('edit', props.reply.id, content ?? '');
  isEditing.value = false;
};

const handleReplySuccess = () => {
  emit('reply', props.reply.id);
  showReplyForm.value = false;
};

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true
});

const renderMarkdown = (content: string) => {
  if (!content) return '';
  return md.render(content);
};

const renderedContent = computed(() => {
  if (!props.reply?.content) return '';
  return renderMarkdown(props.reply.content);
});
</script>
<script lang="ts">
export default {
  name: 'ReplyItem' // 显式设置组件名
}
</script>
<style scoped>
.reply-item {
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.3s ease;
}

.reply-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.reply-author {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.author-avatar {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 1rem;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.author-name {
  font-weight: 600;
  color: #1e293b;
  font-size: 0.95rem;
}

.author-role {
  font-size: 0.75rem;
  color: #64748b;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.teacher-badge {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  padding: 0.125rem 0.375rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: 500;
}

.reply-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: #64748b;
  font-size: 0.875rem;
}

.reply-date {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.reply-content {
  color: #334155;
  line-height: 1.7;
  margin-bottom: 1rem;
  font-size: 0.95rem;
  padding-left: 3.25rem;
}

.reply-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding-left: 3.25rem;
}

.reply-btn, .like-btn, .edit-btn, .delete-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4em;
  padding: 0.45rem 1.1rem;
  border-radius: 6px;
  border: 1.5px solid #e2e8f0;
  background: #fff;
  color: #334155;
  font-size: 0.93rem;
  font-weight: 500;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  transition: all 0.18s;
  cursor: pointer;
}
.reply-btn:hover, .like-btn:hover, .edit-btn:hover, .delete-btn:hover {
  background: #f1f5f9;
  border-color: #bcd0e5;
}
.reply-btn:active, .like-btn:active, .edit-btn:active, .delete-btn:active {
  background: #e2e8f0;
}
.edit-btn {
  color: #2563eb;
  border-color: #93c5fd;
}
.edit-btn:hover {
  background: #e0e7ef;
  color: #1e40af;
}
.delete-btn {
  color: #ef4444;
  border-color: #fca5a5;
}
.delete-btn:hover {
  background: #fee2e2;
  color: #b91c1c;
  border-color: #ef4444;
}
.like-btn {
  color: #64748b;
  border-color: #e2e8f0;
}
.like-btn.liked {
  color: #64748b;
  background: #fff;
  border-color: #e2e8f0;
}
.like-count {
  font-size: 0.875rem;
  color: #64748b;
  margin-left: 0.25rem;
}

.reply-form {
  margin-top: 1rem;
  padding-left: 3.25rem;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .reply-item {
    padding: 1rem;
  }

  .reply-content,
  .reply-actions,
  .reply-form {
    padding-left: 0;
  }

  .reply-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .reply-meta {
    width: 100%;
    justify-content: space-between;
  }
}

/* 动画效果 */
@keyframes likeAnimation {
  0% { transform: scale(1); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.like-button.liked i {
  animation: likeAnimation 0.3s ease;
}

/* 加载状态 */
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
}

.loading-spinner {
  width: 1.5rem;
  height: 1.5rem;
  border: 2px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 点赞用户列表样式 */
.like-users-popover {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(226, 232, 240, 0.8);
  padding: 1rem;
  min-width: 200px;
  z-index: 10;
}

.like-users-list {
  max-height: 200px;
  overflow-y: auto;
}

.like-user-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.like-user-item:hover {
  background: #f8fafc;
}

.like-user-avatar {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.875rem;
  font-weight: 600;
}

.like-user-name {
  font-size: 0.875rem;
  color: #1e293b;
  font-weight: 500;
}

.like-users-empty {
  text-align: center;
  padding: 1rem;
  color: #64748b;
  font-size: 0.875rem;
}

/* 滚动条样式 */
.like-users-list::-webkit-scrollbar {
  width: 6px;
}

.like-users-list::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.like-users-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.like-users-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
