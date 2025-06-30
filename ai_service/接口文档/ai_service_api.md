# 教学内容生成服务 API 文档(老版，没有更新，而且不需要，请直接Swapper看/导出)

> 本文档详细说明了 `ai_service/main.py` 中所有 FastAPI 接口的定义、请求参数、响应内容及其实现原理，并为每个接口文档添加了详细注释，便于开发者理解和二次开发。

---

## 目录
- [教学内容生成服务 API 文档(老版，没有更新，而且不需要，请直接Swapper看/导出)](#教学内容生成服务-api-文档老版没有更新而且不需要请直接swapper看导出)
  - [目录](#目录)
  - [1. `/embedding/upload` (POST)](#1-embeddingupload-post)
    - [请求参数](#请求参数)
    - [请求示例（multipart/form-data）](#请求示例multipartform-data)
    - [响应内容](#响应内容)
    - [实现原理](#实现原理)
  - [2. `/rag/generate` (POST)](#2-raggenerate-post)
    - [请求参数（JSON）](#请求参数json)
    - [请求示例](#请求示例)
    - [响应内容（JSON）](#响应内容json)
    - [实现原理](#实现原理-1)
  - [3. `/rag/generate_exercise` (POST)](#3-raggenerate_exercise-post)
    - [请求参数（JSON）](#请求参数json-1)
    - [请求示例](#请求示例-1)
    - [响应内容（JSON）](#响应内容json-1)
    - [实现原理](#实现原理-2)
  - [4. `/health` (GET)](#4-health-get)
    - [请求参数](#请求参数-1)
    - [响应内容](#响应内容-1)
    - [实现原理](#实现原理-3)
- [备注](#备注)

---

## 1. `/embedding/upload` (POST)

**接口功能**：
上传文档文件，自动解析内容并分块，添加到本地知识库（向量数据库）。

### 请求参数
- `file` (UploadFile, 必填)：待上传的文档文件，支持 PDF、Word、TXT、Markdown、JSON、CSV、Excel、HTML 等格式。
- `course_id` (str, 可选)：课程ID，用于归档文档。

### 请求示例（multipart/form-data）
```
file: <file>
course_id: "123"
```

### 响应内容
- `status` (str)：处理状态（如 success）。
- `message` (str)：处理结果描述。
- `chunks_count` (int)：文档被分割的内容块数量。
- `file_path` (str)：文档最终存储路径。

### 实现原理
1. **文件保存**：将上传的文件保存到临时目录（`storage/temp/`）。
2. **文档归档**：调用 `StorageService.save_document`，将文件移动到归档目录（带时间戳和可选课程ID）。
3. **内容解析**：调用 `DocumentParser.parse_file`，根据文件类型自动分块提取文本。
4. **知识库入库**：调用 `RAGService.add_to_knowledge_base`，将分块内容嵌入向量并存入本地 FAISS 向量数据库。
5. **清理临时文件**：处理完毕后自动删除临时文件。
6. **异常处理**：如有异常，返回 500 错误及详细日志。

---

## 2. `/rag/generate` (POST)

**接口功能**：
根据课程大纲自动生成详细的教学内容方案。

### 请求参数（JSON）
- `course_name` (str, 必填)：课程名称。
- `course_outline` (str, 必填)：课程大纲。
- `expected_hours` (int, 必填)：预期总课时数。

### 请求示例
```
{
  "course_name": "软件工程",
  "course_outline": "1. 软件工程概述...",
  "expected_hours": 16
}
```

### 响应内容（JSON）
- `lessons` (list)：每个课时的详细内容（标题、讲解、练习、建议、知识来源等）。
- `totalHours` (int)：总课时数。
- `timeDistribution` (str)：时间分配建议。
- `teachingAdvice` (str)：整体教学建议。

### 实现原理
1. **知识点提取**：调用大模型（如 deepseek-chat），根据课程大纲自动提取关键知识点。
2. **知识检索**：对每个知识点，调用 `RAGService.search_knowledge_base` 检索本地知识库相关内容。
3. **内容生成**：再次调用大模型，结合大纲和检索到的知识，生成结构化教学内容（JSON 格式）。
4. **异常处理**：如有异常，返回 500 错误及详细日志。

---

## 3. `/rag/generate_exercise` (POST)

**接口功能**：
根据课程内容自动生成多类型练习题。

### 请求参数（JSON）
- `course_name` (str, 必填)：课程名称。
- `lesson_content` (str, 必填)：课时内容。
- `difficulty` (str, 可选)：难度（如 easy/medium/hard，默认 medium）。

### 请求示例
```
{
  "course_name": "软件工程",
  "lesson_content": "软件工程的基本概念...",
  "difficulty": "easy"
}
```

### 响应内容（JSON）
- `exercises` (list)：每道题包含类型、题干、答案、解析、知识点等。

### 实现原理
1. **提示词构建**：调用 `PromptTemplates.get_exercise_generation_prompt` 生成大模型提示词。
2. **大模型生成**：调用 deepseek-chat 生成结构化练习题（JSON 格式）。
3. **异常处理**：如有异常，返回 500 错误及详细日志。

---

## 4. `/health` (GET)

**接口功能**：
健康检查，判断服务是否正常运行。

### 请求参数
无

### 响应内容
- `status` (str)：健康状态（如 healthy）。

### 实现原理
直接返回固定 JSON，供监控或负载均衡探活使用。

---

# 备注
- 所有接口均支持跨域（CORS），可直接被前端或第三方服务调用。
- 详细日志记录在 `logs/` 目录下，便于排查问题。
- 支持多种文档格式，便于知识库建设。
- 依赖大模型 API（如 deepseek-chat、Qwen embedding），需配置好相关 API KEY。

---

> 文档最后更新：2024-06-26 