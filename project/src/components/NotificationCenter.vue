<template>
  <div class="notification-center">    <div class="notification-header">
      <h3>通知中心</h3>
      <div class="header-actions">
        <q-badge v-if="unreadCount > 0" color="red" :label="unreadCount" />
        <q-btn 
          flat 
          icon="mark_email_read" 
          @click="markAllAsRead"
          :disable="unreadCount === 0 || loading"
          tooltip="标记全部已读"
        />
        <q-btn 
          flat 
          icon="refresh" 
          @click="refreshNotifications"
          :loading="loading"
          tooltip="刷新"
        />
      </div>
    </div>    <q-separator />

    <!-- 通知筛选 -->
    <div class="notification-filters">
      <q-tabs v-model="activeTab" dense class="text-grey" indicator-color="primary">
        <q-tab name="all" label="全部" />
        <q-tab name="unread" label="未读" />
        <q-tab name="practice" label="练习通知" />
        <q-tab name="course" label="课程通知" />
        <q-tab name="system" label="系统通知" />
      </q-tabs>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list">
      <div v-if="loading" class="text-center q-pa-md">
        <q-spinner color="primary" size="3em" />
      </div>      <div v-else-if="filteredNotifications.length === 0" class="text-center q-pa-md text-grey-6">
        暂无通知
      </div>

      <q-list v-else separator>
        <q-item 
          v-for="notification in filteredNotifications" 
          :key="notification.id"
          :class="{ 'bg-blue-1': !notification.readFlag }"
          clickable
          @click="handleNotificationClick(notification)"
        >
          <q-item-section avatar>
            <q-icon 
              :name="getNotificationIcon(notification.type)" 
              :color="notification.readFlag ? 'grey-5' : 'primary'"
              size="md"
            />
          </q-item-section>

          <q-item-section>
            <q-item-label 
              :class="{ 'text-weight-bold': !notification.readFlag }"
            >
              {{ notification.title }}
            </q-item-label>
            <q-item-label caption lines="2">
              {{ notification.message }}
            </q-item-label>
            <q-item-label caption class="text-grey-6">
              {{ formatDate(notification.createdAt) }}
            </q-item-label>
          </q-item-section>

          <q-item-section side>
            <div class="row items-center">
              <q-btn 
                v-if="!notification.readFlag"
                flat 
                round 
                dense
                icon="mark_email_read"
                @click.stop="markAsRead(notification.id)"
                tooltip="标记已读"
              />
              <q-btn 
                flat 
                round 
                dense
                icon="delete"
                @click.stop="deleteNotification(notification.id)"
                tooltip="删除"
              />
            </div>
          </q-item-section>
        </q-item>
      </q-list>
    </div>

    <!-- 任务提醒部分 -->
    <q-separator class="q-my-md" />
    
    <div class="task-reminder-section">
      <div class="section-header">
        <h4>任务提醒</h4>
        <q-btn 
          flat 
          icon="add" 
          @click="showCreateTaskDialog = true"
          tooltip="创建任务提醒"
        />
      </div>

      <q-list separator>
        <q-item 
          v-for="task in taskReminders" 
          :key="task.id"
          :class="{ 'text-strike': task.completed }"
        >
          <q-item-section avatar>
            <q-checkbox 
              v-model="task.completed" 
              @update:model-value="toggleTaskComplete(task)"
            />
          </q-item-section>

          <q-item-section>
            <q-item-label>{{ task.title }}</q-item-label>
            <q-item-label caption>{{ task.content }}</q-item-label>
            <q-item-label caption class="text-grey-6">
              截止: {{ formatDate(task.deadline) }}
            </q-item-label>
          </q-item-section>

          <q-item-section side>
            <q-chip 
              :color="getPriorityColor(task.priority)" 
              text-color="white" 
              size="sm"
            >
              {{ task.priority }}
            </q-chip>
          </q-item-section>
        </q-item>
      </q-list>
    </div>

    <!-- 创建任务提醒对话框 -->
    <q-dialog v-model="showCreateTaskDialog">
      <q-card style="width: 400px">
        <q-card-section>
          <div class="text-h6">创建任务提醒</div>
        </q-card-section>

        <q-card-section>
          <q-form @submit="createTaskReminder">
            <q-input
              v-model="newTask.title"
              label="标题"
              required
              class="q-mb-md"
            />
            
            <q-input
              v-model="newTask.content"
              label="内容"
              type="textarea"
              class="q-mb-md"
            />
            
            <q-input
              v-model="newTask.deadline"
              label="截止时间"
              type="datetime-local"
              required
              class="q-mb-md"
            />
            
            <q-select
              v-model="newTask.priority"
              :options="priorityOptions"
              label="优先级"
              required
              class="q-mb-md"
            />

            <div class="row justify-end">
              <q-btn flat label="取消" @click="showCreateTaskDialog = false" />
              <q-btn type="submit" color="primary" label="创建" />
            </div>
          </q-form>
        </q-card-section>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { notificationApi, type Notification, type TaskReminder, type CreateTaskReminderRequest } from '../api/notification'
import { useQuasar } from 'quasar'
import { useAuthStore } from '../stores/auth'

const $q = useQuasar()
const authStore = useAuthStore()

// 定时器引用
let refreshTimer: NodeJS.Timeout | null = null

// 响应式数据
const notifications = ref<Notification[]>([])
const taskReminders = ref<TaskReminder[]>([])
const loading = ref(false)
const unreadCount = ref(0)
const showCreateTaskDialog = ref(false)
const activeTab = ref('all')

// 筛选后的通知列表
const filteredNotifications = computed(() => {
  let filtered = notifications.value
  
  switch (activeTab.value) {
    case 'unread':
      filtered = filtered.filter(n => !n.readFlag)
      break
    case 'practice':
      filtered = filtered.filter(n => n.type === 'PRACTICE_NOTICE' || n.type === 'HOMEWORK_NOTICE')
      break
    case 'course':
      filtered = filtered.filter(n => n.type === 'COURSE_NOTICE')
      break
    case 'system':
      filtered = filtered.filter(n => n.type === 'SYSTEM_NOTICE' || n.type === 'DEADLINE_REMINDER')
      break
    default:
      // 'all' - 显示全部
      break
  }
  
  return filtered
})

// 新任务表单
const newTask = ref<CreateTaskReminderRequest>({
  title: '',
  content: '',
  deadline: '',
  priority: 'MEDIUM'
})

const priorityOptions = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']

// 方法
const refreshNotifications = async () => {
  loading.value = true
  try {
    const [notificationsRes, unreadRes] = await Promise.all([
      notificationApi.getNotifications(),
      notificationApi.getUnreadCount()
    ])
    
    // 处理通知列表响应
    if (notificationsRes.data?.code === 200 || notificationsRes.status === 200) {
      // 兼容不同的响应格式
      const data = notificationsRes.data?.data || notificationsRes.data
      notifications.value = Array.isArray(data) ? data : []
    } else {
      console.error('获取通知列表失败:', notificationsRes.data?.message)
    }
    
    // 处理未读数量响应
    if (unreadRes.data?.code === 200 || unreadRes.status === 200) {
      const unreadData = unreadRes.data?.data || unreadRes.data
      unreadCount.value = typeof unreadData === 'number' ? unreadData : (unreadData?.count || 0)
    } else {
      console.error('获取未读数量失败:', unreadRes.data?.message)
    }
  } catch (error) {
    console.error('获取通知失败:', error)
    $q.notify({
      type: 'negative',
      message: '获取通知失败，请检查网络连接'
    })
  } finally {
    loading.value = false
  }
}

const loadTaskReminders = async () => {
  try {
    // 从auth store获取当前用户ID
    if (!authStore.user?.id) {
      console.warn('用户未登录，无法获取任务提醒')
      return
    }
    
    const response = await notificationApi.getTaskReminders(authStore.user.id)
    
    if (response.code === 200) {
      taskReminders.value = response.data
    } else {
      console.error('获取任务提醒失败:', response.message)
    }
  } catch (error) {
    console.error('获取任务提醒失败:', error)
    $q.notify({
      type: 'negative',
      message: '获取任务提醒失败'
    })
  }
}

const markAsRead = async (id: number) => {
  try {
    const response = await notificationApi.markAsRead(id)
    const success = response.data?.code === 200 || response.status === 200
    
    if (success) {
      const notification = notifications.value.find(n => n.id === id)
      if (notification) {
        notification.readFlag = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
      $q.notify({
        type: 'positive',
        message: '已标记为已读'
      })
    } else {
      throw new Error(response.data?.message || '操作失败')
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    $q.notify({
      type: 'negative',
      message: '标记已读失败'
    })
  }
}

const markAllAsRead = async () => {
  try {
    const response = await notificationApi.markAllAsRead()
    const success = response.data?.code === 200 || response.status === 200
    
    if (success) {
      notifications.value.forEach(n => n.readFlag = true)
      unreadCount.value = 0
      $q.notify({
        type: 'positive',
        message: '已全部标记为已读'
      })
    } else {
      throw new Error(response.data?.message || '操作失败')
    }
  } catch (error) {
    console.error('标记全部已读失败:', error)
    $q.notify({
      type: 'negative',
      message: '全部标记已读失败'
    })
  }
}

const deleteNotification = async (id: number) => {
  try {
    const response = await notificationApi.deleteNotification(id)
    const success = response.data?.code === 200 || response.status === 200
    
    if (success) {
      const deletedNotification = notifications.value.find(n => n.id === id)
      if (deletedNotification && !deletedNotification.readFlag) {
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
      notifications.value = notifications.value.filter(n => n.id !== id)
      $q.notify({
        type: 'positive',
        message: '已删除通知'
      })
    } else {
      throw new Error(response.data?.message || '删除失败')
    }
  } catch (error) {
    console.error('删除通知失败:', error)
    $q.notify({
      type: 'negative',
      message: '删除通知失败'
    })
  }
}

const createTaskReminder = async () => {
  try {
    const response = await notificationApi.createTaskReminder(newTask.value)
    const success = response.data?.code === 200 || response.status === 200
    
    if (success) {
      const createdTask = response.data?.data || response.data
      if (createdTask) {
        taskReminders.value.push(createdTask)
      }
      showCreateTaskDialog.value = false
      newTask.value = {
        title: '',
        content: '',
        deadline: '',
        priority: 'MEDIUM'
      }
      $q.notify({
        type: 'positive',
        message: '任务提醒创建成功'
      })
    } else {
      throw new Error(response.data?.message || '创建失败')
    }
  } catch (error) {
    console.error('创建任务提醒失败:', error)
    $q.notify({
      type: 'negative',
      message: '创建任务提醒失败'
    })
  }
}

const toggleTaskComplete = async (task: TaskReminder) => {
  try {
    const response = task.completed 
      ? await notificationApi.markTaskComplete(task.id)
      : await notificationApi.markTaskUncomplete(task.id)
    
    const success = response.data?.code === 200 || response.status === 200
    
    if (success) {
      $q.notify({
        type: 'positive',
        message: task.completed ? '任务已完成' : '任务已标记为未完成'
      })
    } else {
      throw new Error(response.data?.message || '操作失败')
    }
  } catch (error) {
    console.error('更新任务状态失败:', error)
    // 恢复状态
    task.completed = !task.completed
    $q.notify({
      type: 'negative',
      message: '更新任务状态失败'
    })
  }
}

const handleNotificationClick = (notification: Notification) => {
  // 处理通知点击事件，可以跳转到相关页面
  if (!notification.readFlag) {
    markAsRead(notification.id)
  }
  
  // 根据通知类型跳转到相应页面
  if (notification.relatedType && notification.relatedId) {
    switch (notification.relatedType) {
      case 'PRACTICE':
        // 跳转到练习页面
        console.log('跳转到练习:', notification.relatedId)
        // 这里可以使用 router.push(`/exercise/${notification.relatedId}`)
        break
      case 'HOMEWORK':
        // 跳转到作业页面
        console.log('跳转到作业:', notification.relatedId)
        // 这里可以使用 router.push(`/homework/${notification.relatedId}`)
        break
      case 'COURSE':
        // 跳转到课程页面
        console.log('跳转到课程:', notification.relatedId)
        // 这里可以使用 router.push(`/course/${notification.relatedId}`)
        break
    }
  }
  
  // 显示通知详情
  $q.notify({
    type: 'info',
    message: notification.message,
    timeout: 3000,
    position: 'top'
  })
}

// 工具方法
const getNotificationIcon = (type: string) => {
  const iconMap: Record<string, string> = {
    'PRACTICE_NOTICE': 'assignment',
    'HOMEWORK_NOTICE': 'assignment_turned_in',
    'COURSE_NOTICE': 'school',
    'SYSTEM_NOTICE': 'info',
    'DEADLINE_REMINDER': 'alarm'
  }
  return iconMap[type] || 'notifications'
}

const getPriorityColor = (priority: string) => {
  const colorMap: Record<string, string> = {
    'LOW': 'green',
    'MEDIUM': 'orange',
    'HIGH': 'red',
    'URGENT': 'deep-purple'
  }
  return colorMap[priority] || 'grey'
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  refreshNotifications()
  loadTaskReminders()
  
  // 设置定时刷新通知（每30秒）
  refreshTimer = setInterval(() => {
    refreshNotifications()
  }, 30000)
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.notification-center {
  max-width: 600px;
  margin: 0 auto;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.notification-header h3 {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-filters {
  margin-bottom: 16px;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-list::-webkit-scrollbar {
  width: 6px;
}

.notification-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.notification-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.notification-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header h4 {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.task-reminder-section {
  margin-top: 24px;
}

/* 未读通知高亮 */
.bg-blue-1 {
  background-color: #e3f2fd !important;
  border-left: 4px solid #2196f3;
}

/* 优先级颜色 */
.q-chip.bg-green {
  background-color: #4caf50 !important;
}

.q-chip.bg-orange {
  background-color: #ff9800 !important;
}

.q-chip.bg-red {
  background-color: #f44336 !important;
}

.q-chip.bg-deep-purple {
  background-color: #673ab7 !important;
}

/* 已完成任务样式 */
.text-strike {
  text-decoration: line-through;
  opacity: 0.6;
}

/* 加载状态 */
.loading-indicator {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  color: #666;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .notification-center {
    margin: 0;
    padding: 12px;
    border-radius: 0;
    box-shadow: none;
  }
  
  .notification-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
