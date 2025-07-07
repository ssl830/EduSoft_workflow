<script setup lang="ts">
import {computed, defineProps, ref, watch} from 'vue'
import { useAuthStore } from '../../stores/auth'
import CourseApi from "../../api/course.ts";
import {useRoute} from "vue-router";
import CourseEditDialog from './CourseEditDialog.vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus'
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

const constraintInput = ref<string>('')

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
        const response : any = await CourseApi.getCourseById(courseId.value)
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
        props.course.sections = props.course.sections.filter((s:any) => s.id !== sectionId);
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
const exportLoading = ref(false)
const expectedHours = ref<number | null>(null)

const openTeachingPlanDialog = () => {
    expectedHours.value = null
    showTeachingPlanDialog.value = true
    teachingPlanResult.value = null
}

// 新增：教案生成弹窗
const showPlanDialog = ref(false)
const planDialogMsg = ref('')
function openPlanDialog(msg: string) {
  planDialogMsg.value = msg
  showPlanDialog.value = true
  if (msg.startsWith('正在生成教案')) {
    planDialogMsgIndex.value = 0
    planDialogMsg.value = planDialogMsgs[planDialogMsgIndex.value]
    planDialogInterval = window.setInterval(() => {
      planDialogMsgIndex.value = (planDialogMsgIndex.value + 1) % planDialogMsgs.length
      planDialogMsg.value = planDialogMsgs[planDialogMsgIndex.value]
    }, 2000)
  }
}
function closePlanDialog() {
  showPlanDialog.value = false
  planDialogMsg.value = ''
  if (planDialogInterval) {
    clearInterval(planDialogInterval)
    planDialogInterval = null
  }
}

const handleGenerateTeachingPlan = async () => {
    // 防止重复请求
    if (teachingPlanLoading.value) return;
    if (!expectedHours.value || expectedHours.value <= 0) {
        ElMessage.error('请输入有效的预期总课时数')
        return
    }
    teachingPlanLoading.value = true
    openPlanDialog('正在生成教案') // 只需传入任意"正在生成教案"开头的字符串即可
    try {
        const res : any = await CourseApi.generateTeachingContent({
            course_name: props.course.name,
            course_outline: props.course.outline || '',
            expected_hours: expectedHours.value,
            constraints: constraintInput.value
        })
        if (res.data) {
            teachingPlanResult.value = res.data
        } else {
            teachingPlanResult.value = res
        }
        closePlanDialog()
        planDialogMsg.value = '生成教案成功(๑˃̵ᴗ˂̵)و✧'
        showPlanDialog.value = true
        setTimeout(() => {
          closePlanDialog()
        }, 1500)
    } catch (err: any) {
        closePlanDialog()
        planDialogMsg.value = '生成教案失败(´；д；)`'
        showPlanDialog.value = true
        setTimeout(() => {
          closePlanDialog()
        }, 1500)
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
    exportLoading.value = true // 开始加载
    try {
        const doc = new jsPDF('p', 'mm', 'a4')
        const marginLeft = 15
        const marginTop = 20
        const lineHeight = 8
        const pageWidth = doc.internal.pageSize.getWidth()
        const pageHeight = doc.internal.pageSize.getHeight()
        let cursorY = marginTop

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
            ...renderTeachingPlan.value.lessons.flatMap((lesson:any, idx:any) => [
                `${idx + 1}. ${lesson.title}`,
                ...lesson.timePlan.map((tp:any) => `  【${tp.step}】${tp.minutes}分钟：${tp.content}`),
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
    } finally {
        exportLoading.value = false // 结束加载
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
    const data:any = teachingPlanResult.value
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

// 新增：每节课的细节数据
const lessonDetails = ref<Record<number, any>>({}) // key: lesson idx

// 新增：生成细节和重新生成
const handleLessonDetail = async (idx: number, lesson: any, type: 'detail' | 'regenerate') => {
    const loading = ElLoading.service({ text: '正在生成...', background: 'rgba(255,255,255,0.7)' })
    try {
        const { value: userConstraint, action } = await ElMessageBox.prompt('如有额外要求，请输入；否则留空', type==='detail' ? '生成细节要求' : '重新生成要求', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPlaceholder: '示例：突出案例分析，减少理论描述',
          inputType: 'textarea'
        }).catch(()=>({action:'cancel'} as any))
        if(action==='cancel') { loading.close(); return }
        const params = {
            title: lesson.title || '',
            knowledgePoints: lesson.knowledgePoints || [],
            practiceContent: lesson.practiceContent || '',
            teachingGuidance: lesson.teachingGuidance || '',
            timePlan: lesson.timePlan || [],
            constraints: userConstraint
        }
        let res
        if( type === 'detail' ){
            res = await CourseApi.generateTeachingContentDetail(params)
            console.log(res)
            if (res.data) {
                // 修正：直接操作 teachingPlanResult.value.lessons
                if (teachingPlanResult.value && teachingPlanResult.value.lessons) {
                  teachingPlanResult.value.lessons[idx] = res.data
                }
            } else {
                if (teachingPlanResult.value && teachingPlanResult.value.lessons) {
                  teachingPlanResult.value.lessons[idx] = res
                }
            }
            ElMessage.success('生成成功');
        }else{
            res = await CourseApi.regenerateTeachingContent(params)
            console.log(res)
            if (res.data) {
                if (teachingPlanResult.value && teachingPlanResult.value.lessons) {
                  teachingPlanResult.value.lessons[idx] = res.data
                }
            } else {
                if (teachingPlanResult.value && teachingPlanResult.value.lessons) {
                  teachingPlanResult.value.lessons[idx] = res
                }
            }
            ElMessage.success('生成成功');
        }
    } catch (e) {
        ElMessage.error('请求失败，请稍后再试')
    } finally {
        loading.close()
    }
}

// 新增：教案弹窗关闭时重置
const handleTeachingPlanDialogClose = () => {
    teachingPlanResult.value = null
    teachingPlanLoading.value = false
    expectedHours.value = null
}

// 新增：教案生成弹窗动画文案
const planDialogMsgs = [
  '正在生成教案(。-ω-)✧',
  '正在生成教案📖_(:3 」∠)_',
  '正在生成教案(๑•̀ㅂ•́)و✧',
  '正在生成教案─=≡Σ((( つ•̀ω•́)つ',
]
let planDialogInterval: number | null = null
const planDialogMsgIndex = ref(0)

const stepDetailCache = ref<Record<string, any>>({})

const handleStepDetail = async (lessonIdx: number, lesson: any, step: any) => {
  const key = `${lessonIdx}-${step.step}`
  if (stepDetailCache.value[key]) {
    // already generated, toggle display via marking maybe
    step.detail = stepDetailCache.value[key]
    return
  }
  const loading = ElLoading.service({ text: '正在生成环节细节...', background: 'rgba(255,255,255,0.7)' })
  try {
    const res:any = await CourseApi.generateStepDetail({
      lessonTitle: lesson.title,
      stepName: step.step,
      currentContent: step.content || '',
      knowledgePoints: lesson.knowledgePoints || []
    })
    const detail = res.data || res
    step.detail = detail
    stepDetailCache.value[key] = detail
    ElMessage.success('生成成功')
  } catch (e:any) {
    ElMessage.error('生成失败，请稍后再试')
  } finally {
    loading.close()
  }
}

const sectionDialogVisible = ref(false)
const selectedSection = ref<any>(null)
const sectionExpectedHours = ref<number|null>(null)
const sectionConstraints = ref<string>('')
const sectionFile = ref<File|null>(null)
const sectionLoading = ref(false)
const sectionPlanResult = ref<any>(null)
const sectionPlanLoading = ref(false)

const handleSectionPlanClick = (section: any) => {
  selectedSection.value = section
  sectionExpectedHours.value = null
  sectionConstraints.value = ''
  sectionFile.value = null
  sectionDialogVisible.value = true
}

const handleSectionFileChange = (e: Event) => {
  const files = (e.target as HTMLInputElement).files
  if (files && files.length) {
    sectionFile.value = files[0]
  }
}

const handleGenerateSectionPlan = async () => {
  if (!sectionFile.value) {
    ElMessage.error('请上传章节大纲文件')
    return
  }
  if (!sectionExpectedHours.value || sectionExpectedHours.value <= 0) {
    ElMessage.error('请输入有效的课时数')
    return
  }
  sectionPlanLoading.value = true
  openPlanDialog('正在生成章节教案')
  try {
    const formData = new FormData()
    formData.append('file', sectionFile.value as Blob)
    formData.append('course_name', props.course.name)
    formData.append('section_title', selectedSection.value.title)
    formData.append('expected_hours', String(sectionExpectedHours.value))
    if (sectionConstraints.value) {
      formData.append('constraints', sectionConstraints.value)
    }
    const res:any = await CourseApi.generateSectionTeachingContent(formData)
    const body = res.data ? res.data : res
    if (body && body.status === 'fail') {
      throw new Error(body.message || '生成失败')
    }
    sectionPlanResult.value = body
    closePlanDialog()
    planDialogMsg.value = '章节教案生成成功(๑˃̵ᴗ˂̵)و✧'
    showPlanDialog.value = true
    setTimeout(()=>{ closePlanDialog() },1500)
  } catch (e:any){
    closePlanDialog()
    planDialogMsg.value = '章节教案生成失败(´；д；)`'
    showPlanDialog.value = true
    setTimeout(()=>{ closePlanDialog() },1500)
    ElMessage.error('生成失败，请稍后再试')
  } finally {
    sectionPlanLoading.value = false
  }
}

const renderSectionPlan = computed(()=>{
  if (!sectionPlanResult.value) return null
  const data:any = sectionPlanResult.value
  return {
    lessons: Array.isArray(data.lessons)? data.lessons:[],
    totalHours: data.totalHours,
    teachingAdvice: data.teachingAdvice || {}
  }
})

const exportSectionPlanAsPDF = async ()=>{
  if (!renderSectionPlan.value){
    ElMessage.error('无教案内容，无法导出')
    return
  }
  exportLoading.value = true
  try{
    const doc = new jsPDF('p','mm','a4')
    const marginLeft =15
    const marginTop=20
    const lineHeight=8
    const pageWidth=doc.internal.pageSize.getWidth()
    const pageHeight=doc.internal.pageSize.getHeight()
    let cursorY=marginTop
    const fontName='SourceHanSerifSC'
    const fontUrl='/src/assets/fonts/SourceHanSerifSC-VF.ttf'
    if(!doc.getFontList()[fontName]){
      const fontData = await fetch(fontUrl).then(r=>r.arrayBuffer())
      doc.addFileToVFS(`${fontName}.ttf`, arrayBufferToBase64(fontData))
      doc.addFont(`${fontName}.ttf`,fontName,'normal')
    }
    doc.setFont(fontName,'normal')
    doc.setFontSize(12)
    const maxTextWidth=pageWidth-2*marginLeft
    const addText=(text:string)=>{
      const lines=doc.splitTextToSize(text,maxTextWidth)
      lines.forEach((l:string)=>{
        if(cursorY>pageHeight-marginTop){ doc.addPage(); cursorY=marginTop }
        doc.text(l,marginLeft,cursorY)
        cursorY+=lineHeight
      })
    }
    const textContent=[
      `章节教案 - ${selectedSection.value.title}`,
      '',
      `总课时数：${renderSectionPlan.value.totalHours}`,
      '',
      '课时安排：',
      ...renderSectionPlan.value.lessons.flatMap((lesson:any,idx:number)=>[
        `${idx+1}. ${lesson.title}`,
        ...lesson.timePlan.map((tp:any)=>`  【${tp.step}】${tp.minutes}分钟：${tp.content}`),
        ...(lesson.knowledgePoints?.length?[`  知识点：${lesson.knowledgePoints.join('，')}`]:[]),
        ...(lesson.practiceContent?[`  实践内容：${lesson.practiceContent}`]:[]),
        ...(lesson.teachingGuidance?[`  教学提示：${lesson.teachingGuidance}`]:[]),
        ''
      ]),
      `整体教学建议：${renderSectionPlan.value.teachingAdvice}`
    ]
    textContent.forEach(line=>{
      if(line.trim()===''){cursorY+=lineHeight}else{addText(line)}
    })
    doc.save(`${selectedSection.value.title}-教案.pdf`)
    ElMessage.success('导出成功')
  }catch(err){ElMessage.error('导出失败');console.error(err)}
  finally{exportLoading.value=false}
}

const handleSectionLessonDetail = async (idx:number, lesson:any, type:'detail'|'regenerate')=>{
  const loading = ElLoading.service({text:'正在生成...',background:'rgba(255,255,255,0.7)'})
  try{
    const { value: userConstraint, action } = await ElMessageBox.prompt('如有额外要求，请输入；否则留空', type==='detail'?'生成细节要求':'重新生成要求', {confirmButtonText:'确定',cancelButtonText:'取消',inputType:'textarea'}).catch(()=>({action:'cancel'} as any))
    if(action==='cancel'){loading.close();return}
    const params={
      title:lesson.title||'',
      knowledgePoints:lesson.knowledgePoints||[],
      practiceContent:lesson.practiceContent||'',
      teachingGuidance:lesson.teachingGuidance||'',
      timePlan:lesson.timePlan||[],
      constraints:userConstraint
    }
    let res
    if(type==='detail') res = await CourseApi.generateTeachingContentDetail(params)
    else res = await CourseApi.regenerateTeachingContent(params)
    if(res.data){
      if(sectionPlanResult.value && sectionPlanResult.value.lessons){ sectionPlanResult.value.lessons[idx]=res.data }
    }else{
      if(sectionPlanResult.value && sectionPlanResult.value.lessons){ sectionPlanResult.value.lessons[idx]=res }
    }
    ElMessage.success('生成成功')
  }catch(e){ElMessage.error('请求失败，请稍后再试')}
  finally{loading.close()}
}

const allSectionLessonIndexes = ref<number[]>([])
watch(renderSectionPlan,(val)=>{
  if(val && val.lessons){
    allSectionLessonIndexes.value = val.lessons.map((_:any,idx:number)=>idx)
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
                                    @click="handleSectionPlanClick(section)"
                                    title="生成教案"
                                >
                                    生成教案
                                </button>
                                <button
                                    v-if="isTeacher"
                                    class="btn-action preview"
                                    @click="deleteSection(section.id)"
                                    title="删除"
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
        <el-dialog
            v-model="showTeachingPlanDialog"
            title="生成教案"
            width="800px"
            :close-on-click-modal="false"
            @close="handleTeachingPlanDialogClose"
        >
            <div>
                <label>请输入预期总课时数：</label>
                <el-input v-model.number="expectedHours" type="number" min="1" placeholder="如 32" style="width: 200px; margin-bottom: 16px;" />
                <el-button type="primary" :loading="teachingPlanLoading" @click="handleGenerateTeachingPlan" style="margin-left: 16px;">生成</el-button>
            </div>
            <div v-if="teachingPlanResult" style="margin-top: 24px;">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <span style="font-weight: bold;">教案内容</span>
                    <el-button size="small" @click="exportTeachingPlanAsPDF" :loading="exportLoading" :disabled="exportLoading">
                        导出
                    </el-button>
                </div>
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
                                    <!-- 新增：按钮组 -->
                                    <div style="margin-bottom: 10px;">
                                        <el-button size="small" @click="handleLessonDetail(idx, lesson, 'detail')">生成细节</el-button>
                                        <el-button size="small" type="warning" @click="handleLessonDetail(idx, lesson, 'regenerate')">重新生成</el-button>
                                    </div>
                                    <!-- 新增：细节展示 -->
                                    <div v-if="lessonDetails[idx]" class="lesson-detail-block">
                                        <div v-if="lessonDetails[idx].knowledgePoints && lessonDetails[idx].knowledgePoints.length" style="margin-bottom: 8px;">
                                            <span style="font-weight: 500;">知识点：</span>
                                            <ol class="knowledge-list">
                                                <li v-for="(kp, kidx) in lessonDetails[idx].knowledgePoints" :key="kidx">{{ kp }}</li>
                                            </ol>
                                        </div>
                                        <div v-if="lessonDetails[idx].practiceContent" style="margin-bottom: 6px;">
                                            <span style="font-weight: 500;">实践内容：</span>
                                            <span>{{ lessonDetails[idx].practiceContent }}</span>
                                        </div>
                                        <div v-if="lessonDetails[idx].teachingGuidance" style="margin-bottom: 6px;">
                                            <span style="font-weight: 500;">教学提示：</span>
                                            <span>{{ lessonDetails[idx].teachingGuidance }}</span>
                                        </div>
                                        <div v-if="lessonDetails[idx].timePlan && lessonDetails[idx].timePlan.length">
                                            <span style="font-weight: 500;">时间分配：</span>
                                            <el-table :data="lessonDetails[idx].timePlan" border size="small" style="margin-bottom: 8px;">
                                                <el-table-column prop="step" label="教学环节" width="90"/>
                                                <el-table-column prop="minutes" label="时长" width="60">
                                                    <template #default="scope">
                                                        {{ formatMinutes(scope.row.minutes) }}
                                                    </template>
                                                </el-table-column>
                                                <el-table-column prop="content" label="内容"/>
                                                <el-table-column label="操作" width="90">
                                                    <template #default="scope">
                                                        <el-button size="small" type="primary" @click="handleStepDetail(idx, lesson, scope.row)">生成细节</el-button>
                                                    </template>
                                                </el-table-column>
                                            </el-table>
                                        </div>
                                    </div>
                                    <!-- 原有内容 -->
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
                                        <ol class="knowledge-list">
                                            <li v-for="(kp, kidx) in lesson.knowledgePoints" :key="kidx">{{ kp }}</li>
                                        </ol>
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
            <label style="margin-top:12px; display:block;">可选：额外要求/约束</label>
            <el-input v-model="constraintInput" type="textarea" rows="3" placeholder="如：加强案例分析；突出实践环节"></el-input>
        </el-dialog>

        <!-- 教案生成状态弹窗 -->
        <div v-if="showPlanDialog" class="kb-dialog-mask">
          <div class="kb-dialog">
            <div class="kb-dialog-content">
              <div v-if="planDialogMsg.startsWith('正在生成教案')">
                <div class="kb-dialog-spinner"></div>
              </div>
              <div class="kb-dialog-msg">{{ planDialogMsg }}</div>
            </div>
          </div>
        </div>

        <!-- 章节教案生成弹窗 -->
        <el-dialog
            v-model="sectionDialogVisible"
            title="生成章节教案"
            width="600px"
            :close-on-click-modal="false"
        >
            <div>
                <p>章节：{{ selectedSection?.title || '' }}</p>
                <el-input v-model.number="sectionExpectedHours" type="number" min="1" placeholder="请输入预期课时数" style="width: 200px; margin-bottom: 16px;" />
                <div style="margin-bottom: 12px;">
                    <input type="file" accept=".doc,.docx,.pdf" @change="handleSectionFileChange" />
                </div>
                <el-input v-model="sectionConstraints" type="textarea" rows="3" placeholder="可选：额外要求/约束"></el-input>

                <!-- 生成后展示教案 -->
                <div v-if="renderSectionPlan" style="margin-top:24px;">
                  <div style="display:flex;justify-content:space-between;align-items:center;">
                      <span style="font-weight:bold;">章节教案内容</span>
                      <el-button size="small" :loading="exportLoading" @click="exportSectionPlanAsPDF" :disabled="exportLoading">导出 PDF</el-button>
                  </div>
                  <el-collapse v-model="allSectionLessonIndexes" accordion>
                      <el-collapse-item v-for="(lesson,idx) in renderSectionPlan.lessons" :key="idx" :name="idx">
                          <template #title>{{ idx+1 }}. {{ lesson.title }}</template>

                          <div style="margin-bottom:8px;">
                              <el-button size="small" @click="handleSectionLessonDetail(idx, lesson, 'detail')">生成细节</el-button>
                              <el-button size="small" type="warning" @click="handleSectionLessonDetail(idx, lesson, 'regenerate')">重新生成</el-button>
                          </div>
                          <div style="margin-bottom:8px;">
                              <span style="font-weight:500;">时间分配：</span>
                          </div>
                          <el-table :data="lesson.timePlan" border size="small" style="margin-bottom:8px;">
                              <el-table-column prop="step" label="教学环节" width="90" />
                              <el-table-column prop="minutes" label="时长" width="60">
                                  <template #default="scope">{{ formatMinutes(scope.row.minutes) }}</template>
                              </el-table-column>
                              <el-table-column prop="content" label="内容" />
                          </el-table>
                          <div v-if="lesson.knowledgePoints && lesson.knowledgePoints.length" style="margin-bottom:6px;">
                              <span style="font-weight:500;">知识点：</span>
                              <ol class="knowledge-list"><li v-for="(kp,kidx) in lesson.knowledgePoints" :key="kidx">{{ kp }}</li></ol>
                          </div>
                          <div v-if="lesson.practiceContent" style="margin-bottom:6px;"><span style="font-weight:500;">实践内容：</span>{{ lesson.practiceContent }}</div>
                          <div v-if="lesson.teachingGuidance" style="margin-bottom:6px;"><span style="font-weight:500;">教学提示：</span>{{ lesson.teachingGuidance }}</div>
                      </el-collapse-item>
                  </el-collapse>
                  <el-divider content-position="left">整体教学建议</el-divider>
                  <div v-if="renderSectionPlan.teachingAdvice">{{ renderSectionPlan.teachingAdvice }}</div>
                </div>

                <div style="margin-top: 16px; text-align:right;">
                    <el-button @click="sectionDialogVisible = false">关闭</el-button>
                    <el-button type="primary" :loading="sectionLoading" @click="handleGenerateSectionPlan">重新生成</el-button>
                </div>
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

.lesson-detail-block {
    background: #f4f8fd;
    border: 1px solid #e3f2fd;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 12px;
}

.knowledge-list {
    margin: 0 0 0 1.5em;
    padding: 0;
    list-style-type: decimal;
    word-break: break-all;
    white-space: pre-line;
    /* 让li自动换行 */
}

.knowledge-list li {
    margin-bottom: 2px;
    word-break: break-all;
    white-space: pre-line;
    line-height: 1.7;
}

/* 教案生成弹窗样式，复用CourseResourceList.vue */
.kb-dialog-mask {
  position: fixed;
  z-index: 9999;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
}
.kb-dialog {
  background: #fff;
  border-radius: 12px;
  min-width: 260px;
  min-height: 120px;
  box-shadow: 0 4px 24px rgba(44,110,207,0.13);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 2.5rem;
  animation: fadein 0.2s;
}
.kb-dialog-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.kb-dialog-msg {
  font-size: 1.1rem;
  color: #512da8;
  margin-top: 1rem;
  text-align: center;
}
.kb-dialog-spinner {
  width: 36px;
  height: 36px;
  border: 4px solid #ede7f6;
  border-top: 4px solid #8e24aa;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 0.5rem;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
@keyframes fadein {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
