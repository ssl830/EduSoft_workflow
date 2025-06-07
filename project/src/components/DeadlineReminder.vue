<template>
  <div class="deadline-reminder">
    <q-card class="reminder-card">
      <q-card-section>
        <div class="text-h6 text-center">
          <q-icon name="alarm" color="warning" size="md" />
          DDL提醒助手
        </div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <!-- 紧急任务提醒 -->
        <div v-if="urgentTasks.length > 0" class="urgent-section q-mb-md">
          <q-banner inline-actions class="text-white bg-red">
            <q-icon name="warning" />
            {{ urgentTasks.length }} 个任务即将到期！
            
            <template #action>
              <q-btn 
                flat 
                color="white" 
                label="查看详情" 
                @click="showUrgentDetails = !showUrgentDetails"
              />
            </template>
          </q-banner>

          <q-expansion-item 
            v-model="showUrgentDetails"
            class="q-mt-sm"
          >
            <q-list separator>
              <q-item v-for="task in urgentTasks" :key="task.id">
                <q-item-section avatar>
                  <q-icon name="schedule" color="red" />
                </q-item-section>
                <q-item-section>
                  <q-item-label class="text-weight-bold">{{ task.title }}</q-item-label>
                  <q-item-label caption>{{ task.content }}</q-item-label>
                  <q-item-label caption class="text-red">
                    {{ getTimeRemaining(task.deadline) }}
                  </q-item-label>
                </q-item-section>
              </q-item>
            </q-list>
          </q-expansion-item>
        </div>

        <!-- 即将到期任务 -->
        <div v-if="upcomingTasks.length > 0" class="upcoming-section">
          <h6 class="text-orange">即将到期任务 ({{ upcomingTasks.length }})</h6>
          
          <q-list separator>
            <q-item v-for="task in upcomingTasks" :key="task.id">
              <q-item-section avatar>
                <q-icon name="schedule" color="orange" />
              </q-item-section>
              <q-item-section>
                <q-item-label>{{ task.title }}</q-item-label>
                <q-item-label caption>{{ task.content }}</q-item-label>
                <q-item-label caption class="text-orange">
                  {{ getTimeRemaining(task.deadline) }}
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

        <!-- 无任务提醒 -->
        <div v-if="urgentTasks.length === 0 && upcomingTasks.length === 0" class="no-tasks text-center q-py-md">
          <q-icon name="check_circle" color="green" size="3em" />
          <p class="text-green q-mt-sm">太棒了！暂无紧急任务</p>
        </div>
      </q-card-section>

      <q-separator />

      <q-card-section>
        <div class="settings-section">
          <div class="row items-center justify-between">
            <span>自动检查DDL</span>
            <q-toggle 
              v-model="autoCheckEnabled" 
              @update:model-value="toggleAutoCheck"
              color="primary"
            />
          </div>
          
          <div class="row items-center justify-between q-mt-sm">
            <span>检查间隔</span>
            <q-select
              v-model="checkInterval"
              :options="intervalOptions"
              style="min-width: 120px"
              dense
              @update:model-value="updateCheckInterval"
            />
          </div>

          <div class="row items-center justify-between q-mt-sm">
            <span>提前提醒天数</span>
            <q-input
              v-model.number="reminderDays"
              type="number"
              min="1"
              max="30"
              style="max-width: 80px"
              dense
              @update:model-value="updateReminderDays"
            />
          </div>
        </div>
      </q-card-section>

      <q-card-actions align="center">
        <q-btn 
          color="primary" 
          @click="checkDeadlinesNow"
          :loading="checking"
        >
          <q-icon name="refresh" />
          立即检查
        </q-btn>
        
        <q-btn 
          flat
          color="grey"
          @click="showStats = !showStats"
        >
          统计信息
        </q-btn>
      </q-card-actions>

      <!-- 统计信息 -->
      <q-expansion-item v-model="showStats" label="统计信息">
        <q-card-section class="bg-grey-1">
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-value">{{ totalTasks }}</div>
              <div class="stat-label">总任务数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ completedTasks }}</div>
              <div class="stat-label">已完成</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ checkCount }}</div>
              <div class="stat-label">检查次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ lastCheckTime }}</div>
              <div class="stat-label">最后检查</div>
            </div>
          </div>
        </q-card-section>
      </q-expansion-item>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { notificationApi, type TaskReminder } from '../api/notification'
import { useQuasar } from 'quasar'

// 组件选项
defineOptions({
  name: 'DeadlineReminder'
})

const $q = useQuasar()

// 响应式数据
const tasks = ref<TaskReminder[]>([])
const autoCheckEnabled = ref(true)
const checkInterval = ref(5) // 分钟
const reminderDays = ref(3)
const checking = ref(false)
const showUrgentDetails = ref(false)
const showStats = ref(false)
const checkCount = ref(0)
const lastCheckTime = ref('')
const totalTasks = ref(0)
const completedTasks = ref(0)

let checkTimer: NodeJS.Timeout | null = null

// 选项
const intervalOptions = [
  { label: '1分钟', value: 1 },
  { label: '5分钟', value: 5 },
  { label: '10分钟', value: 10 },
  { label: '30分钟', value: 30 },
  { label: '1小时', value: 60 }
]

// 计算属性
const urgentTasks = computed(() => {
  const now = new Date().getTime()
  return tasks.value.filter(task => {
    if (task.completed) return false
    const deadline = new Date(task.deadline).getTime()
    const hoursRemaining = (deadline - now) / (1000 * 60 * 60)
    return hoursRemaining <= 24 && hoursRemaining > 0 // 24小时内到期
  })
})

const upcomingTasks = computed(() => {
  const now = new Date().getTime()
  return tasks.value.filter(task => {
    if (task.completed) return false
    const deadline = new Date(task.deadline).getTime()
    const daysRemaining = (deadline - now) / (1000 * 60 * 60 * 24)
    return daysRemaining > 1 && daysRemaining <= reminderDays.value // 1天到设定天数内到期
  })
})

// 方法
const checkDeadlinesNow = async () => {
  checking.value = true
  checkCount.value++
  lastCheckTime.value = new Date().toLocaleTimeString('zh-CN')
  
  try {
    // 这里应该从用户认证状态获取userId
    const userId = 1 // 临时硬编码
    const response = await notificationApi.getTaskReminders(userId)
    
    if (response.code === 200) {
      tasks.value = response.data
      totalTasks.value = response.data.length
      completedTasks.value = response.data.filter((task: TaskReminder) => task.completed).length
      
      // 检查是否有新的紧急任务需要通知
      if (urgentTasks.value.length > 0) {
        showUrgentNotification()
      }
    }
  } catch (error) {
    console.error('检查DDL失败:', error)
    $q.notify({
      type: 'negative',
      message: '检查DDL失败，请稍后重试'
    })
  } finally {
    checking.value = false
  }
}

const showUrgentNotification = () => {
  if (urgentTasks.value.length === 0) return
  
  // 浏览器通知
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification('DDL提醒', {
      body: `您有 ${urgentTasks.value.length} 个任务即将到期！`,
      icon: '/favicon.ico'
    })
  }
  
  // Quasar通知
  $q.notify({
    type: 'warning',
    message: `⚠️ 紧急提醒：${urgentTasks.value.length} 个任务即将到期`,
    timeout: 5000,
    actions: [
      {
        label: '查看',
        color: 'white',
        handler: () => {
          showUrgentDetails.value = true
        }
      }
    ]
  })
}

const toggleAutoCheck = (enabled: boolean) => {
  if (enabled) {
    startAutoCheck()
  } else {
    stopAutoCheck()
  }
}

const updateCheckInterval = () => {
  if (autoCheckEnabled.value) {
    stopAutoCheck()
    startAutoCheck()
  }
}

const updateReminderDays = () => {
  // 重新检查任务，应用新的提醒天数设置
  checkDeadlinesNow()
}

const startAutoCheck = () => {
  stopAutoCheck() // 先清除现有定时器
  
  if (autoCheckEnabled.value) {
    checkTimer = setInterval(() => {
      checkDeadlinesNow()
    }, checkInterval.value * 60 * 1000) // 转换为毫秒
  }
}

const stopAutoCheck = () => {
  if (checkTimer) {
    clearInterval(checkTimer)
    checkTimer = null
  }
}

// 工具方法
const getTimeRemaining = (deadline: string) => {
  const now = new Date().getTime()
  const deadlineTime = new Date(deadline).getTime()
  const diff = deadlineTime - now
  
  if (diff <= 0) {
    return '已过期'
  }
  
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  if (days > 0) {
    return `${days}天${hours}小时后到期`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟后到期`
  } else {
    return `${minutes}分钟后到期`
  }
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

// 请求浏览器通知权限
const requestNotificationPermission = async () => {
  if ('Notification' in window && Notification.permission === 'default') {
    const permission = await Notification.requestPermission()
    if (permission === 'granted') {
      $q.notify({
        type: 'positive',
        message: '通知权限已授权，您将收到DDL提醒'
      })
    }
  }
}

// 生命周期
onMounted(() => {
  checkDeadlinesNow()
  startAutoCheck()
  requestNotificationPermission()
})

onUnmounted(() => {
  stopAutoCheck()
})
</script>

<style scoped>
.deadline-reminder {
  max-width: 500px;
  margin: 0 auto;
}

.reminder-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.urgent-section {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.8; }
  100% { opacity: 1; }
}

.settings-section {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  text-align: center;
}

.stat-item {
  padding: 12px;
  background: white;
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1976d2;
}

.stat-label {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.no-tasks {
  opacity: 0.8;
}
</style>
