<template>
  <div class="thread-detail-page p-6 bg-gray-50 min-h-screen">
    <div class="max-w-3xl mx-auto">      <button @click="goBack" class="mb-6 btn btn-secondary flex items-center">
        <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
          <path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd" />
        </svg>
        返回列表
      </button>

      <div v-if="loadingThread || loadingPosts" class="text-center py-10">
        <p class="text-blue-600">加载中...</p>
      </div>

      <div v-else-if="threadError || postsError" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative mb-6" role="alert">
        <strong class="font-bold">错误!</strong>
        <span class="block sm:inline"> {{ threadError || postsError }}</span>
      </div>

      <div v-else-if="!thread" class="text-center py-10 bg-white shadow-md rounded-lg">
        <p class="text-gray-600 text-lg">讨论帖未找到。</p>
      </div>

      <div v-else class="bg-white shadow-xl rounded-lg overflow-hidden">
        <!-- Thread Details -->
        <div class="p-6 border-b border-gray-200">          <h1 class="text-3xl font-bold text-blue-700 mb-3">{{ thread.title }}</h1>
          <p class="text-sm text-gray-500 mb-4">
            由 <span class="font-medium text-blue-600">{{ thread.creatorNum }}</span> 创建于 {{ formatDate(thread.createdAt) }}
          </p>
          <div class="prose max-w-none text-gray-800" v-html="thread.content"></div>
        </div>        <!-- Posts/Replies -->
        <div class="p-6">
          <h2 class="text-xl font-semibold text-blue-600 mb-4">回复 ({{ posts.length }})</h2>
          
          <!-- 顶层回复列表 -->
          <div v-if="posts.length > 0" class="space-y-6 mb-6">
            <div v-for="post in topLevelPosts" :key="post.id" class="reply-container">
              <!-- 顶层回复 -->
              <ReplyItem 
                :reply="post" 
                :level="0"
                @delete="handleDeleteReply"
                @edit="handleEditReply"
                @reply="handleReplyToReply"
                @like="handleLikeReply"
                @load-children="loadChildReplies"
              />
              
              <!-- 子回复列表 -->
              <div v-if="post.children && post.children.length > 0" class="ml-8 mt-4 space-y-3">
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
              
              <!-- 加载更多子回复按钮 -->
              <div v-if="hasMoreChildren(post)" class="ml-8 mt-2">
                <button 
                  @click="loadMoreChildren(post)"
                  :disabled="loadingChildren[post.id]"
                  class="text-blue-600 hover:text-blue-800 text-sm flex items-center"
                >
                  <svg v-if="loadingChildren[post.id]" class="animate-spin h-4 w-4 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"></path>
                  </svg>
                  <svg v-else class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
                  </svg>
                  {{ loadingChildren[post.id] ? '加载中...' : '查看更多回复' }}
                </button>
              </div>
            </div>
          </div>
          <p v-else class="text-gray-500 mb-6">还没有回复，快来抢沙发！</p>

          <!-- Create Post Form -->
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
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed, provide } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import discussionApi, { type Discussion, type DiscussionReply } from '../../api/discussion';
import ReplyItem from '../../components/ReplyItem.vue';
import ReplyForm from '../../components/ReplyForm.vue';

const route = useRoute();
const router = useRouter();

// 当前用户信息
const userRole = ref<'student' | 'assistant' | 'teacher'>('student'); // 默认为学生
const currentUserId = ref<string>('user123'); // 模拟当前用户ID

// 提供用户上下文给子组件
provide('currentUserId', currentUserId.value);
provide('userRole', userRole.value);

const threadId = ref<string>(route.params.threadId as string);
const thread = ref<Discussion | null>(null);
const posts = ref<DiscussionReply[]>([]);
const loadingChildren = ref<{ [key: number]: boolean }>({});

const loadingThread = ref(true);
const threadError = ref<string | null>(null);
const loadingPosts = ref(true);
const postsError = ref<string | null>(null);

// 计算顶层回复
const topLevelPosts = computed(() => {
  return posts.value.filter(post => !post.parentReplyId);
});

// 构建回复树结构
const buildReplyTree = (replies: DiscussionReply[]): DiscussionReply[] => {
  const replyMap = new Map<number, DiscussionReply>();
  const topLevel: DiscussionReply[] = [];

  // 初始化所有回复，确保children数组存在
  replies.forEach(reply => {
    replyMap.set(reply.id, { ...reply, children: [] });
  });

  // 构建树结构
  replies.forEach(reply => {
    const replyWithChildren = replyMap.get(reply.id)!;
    if (reply.parentReplyId) {
      const parent = replyMap.get(reply.parentReplyId);
      if (parent) {
        parent.children!.push(replyWithChildren);
      }
    } else {
      topLevel.push(replyWithChildren);
    }
  });

  return topLevel;
};

const fetchThreadDetails = async () => {
  loadingThread.value = true;
  threadError.value = null;
  try {
    const response = await discussionApi.getDiscussionDetail(Number(threadId.value));
    thread.value = response.data;
  } catch (err: any) {
    console.error('Failed to fetch thread details:', err);
    threadError.value = err.response?.data?.message || '无法加载讨论帖详情。';
  } finally {
    loadingThread.value = false;
  }
};

const fetchPosts = async () => {
  loadingPosts.value = true;
  postsError.value = null;
  try {
    // 获取顶层回复
    const response = await discussionApi.getTopLevelReplies(Number(threadId.value));
    const topLevelReplies = response.data.sort((a, b) => 
      new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );
    
    // 为每个顶层回复加载少量子回复
    for (const reply of topLevelReplies) {
      try {
        const childrenResponse = await discussionApi.getChildReplies(reply.id);
        reply.children = childrenResponse.data.slice(0, 3); // 只显示前3个子回复
      } catch (err) {
        console.warn(`Failed to fetch children for reply ${reply.id}:`, err);
        reply.children = [];
      }
    }
    
    posts.value = topLevelReplies;
  } catch (err: any) {
    console.error('Failed to fetch posts:', err);
    postsError.value = err.response?.data?.message || '无法加载回复列表。';
  } finally {
    loadingPosts.value = false;
  }
};

// 加载子回复
const loadChildReplies = async (parentId: number) => {
  try {
    const response = await discussionApi.getChildReplies(parentId);
    const parent = posts.value.find(p => p.id === parentId);
    if (parent) {
      parent.children = response.data.sort((a, b) => 
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );
    }
  } catch (err: any) {
    console.error('Failed to load child replies:', err);
  }
};

// 加载更多子回复
const loadMoreChildren = async (parent: DiscussionReply) => {
  loadingChildren.value[parent.id] = true;
  try {
    const response = await discussionApi.getChildReplies(parent.id);
    parent.children = response.data.sort((a, b) => 
      new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
    );
  } catch (err: any) {
    console.error('Failed to load more children:', err);
  } finally {
    loadingChildren.value[parent.id] = false;
  }
};

// 检查是否有更多子回复
const hasMoreChildren = (parent: DiscussionReply): boolean => {
  // 这里可以根据实际API返回的数据来判断
  // 暂时简单判断：如果子回复数量为3（我们的限制），可能还有更多
  return (parent.children?.length || 0) === 3;
};

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
    // 模拟点赞效果，实际项目中需要调用点赞API
    const updateLike = (replies: DiscussionReply[]): void => {
      replies.forEach(reply => {
        if (reply.id === replyId) {
          reply.likeCount = (reply.likeCount || 0) + 1;
        }
        if (reply.children) {
          updateLike(reply.children);
        }
      });
    };
    
    updateLike(posts.value);
    console.log(`点赞回复: ${replyId}`);
  } catch (err: any) {
    console.error('点赞失败:', err);
    alert('点赞失败，请重试');
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

onMounted(() => {
  if (threadId.value) {
    fetchThreadDetails();
    fetchPosts();
  } else {
    threadError.value = '未指定讨论帖ID。';
    postsError.value = '未指定讨论帖ID。';
    loadingThread.value = false;
    loadingPosts.value = false;
  }
});

</script>

<style scoped>
/* For rendering HTML content safely, consider using a library or DOMPurify if content is user-generated and complex */
.prose :deep(p) {
  margin-bottom: 1em;
}
</style>

