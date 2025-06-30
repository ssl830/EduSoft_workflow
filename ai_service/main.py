"""
AI服务主入口
负责初始化 FastAPI 应用、配置跨域请求、中间件、加载各个服务模块，以及定义 API 接口。
"""
import os
from typing import List, Dict, Optional
from fastapi import FastAPI, File, UploadFile, HTTPException, Form, Body
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from services.doc_parser import DocumentParser
from services.embedding import EmbeddingService
from services.faiss_db import FAISSDatabase
from services.rag import RAGService
from services.storage import StorageService
from utils.logger import api_logger as logger

app = FastAPI(title="教学内容生成服务")

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化服务
storage_service = StorageService()
doc_parser = DocumentParser()
embedding_service = EmbeddingService()
vector_db = FAISSDatabase(dim=1536)
# 如果需要加载已有数据库，可加如下代码：
# vector_db.load(storage_service.get_vector_db_path())
rag_service = RAGService()

class TeachingContentRequest(BaseModel):
    course_name: str
    course_outline: str
    expected_hours: int

class ExerciseGenerationRequest(BaseModel):
    course_name: str
    lesson_content: str
    difficulty: Optional[str] = "medium"
    choose_count: Optional[int] = 5
    fill_blank_count: Optional[int] = 5
    question_count: Optional[int] = 2
    custom_types: Optional[Dict[str, int]] = None

@app.post("/embedding/upload")
async def upload_file(
    file: UploadFile = File(...),
    course_id: Optional[str] = Form(None)
):
    """
    上传文件并处理入库
    """
    try:
        # 保存到临时目录
        temp_path = os.path.join(storage_service.get_temp_dir(), file.filename)
        content = await file.read()
        with open(temp_path, "wb") as f:
            f.write(content)
        
        try:
            # 保存文档
            doc_path = storage_service.save_document(temp_path, course_id)
            logger.info(f"Saved document to {doc_path}")
            
            # 解析文件为文本块（chunks)
            chunks = doc_parser.parse_file(doc_path)
            logger.info(f"Successfully parsed file {file.filename} into {len(chunks)} chunks")
            
            # 添加到RAG知识库
            rag_service.add_to_knowledge_base(chunks)
            logger.info(f"Successfully added {len(chunks)} chunks to knowledge base")
            
            return {
                "status": "success",
                "message": f"文件 {file.filename} 处理成功",
                "chunks_count": len(chunks),
                "file_path": doc_path
            }
            
        finally:
            # 清理临时文件
            if os.path.exists(temp_path):
                os.remove(temp_path)
            
    except Exception as e:
        logger.error(f"Error processing file {file.filename}: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/rag/generate")
async def generate_teaching_content(request: TeachingContentRequest):
    """
    生成教学内容
    """
    try:
        result = rag_service.generate_teaching_content(
            course_outline=request.course_outline,
            course_name=request.course_name,
            expected_hours=request.expected_hours
        )
        logger.info(f"Successfully generated teaching content for {request.course_name}")
        return result
    except Exception as e:
        logger.error(f"Error generating teaching content: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/rag/generate_exercise")
async def generate_exercise(request: ExerciseGenerationRequest):
    """
    生成练习题
    """
    try:
        result = rag_service.generate_exercises(
            course_name=request.course_name,
            lesson_content=request.lesson_content,
            difficulty=request.difficulty,
            choose_count=request.choose_count,
            fill_blank_count=request.fill_blank_count,
            question_count=request.question_count,
            custom_types=request.custom_types
        )
        logger.info(f"Successfully generated exercises for {request.course_name}")
        return result
    except Exception as e:
        logger.error(f"Error generating exercises: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    """
    健康检查接口
    """
    return {"status": "healthy"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000) 