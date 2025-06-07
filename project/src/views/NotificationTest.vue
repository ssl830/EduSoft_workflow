<template>
  <div class="notification-test-page">
    <div class="page-header">
      <h2>通知中心功能测试</h2>
      <p>测试通知列表、任务提醒等功能</p>
    </div>

    <div class="test-content">
      <!-- 通知中心组件 -->
      <NotificationCenter />
    </div>

    <!-- API测试工具 -->
    <q-card class="api-test-card q-mt-lg">
      <q-card-section>
        <h4>API测试工具</h4>
      </q-card-section>
      
      <q-card-section>
        <div class="row q-gutter-md">
          <q-btn 
            color="primary" 
            @click="testGetNotifications"
            :loading="loading.notifications"
          >
            获取通知列表
          </q-btn>
          
          <q-btn 
            color="secondary" 
            @click="testGetUnreadCount"
            :loading="loading.unreadCount"
          >
            获取未读数量
          </q-btn>
          
          <q-btn 
            color="positive" 
            @click="testCreateTaskReminder"
            :loading="loading.createTask"
          >
            创建测试任务
          </q-btn>
          
          <q-btn 
            color="warning" 
            @click="testMarkAllRead"
            :loading="loading.markAll"
          >
            全部标记已读
          </q-btn>
        </div>
      </q-card-section>

      <!-- API响应显示 -->
      <q-card-section v-if="apiResponse">
        <h5>API响应结果：</h5>
        <pre class="api-response">{{ JSON.stringify(apiResponse, null, 2) }}</pre>
      </q-card-section>
    </q-card>

    <!-- 功能说明 -->
    <q-card class="info-card q-mt-lg">
      <q-card-section>
        <h4>功能说明</h4>
        <q-list>
          <q-item>
            <q-item-section avatar>
              <q-icon name="notifications" color="primary" />
            </q-item-section>
            <q-item-section>
              <q-item-label>通知管理</q-item-label>
              <q-item-label caption>
                查看、标记已读、删除通知，支持多种通知类型
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section avatar>
              <q-icon name="task_alt" color="positive" />
            </q-item-section>
            <q-item-section>
              <q-item-label>任务提醒</q-item-label>
              <q-item-label caption>
                创建、编辑、完成任务提醒，支持优先级设置
              </q-item-label>
            </q-item-section>
          </q-item>

          <q-item>
            <q-item-section avatar>
              <q-icon name="api" color="secondary" />
            </q-item-section>
            <q-item-section>
              <q-item-label>API接口</q-item-label>
              <q-item-label caption>
                已实现所有后端接口规范要求的API方法
              </q-item-label>
            </q-item-section>
          </q-item>
        </q-list>
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import NotificationCenter from '../components/NotificationCenter.vue'
import { notificationApi } from '../api/notification'
import { useQuasar } from 'quasar'

const $q = useQuasar()

// 响应式数据
const apiResponse = ref<any>(null)
const loading = ref({
  notifications: false,
  unreadCount: false,
  createTask: false,
  markAll: false
})

// API测试方法
const testGetNotifications = async () => {
  loading.value.notifications = true
  try {
    const response = await notificationApi.getNotifications()
    apiResponse.value = response
    $q.notify({
      type: 'positive',
      message: '获取通知列表成功'
    })
  } catch (error: any) {
    console.error('获取通知失败:', error)
    apiResponse.value = { error: error?.message || '未知错误' }
    $q.notify({
      type: 'negative',
      message: '获取通知失败'
    })
  } finally {
    loading.value.notifications = false
  }
}

const testGetUnreadCount = async () => {
  loading.value.unreadCount = true
  try {
    const response = await notificationApi.getUnreadCount()
    apiResponse.value = response
    $q.notify({
      type: 'positive',
      message: '获取未读数量成功'
    })
  } catch (error: any) {
    console.error('获取未读数量失败:', error)
    apiResponse.value = { error: error?.message || '未知错误' }
    $q.notify({
      type: 'negative',
      message: '获取未读数量失败'
    })
  } finally {
    loading.value.unreadCount = false
  }
}

const testCreateTaskReminder = async () => {
  loading.value.createTask = true
  try {
    const testTask = {
      title: '测试任务提醒',
      content: '这是一个由API测试工具创建的测试任务',
      deadline: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString(), // 明天
      priority: 'MEDIUM' as const
    }
    
    const response = await notificationApi.createTaskReminder(testTask)
    apiResponse.value = response
    $q.notify({
      type: 'positive',
      message: '创建测试任务成功'
    })
  } catch (error: any) {
    console.error('创建任务失败:', error)
    apiResponse.value = { error: error?.message || '未知错误' }
    $q.notify({
      type: 'negative',
      message: '创建任务失败'
    })
  } finally {
    loading.value.createTask = false
  }
}

const testMarkAllRead = async () => {
  loading.value.markAll = true
  try {
    const response = await notificationApi.markAllAsRead()
    apiResponse.value = response
    $q.notify({
      type: 'positive',
      message: '全部标记已读成功'
    })
  } catch (error: any) {
    console.error('标记已读失败:', error)
    apiResponse.value = { error: error?.message || '未知错误' }
    $q.notify({
      type: 'negative',
      message: '标记已读失败'
    })
  } finally {
    loading.value.markAll = false
  }
}
</script>

<style scoped>
.notification-test-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h2 {
  color: #1976d2;
  margin-bottom: 10px;
}

.page-header p {
  color: #666;
  font-size: 16px;
}

.test-content {
  display: flex;
  justify-content: center;
}

.api-test-card {
  border-radius: 12px;
}

.api-response {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.info-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}
</style>
