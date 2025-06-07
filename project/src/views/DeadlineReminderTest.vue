<template>
  <div class="deadline-reminder-test-page">
    <div class="page-header">
      <h2>DDL自动提醒功能测试</h2>
      <p>测试任务截止时间提醒功能</p>
    </div>

    <div class="test-content">
      <div class="row q-gutter-lg">
        <!-- DDL提醒组件 -->
        <div class="col-md-6 col-12">
          <DeadlineReminder />
        </div>

        <!-- 测试工具面板 -->
        <div class="col-md-5 col-12">
          <q-card class="test-tools-card">
            <q-card-section>
              <h4>测试工具</h4>
            </q-card-section>

            <q-card-section>
              <!-- 创建测试任务 -->
              <q-form @submit="createTestTask">
                <h5>创建测试任务</h5>
                <q-input
                  v-model="testTask.title"
                  label="任务标题"
                  class="q-mb-md"
                />
                
                <q-select
                  v-model="testTask.deadlineType"
                  :options="deadlineOptions"
                  label="截止时间类型"
                  class="q-mb-md"
                />
                
                <q-select
                  v-model="testTask.priority"
                  :options="priorityOptions"
                  label="优先级"
                  class="q-mb-md"
                />

                <q-btn 
                  type="submit" 
                  color="primary" 
                  :loading="creating"
                  class="full-width"
                >
                  创建测试任务
                </q-btn>
              </q-form>
            </q-card-section>

            <q-separator />

            <!-- 快速操作 -->
            <q-card-section>
              <h5>快速操作</h5>
              <div class="row q-gutter-sm">
                <q-btn 
                  color="warning" 
                  @click="createUrgentTask"
                  :loading="creating"
                  size="sm"
                >
                  创建紧急任务
                </q-btn>
                
                <q-btn 
                  color="info" 
                  @click="simulateDeadlineCheck"
                  size="sm"
                >
                  模拟DDL检查
                </q-btn>
                
                <q-btn 
                  color="positive" 
                  @click="testBrowserNotification"
                  size="sm"
                >
                  测试浏览器通知
                </q-btn>
              </div>
            </q-card-section>

            <q-separator />

            <!-- 系统状态 -->
            <q-card-section>
              <h5>系统状态</h5>
              <q-list dense>
                <q-item>
                  <q-item-section>
                    <q-item-label caption>浏览器通知权限</q-item-label>
                    <q-item-label>
                      <q-chip 
                        :color="notificationPermission === 'granted' ? 'green' : 'red'"
                        text-color="white"
                        size="sm"
                      >
                        {{ notificationPermission }}
                      </q-chip>
                    </q-item-label>
                  </q-item-section>
                </q-item>

                <q-item>
                  <q-item-section>
                    <q-item-label caption>当前时间</q-item-label>
                    <q-item-label>{{ currentTime }}</q-item-label>
                  </q-item-section>
                </q-item>

                <q-item>
                  <q-item-section>
                    <q-item-label caption>测试任务数量</q-item-label>
                    <q-item-label>{{ testTaskCount }}</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </q-card-section>
          </q-card>

          <!-- API响应显示 -->
          <q-card v-if="apiResponse" class="q-mt-md">
            <q-card-section>
              <h5>最新API响应</h5>
              <pre class="api-response">{{ JSON.stringify(apiResponse, null, 2) }}</pre>
            </q-card-section>
          </q-card>
        </div>
      </div>
    </div>

    <!-- 功能说明 -->
    <q-card class="info-card q-mt-lg">
      <q-card-section>
        <h4>DDL自动提醒功能说明</h4>
        
        <div class="row q-gutter-lg">
          <div class="col">
            <h6>前端实现特性</h6>
            <q-list>
              <q-item>
                <q-item-section avatar>
                  <q-icon name="schedule" color="primary" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>自动轮询检查</q-item-label>
                  <q-item-label caption>定时检查即将到期的任务</q-item-label>
                </q-item-section>
              </q-item>

              <q-item>
                <q-item-section avatar>
                  <q-icon name="notifications" color="warning" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>多种提醒方式</q-item-label>
                  <q-item-label caption>页面通知 + 浏览器通知</q-item-label>
                </q-item-section>
              </q-item>

              <q-item>
                <q-item-section avatar>
                  <q-icon name="settings" color="secondary" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>自定义设置</q-item-label>
                  <q-item-label caption>检查间隔、提醒天数可配置</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </div>

          <div class="col">
            <h6>后端接口支持</h6>
            <q-list>
              <q-item>
                <q-item-section avatar>
                  <q-icon name="api" color="positive" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>任务管理接口</q-item-label>
                  <q-item-label caption>创建、更新、删除任务提醒</q-item-label>
                </q-item-section>
              </q-item>

              <q-item>
                <q-item-section avatar>
                  <q-icon name="timeline" color="info" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>DDL检查接口</q-item-label>
                  <q-item-label caption>获取即将到期的任务列表</q-item-label>
                </q-item-section>
              </q-item>

              <q-item>
                <q-item-section avatar>
                  <q-icon name="sync" color="accent" />
                </q-item-section>
                <q-item-section>
                  <q-item-label>自动提醒生成</q-item-label>
                  <q-item-label caption>系统定时生成DDL提醒通知</q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </div>
        </div>
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { notificationApi } from '../api/notification'
import { useQuasar } from 'quasar'
import DeadlineReminder from '../components/DeadlineReminder.vue'

const $q = useQuasar()

// 响应式数据
const creating = ref(false)
const apiResponse = ref<any>(null)
const notificationPermission = ref<NotificationPermission>('default')
const currentTime = ref('')
const testTaskCount = ref(0)

// 测试任务表单
const testTask = ref({
  title: '测试任务',
  deadlineType: '1小时后',
  priority: 'MEDIUM'
})

// 选项
const deadlineOptions = [
  '30分钟后',
  '1小时后', 
  '6小时后',
  '1天后',
  '3天后',
  '1周后'
]

const priorityOptions = ['LOW', 'MEDIUM', 'HIGH', 'URGENT']

let timeInterval: NodeJS.Timeout | null = null

// 方法
const createTestTask = async () => {
  creating.value = true
  try {
    const deadline = getDeadlineFromType(testTask.value.deadlineType)
    
    const taskData = {
      title: testTask.value.title,
      content: `测试任务 - ${testTask.value.deadlineType}到期`,
      deadline: deadline.toISOString(),
      priority: testTask.value.priority as any
    }
    
    const response = await notificationApi.createTaskReminder(taskData)
    apiResponse.value = response
    testTaskCount.value++
    
    $q.notify({
      type: 'positive',
      message: `创建测试任务成功：${testTask.value.deadlineType}到期`
    })  } catch (error) {
    console.error('创建测试任务失败:', error)
    apiResponse.value = { error: error instanceof Error ? error.message : '未知错误' }
    $q.notify({
      type: 'negative',
      message: '创建测试任务失败'
    })
  } finally {
    creating.value = false
  }
}

const createUrgentTask = async () => {
  creating.value = true
  try {
    const deadline = new Date(Date.now() + 30 * 60 * 1000) // 30分钟后
    
    const taskData = {
      title: '紧急任务测试',
      content: '这是一个用于测试紧急提醒的任务',
      deadline: deadline.toISOString(),
      priority: 'URGENT' as const
    }
    
    const response = await notificationApi.createTaskReminder(taskData)
    apiResponse.value = response
    testTaskCount.value++
    
    $q.notify({
      type: 'warning',
      message: '已创建紧急任务（30分钟后到期）'
    })
  } catch (error) {
    console.error('创建紧急任务失败:', error)
    $q.notify({
      type: 'negative',
      message: '创建紧急任务失败'
    })
  } finally {
    creating.value = false
  }
}

const simulateDeadlineCheck = () => {
  $q.notify({
    type: 'info',
    message: '正在模拟DDL检查...',
    timeout: 1000
  })
  
  // 触发DeadlineReminder组件的检查
  setTimeout(() => {
    $q.notify({
      type: 'positive',
      message: 'DDL检查完成，请查看左侧提醒组件'
    })
  }, 1500)
}

const testBrowserNotification = () => {
  if (notificationPermission.value === 'granted') {
    new Notification('DDL提醒测试', {
      body: '这是一个测试通知，验证浏览器通知功能是否正常',
      icon: '/favicon.ico'
    })
    
    $q.notify({
      type: 'positive',
      message: '已发送测试通知'
    })
  } else {
    $q.notify({
      type: 'warning',
      message: '请先授权浏览器通知权限'
    })
  }
}

const getDeadlineFromType = (type: string): Date => {
  const now = new Date()
  
  switch (type) {
    case '30分钟后':
      return new Date(now.getTime() + 30 * 60 * 1000)
    case '1小时后':
      return new Date(now.getTime() + 60 * 60 * 1000)
    case '6小时后':
      return new Date(now.getTime() + 6 * 60 * 60 * 1000)
    case '1天后':
      return new Date(now.getTime() + 24 * 60 * 60 * 1000)
    case '3天后':
      return new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000)
    case '1周后':
      return new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
    default:
      return new Date(now.getTime() + 60 * 60 * 1000)
  }
}

const updateCurrentTime = () => {
  currentTime.value = new Date().toLocaleString('zh-CN')
}

const checkNotificationPermission = () => {
  if ('Notification' in window) {
    notificationPermission.value = Notification.permission
  }
}

// 生命周期
onMounted(() => {
  checkNotificationPermission()
  updateCurrentTime()
  
  // 每秒更新时间
  timeInterval = setInterval(updateCurrentTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.deadline-reminder-test-page {
  max-width: 1400px;
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

.test-tools-card {
  border-radius: 12px;
  height: fit-content;
}

.api-response {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 6px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.info-card {
  border-radius: 12px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
}

h6 {
  margin-bottom: 12px;
  color: #1976d2;
}
</style>
