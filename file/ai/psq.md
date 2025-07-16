## 教师侧

### 资料上传与知识库更新

教师可以在课程文件管理界面上传课件、学术论文、图片或视频等资料，系统会自动完成文本解析、分块、向量化并写入本地 FAISS 向量库，为后续 RAG 检索提供支撑。

#### 需求分析
1. 教师应当可以在网站上上传资料，资料将自动分块、编码，自动更新本地知识库
2. 应当支持多种资料格式，支持'.pdf', '.docx', '.doc', '.txt', '.md', '.json', '.csv', '.xlsx', '.html'等多种常见文本格式
3. 应当支持图片、视频等多模态资料上传，.mp4', '.avi', '.mov', '.mkv', '.jpg', '.jpeg', '.png', '.bmp', '.tiff'
4. 应当保证隐私数据和保密资料，使得部分资料的信息只保存在用户端，而不上传至服务端，但是要保证ai功能同时利用服务端知识库与用户端知识库
5. 为了保证知识库不被污染，只有教师、管理员能更新服务器知识库。

#### 技术实现思路
1. **前端**：`CourseDetail.vue` 调用 `FileController(/courses/{courseId}/upload)`，携带 `uploadToKnowledgeBase=true` 标记。
2. **后端 Java**：
   - `FileUploadImpl.uploadFile()` 经 `IFileStorageProvider` 上传至对象存储，随后调用 `AiServiceClient.uploadMaterial()` 将 `MultipartFile` 透传至 Python 微服务。
   - 课堂/章节目录由 `FolderService` 自动创建；版本控制通过 `FileInfo.version` 字段+`recursionFindName()` 重命名实现。
3. **Python 微服务**：
   - `main.py` 中 `/embedding/upload` 路由接收文件后写入教师私有 `StorageService` 目录，然后交由 `DocumentParser` 解析。
   - `DocumentParser` 针对 13 种扩展名分派到 `parse_pdf / parse_docx / parse_video / parse_image …`，其中 `parse_video` 调用 `moviepy`+`ffmpeg` 提取音轨，再用 `openai-whisper` 转写；`parse_image` 使用 `pytesseract` OCR。
   - 解析得到的 `chunks` 经过 `EmbeddingService.get_chunks_embeddings()` 转为 1536 维向量并写入 `FAISSDatabase`（每位教师独立文件）。
4. **查询阶段**：其它 RAG 接口统一调用 `RAGService.search_knowledge_base()`，在选定的 KB 向量库上做余弦检索返回 Top-k 结果。

#### 创新点
1. 多模态统一向量化 Pipeline：采用 `doc_parser` + FFmpeg + Tesseract + Whisper 组成的链式解析器，对图片型 PDF/纯图片执行 OCR，对视频提取音轨再做 ASR，最终文本全部走统一分块-embedding 逻辑，保证不同媒介的知识可被同一检索模型消费。
2. 私密 / 联合双层知识库架构：通过动态切换 `storage/base_path` 与 `storage/selected`，实现 Local-First 的隐私存储及多路径聚合检索，满足“可离线、可协同”场景。
3. 向量维度自适应：`EmbeddingService` 启动时根据环境变量和模型自动确定维度并校验 / 重建 FAISS 索引。
4. 异步事务 + 幂等上传：前端轮询处理进度，后端使用临时文件 + SHA-256 去重；任何步骤失败自动回滚并持久化日志至 `embedding_logger`，确保知识库一致性。


#### 使用说明
**方式一：** 教师在课程文件页点击“上传”，选择文件并勾选“同步到知识库”。这里的上传文件可以将文件资源分享给班级的学生。
1. 请先创建课程与该课程的班级（比如Linux开发课程，有周一下午班，周二下午班）
    ![alt text](image-2.png)
    ![alt text](image-1.png)
2. 点击课程中心的相应课程
    ![alt text](image.png)
3. 进入教学资料页面
    ![alt text](image-17.png)
4. 点击上传资料，设置相关信息如可见性（课程内公开即对属于该课程的所有班级的学生公开，班级内公开即选定班级公开）
   勾选上传到知识库则会将资料同步至知识库
   ![alt text](image-18.png)
   ![alt text](image-19.png)


**方式二：** 私密知识库/联合知识库中导入私密知识库/广播式导入知识库（见后"私密知识库与联合知识库设计（核心创新）"）

---

### 备课与教案设计

系统依据课程大纲自动生成学期或章节级别的教学计划、重点知识点与课堂练习，为教师提供可编辑的教案草稿。

#### 需求分析
 1. 教师在创建课程的时候输入了课程大纲，应当可以一键根据课程大纲和输入课程的学时来生成总体的课程教学安排
 2. 教师可能需要针对某个章节设计课时安排，所以应当设计根据上传的章节大纲与章节课时数来生成每个章节每一课时的教学安排
 3. 当一次生成多个课时的内容时，教师应当能够重新生成设计所有内容，也可以重新生成设计某一个课时的内容
 4. 应当保持个性化生成，教师可以输入自己的个性化要求，生成符合教师要求的内容
 5. 应当保持用户交互性，当生成一版教案后，教师可以针对生成的内容提供反馈意见进行重新生成
 6. 由于token限制，某个课时内容可能不足够详细，当生成第一版大体的课时思路后，应当支持选择某个课时来生成更加详细教案，同时此处也应当兼顾用户交互性，用户可以输入生成更详细教案的个人要求。
 7. 各个课时应当可折叠，页面保证美观。
 8. 生成的教案内容支持pdf格式导出
 
#### 技术实现思路
1. **前端**：`CourseCreate.vue` 和 `ClassCreate.vue` 通过 `generateTeachingContent()` (api/ai.ts) 调用 `/api/ai/rag/generate`，并在教案页提供“细节”与“重新生成”入口。
2. **后端 Java**：`AiAssistantController.generateTeachingContent()` 将请求透传至 Python 微服务，同时记录调用日志。
3. **Python 微服务**：`rag_service.generate_teaching_content()` 组合课纲摘要 + 检索到的知识片段作为 Prompt，调用开源 LLM（Qwen-chat-int4）得到 JSON 结构化结果。
4. **二次细化**：教师选择章节后由 `/rag/generate_section` 处理上传的章节文件，或通过 `/rag/detail`、`/rag/regenerate` 接口迭代。
5. **持久化**：完成后端持久化到 `TeachingResource`，并可在 `CourseDetail.vue` 导出 PDF。


#### 创新点
1. 分层 Prompt-Engineering：先生成章节纲要，再按课时细化，最后对知识点/活动逐级展开，解决大纲→细节的 Token 瓶颈。
2. 双向交互 Loop：提供 `/rag/feedback` 与 `/rag/regenerate`，教师可标注修改意见并一键重生，形成 AI-Human 共创闭环。
3. 可插拔模型：LLM 调用通过环境变量热切换（如 Qwen/Baichuan/ChatGLM），同一接口无需改动即可试验不同开源模型效果。

**时序图：教案生成调用链**
```mermaid
sequenceDiagram
    participant T as 教师(前端)
    participant J as AiAssistantController
    participant P as Python FastAPI
    participant R as RAGService

    T->>J: 点击“生成教案”
    J->>P: POST /rag/generate
    P->>R: generate_teaching_content()
    R->>R: 提取知识点 & 检索 KB
    R-->>P: JSON 教案
    P-->>J: 转发结果
    J-->>T: 渲染课程/课时树
```


#### 使用说明
1. 请先创建课程与该课程的班级（比如Linux开发课程，有周一下午班，周二下午班）
    ![alt text](image-2.png)
    ![alt text](image-1.png)
2. 点击课程中心的相应课程
    ![alt text](image.png)
3. 点击课程概况界面，在此可以完善改动课程大纲
    ![alt text](image-3.png)
4. 点击课程章节右侧的生成教案，输入学时和个性化要求，生成总的课程教案。
    ![alt text](image-4.png)
5. 每一个学时的教案内容都可以根据个性化建议单独重新生成或者生成详细内容，也可以再最底部输入建议重新生成所有课时，教案包括课时安排、每课时时间安排、知识点、教学建议等
    ![alt text](image-5.png)
    ![alt text](image-6.png)
    ![alt text](image-7.png)
    ![alt text](image-8.png)
6. 满意后可以点击右上角的导出，导出PDF
   ![alt text](image-9.png)
   ![alt text](image-10.png)
   


---

### 考核内容自动生成

依据课堂内容或题型配置，自动创建多种题型（选择、填空、简答、编程等）及参考答案，并直接写入教师题库。

#### 需求分析

1. 教师可以选择自己的课程、章节来生成多种题目，支持多种题型，支持设计题目的难度级别
2. 应当支持批量生成，一次生成多种不同类型的题目
3. 每个老师生成过的题目应当保存在老师的题库中，方便持久保存、回顾，同时这种设计可以对练习题进行筛选，再加入正式的练习中
4. 在自动化生成同时保留教师自主设计题目的权力
5. 题库支持按照课程筛选，教师可以从题库中任意挑选练习题目，设计题目分值，组建练习题，选择课程下面的班级发布练习

#### 技术实现思路
1. **前端**：`ExerciseCreate.vue` 组装参数，通过 `generateExercises()` 调用 `/api/ai/rag/generate_exercise`。
2. **后端 Java**：`AiAssistantController.generateExercises()` 负责日志与异常处理，再转发至微服务。
3. **Python 微服务**：`rag_service.generate_exercises()` 针对不同 `type-count` 组合动态 Prompt，RAG 检索课程向量库保证语境一致，生成结构化 JSON 返回。
4. **数据落库**：返回后 `ExerciseCreate.vue` 提供“保存至题库”选项，后端写入 `Question`、`Practice` 相关表。

#### 创新点
1. 错题反向生成：`generate_exercise_from_selected()` 将学生错题 JSON 发送给微服务重新组合新题，实现有针对性的诊断式练习。
2. 难度 & 题型可配置：`generate_exercises()` 接收 `difficulty / choose_count / fill_blank_count …` 直接影响 Prompt，实现定制化输出。
3. 知识点溯源：返回结果中携带 `knowledge_points` 与 `source` 字段，可定位至原文片段。

#### 流程图：题目生成 Pipeline
```mermaid
graph LR
    X[教师配置参数] --> Y[前端 API: generateExercises]
    Y --> J2(AiAssistantController)
    J2 --> P2(/rag/generate_exercise)
    P2 --> R2(RAGService)
    R2 -->|Prompt + KB| LLM[Deepseek-chat]
    LLM --> R2
    R2 --> P2
    P2 --> J2
    J2 --> Z[前端展示 & 保存题库]
```

#### 使用说明
1. 在侧边栏点击题库中心或者顶部栏的题库，进入题库中心
   ![alt text](image-20.png)
2. 点击生成题目，输入期望题型可实现自动生成题目
   ![alt text](image-21.png)
   ![alt text](image-22.png)
3. 支持自定义题型（主观题会显示为问答题，但题型符合老师要求）
   ![alt text](image-23.png)
   ![alt text](image-24.png)
4. 在生成练习页面除了自己设计题目也可以可以添加题库中生成的题目并设计该题目在练习当中的分值。
   ![alt text](image-25.png)
   ![alt text](image-26.png)

---

### 学情数据分析

系统收集学生答题数据，自动计算得分率、错误类型分布，并由大模型生成知识掌握概述与改进建议，展示于教师数据大屏。

#### 需求分析
- 自动统计客观题正确率、主观题 AI 判分。
- 生成整体/分知识点的掌握度报告。
- 支持班级维度、个人维度的多层分析。

#### 技术实现思路
1. **数据统计**：`PracticeQuestionStatMapper` 汇总学生答题正确率；`DashboardMapper.getTopWrongKnowledgePoints()` 返回高频错误知识点。
2. **主观题评测**：`AiAssistantController.evaluateSubjective` 调用 `AiAssistantService.evaluateSubjective()` -> `/rag/evaluate_subjective`，由 Deepseek-chat 输出 JSON 包含评分 / 分析 / 建议。
3. **综合分析**：`AiAssistantService.analyzeExercise()` 组装题目 error_rate 数组发往 `/rag/analyze_exercise`，微服务基于 KB 与成绩生成诊断报告。
4. **可视化**：`DashboardOverview.vue` 通过 ECharts 绘制正确率趋势、知识点热图；支持导出 PDF 供教学评估。

#### 创新点
1. 混合评分引擎：客观题得分直接由后端规则计算，主观题调用 `rag_service.evaluate_subjective_answer()` 使用 Deepseek-chat 进行 Rubric 评分。
2. 知识点热图：`DashboardService` 汇总 `PracticeQuestionStatMapper` 的错误率，前端将结果渲染为高频错误知识点热图，辅助教师聚焦薄弱环节。
3. 自动教学策略建议：微服务在 `analyze_exercise()` 中结合得分率与知识点返回改进建议字段，页面以 Card 形式展示。

#### 时序图：学情分析与可视化
```mermaid
sequenceDiagram
    participant S as 学生
    participant FE as 前端
    participant B as PracticeController
    participant DB as MySQL
    participant J3 as AiAssistantController
    participant P3 as FastAPI
    participant R3 as RAGService

    S->>FE: 提交练习答案
    FE->>B: POST /practice/submit
    B->>DB: 记录得分
    教师->>FE: 点击“学情分析”
    FE->>J3: POST /api/ai/analyze-exercise
    J3->>DB: 查询题目得分统计
    J3->>P3: POST /rag/analyze_exercise (题目+error_rate[])
    P3->>R3: analyze_exercise()
    R3-->>P3: 报告 JSON
    P3-->>J3: 转发结果
    J3-->>FE: 报告 + 图表数据
    FE->>FE: ECharts 渲染趋势/热图
```
#### 使用说明
1. 学生端点击班级中心，进入相应班级，进入"学习进度"页面
    ![alt text](image-32.png)
2. 点击练习，进行作答，客观题自动批改，主观题将发送给ai进行ai自动批改
   ![alt text](image-33.png)
   ![alt text](image-34.png)
3. 教师点击侧边栏的练习分析
    ![alt text](image-27.png)
4. 选择练习点击ai分析
   ![alt text](image-28.png)
   ![alt text](image-29.png)
5. 点击右上角导出可以导出分析结果
    ![alt text](image-30.png)
    ![alt text](image-31.png)
---

## 私密知识库与联合知识库设计（核心创新）

### 设计动机
传统 RAG 系统通常只有“单一服务器知识库”。在高校教学场景中存在两大痛点：
1. 部分课件/论文涉及版权或未公开研究资料，教师只希望在个人电脑本地使用，拒绝上传到公共服务器。
2. 教师往往跨课程、跨学期复用资料，需要在多套资料之间灵活组合检索，而不是被迫切换工程或重复上传。

### 架构概览
```mermaid
graph TD;
    FE[前端 Knowledge-Base Settings] -->|REST| BE_JAVA(AiAssistantController);
    BE_JAVA -->|/storage/selected| PY_FASTAPI(main.py);
    PY_FASTAPI -->|set_base_paths| STORAGE(StorageService);
    STORAGE -->|vector_db_paths| RAG(RAGService);
    RAG -->|search 聚合| FAISS_A(FAISS_A);
    RAG -->|search 聚合| FAISS_B(FAISS_B);
    RAG -->|search聚合| FAISS_C(FAISS_C);
```

### 时序图：私密 / 联合知识库切换

```mermaid
sequenceDiagram
    participant U as 教师(前端)
    participant J as AiAssistantController
    participant P as Python FastAPI
    participant S as StorageService

    U->>J: 选择 KB 路径数组
    J->>P: POST /storage/selected
    P->>S: set_base_paths(paths)
    S-->>P: 更新 root_paths & 初始化目录
    P-->>J: 成功响应
    J-->>U: UI Toast "知识库切换成功"
```

### 流程图：资料上传并向量化流水线

```mermaid
graph LR;
    A[教师上传文件] --> B{FileController<br>visibility?};
    B -->|uploadToKB=true| C[FileUploadImpl<br>存储文件];
    C --> D[AiServiceClient.uploadMaterial];
    D --> E["/embedding/upload"];
    E --> F[DocumentParser<br>多模态解析];
    F --> G[EmbeddingService<br>向量化];
    G --> H[FAISS DB<br>写入];
    H --> I{完成};
    B -->|uploadToKB=false| I;
```


### 类图：StorageService & RAGService 关系

```mermaid
classDiagram
    class StorageService {
        +List~str~ root_paths
        +set_base_paths(paths)
        +save_document()
        +get_vector_db_paths()
    }

    class RAGService {
        +Dict vector_dbs
        +search_knowledge_base()
        +add_to_knowledge_base()
    }

    StorageService "1" --> "*" RAGService : 提供路径
```

### 检索算法
```python
aggregated = []
for db in self.vector_dbs.values():
    aggregated.extend(db.search(query_embedding, top_k))

aggregated_sorted = sorted(aggregated, key=lambda x: x['distance'])

unique, seen = [], set()
for item in aggregated_sorted:
    key = (item['content'], item['source'])
    if key not in seen:
        seen.add(key); unique.append(item)
    if len(unique) == top_k:
        break
return unique
```
复杂度 **O(N·log N)**，其中 N = 激活 KB 数 × top_k，每次检索仅扫描向量库的 ANN 结果，性能与单库相当。

### 优势分析
1. **隐私保护**：本地私密 KB 不上云，满足涉密教材要求。
2. **横向扩展**：任意新增 KB 只需新增路径；系统自动初始化目录与向量库，无需重启。
3. **维度兼容**：不同 KB 可针对不同课程、不同 embedding 维度独立存储，`RAGService` 在查询时自动适配。
4. **教学灵活性**：教师可为不同学期或协同教师创建独立 KB，再用联合模式“一站式”检索。

### 与现有工作的比较
我们查阅现有开源 RAG 框架（LangChain、LLamaIndex 等），均默认单知识库或通过“合并文档”方式实现多库查询，缺乏：
• 路由级动态切换 + 多路径持久化；
• 针对教师权限粒度的本地/服务器隔离；
• 内置 dedup + 距离排序聚合算法。

而本方案在 **本地私密存储 + 多库聚合检索** 场景下具备独创性，特别适用于高校具有强隐私与协作并存需求的教学场景。

### 使用说明

1. 教师点击"私密知识库设置界面"
   ![alt text](image-11.png)
2. 输入一个本地目录路径为知识库保存路径，顶部将显示当前使用的知识库的路径，（当前使用知识库更新后，所有的知识库检索、更新都会在当前使用知识库当中进行）。同时使用过的路径将记录在历史路径中。可以为知识库起别名方便辨识区分，也可以编辑、删除、一键复制历史路径。
   ![alt text](image-12.png)
   ![alt text](image-13.png)
3. 点击上传资料到当前知识库，知识库将保存在本地的目录下。
   ![alt text](image-14.png)
4. 如果想同时使用多个知识库聚集，可以设置联合知识库并点击保存设置。
   ![alt text](image-15.png)
5. 后续上传文件、检索等操作将同步在多个本地知识库进行
   ![alt text](image-16.png)