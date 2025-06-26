# 学生侧：在线学习助手（AI问答）模块说明（RAG方案）

## 一、后端涉及文件
- `src/main/java/org/example/edusoft/controller/ai/AiAssistantController.java`：AI问答接口控制器
- `src/main/java/org/example/edusoft/service/ai/AiAssistantService.java`：AI问答服务
- `src/main/java/org/example/edusoft/service/ai/KnowledgeBaseService.java`：本地知识库检索服务
- `src/main/java/org/example/edusoft/service/ai/EmbeddingService.java`：embedding服务（可API或本地）
- `src/main/java/org/example/edusoft/dto/ai/AiAskRequest.java`、`AiAskResponse.java`：请求与响应DTO

## 二、主要接口
- `POST /api/ai/ask`
  - 流程：
    1. 问题embedding（API或本地）
    2. 本地知识库向量检索（FAISS/Milvus等）
    3. 拼接prompt，调用大模型API（如DeepSeek、ChatGLM等）
    4. 返回答案及知识点出处

## 三、依赖与部署说明
- embedding模型可用API或本地部署
- 本地知识库向量检索（FAISS/Milvus/Weaviate等）
- 大模型API（DeepSeek、ChatGLM等）

## 四、前端涉及文件
- `frontend/project/src/views/help/HelpAndFeedback.vue`：增加AI问答入口与对话框
- `frontend/project/src/api/ai.ts`：AI问答接口封装

## 五、后续扩展
- 可扩展为教师侧内容生成、题目生成、学情分析等AI能力
- 可在更多页面嵌入AI助手入口 