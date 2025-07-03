<template>
    <div class="check-container">
        <div v-if="loading">加载中...</div>
        <div v-else-if="error" class="error-message">{{ error }}</div>
        <div>
            <div v-for="(question, qIdx) in questions" :key="question.questionName" class="question-item">
                <h3>{{ question.questionName }}</h3>
                <p class="answer-text">学生答案：{{ question.answerText }}</p>
                <p>最大分值：{{ question.maxScore }}</p>
                <div class="score-input">
                    <label>
                        评分：
                        <input type="number" v-model.number="question.score" :min="0" :max="question.maxScore"
                            class="score-field" />
                    </label>
                </div>
                <div v-if="question.aiResult === undefined || question.aiResult === null" class="ai-card ai-score ai-loading">
                    <span class="ai-label">AI分析中...</span>
                </div>
                <div v-else-if="question.aiResult && question.aiResult.error" class="ai-card ai-score">
                    <span class="ai-label">AI分析出错</span>
                    <span style="color:#e74c3c">{{ question.aiResult.error }}</span>
                </div>
                <div v-else>
                    <div class="ai-card ai-score">
                        <span class="ai-label">AI评分建议</span>
                        <span class="ai-score-value">{{ question.aiResult.score }} 分</span>
                        <button class="ai-detail-btn" @click="showAiDetail[qIdx] = !showAiDetail[qIdx]">
                            {{ showAiDetail[qIdx] ? '收起详情' : '查看详情' }}
                        </button>
                    </div>
                    <template v-if="showAiDetail[qIdx]">
                        <div class="ai-card ai-feedback">
                            <span class="ai-label">AI分析</span>
                            <div class="ai-feedback-text">{{ question.aiResult.feedback }}</div>
                        </div>
                        <div v-if="question.aiResult.analysis" class="ai-card ai-analysis">
                            <div class="ai-analysis-group">
                                <span class="ai-analysis-title correct"><i class="iconfont">✔</i> 正确点</span>
                                <ul>
                                    <li v-for="item in question.aiResult.analysis.correct_points" :key="item">{{ item }}</li>
                                </ul>
                            </div>
                            <div class="ai-analysis-group">
                                <span class="ai-analysis-title incorrect"><i class="iconfont">✘</i> 错误点</span>
                                <ul>
                                    <li v-for="item in question.aiResult.analysis.incorrect_points" :key="item">{{ item }}</li>
                                </ul>
                            </div>
                            <div class="ai-analysis-group">
                                <span class="ai-analysis-title knowledge"><i class="iconfont">📚</i> 涉及知识点</span>
                                <ul>
                                    <li v-for="item in question.aiResult.analysis.knowledge_points" :key="item">{{ item }}</li>
                                </ul>
                            </div>
                        </div>
                        <div class="ai-card ai-suggestion">
                            <span class="ai-label">改进建议</span>
                            <ul>
                                <li v-for="item in (question.aiResult.improvement_suggestions || [])" :key="item">{{ item }}</li>
                            </ul>
                        </div>
                        <div v-if="question.aiResult.knowledge_context && question.aiResult.knowledge_context.length" class="ai-card ai-knowledge-context">
                            <span class="ai-label">知识库相关内容</span>
                            <ul>
                                <li v-for="ctx in question.aiResult.knowledge_context" :key="ctx.source">
                                    <div class="ai-ctx-content">{{ ctx.content }}</div>
                                    <div class="ai-ctx-source">来源：{{ ctx.source }}</div>
                                </li>
                            </ul>
                        </div>
                    </template>
                </div>
            </div>
        <div class="total-score">总成绩：{{ totalScore }}</div>
        <button @click="submitScores" class="submit-button">提交评分</button>
    </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import ExerciseApi from "../../api/exercise.ts";
import Exercise from "../../api/exercise.ts";
import { useRoute, useRouter } from "vue-router";
import { evaluateSubjectiveAnswer } from '@/api/ai'

interface QuestionItem {
    questionName: string;
    answerText: string;
    maxScore: number;
    score: number;
    sortOrder: number;
}
const route = useRoute()
const router = useRouter()
const practiceId = route.params.practiceId
const submissionId = route.params.submissionId

const error = ref('')

const questions = ref<QuestionItem[]>([]);
const loading = ref(true);
const isLoading = ref(false);
const showAiDetail = ref<Array<boolean>>([]);

const fetchPendingAnswers = async () => {
    try {
        const response = await ExerciseApi.fetchPendingAnswers({
            submissionId: submissionId,
        });
        if (response.code === 200) {
            questions.value = response.data;
            showAiDetail.value = questions.value.map(() => false);
            // 针对每道题调用AI评估
            await Promise.all(
                questions.value.map(async (q, idx) => {
                    try {
                        const res = await evaluateSubjectiveAnswer({
                            question: q.questionName,
                            student_answer: q.answerText,
                            reference_answer: '',
                            max_score: q.maxScore || 5
                        });
                        q.aiResult = res;
                    } catch (e) {
                        q.aiResult = { error: 'AI评估失败，请稍后重试' };
                    }
                })
            );
        } else {
            console.error('获取待评分答案失败:', response.message);
        }
    } catch (error) {
        console.error('请求失败:', error);
    } finally {
        loading.value = false;
    }
};

onMounted(fetchPendingAnswers);

const totalScore = computed(() => {
    return questions.value.reduce((sum, q) => sum + q.score, 0);
});

const submitScores = async () => {
    const isValid = questions.value.every(q =>
        q.score >= 0 && q.score <= q.maxScore
    );
    if (!isValid) {
        error.value = '分数必须在0到最大分值之间';
        return;
    }

    const requestData = {
        submissionId: Number(submissionId),
        questions: questions.value.map(q => ({
            answerText: q.answerText,
            score: q.score,
            maxScore: q.maxScore,
            sortOrder: q.sortOrder,
        })),
    };

    try {
        const response = await Exercise.gradeAnswer(requestData);
        if (response.code === 200) {
            router.push('/')
        } else {
            error.value = '评分提交失败：' + (response.data?.message || '未知错误');
        }
    } catch (err) {
        error.value = '请求失败，请稍后重试';
        console.error(err);
    }
};
</script>

<style scoped>
.error-message {
    color: #e53935;
    padding: 1rem;
    text-align: center;
    background-color: #ffebee;
    border-radius: 8px;
    margin: 1rem 0;
}

.check-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
}

.question-item {
    margin-bottom: 20px;
    padding: 20px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    background-color: #f9f9f9;
}

.answer-text {
    color: #666;
    margin: 10px 0;
}

.score-input {
    margin-top: 10px;
}

.score-field {
    padding: 6px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    width: 100px;
}

.total-score {
    margin-top: 30px;
    font-size: 18px;
    font-weight: bold;
    color: #2c3e50;
}

.submit-button {
    margin-top: 20px;
    padding: 10px 20px;
    background-color: #3498db;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s;
}

.submit-button:hover {
    background-color: #2980b9;
}
/* AI分析美化样式 */
.ai-card {
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(52, 152, 219, 0.08);
    margin: 24px 0 0 0;
    padding: 24px 28px 18px 28px;
    transition: box-shadow 0.2s;
    border-left: 5px solid #3498db;
}
.ai-label {
    font-weight: bold;
    font-size: 18px;
    color: #3498db;
    margin-bottom: 10px;
    display: inline-block;
}
.ai-score-value {
    font-size: 22px;
    color: #e67e22;
    font-weight: bold;
    margin-left: 16px;
}
.ai-loading {
    text-align: center;
    color: #3498db;
    font-size: 18px;
    font-weight: 500;
    background: #f3f8fd;
    border-left: 5px solid #d0e6fa;
}
.ai-detail-btn {
    margin-left: 24px;
    padding: 4px 16px;
    font-size: 15px;
    background: #f3f8fd;
    color: #3498db;
    border: 1px solid #d0e6fa;
    border-radius: 4px;
    cursor: pointer;
    transition: background 0.2s, color 0.2s;
}
.ai-detail-btn:hover {
    background: #3498db;
    color: #fff;
}
.ai-feedback-text {
    margin-top: 8px;
    color: #34495e;
    font-size: 16px;
    line-height: 1.7;
}
.ai-analysis {
    display: flex;
    gap: 32px;
    flex-wrap: wrap;
    margin-top: 10px;
}
.ai-analysis-group {
    flex: 1 1 200px;
    min-width: 180px;
}
.ai-analysis-title {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 6px;
    display: flex;
    align-items: center;
    gap: 4px;
}
.ai-analysis-title.correct {
    color: #27ae60;
}
.ai-analysis-title.incorrect {
    color: #e74c3c;
}
.ai-analysis-title.knowledge {
    color: #2980b9;
}
.ai-analysis-group ul {
    margin: 0 0 8px 0;
    padding-left: 20px;
    color: #555;
    font-size: 15px;
}
.ai-suggestion ul {
    margin: 8px 0 0 0;
    padding-left: 22px;
    color: #8e44ad;
    font-size: 15px;
}
.ai-knowledge-context ul {
    margin: 8px 0 0 0;
    padding-left: 0;
}
.ai-ctx-content {
    color: #34495e;
    font-size: 15px;
    margin-bottom: 2px;
}
.ai-ctx-source {
    color: #888;
    font-size: 12px;
    margin-bottom: 8px;
}
.ai-card:not(:last-child) {
    margin-bottom: 8px;
}
.iconfont {
    font-style: normal;
    font-size: 16px;
    margin-right: 4px;
}
</style>
