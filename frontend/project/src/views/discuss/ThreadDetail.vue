<template>
  <div class="thread-detail-page">
    <div class="max-w-3xl mx-auto">
      <button @click="goBack" class="back-button">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
        </svg>
        返回列表
      </button>

      <!-- Loading State -->
      <div v-if="loadingThread || loadingPosts" class="loading-state">
        <div class="loading-spinner"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="threadError || postsError" class="error-state">
        <strong class="font-bold">错误!</strong>
        <span class="block sm:inline"> {{ threadError || postsError }}</span>
      </div>

      <!-- Empty State -->
      <div v-else-if="!thread" class="empty-state">
        <div class="empty-state-icon">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <p class="text-lg">讨论帖未找到。</p>
      </div>

      <!-- Thread Content -->
      <div v-else class="thread-container">
        <div class="thread-header">
          <h1 class="thread-title">{{ thread.title }}</h1>
          <div class="thread-meta">
            <div class="creator-info">
              <div class="creator-avatar">{{ thread.creatorNum[0] }}</div>
              <span class="creator-name">{{ thread.creatorNum }}</span>
            </div>
            <span class="text-gray-400">•</span>
            <span>{{ formatDate(thread.createdAt) }}</span>
            <!-- 主贴删除按钮，仅作者或老师可见 -->
            <button v-if="thread && (thread.creatorId == currentUserId || userRole === 'teacher')" @click="handleDeleteThread" class="btn btn-delete-thread ml-4">
              <i class="fas fa-trash-alt"></i> 删除帖子
            </button>
          </div>
        </div>

        <div class="thread-content prose" v-html="renderedContent"></div>

        <!-- 回复区折叠按钮 -->
        <div class="flex justify-end mb-2">
          <button class="btn btn-toggle-replies" @click="repliesCollapsed = !repliesCollapsed">
            <i :class="repliesCollapsed ? 'fas fa-chevron-down' : 'fas fa-chevron-up'"></i>
            {{ repliesCollapsed ? '展开回复' : '收起回复' }}
          </button>
        </div>

        <!-- Replies Section -->
        <div class="replies-section" v-show="!repliesCollapsed">
          <div class="replies-header">
            <h2 class="replies-title">
              回复
              <span class="replies-count">{{ posts.length }}</span>
            </h2>
          </div>

          <!-- Reply List -->
          <div v-if="posts.length > 0" class="reply-list">
            <div v-for="post in posts" :key="post.id" class="reply-container">
              <ReplyItem
                :reply="post"
                :level="0"
                @delete="handleDeleteReply"
                @edit="handleEditReply"
                @reply="handleReplyToReply"
                @like="handleLikeReply"
                @load-children="loadChildReplies"
              />

              <!-- Child Replies -->
              <div v-if="post.children && post.children.length > 0" class="child-replies">
                <ReplyItem
                  v-for="childReply in post.children"
                  :key="childReply.id"
                  :reply="childReply"
                  :level="1"
                  @delete="handleDeleteReply"
                  @edit="handleEditReply"
                  @reply="handleReplyToReply"
                  @like="handleLikeReply"
                />
              </div>
            </div>
          </div>

          <!-- Empty Replies State -->
          <div v-else class="empty-state">
            <div class="empty-state-icon">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <p class="text-lg">还没有回复，快来抢沙发！</p>
          </div>

          <!-- Reply Form -->
          <div class="reply-form-container">
          <ReplyForm
            :discussion-id="Number(threadId)"
            :parent-reply-id="null"
            @success="handleReplySuccess"
            @cancel="() => {}"
            placeholder="发表你的回复..."
            submit-text="提交回复"
          />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, provide, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import discussionApi, { type DiscussionReply } from '../../api/discussion';
import ReplyItem from '../../components/ReplyItem.vue';
import ReplyForm from '../../components/ReplyForm.vue';
import MarkdownIt from 'markdown-it';

const route = useRoute();
const router = useRouter();

// 当前用户信息
const userRole = ref<'student' | 'assistant' | 'teacher'>('student'); // 默认为学生
const currentUserId = ref<string>('user123'); // 模拟当前用户ID

// 提供用户上下文给子组件
provide('currentUserId', currentUserId.value);
provide('userRole', userRole.value);

const threadId = ref<string>(route.params.threadId as string);
const thread = ref<any>(null);
const posts = ref<DiscussionReply[]>([]);
const loadingChildren = ref<{ [key: number]: boolean }>({});

const loadingThread = ref(true);
const threadError = ref<string | null>(null);
const loadingPosts = ref(true);
const postsError = ref<string | null>(null);

const repliesCollapsed = ref(false);

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true
});

// 计算属性
const renderedContent = computed(() => {
  if (!thread.value?.content) return '';
  return md.render(thread.value.content);
});

// // 计算顶层回复
// const topLevelPosts = computed(() => {
//   return posts.value.filter(post => !post.parentReplyId);
// });
//
// // 构建回复树结构
// const buildReplyTree = (replies: DiscussionReply[]): DiscussionReply[] => {
//   const replyMap = new Map<number, DiscussionReply>();
//   const topLevel: DiscussionReply[] = [];
//
//   // 初始化所有回复，确保children数组存在
//   replies.forEach(reply => {
//     replyMap.set(reply.id, { ...reply, children: [] });
//   });
//
//   // 构建树结构
//   replies.forEach(reply => {
//     const replyWithChildren = replyMap.get(reply.id)!;
//     if (reply.parentReplyId) {
//       const parent = replyMap.get(reply.parentReplyId);
//       if (parent) {
//         parent.children!.push(replyWithChildren);
//       }
//     } else {
//       topLevel.push(replyWithChildren);
//     }
//   });
//
//   return topLevel;
// };

const fetchThreadDetails = async () => {
  loadingThread.value = true;
  threadError.value = null;
  try {
  const response = await discussionApi.getDiscussionDetail(Number(threadId.value));
  console.log('Thread details response:', response);
  thread.value = (response && typeof response === 'object' && 'data' in response) ? (response as any).data : response;
  } catch (err: any) {
    console.error('Failed to fetch thread details:', err);
    threadError.value = err.response?.data?.message || '无法加载讨论帖详情。';
    thread.value = null;
  } finally {
    loadingThread.value = false;
  }
};

const fetchPosts = async () => {
  loadingPosts.value = true;
  postsError.value = null;
  try {
    // 获取顶层回复
    const response:any = await discussionApi.getTopLevelReplies(Number(threadId.value));
    console.log('Top level replies response:', response);

    const topLevel: any[] = Array.isArray(response)
      ? response
      : (response && typeof response === 'object' && 'data' in response && Array.isArray((response as any).data))
        ? (response as any).data
        : [];

    // 将后端返回的数据结构转换为前端需要的格式
    posts.value = topLevel.map((reply:any) => ({
      id: reply.id,
      content: reply.content,
      creatorId: reply.userId,
      creatorNum: reply.userNum,
      discussionId: reply.discussionId,
      isTeacher: reply.isTeacherReply || false,
      createdAt: reply.createdAt,
      updatedAt: reply.updatedAt,
      parentReplyId: reply.parentReplyId,
      children: []
    }));

    // 为每个顶层回复加载子回复
    for (const post of posts.value) {
      if (!loadingChildren.value[post.id]) {
        loadingChildren.value[post.id] = true;
        try {
          const childrenResponse:any = await discussionApi.getChildReplies(post.id);
          console.log(`Child replies for post ${post.id}:`, childrenResponse);

          const children = Array.isArray(childrenResponse)
            ? childrenResponse
            : (childrenResponse && typeof childrenResponse === 'object' && 'data' in childrenResponse && Array.isArray((childrenResponse as any).data))
              ? (childrenResponse as any).data
              : [];

          post.children = children.map((child:any) => ({
            id: child.id,
            content: child.content,
            creatorId: child.userId,
            creatorNum: child.userNum,
            discussionId: child.discussionId,
            isTeacher: child.isTeacherReply || false,
            createdAt: child.createdAt,
            updatedAt: child.updatedAt,
            parentReplyId: child.parentReplyId
          }));
      } catch (err) {
          console.error(`获取回复(${post.id})的子回复失败:`, err);
          post.children = [];
        } finally {
          loadingChildren.value[post.id] = false;
        }
      }
    }
  } catch (err: any) {
    console.error('Failed to fetch posts:', err);
    postsError.value = err.response?.data?.message || '无法加载回复列表。';
    posts.value = [];
  } finally {
    loadingPosts.value = false;
  }
};

// 加载子回复
const loadChildReplies = async (parentId: number) => {
  try {
    const response:any = await discussionApi.getChildReplies(parentId);
    console.log(`Loading child replies for parent ${parentId}:`, response);

    const children = Array.isArray(response)
      ? response
      : (response && typeof response === 'object' && 'data' in response && Array.isArray((response as any).data))
        ? (response as any).data
        : [];

    const parent = posts.value.find(p => p.id === parentId);
    if (parent) {
      parent.children = children.map((child:any) => ({
        id: child.id,
        content: child.content,
        creatorId: child.userId,
        creatorNum: child.userNum,
        discussionId: child.discussionId,
        isTeacher: child.isTeacherReply || false,
        createdAt: child.createdAt,
        updatedAt: child.updatedAt,
        parentReplyId: child.parentReplyId
      }));
    }
  } catch (err: any) {
    console.error('Failed to load child replies:', err);
  }
};

// // 加载更多子回复
// const loadMoreChildren = async (parent: DiscussionReply) => {
//   loadingChildren.value[parent.id] = true;
//   try {
//     const response = await discussionApi.getChildReplies(parent.id);
//     console.log(`Loading more children for parent ${parent.id}:`, response);
//
//     parent.children = (response || []).map(child => ({
//       id: child.id,
//       content: child.content,
//       creatorId: child.userId,
//       creatorNum: child.userNum,
//       discussionId: child.discussionId,
//       isTeacher: child.isTeacherReply || false,
//       createdAt: child.createdAt,
//       updatedAt: child.updatedAt,
//       parentReplyId: child.parentReplyId
//     }));
//   } catch (err: any) {
//     console.error('Failed to load more children:', err);
//   } finally {
//     loadingChildren.value[parent.id] = false;
//   }
// };
//
// // 检查是否有更多子回复
// const hasMoreChildren = (parent: DiscussionReply): boolean => {
//   // 这里可以根据实际API返回的数据来判断
//   // 暂时简单判断：如果子回复数量为3（我们的限制），可能还有更多
//   return (parent.children?.length || 0) === 3;
// };

// 处理回复成功
const handleReplySuccess = () => {
  fetchPosts(); // 重新加载回复列表
};

// 处理删除回复
const handleDeleteReply = async (replyId: number) => {
  if (!confirm('确定要删除这条回复吗？此操作不可撤销。')) {
    return;
  }

  try {
    await discussionApi.deleteReply(replyId);
    // 从列表中移除被删除的回复
    const removeReply = (replies: DiscussionReply[]): DiscussionReply[] => {
      return replies.filter(reply => {
        if (reply.id === replyId) {
          return false;
        }
        if (reply.children) {
          reply.children = removeReply(reply.children);
        }
        return true;
      });
    };

    posts.value = removeReply(posts.value);
  } catch (err: any) {
    console.error('删除失败:', err);
    alert('删除失败，请重试');
  }
};

// 处理编辑回复
const handleEditReply = async (replyId: number, newContent: string) => {
  try {
    const response = await discussionApi.updateReply(replyId, { content: newContent });

    // 更新本地数据
    const updateReply = (replies: DiscussionReply[]): void => {
      replies.forEach(reply => {
        if (reply.id === replyId) {
          reply.content = response.data.content;
          reply.updatedAt = response.data.updatedAt;
        }
        if (reply.children) {
          updateReply(reply.children);
        }
      });
    };

    updateReply(posts.value);
  } catch (err: any) {
    console.error('编辑失败:', err);
    alert('编辑失败，请重试');
  }
};

// 处理回复的回复
const handleReplyToReply = (parentReplyId: number) => {
  // 这个功能可以通过ReplyForm组件来处理
  console.log('Reply to reply:', parentReplyId);
};

// 处理点赞回复
const handleLikeReply = async (replyId: number) => {
  try {
    // 获取当前点赞状态
    const statusResponse = await discussionApi.getLikeStatus(replyId);
    const { liked } = statusResponse.data;

    // 根据当前状态决定是点赞还是取消点赞
    if (liked) {
      await discussionApi.unlikeReply(replyId);
    } else {
      await discussionApi.likeReply(replyId);
    }

    // 更新回复的点赞数
    const updateLike = (replies: DiscussionReply[]): void => {
      replies.forEach(reply => {
        if (reply.id === replyId) {
          // 更新点赞数
          reply.likeCount = (reply.likeCount || 0) + (liked ? -1 : 1);
        }
        if (reply.children) {
          updateLike(reply.children);
        }
      });
    };

    updateLike(posts.value);
  } catch (err: any) {
    console.error('点赞失败:', err);
  }
};

const formatDate = (dateString: string) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit'
  });
};

const goBack = () => {
  if (thread.value?.courseId) {
    router.push(`/courses/${thread.value.courseId}/discussions`);
  } else {
    router.back(); // Fallback if courseId is not available
  }
};

// const renderMarkdown = (content: string) => {
//   if (!content) return '';
//   return md.render(content);
// };

// 添加更多的调试信息
watch(thread, (newVal) => {
  console.log('Thread value changed:', newVal);
}, { deep: true });

watch(posts, (newVal) => {
  console.log('Posts value changed:', newVal);
}, { deep: true });

// 删除主贴
const handleDeleteThread = async () => {
  if (!confirm('确定要删除这个帖子吗？此操作不可撤销。')) return;
  // try {
  //   await discussionApi.deleteThread(Number(threadId.value));
  //   router.back();
  // } catch (err: any) {
  //   alert('删除失败，请重试');
  // }
};

onMounted(async () => {
  console.log('ThreadDetail mounted, threadId:', threadId.value);
  await fetchThreadDetails();
  await fetchPosts();
});

</script>

<style scoped>
.thread-detail-page {
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  min-height: 100vh;
  padding: 2rem;
}

.back-button {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  color: #64748b;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.back-button:hover {
  transform: translateX(-4px);
  background: #f8fafc;
  color: #3b82f6;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.thread-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.8);
  backdrop-filter: blur(20px);
}

.thread-header {
  padding: 2rem;
  border-bottom: 1px solid #e2e8f0;
  background: linear-gradient(135deg, #f8fafc, white);
}

.thread-title {
  font-size: 2rem;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 1rem;
  line-height: 1.3;
}

.thread-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: #64748b;
  font-size: 0.875rem;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.creator-avatar {
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

.creator-name {
  font-weight: 600;
  color: #3b82f6;
}

.thread-content {
  padding: 2rem;
  color: #334155;
  line-height: 1.8;
  font-size: 1.1rem;
}

.replies-section {
  padding: 2rem;
  background: #f8fafc;
}

.replies-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.replies-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.replies-count {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  font-size: 0.875rem;
  font-weight: 600;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.reply-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.3s ease;
}

.reply-container:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.child-replies {
  margin-left: 3rem;
  padding-left: 1.5rem;
  border-left: 2px solid #e2e8f0;
}

.reply-form-container {
  margin-top: 2rem;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 4rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.loading-spinner {
  width: 2.5rem;
  height: 2.5rem;
  border: 3px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-state {
  padding: 2rem;
  background: #fee2e2;
  border: 1px solid #fca5a5;
  border-radius: 12px;
  color: #dc2626;
  text-align: center;
}

.empty-state {
  padding: 4rem 2rem;
  text-align: center;
  background: white;
  border-radius: 12px;
  color: #64748b;
}

.empty-state-icon {
  font-size: 3rem;
  color: #94a3b8;
  margin-bottom: 1rem;
}

/* Markdown content styles */
.prose :deep(h1) {
  font-size: 2rem;
  font-weight: 700;
  margin: 2rem 0 1rem;
  color: #1e293b;
}

.prose :deep(h2) {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 1.5rem 0 1rem;
  color: #1e293b;
}

.prose :deep(h3) {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 1.25rem 0 0.75rem;
  color: #1e293b;
}

.prose :deep(p) {
  margin-bottom: 1.25rem;
  line-height: 1.8;
}

.prose :deep(ul), .prose :deep(ol) {
  margin: 1rem 0;
  padding-left: 1.5rem;
}

.prose :deep(li) {
  margin: 0.5rem 0;
}

.prose :deep(blockquote) {
  border-left: 4px solid #e2e8f0;
  padding-left: 1rem;
  margin: 1.5rem 0;
  color: #64748b;
  font-style: italic;
}

.prose :deep(code) {
  background: #f1f5f9;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-size: 0.875em;
  color: #2563eb;
}

.prose :deep(pre) {
  background: #1e293b;
  color: #f8fafc;
  padding: 1rem;
  border-radius: 8px;
  overflow-x: auto;
  margin: 1.5rem 0;
}

.prose :deep(pre code) {
  background: transparent;
  color: inherit;
  padding: 0;
  border-radius: 0;
}

.prose :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1.5rem 0;
}

.prose :deep(hr) {
  border: none;
  border-top: 1px solid #e2e8f0;
  margin: 2rem 0;
}

.prose :deep(a) {
  color: #3b82f6;
  text-decoration: none;
  transition: color 0.2s ease;
}

.prose :deep(a:hover) {
  color: #2563eb;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .thread-detail-page {
    padding: 1rem;
  }

  .thread-title {
    font-size: 1.5rem;
  }

  .thread-header,
  .thread-content,
  .replies-section {
    padding: 1.5rem;
  }

  .child-replies {
    margin-left: 1.5rem;
    padding-left: 1rem;
  }
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4em;
  padding: 0.5rem 1.2rem;
  border-radius: 6px;
  border: 1.5px solid #e2e8f0;
  background: #fff;
  color: #334155;
  font-size: 0.97rem;
  font-weight: 500;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
  transition: all 0.18s;
  cursor: pointer;
}
.btn:hover {
  background: #f1f5f9;
  border-color: #bcd0e5;
}
.btn:active {
  background: #e2e8f0;
}
.btn-delete-thread {
  color: #ef4444;
  border-color: #fca5a5;
  background: #fff;
}
.btn-delete-thread:hover {
  background: #fee2e2;
  border-color: #ef4444;
}
.btn-toggle-replies {
  font-size: 0.95rem;
  color: #2563eb;
  background: #f8fafc;
  border-color: #e2e8f0;
}
.btn-toggle-replies:hover {
  background: #e0e7ef;
  color: #1e40af;
}
</style>

