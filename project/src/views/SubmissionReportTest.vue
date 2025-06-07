<template>
  <div class="submission-report-test">
    <div class="page-header">
      <h2>提交报告导出功能测试</h2>
      <p>测试通过 submissionId 导出练习提交报告的功能</p>
    </div>

    <!-- 错误提示 -->
    <div v-if="downloadStatus.error" class="alert alert-error">
      {{ downloadStatus.error }}
      <button class="alert-close" @click="downloadStatus.error = null">&times;</button>
    </div>

    <!-- 成功提示 -->
    <div v-if="downloadStatus.success" class="alert alert-success">
      {{ downloadStatus.success }}
      <button class="alert-close" @click="downloadStatus.success = null">&times;</button>
    </div>

    <div class="test-content">
      <div class="row q-gutter-lg">
        <!-- 测试数据展示 -->
        <div class="col-md-6 col-12">
          <div class="card">
            <div class="card-header">
              <h3>模拟提交记录</h3>
            </div>
            <div class="card-body">
              <div class="submission-list">
                <div 
                  v-for="submission in mockSubmissions" 
                  :key="submission.id"
                  class="submission-item"
                >
                  <div class="submission-info">
                    <h4>{{ submission.exerciseTitle }}</h4>
                    <p><strong>提交ID:</strong> {{ submission.id }}</p>
                    <p><strong>学生:</strong> {{ submission.studentName }}</p>
                    <p><strong>得分:</strong> {{ submission.score }}/{{ submission.totalScore }}</p>
                    <p><strong>提交时间:</strong> {{ new Date(submission.submittedAt).toLocaleString() }}</p>
                    <p><strong>状态:</strong> 
                      <span :class="['status-badge', submission.status === 'completed' ? 'status-completed' : 'status-pending']">
                        {{ submission.status === 'completed' ? '已完成' : '待评分' }}
                      </span>
                    </p>
                  </div>
                  
                  <div class="submission-actions">
                    <button 
                      @click="exportSubmissionReport(submission.id)"
                      class="btn btn-export"
                      :disabled="downloadStatus.loading"
                    >
                      {{ downloadStatus.loading ? '导出中...' : '导出报告' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 功能说明 -->
        <div class="col-md-5 col-12">
          <div class="card">
            <div class="card-header">
              <h3>功能说明</h3>
            </div>
            <div class="card-body">
              <div class="feature-info">
                <h4>接口信息</h4>
                <div class="api-info">
                  <p><strong>API 路径:</strong></p>
                  <code>GET /api/record/submission/{submissionId}/export-report</code>
                </div>

                <h4>功能特点</h4>
                <ul class="feature-list">
                  <li>✅ 通过提交ID直接导出报告</li>
                  <li>✅ 自动生成PDF格式报告</li>
                  <li>✅ 支持浏览器直接下载</li>
                  <li>✅ 包含完整的提交详情</li>
                  <li>✅ 错误处理和用户反馈</li>
                </ul>

                <h4>使用场景</h4>
                <ul class="scenario-list">
                  <li>📊 学生查看自己的练习报告</li>
                  <li>👨‍🏫 教师导出学生提交详情</li>
                  <li>📈 生成学习分析报告</li>
                  <li>📋 存档和记录管理</li>
                </ul>

                <h4>报告内容</h4>
                <ul class="content-list">
                  <li>🎯 练习基本信息</li>
                  <li>📝 题目详情和答案</li>
                  <li>💯 评分结果和统计</li>
                  <li>📊 班级排名和对比</li>
                  <li>💡 答题分析和建议</li>
                </ul>

                <div class="test-status">
                  <h4>测试状态</h4>
                  <p><strong>下载次数:</strong> {{ downloadCount }}</p>
                  <p><strong>成功次数:</strong> {{ successCount }}</p>
                  <p><strong>失败次数:</strong> {{ failureCount }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 手动测试区域 -->
      <div class="card manual-test">
        <div class="card-header">
          <h3>手动测试</h3>
        </div>
        <div class="card-body">
          <div class="manual-test-form">
            <div class="form-group">
              <label for="submissionId">提交ID:</label>
              <input 
                id="submissionId"
                v-model="manualSubmissionId" 
                type="text" 
                placeholder="请输入提交ID" 
                class="form-input"
              />
            </div>
            <button 
              @click="exportSubmissionReport(manualSubmissionId)"
              class="btn btn-primary"
              :disabled="!manualSubmissionId || downloadStatus.loading"
            >
              {{ downloadStatus.loading ? '导出中...' : '测试导出' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import StudyRecordsApi from '../api/studyRecords'

// 响应式数据
const downloadStatus = ref<{
  loading: boolean;
  error: string | null;
  success: string | null;
}>({
  loading: false,
  error: null,
  success: null
});

const downloadCount = ref(0)
const successCount = ref(0)
const failureCount = ref(0)
const manualSubmissionId = ref('')

// 模拟提交记录数据
const mockSubmissions = ref([
  {
    id: '1001',
    exerciseTitle: '人工智能基础练习',
    studentName: '张三',
    score: 85,
    totalScore: 100,
    submittedAt: '2024-12-01T14:30:00Z',
    status: 'completed'
  },
  {
    id: '1002',
    exerciseTitle: '数据结构与算法',
    studentName: '李四',
    score: 92,
    totalScore: 100,
    submittedAt: '2024-12-01T15:45:00Z',
    status: 'completed'
  },
  {
    id: '1003',
    exerciseTitle: '计算机网络原理',
    studentName: '王五',
    score: 78,
    totalScore: 100,
    submittedAt: '2024-12-01T16:20:00Z',
    status: 'pending'
  }
])

// 文件下载处理
const handleFileDownload = (blob: Blob, filename: string) => {
  try {
    // 检查是否为错误响应（通常错误响应是JSON格式）
    if (blob.type === 'application/json') {
      const reader = new FileReader();
      reader.onload = () => {
        try {
          const errorData = JSON.parse(reader.result as string);
          downloadStatus.value.error = errorData.message || '下载失败，请稍后再试';
          failureCount.value++;
        } catch {
          downloadStatus.value.error = '下载失败，请稍后再试';
          failureCount.value++;
        }
      };
      reader.readAsText(blob);
      return;
    }

    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    
    // 安全地移除DOM元素和释放URL对象
    if (link.parentNode) {
      document.body.removeChild(link);
    }
    window.URL.revokeObjectURL(url);
    
    // 显示成功消息
    downloadStatus.value.success = `报告导出成功: ${filename}`;
    downloadStatus.value.error = null;
    successCount.value++;
    
  } catch (error) {
    console.error('文件下载失败:', error);
    downloadStatus.value.error = '文件下载失败，请稍后再试';
    failureCount.value++;
  }
};

// 导出提交报告
const exportSubmissionReport = async (submissionId: string) => {
  if (!submissionId) {
    downloadStatus.value.error = '请提供有效的提交ID';
    return;
  }

  try {
    downloadStatus.value.loading = true;
    downloadStatus.value.error = null;
    downloadStatus.value.success = null;
    downloadCount.value++;

    console.log(`正在导出提交ID为 ${submissionId} 的报告...`);
    
    const response = await StudyRecordsApi.exportSubmissionReport(submissionId);
    const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
    const filename = `提交报告_${submissionId}_${timestamp}.pdf`;
    
    handleFileDownload(response.data, filename);
    
  } catch (err: any) {
    console.error('导出提交报告失败:', err);
    failureCount.value++;
    
    // 处理不同类型的错误
    if (err.response?.status === 401) {
      downloadStatus.value.error = '未登录或登录已过期，请重新登录';
    } else if (err.response?.status === 403) {
      downloadStatus.value.error = '权限不足，无法导出报告';
    } else if (err.response?.status === 404) {
      downloadStatus.value.error = `提交记录 ${submissionId} 不存在或报告未生成`;
    } else if (err.response?.data && typeof err.response.data === 'string') {
      downloadStatus.value.error = err.response.data;
    } else {
      downloadStatus.value.error = err.message || '导出报告失败，请稍后再试';
    }
  } finally {
    downloadStatus.value.loading = false;
  }
};

// 清除提示消息
const clearMessages = () => {
  downloadStatus.value.error = null;
  downloadStatus.value.success = null;
};

onMounted(() => {
  console.log('提交报告导出测试页面已加载');
  console.log('可用的模拟提交ID:', mockSubmissions.value.map(s => s.id));
});
</script>

<style scoped>
.submission-report-test {
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

.row {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.col-md-6 {
  flex: 1;
}

.col-md-5 {
  flex: 0 0 42%;
}

.card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%);
  color: white;
  padding: 20px;
}

.card-header h3 {
  margin: 0;
  font-size: 1.2rem;
}

.card-body {
  padding: 20px;
}

.submission-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.submission-item {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  background: #f9f9f9;
}

.submission-info h4 {
  color: #1976d2;
  margin: 0 0 10px 0;
  font-size: 1.1rem;
}

.submission-info p {
  margin: 5px 0;
  font-size: 0.9rem;
}

.submission-actions {
  margin-top: 15px;
  text-align: right;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-completed {
  background: #e8f5e8;
  color: #2e7d32;
}

.status-pending {
  background: #fff3e0;
  color: #f57c00;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s ease;
  font-size: 0.9rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-export {
  background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
  color: white;
}

.btn-export:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.btn-primary {
  background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(25, 118, 210, 0.3);
}

.feature-list,
.scenario-list,
.content-list {
  padding-left: 20px;
  margin: 10px 0;
}

.feature-list li,
.scenario-list li,
.content-list li {
  margin: 8px 0;
  line-height: 1.5;
}

.api-info {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 6px;
  margin: 10px 0;
}

.api-info code {
  background: #e0e0e0;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
}

.test-status {
  background: #f0f8ff;
  padding: 15px;
  border-radius: 8px;
  margin-top: 20px;
}

.manual-test {
  margin-top: 20px;
}

.manual-test-form {
  display: flex;
  gap: 15px;
  align-items: end;
}

.form-group {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

.form-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 0.9rem;
}

.form-input:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
}

.alert {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  position: relative;
  font-weight: 500;
}

.alert-error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ef9a9a;
}

.alert-success {
  background: #e8f5e8;
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

.alert-close {
  position: absolute;
  top: 10px;
  right: 15px;
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  opacity: 0.7;
}

.alert-close:hover {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .row {
    flex-direction: column;
  }
  
  .col-md-5 {
    flex: 1;
  }
  
  .manual-test-form {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
