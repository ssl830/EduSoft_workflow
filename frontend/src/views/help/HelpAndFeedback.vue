<template>
  <div class="help-feedback-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <q-icon name="help_outline" class="q-mr-sm" />
        帮助与反馈
      </h1>
      <p class="page-description">
        欢迎访问帮助中心，在这里您可以找到常见问题的解答，提交反馈意见，或联系我们的技术支持团队。
      </p>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-grid">
      <!-- 左侧：帮助中心 -->
      <div class="help-section">
        <q-card class="section-card">
          <q-card-section>
            <div class="section-header">
              <q-icon name="quiz" color="primary" size="md" />
              <h2>常见问题</h2>
            </div>

            <q-expansion-item
              v-for="(faq, index) in faqs"
              :key="index"
              :label="faq.question"
              icon="help_outline"
              header-class="text-weight-medium"
              class="q-mb-sm"
            >
              <q-card flat>
                <q-card-section class="q-pt-none">
                  <div v-html="faq.answer" class="faq-answer"></div>
                </q-card-section>
              </q-card>
            </q-expansion-item>
          </q-card-section>
        </q-card>

        <q-card class="section-card q-mt-md">
          <q-card-section>
            <div class="section-header">
              <q-icon name="library_books" color="secondary" size="md" />
              <h2>使用指南</h2>
            </div>

            <div class="guide-links">
              <q-btn
                v-for="guide in guides"
                :key="guide.id"
                flat
                align="left"
                :icon="guide.icon"
                :label="guide.title"
                class="guide-link"
                @click="openGuide(guide)"
              />
            </div>
          </q-card-section>
        </q-card>
      </div>

      <!-- 右侧：反馈区域 -->
      <div class="feedback-section">
        <q-card class="section-card">
          <q-card-section>
            <div class="section-header">
              <q-icon name="feedback" color="accent" size="md" />
              <h2>意见反馈</h2>
            </div>

            <q-form @submit="submitFeedback" class="feedback-form">
              <q-select
                v-model="feedbackForm.type"
                :options="feedbackTypes"
                label="反馈类型"
                emit-value
                map-options
                outlined
                class="q-mb-md"
                :rules="[val => !!val || '请选择反馈类型']"
              />

              <q-input
                v-model="feedbackForm.title"
                label="标题"
                outlined
                maxlength="100"
                counter
                class="q-mb-md"
                :rules="[val => !!val || '请输入标题']"
              />

              <q-input
                v-model="feedbackForm.description"
                label="详细描述"
                type="textarea"
                outlined
                rows="6"
                maxlength="1000"
                counter
                class="q-mb-md"
                :rules="[val => !!val || '请输入详细描述']"
                hint="请详细描述您遇到的问题或建议"
              />

              <q-input
                v-model="feedbackForm.contact"
                label="联系方式（可选）"
                outlined
                class="q-mb-md"
                hint="留下邮箱或QQ，以便我们联系您"
              />

              <div class="form-actions">
                <q-btn
                  type="submit"
                  color="primary"
                  icon="send"
                  label="提交反馈"
                  :loading="submitting"
                  class="q-mr-sm"
                />
                <q-btn
                  flat
                  icon="refresh"
                  label="重置"
                  @click="resetForm"
                />
              </div>
            </q-form>
          </q-card-section>
        </q-card>

        <!-- 联系信息 -->
        <q-card class="section-card q-mt-md">
          <q-card-section>
            <div class="section-header">
              <q-icon name="contact_support" color="info" size="md" />
              <h2>联系我们</h2>
            </div>

            <div class="contact-info">
              <div class="contact-item">
                <q-icon name="email" color="primary" />
                <div>
                  <div class="contact-label">技术支持邮箱</div>
                  <div class="contact-value">support@edusoft.com</div>
                </div>
              </div>

              <div class="contact-item">
                <q-icon name="schedule" color="primary" />
                <div>
                  <div class="contact-label">服务时间</div>
                  <div class="contact-value">周一至周五 9:00-18:00</div>
                </div>
              </div>

              <div class="contact-item">
                <q-icon name="chat" color="primary" />
                <div>
                  <div class="contact-label">在线客服</div>
                  <div class="contact-value">工作时间内实时响应</div>
                </div>
              </div>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useQuasar } from 'quasar'

const $q = useQuasar()

// 常见问题数据
const faqs = ref([
  {
    question: '如何重置密码？',
    answer: '您可以在登录页面点击"忘记密码"链接，输入注册邮箱，系统会发送重置密码的邮件到您的邮箱。请查收邮件并按照指引重置密码。'
  },
  {
    question: '如何提交作业？',
    answer: '在"作业"页面中，找到对应的作业项目，点击"提交作业"按钮。您可以上传文件或在线编辑提交。请注意作业截止时间，逾期提交可能会影响成绩。'
  },
  {
    question: '为什么无法访问某些课程内容？',
    answer: '可能的原因包括：<br/>1. 您尚未加入该课程<br/>2. 课程内容尚未开放<br/>3. 网络连接问题<br/>请联系课程教师或技术支持获取帮助。'
  },
  {
    question: '如何查看学习进度？',
    answer: '在个人中心的"学习记录"页面，您可以查看详细的学习进度，包括课程完成情况、作业提交记录、练习成绩等信息。'
  },
  {
    question: '系统支持哪些浏览器？',
    answer: '我们推荐使用以下浏览器的最新版本：<br/>• Chrome 80+<br/>• Firefox 75+<br/>• Safari 13+<br/>• Edge 80+'
  }
])

// 使用指南数据
const guides = ref([
  { id: 1, title: '新用户入门指南', icon: 'person_add', url: '/guide/getting-started' },
  { id: 2, title: '课程学习指南', icon: 'school', url: '/guide/course-learning' },
  { id: 3, title: '作业提交指南', icon: 'assignment', url: '/guide/homework' },
  { id: 4, title: '在线练习指南', icon: 'quiz', url: '/guide/practice' },
  { id: 5, title: '讨论区使用指南', icon: 'forum', url: '/guide/discussion' }
])

// 反馈类型选项
const feedbackTypes = ref([
  { label: '功能建议', value: 'suggestion' },
  { label: '问题反馈', value: 'bug' },
  { label: '使用咨询', value: 'question' },
  { label: '其他', value: 'other' }
])

// 反馈表单
const feedbackForm = reactive({
  type: '',
  title: '',
  description: '',
  contact: ''
})

const submitting = ref(false)

// 打开使用指南
const openGuide = (guide: any) => {
  $q.notify({
    type: 'info',
    message: `正在打开${guide.title}...`,
    position: 'top'
  })
  // 这里可以添加实际的跳转逻辑
  // router.push(guide.url)
}

// 提交反馈
const submitFeedback = async () => {
  submitting.value = true

  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))

    $q.notify({
      type: 'positive',
      message: '反馈提交成功！我们会尽快处理您的意见。',
      position: 'top'
    })

    resetForm()
  } catch (error) {
    $q.notify({
      type: 'negative',
      message: '提交失败，请稍后重试',
      position: 'top'
    })
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(feedbackForm, {
    type: '',
    title: '',
    description: '',
    contact: ''
  })
}
</script>

<style scoped lang="scss">
.help-feedback-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;

  .page-title {
    font-size: 2rem;
    font-weight: 600;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .page-description {
    font-size: 1rem;
    max-width: 600px;
    margin: 0 auto;
    line-height: 1.6;
  }
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;

  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
  }
}

.section-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    font-size: 1.25rem;
    font-weight: 600;
    margin: 0 0 0 12px;
  }
}

.faq-answer {
  line-height: 1.6;
}

.guide-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.guide-link {
  justify-content: flex-start;
  padding: 12px 16px;
  border-radius: 8px;
  transition: all 0.2s ease;

}

.feedback-form {
  .form-actions {
    display: flex;
    align-items: center;
    margin-top: 16px;
  }
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;

  .contact-label {
    font-weight: 500;
    margin-bottom: 4px;
  }

  .contact-value {
    font-size: 0.9rem;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .help-feedback-container {
    padding: 16px;
  }

  .page-header {
    .page-title {
      font-size: 1.5rem;
    }
  }

  .content-grid {
    gap: 16px;
  }
}
</style>
