# 教学内容自动生成模块说明

## 一、功能概述
根据教师提供的课程大纲，自动生成包含知识讲解、实训练习与指导、时间分配等在内的完整教学内容。

## 二、技术架构
采用Java主系统 + Python AI服务的微服务架构：
- Java SpringBoot负责业务逻辑和接口封装
- Python FastAPI负责AI服务，包括RAG检索和大模型调用
- 前端Vue负责用户交互界面

## 三、涉及文件
### 1. Java后端
- Controller: `src/main/java/org/example/edusoft/controller/ai/TeachingContentController.java`
- Service: `src/main/java/org/example/edusoft/service/ai/TeachingContentService.java`
- DTO: 
  - `src/main/java/org/example/edusoft/dto/ai/TeachingContentRequest.java`
  - `src/main/java/org/example/edusoft/dto/ai/TeachingContentResponse.java`
- AI Client: `src/main/java/org/example/edusoft/ai/AIServiceClient.java`

### 2. Vue前端
- 页面组件: `frontend/project/src/views/course/CourseTeachingContent.vue`
- API封装: `frontend/project/src/api/ai.ts`
- 路由配置: `frontend/project/src/router/index.ts`

### 3. Python AI服务
- FastAPI入口: `main.py`
- RAG服务: `services/rag.py`
- 知识库: `services/faiss_db.py`

## 四、主要接口
### 1. 前端接口
- 路由: `/course/:courseId/teaching-content`
- API: `POST /api/ai/teaching/generate`
  - 请求参数:
    ```typescript
    {
      courseOutline: string;    // 课程大纲
      courseId: number;         // 课程ID
      courseName: string;       // 课程名称
      expectedHours: number;    // 预期课时数
    }
    ```
  - 响应数据:
    ```typescript
    {
      lessons: Array<{
        title: string;          // 课时标题
        content: string;        // 知识点讲解
        practiceContent: string;// 实训练习
        teachingGuidance: string;// 教学指导
        suggestedHours: number; // 建议课时
        knowledgeSources: string[];// 知识点来源
      }>;
      totalHours: number;       // 总课时数
      timeDistribution: string; // 时间分配建议
      teachingAdvice: string;   // 教学建议
    }
    ```

### 2. Python服务接口
- Endpoint: `POST /rag/generate_teaching_content`
- 功能: 根据课程大纲和本地知识库生成教学内容
- 实现流程:
  1. 分析课程大纲，提取关键知识点
  2. 从本地知识库检索相关内容
  3. 调用大模型生成教学内容
  4. 返回结构化的教学内容数据

## 五、使用说明
1. 教师在课程详情页面点击"生成教学内容"
2. 输入课程大纲和预期课时数
3. 系统自动生成教学内容，包括:
   - 每节课的知识点讲解
   - 配套的实训练习
   - 教学指导建议
   - 时间分配方案
4. 教师可以预览、编辑、导出或保存生成的内容

## 六、后续优化
1. 支持批量生成和导入课程大纲
2. 增加教学内容模板配置
3. 支持多种导出格式（Word、PDF等）
4. 集成课程资源管理系统
5. 添加教学内容评估和反馈机制 