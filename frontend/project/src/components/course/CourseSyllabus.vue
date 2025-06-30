<script setup lang="ts">
import {computed, defineProps, ref, watch} from 'vue'
import { useAuthStore } from '../../stores/auth'
import CourseApi from "../../api/course.ts";
import {useRoute} from "vue-router";
import CourseEditDialog from './CourseEditDialog.vue'
import { ElMessage } from 'element-plus'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import jsPDF from 'jspdf'

const props = defineProps<{
    course: any;
}>()

const authStore = useAuthStore()
const isTeacher = authStore.userRole === 'teacher'
const error = ref('')
const uploadError = ref('')
const showSectionForm = ref(false)
const editDialogVisible = ref(false)
const uploadForm = ref({
    sections: [
        { sectionId: -1, title: '' }
    ]
})

// Remove an option
const removeSection = (index: number) => {
    if (uploadForm.value.sections.length > 1) {
        uploadForm.value.sections.splice(index, 1)
    }
}

// Add an option for choice questions
const addSection = () => {
    uploadForm.value.sections.push({ sectionId: -1, title: '' })
}

const uploadSections = async () => {
    const hasEmptyField = uploadForm.value.sections.some(section =>
        section.title.trim() === '' ||
        String(section.sectionId).trim() === '' ||
        isNaN(Number(section.sectionId)) ||  // 新增数字校验
        Number(section.sectionId) < 0       // 确保转换为数字后比较
    )

    if (hasEmptyField) {
        uploadError.value = '请补全所有章节的序号和名称，并保证章节序号>=0'
        return
    }

    try {
        const response = await CourseApi.uploadSections(props.course.id, {sections: uploadForm.value.sections})
        console.log(response)
        console.log("Hereeeeeeeeee")
        showSectionForm.value = !showSectionForm.value
        // 删除section
    } catch (err) {
        error.value = '上传章节失败，请稍后再试'
        console.error(err)
    }
    fetchCourse()
}
const route = useRoute()
const courseId = computed(() => route.params.id as string)
const fetchCourse = async() => {
    try {
        const response = await CourseApi.getCourseById(courseId.value)
        if (response.code === 200) {
            Object.assign(props.course, response.data)
        } else {
            console.log("hereeeeeeee")
            error.value = response.message || '获取课程详情失败'
        }
    } catch (err) {
        error.value = '获取课程详情失败，请稍后再试'
        console.error(err)
    }
}

// Download resource
const deleteSection = async (sectionId: bigint) => {
    try {
        const response = await CourseApi.deleteSection(props.course.id, sectionId)
        props.course.sections = props.course.sections.filter(s => s.id !== sectionId);
        console.log(response)
        // 删除section
    } catch (err) {
        error.value = '下载资源失败，请稍后再试'
        console.error(err)
    }
}

const handleEditSuccess = async () => {
    await fetchCourse()
}

const showTeachingPlanDialog = ref(false)
const teachingPlanLoading = ref(false)
const teachingPlanResult = ref<any>(null)
const expectedHours = ref<number | null>(null)

const openTeachingPlanDialog = () => {
    expectedHours.value = null
    showTeachingPlanDialog.value = true
    teachingPlanResult.value = null
}

const handleGenerateTeachingPlan = async () => {
    // 防止重复请求
    if (teachingPlanLoading.value) return;
    if (!expectedHours.value || expectedHours.value <= 0) {
        ElMessage.error('请输入有效的预期总课时数')
        return
    }
    teachingPlanLoading.value = true
    try {
        const res = await CourseApi.generateTeachingContent({
            course_name: props.course.name,
            course_outline: props.course.outline || '',
            expected_hours: expectedHours.value
        })
        if (res.data) {
            teachingPlanResult.value = res.data
        } else {
            teachingPlanResult.value = res
        }
    } catch (err: any) {
        ElMessage.error('生成教案失败，请稍后再试')
        teachingPlanResult.value = null
    } finally {
        teachingPlanLoading.value = false
    }
}

// 全局字体加载状态
// let fontLoaded = false;
// let fontLoadingPromise: Promise<void> | null = null;

// 新的导出PDF方法：直接将内容转为纯文本
const exportTeachingPlanAsPDF = async () => {
    if (!renderTeachingPlan.value) {
        ElMessage.error('无教案内容，无法导出')
        return
    }

    const doc = new jsPDF('p', 'mm', 'a4')
    const marginLeft = 15
    const marginTop = 20
    const lineHeight = 8
    const pageWidth = doc.internal.pageSize.getWidth()
    const pageHeight = doc.internal.pageSize.getHeight()
    let cursorY = marginTop

    try {
        // 1. 使用更可靠的方式加载字体
        const fontName = 'SourceHanSerifSC'
        const fontUrl = '/src/assets/fonts/SourceHanSerifSC-VF.ttf' // 确保字体文件在public目录中

        // 检查是否已加载字体
        if (!doc.getFontList()[fontName]) {
            const fontData = await fetch(fontUrl).then(res => res.arrayBuffer())
            doc.addFileToVFS(`${fontName}.ttf`, arrayBufferToBase64(fontData))
            doc.addFont(`${fontName}.ttf`, fontName, 'normal')
        }

        doc.setFont(fontName, 'normal')
        doc.setFontSize(12)

        // 2. 添加自动换行功能
        const maxTextWidth = pageWidth - 2 * marginLeft; // 计算最大文本宽度

        const addTextWithWrapping = (text: string) => {
            // 分割文本为适合宽度的行
            const lines = doc.splitTextToSize(text, maxTextWidth);
            lines.forEach((line: string) => {
                if (cursorY > pageHeight - marginTop) {
                    doc.addPage();
                    cursorY = marginTop;
                }
                doc.text(line, marginLeft, cursorY);
                cursorY += lineHeight;
                // console.log(line)
            });
        };

        // 3. 使用带换行的文本输出
        const textContent = [
            '教案',
            '',
            `总课时数：${renderTeachingPlan.value.totalHours}`,
            '',
            '课时安排：',
            ...renderTeachingPlan.value.lessons.flatMap((lesson, idx) => [
                `${idx + 1}. ${lesson.title}`,
                ...lesson.timePlan.map(tp => `  【${tp.step}】${tp.minutes}分钟：${tp.content}`),
                ...(lesson.knowledgePoints?.length ? [`  知识点：${lesson.knowledgePoints.join('，')}`] : []),
                ...(lesson.practiceContent ? [`  实践内容：${lesson.practiceContent}`] : []),
                ...(lesson.teachingGuidance ? [`  教学提示：${lesson.teachingGuidance}`] : []),
                ''
            ]),
            `整体教学建议：${renderTeachingPlan.value.teachingAdvice}`
        ];

        // 输出带自动换行的内容
        textContent.forEach(line => {
            if (line.trim() === '') {
                // 空行处理
                cursorY += lineHeight;
            } else {
                addTextWithWrapping(line);
            }
        });

        doc.save('教案.pdf');
        ElMessage.success('导出成功');
    } catch (err) {
        console.error('导出PDF失败:', err);
        ElMessage.error('导出失败，请稍后再试');
    }
}

// 辅助函数：ArrayBuffer转Base64
const arrayBufferToBase64 = (buffer: ArrayBuffer) => {
    let binary = ''
    const bytes = new Uint8Array(buffer)
    for (let i = 0; i < bytes.byteLength; i++) {
        binary += String.fromCharCode(bytes[i])
    }
    return window.btoa(binary)
}

const formatMinutes = (min: number) => min + ' 分钟'

const renderTeachingPlan = computed(() => {
    if (!teachingPlanResult.value) return null
    const data = teachingPlanResult.value
    return {
        lessons: Array.isArray(data.lessons) ? data.lessons : [],
        totalHours: data.totalHours,
        teachingAdvice: data.teachingAdvice || {},
    }
})

// 自动展开所有课时
const allLessonIndexes = ref<number[]>([])
watch(renderTeachingPlan, (val) => {
    if (val && val.lessons) {
        allLessonIndexes.value = val.lessons.map((_: any, idx: number) => idx)
    }
})
</script>

<template>
    <div class="course-syllabus">
        <section class="syllabus-section">
            <div class="section-header">
                <h2>课程信息</h2>
                <button v-if="isTeacher" class="btn-edit" @click="editDialogVisible = true">编辑</button>
            </div>
            <div class="section-content">
                <div class="info-block">
                    <h3>教学目标</h3>
                    <div v-if="course.objective" v-html="course.objective"></div>
                    <div v-else class="empty-state">尚未设置教学目标</div>
                </div>
                <div class="info-block">
                    <h3>考核方式</h3>
                    <div v-if="course.assessment" v-html="course.assessment"></div>
                    <div v-else class="empty-state">尚未设置考核方式</div>
                </div>
                <div class="info-block">
                    <h3>课程大纲</h3>
                    <div v-if="course.outline" v-html="course.outline"></div>
                    <div v-else class="empty-state">尚未设置课程大纲</div>
                </div>
            </div>
        </section>

        <section class="syllabus-section">
            <div class="section-header">
                <h2>课程章节</h2>
                <div>
                    <button v-if="isTeacher" class="btn-edit" @click="showSectionForm = !showSectionForm">{{ showSectionForm ? '取消新增' : '新增章节' }}</button>
                    <button v-if="isTeacher" class="btn-edit" style="margin-left: 10px;" @click="openTeachingPlanDialog">生成教案</button>
                </div>
            </div>

            <div class="resource-list-container">
                <div v-if="showSectionForm" class="upload-form">
                    <h3>新增章节</h3>

                    <div v-if="uploadError" class="error-message">{{ uploadError }}</div>

                    <div class="form-group">
                        <label>选项</label>
                        <div
                            v-for="(section, index) in uploadForm.sections"
                            :key="index"
                            class="option-row"
                        >
                            <input
                                v-model="section.sectionId"
                                type="text"
                                :placeholder="`章节号`"
                                class="option-input"
                                style="max-width: 60px; margin-right: 20px"
                            />
                            <input
                                v-model="section.title"
                                type="text"
                                :placeholder="`章节${section.sectionId}标题`"
                                class="option-input"
                            />
                            <button
                                type="button"
                                class="btn-icon"
                                @click="removeSection(index)"
                                :disabled="uploadForm.sections.length <= 1"
                            >
                                ✕
                            </button>
                        </div>

                        <button
                            type="button"
                            class="btn-text"
                            @click="addSection"
                        >
                            + 添加章节
                        </button>
                    </div>
                    <div class="form-actions">
                        <button
                            type="button"
                            class="btn-primary"
                            @click="uploadSections"
                        >
                            上传
                        </button>
                    </div>
                </div>
                <div v-if="course.sections" class="resource-table-wrapper">
                    <table class="resource-table">
                        <thead>
                        <tr>
                            <th>章节序号</th>
                            <th>章节名称</th>
                            <th>操作</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr v-for="section in course.sections" :key="section.id">
                            <td>{{ section.sortOrder }}</td>
                            <td>{{ section.title }}</td>
                            <td class="actions">
                                <button
                                    v-if="isTeacher"
                                    class="btn-action preview"
                                    @click="deleteSection(section.id)"
                                    title="编辑"
                                >
                                    删除
                                </button>
                                <span v-else>无权限操作</span>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <div v-else-if="error" class="error-message">{{ error }}</div>
                <div v-else class="empty-state">尚未设置课程章节</div>
            </div>
        </section>

        <!-- 教案生成弹窗 -->
        <el-dialog v-model="showTeachingPlanDialog" title="生成教案" width="800px" :close-on-click-modal="false"
                   @close="() => { teachingPlanResult.value = null; teachingPlanLoading.value = false; expectedHours.value = null }">
            <div>
                <label>请输入预期总课时数：</label>
                <el-input v-model.number="expectedHours" type="number" min="1" placeholder="如 32" style="width: 200px; margin-bottom: 16px;" />
                <el-button type="primary" :loading="teachingPlanLoading" @click="handleGenerateTeachingPlan" style="margin-left: 16px;">生成</el-button>
            </div>
            <div v-if="teachingPlanResult" style="margin-top: 24px;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-weight: bold;">教案内容</span>
                    <el-button size="small" @click="exportTeachingPlanAsPDF">导出</el-button>
                </div>
                <!-- 美观展示教案内容 -->
                <div v-if="renderTeachingPlan" id="teaching-plan-pdf-content" style="margin-top: 12px; background: #fff; padding: 16px;">
                    <div>
                        <div style="font-size: 16px; font-weight: bold; margin-bottom: 8px;">
                            总课时数：{{ renderTeachingPlan.totalHours }}
                        </div>
                        <el-divider content-position="left">课时安排</el-divider>
                        <el-collapse v-model="allLessonIndexes" :accordion="false">
                            <el-collapse-item
                                v-for="(lesson, idx) in renderTeachingPlan.lessons"
                                :key="idx"
                                :title="(idx+1) + '. ' + lesson.title"
                                :name="idx"
                            >
                                <div>
                                    <div style="margin-bottom: 8px;">
                                        <span style="font-weight: 500;">时间分配：</span>
                                    </div>
                                    <el-table :data="lesson.timePlan" border size="small" style="margin-bottom: 8px;">
                                        <el-table-column prop="step" label="教学环节" width="90"/>
                                        <el-table-column prop="minutes" label="时长" width="60">
                                            <template #default="scope">
                                                {{ formatMinutes(scope.row.minutes) }}
                                            </template>
                                        </el-table-column>
                                        <el-table-column prop="content" label="内容"/>
                                    </el-table>
                                    <div v-if="lesson.knowledgePoints && lesson.knowledgePoints.length" style="margin-bottom: 6px;">
                                        <span style="font-weight: 500;">知识点：</span>
                                        <el-tag
                                            v-for="(kp, kidx) in lesson.knowledgePoints"
                                            :key="kidx"
                                            type="info"
                                            style="margin-right: 6px;"
                                        >{{ kp }}</el-tag>
                                    </div>
                                    <div v-if="lesson.practiceContent" style="margin-bottom: 6px;">
                                        <span style="font-weight: 500;">实践内容：</span>
                                        <span>{{ lesson.practiceContent }}</span>
                                    </div>
                                    <div v-if="lesson.teachingGuidance" style="margin-bottom: 6px;">
                                        <span style="font-weight: 500;">教学提示：</span>
                                        <span>{{ lesson.teachingGuidance }}</span>
                                    </div>
                                </div>
                            </el-collapse-item>
                        </el-collapse>
                        <el-divider content-position="left">整体教学建议</el-divider>
                        <div v-if="renderTeachingPlan.teachingAdvice">
                            {{ renderTeachingPlan.teachingAdvice }}
                        </div>
                    </div>
                </div>
                <!-- 隐藏的原始JSON，复制用 -->
                <textarea v-show="false" :value="JSON.stringify(teachingPlanResult, null, 2)" readonly></textarea>
            </div>
        </el-dialog>

        <CourseEditDialog
            v-if="course"
            v-model="editDialogVisible"
            :course="course"
            @success="handleEditSuccess"
        />
    </div>
</template>

<style scoped>
.upload-form {
    background-color: #f9f9f9;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 1.5rem;
    border: 1px solid #e0e0e0;
}

.upload-form h3 {
    margin-top: 0;
    margin-bottom: 1rem;
}

.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
}

.form-row {
    display: flex;
    gap: 1rem;
}

.form-group-half {
    flex: 1;
}

.form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
}

.btn-primary, .btn-secondary {
    padding: 0.5rem 1rem;
    border-radius: 4px;
    font-weight: 500;
    cursor: pointer;
    border: none;
    transition: background-color 0.2s;
}

.btn-primary {
    background-color: #2c6ecf;
    color: white;
}

.btn-primary:hover {
    background-color: #215bb4;
}

.error-message {
    background-color: #ffebee;
    color: #c62828;
    padding: 1rem;
    border-radius: 4px;
    margin-bottom: 1rem;
}

.course-syllabus {
    padding: 1.5rem;
}

.syllabus-section {
    margin-bottom: 2rem;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    padding-bottom: 0.5rem;
    border-bottom: 2px solid #e3f2fd;
}

.section-header h2 {
    margin: 0;
    font-size: 1.25rem;
    color: #1976d2;
}

.btn-edit {
    background: none;
    border: 1px solid #2c6ecf;
    color: #2c6ecf;
    cursor: pointer;
    font-size: 0.9rem;
    padding: 0.25rem 0.75rem;
    border-radius: 4px;
    transition: all 0.2s;
}

.btn-edit:hover {
    background-color: #e3f2fd;
    border-color: #1e88e5;
}

.resource-table-wrapper {
    overflow-x: auto;
}

.resource-table {
    width: 100%;
    border-collapse: collapse;
}

.resource-table th,
.resource-table td {
    padding: 1rem;
    text-align: left;
    border-bottom: 1px solid #e0e0e0;
}

.resource-table th {
    background-color: #f5f5f5;
    font-weight: 600;
}

.resource-table tr:hover {
    background-color: #f9f9f9;
}

.actions {
    display: flex;
    gap: 0.5rem;
}

.btn-action {
    padding: 0.375rem 0.75rem;
    border-radius: 4px;
    border: none;
    font-size: 0.875rem;
    cursor: pointer;
    transition: background-color 0.2s;
}

.btn-action.preview {
    background-color: #e3f2fd;
    color: #1976d2;
}

.btn-action.preview:hover {
    background-color: #bbdefb;
}

.section-content {
    line-height: 1.6;
}

.empty-state {
    padding: 1.5rem;
    background-color: #f5f5f5;
    border-radius: 4px;
    color: #757575;
    text-align: center;
}

.form-group {
    margin-bottom: 1.25rem;
}

.form-group label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
    transition: border-color 0.3s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
    border-color: #2c6ecf;
    outline: none;
}

.option-row {
    display: flex;
    align-items: center;
    margin-bottom: 0.5rem;
}

.option-input {
    flex: 1;
}

.btn-icon {
    background: none;
    border: none;
    cursor: pointer;
    color: #757575;
    font-size: 1rem;
    padding: 0.5rem;
}

.btn-icon:hover {
    color: #d32f2f;
}

.btn-icon:disabled {
    color: #bdbdbd;
    cursor: not-allowed;
}

.btn-text {
    background: none;
    border: none;
    color: #2c6ecf;
    padding: 0;
    font-weight: 500;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    margin-top: 0.5rem;
}

.btn-text:hover {
    text-decoration: underline;
}

.info-block {
    margin-bottom: 1.5rem;
    padding: 1rem;
    background-color: #f8f9fa;
    border-radius: 6px;
}

.info-block h3 {
    margin: 0 0 0.5rem 0;
    color: #2c6ecf;
    font-size: 1rem;
}

.info-block .empty-state {
    color: #999;
    font-style: italic;
}
</style>
