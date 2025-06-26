# AI微服务集成模块说明

## 一、目录结构
```
ai_service/
├── ai_service.egg-info/      # Python包元数据
├── services/                 # 核心服务模块
│   ├── doc_parser.py        # 文档解析器
│   ├── embedding.py         # DeepSeek embedding实现
│   ├── faiss_db.py         # FAISS向量库操作
│   ├── prompts.py          # 提示词模板
│   ├── rag.py              # RAG问答实现
│   └── storage.py          # 存储服务
├── utils/
│   └── logger.py           # 日志工具
├── venv/                    # Python虚拟环境
├── init.py                 # 初始化配置
├── main.py                 # FastAPI主入口
├── requirements.txt        # Python依赖
└── setup.py               # 包安装配置
```

## 二、Java后端涉及文件
- `src/main/java/org/example/edusoft/ai/AIServiceClient.java`：AI服务HTTP调用工具类
- 业务相关Controller/Service中集成AIServiceClient

## 三、Python服务核心模块说明
- `main.py`：FastAPI主入口，定义/embedding/upload、/rag/answer、/rag/generate_exercise等接口
- `services/embedding.py`：DeepSeek embedding API调用实现
- `services/faiss_db.py`：FAISS向量库操作与管理
- `services/rag.py`：RAG问答与练习题生成实现
- `services/doc_parser.py`：支持PDF、Word、Excel、Markdown等多格式文档解析
- `services/prompts.py`：LLM提示词模板管理
- `services/storage.py`：文件存储服务
- `utils/logger.py`：统一日志记录

## 四、主要接口
- `POST /embedding/upload`：上传资料并自动分块、embedding、入库
- `POST /rag/answer`：RAG智能问答，参数question
- `POST /rag/generate_exercise`：自动生成练习题与答案，参数course_outline

## 五、依赖说明
### Java端
- SpringBoot
- RestTemplate/WebClient

### Python端
- Web框架：FastAPI、uvicorn
- 文档处理：pdfplumber、python-docx、openpyxl、beautifulsoup4、markdown
- AI/ML：numpy、pandas、scikit-learn、faiss-cpu
- HTTP客户端：httpx
- 工具库：python-multipart、python-dotenv、python-jose、loguru、tenacity
- DeepSeek API Key
- FAISS本地存储

## 六、实现说明
- SpringBoot负责所有业务和AI服务转发，AI能力由Python微服务实现
- 资料上传后自动调用/embedding/upload，问答和生成练习分别调用/rag/answer和/generate_exercise
- 前端接口保持不变，由SpringBoot统一转发
- 支持多种文档格式解析和处理
- 使用FAISS进行高效向量检索
- 统一的日志记录和错误处理

## 七、后续扩展
- 支持多课程知识库隔离、异步任务、权限校验等
- Python服务可独立扩展embedding/大模型API
- 优化文档解析和向量检索性能
- 增强错误处理和重试机制

## 八、课件上传自动同步AI知识库集成说明
- 教师通过前端上传课件时，SpringBoot后端`TeachingResourceController`的`/api/resources/upload`接口会调用`TeachingResourceService.uploadResource`完成课件上传。
- 上传成功后，自动调用`syncToAIKnowledgeBase`方法，通过`AIServiceClient`将课件文件POST到Python AI微服务的`/embedding/upload`接口。
- AI微服务自动完成内容提取、embedding、向量入库，知识库实时更新。
- 日志记录同步结果，便于后续排查。 