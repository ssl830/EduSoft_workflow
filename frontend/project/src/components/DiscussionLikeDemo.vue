<!-- 讨论点赞功能示例组件 -->
<template>
  <div class="discussion-like-demo">
    <h2>讨论点赞功能测试</h2>
    
    <!-- 讨论ID输入 -->
    <div class="input-section">
      <q-input
        v-model.number="discussionId"
        label="讨论ID"
        type="number"
        outlined
        dense
        style="width: 200px"
      />
    </div>

    <!-- 点赞操作区域 -->
    <div class="like-actions">
      <q-btn
        @click="likeDiscussion"
        :loading="loading.like"
        color="primary"
        icon="thumb_up"
        label="点赞"
        :disable="!discussionId"
      />
      
      <q-btn
        @click="checkIfLiked"
        :loading="loading.check"
        color="secondary"
        icon="search"
        label="检查是否已点赞"
        :disable="!discussionId"
      />
      
      <q-btn
        @click="getLikeCount"
        :loading="loading.count"
        color="info"
        icon="analytics"
        label="获取点赞数"
        :disable="!discussionId"
      />
    </div>

    <!-- 点赞状态显示 -->
    <div class="like-status" v-if="likeStatus">
      <q-card class="q-mt-md">
        <q-card-section>
          <div class="text-h6">点赞状态</div>
          <div>是否已点赞: {{ likeStatus.hasLiked ? '是' : '否' }}</div>
          <div>点赞数量: {{ likeStatus.count }}</div>
          <div v-if="likeStatus.myLikeId">我的点赞ID: {{ likeStatus.myLikeId }}</div>
        </q-card-section>
        <q-card-actions v-if="likeStatus.hasLiked && likeStatus.myLikeId">
          <q-btn
            @click="unlikeDiscussion"
            :loading="loading.unlike"
            color="negative"
            icon="thumb_down"
            label="取消点赞"
          />
        </q-card-actions>
      </q-card>
    </div>

    <!-- 点赞列表 -->
    <div class="like-list" v-if="likeList.length > 0">
      <q-card class="q-mt-md">
        <q-card-section>
          <div class="text-h6">点赞列表</div>
          <q-list>
            <q-item
              v-for="like in likeList"
              :key="like.id"
              clickable
              @click="getLikeRecord(like.id)"
            >
              <q-item-section>
                <q-item-label>用户ID: {{ like.userId }}</q-item-label>
                <q-item-label caption>
                  点赞时间: {{ formatDate(like.createdAt) }}
                </q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-item-label>ID: {{ like.id }}</q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
      </q-card>
    </div>

    <!-- 用户点赞记录 -->
    <div class="user-likes-section">
      <q-input
        v-model.number="userId"
        label="用户ID"
        type="number"
        outlined
        dense
        style="width: 200px"
        class="q-mt-md"
      />
      <q-btn
        @click="getUserLikes"
        :loading="loading.userLikes"
        color="accent"
        icon="person"
        label="获取用户点赞记录"
        :disable="!userId"
        class="q-ml-md"
      />
    </div>

    <!-- 用户点赞列表 -->
    <div class="user-like-list" v-if="userLikeList.length > 0">
      <q-card class="q-mt-md">
        <q-card-section>
          <div class="text-h6">用户点赞记录</div>
          <q-list>
            <q-item
              v-for="like in userLikeList"
              :key="like.id"
              clickable
            >
              <q-item-section>
                <q-item-label>讨论ID: {{ like.discussionId }}</q-item-label>
                <q-item-label caption>
                  点赞时间: {{ formatDate(like.createdAt) }}
                </q-item-label>
              </q-item-section>
            </q-item>
          </q-list>
        </q-card-section>
      </q-card>
    </div>

    <!-- 错误和成功消息 -->
    <div class="messages q-mt-md">
      <q-banner v-if="errorMessage" type="negative" class="q-mb-md">
        {{ errorMessage }}
      </q-banner>
      <q-banner v-if="successMessage" type="positive" class="q-mb-md">
        {{ successMessage }}
      </q-banner>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import discussionApi, { DiscussionLike } from '../api/discussion'

// 响应式数据
const discussionId = ref<number>(1)
const userId = ref<number>(1)
const likeList = ref<DiscussionLike[]>([])
const userLikeList = ref<DiscussionLike[]>([])

// 点赞状态
const likeStatus = ref<{
  hasLiked: boolean
  count: number
  myLikeId?: number
} | null>(null)

// 加载状态
const loading = reactive({
  like: false,
  unlike: false,
  check: false,
  count: false,
  userLikes: false
})

// 消息
const errorMessage = ref('')
const successMessage = ref('')

// 清除消息
const clearMessages = () => {
  errorMessage.value = ''
  successMessage.value = ''
}

// 显示成功消息
const showSuccess = (message: string) => {
  clearMessages()
  successMessage.value = message
  setTimeout(() => successMessage.value = '', 3000)
}

// 显示错误消息
const showError = (message: string) => {
  clearMessages()
  errorMessage.value = message
  setTimeout(() => errorMessage.value = '', 5000)
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 点赞讨论
const likeDiscussion = async () => {
  if (!discussionId.value) return
  
  loading.like = true
  try {
    const response = await discussionApi.likeDiscussion(discussionId.value)
    console.log('APIfox 点赞响应:', response.data)
    showSuccess(`点赞成功！点赞ID: ${response.data.id}`)
    
    // 更新状态
    await Promise.all([checkIfLiked(), getLikeCount(), getDiscussionLikes()])
  } catch (error: any) {
    console.error('APIfox 点赞错误:', error)
    showError(error.response?.data?.message || error.message || '点赞失败')
  } finally {
    loading.like = false
  }
}

// 取消点赞
const unlikeDiscussion = async () => {
  if (!likeStatus.value?.myLikeId) return
  
  loading.unlike = true
  try {
    const response = await discussionApi.unlikeDiscussion(likeStatus.value.myLikeId)
    console.log('APIfox 取消点赞响应:', response.data)
    showSuccess(response.data.message || '取消点赞成功！')
    
    // 更新状态
    await Promise.all([checkIfLiked(), getLikeCount(), getDiscussionLikes()])
  } catch (error: any) {
    console.error('APIfox 取消点赞错误:', error)
    showError(error.response?.data?.message || error.message || '取消点赞失败')
  } finally {
    loading.unlike = false
  }
}

// 检查是否已点赞
const checkIfLiked = async () => {
  if (!discussionId.value) return
  
  loading.check = true
  try {
    const response = await discussionApi.checkIfLiked(discussionId.value)
    console.log('APIfox 点赞检查响应:', response.data)
    
    if (!likeStatus.value) {
      likeStatus.value = { hasLiked: false, count: 0 }
    }
    likeStatus.value.hasLiked = response.data.hasLiked
    
    // 如果已点赞，尝试找到我的点赞ID
    if (response.data.hasLiked) {
      await getDiscussionLikes()
    }
  } catch (error: any) {
    console.error('APIfox 点赞检查错误:', error)
    showError(error.response?.data?.message || error.message || '检查点赞状态失败')
  } finally {
    loading.check = false
  }
}

// 获取点赞数量
const getLikeCount = async () => {
  if (!discussionId.value) return
  
  loading.count = true
  try {
    const response = await discussionApi.getDiscussionLikeCount(discussionId.value)
    console.log('APIfox 点赞数量响应:', response.data)
    
    if (!likeStatus.value) {
      likeStatus.value = { hasLiked: false, count: 0 }
    }
    likeStatus.value.count = response.data.count
  } catch (error: any) {
    console.error('APIfox 获取点赞数量错误:', error)
    showError(error.response?.data?.message || error.message || '获取点赞数量失败')
  } finally {
    loading.count = false
  }
}

// 获取讨论的所有点赞
const getDiscussionLikes = async () => {
  if (!discussionId.value) return
  
  try {
    const response = await discussionApi.getDiscussionLikes(discussionId.value)
    console.log('APIfox 讨论点赞列表响应:', response.data)
    likeList.value = response.data
    
    // 尝试找到当前用户的点赞记录
    // 注意：这里需要从用户认证状态中获取当前用户ID
    // 暂时使用userId输入框的值
    if (userId.value && likeStatus.value) {
      const myLike = response.data.find(like => like.userId === userId.value)
      if (myLike) {
        likeStatus.value.myLikeId = myLike.id
      }
    }
  } catch (error: any) {
    console.error('APIfox 获取讨论点赞列表错误:', error)
    showError(error.response?.data?.message || error.message || '获取点赞列表失败')
  }
}

// 获取点赞记录详情
const getLikeRecord = async (likeId: number) => {
  try {
    const response = await discussionApi.getLikeRecord(likeId)
    console.log('APIfox 点赞记录详情:', response.data)
    showSuccess(`点赞记录详情已打印到控制台`)
  } catch (error: any) {
    console.error('APIfox 获取点赞记录详情错误:', error)
    showError(error.response?.data?.message || error.message || '获取点赞记录详情失败')
  }
}

// 获取用户的所有点赞
const getUserLikes = async () => {
  if (!userId.value) return
  
  loading.userLikes = true
  try {
    const response = await discussionApi.getUserLikes(userId.value)
    console.log('APIfox 用户点赞记录响应:', response.data)
    userLikeList.value = response.data
    
    // 获取用户点赞数量
    const countResponse = await discussionApi.getUserLikeCount(userId.value)
    console.log('APIfox 用户点赞数量:', countResponse.data)
    showSuccess(`获取成功！用户总点赞数: ${countResponse.data.count}`)
  } catch (error: any) {
    console.error('APIfox 获取用户点赞记录错误:', error)
    showError(error.response?.data?.message || error.message || '获取用户点赞记录失败')
  } finally {
    loading.userLikes = false
  }
}
</script>

<style scoped>
.discussion-like-demo {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.input-section {
  margin-bottom: 20px;
}

.like-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.user-likes-section {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 20px;
}

.q-banner {
  border-radius: 4px;
}
</style>
