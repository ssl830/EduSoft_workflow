"""
RAG (Retrieval-Augmented Generation) 服务
用于基于本地知识库生成教学内容
"""
import os
import json
from typing import List, Dict
from openai import OpenAI
from dotenv import load_dotenv
from .embedding import EmbeddingService
from .faiss_db import FAISSDatabase
from .prompts import PromptTemplates
from utils.logger import rag_logger as logger
import numpy as np

load_dotenv()

class RAGService:
    """RAG服务类"""
    
    def __init__(self):
        """初始化服务"""
        self.embedding_service = EmbeddingService()
        self.vector_db = FAISSDatabase()
        self.client = OpenAI(
            api_key=os.getenv("DEEPSEEK_API_KEY"),
            base_url="https://api.deepseek.com/v1"
        )
        
        # 加载已有的知识库
        if os.path.exists('data/vector_db'):
            try:
                self.vector_db.load('data/vector_db')
                logger.info("Successfully loaded existing vector database")
            except Exception as e:
                logger.error(f"Error loading vector database: {str(e)}")
                raise
            
    def add_to_knowledge_base(self, chunks: List[Dict[str, str]]):
        """将文档添加到知识库"""
        try:
            embeddings, contents, sources = self.embedding_service.get_chunks_embeddings(chunks)
            self.vector_db.add_embeddings(embeddings, contents, sources)
            self.vector_db.save('data/vector_db')
            logger.info(f"Successfully added {len(chunks)} chunks to knowledge base")
        except Exception as e:
            logger.error(f"Error adding chunks to knowledge base: {str(e)}")
            raise
        
    def search_knowledge_base(self, query: str, top_k: int = 5) -> List[Dict[str, str]]:
        """搜索知识库"""
        try:
            query_embedding = self.embedding_service.get_embedding(query)
            # 如果返回的是 list，转为 np.ndarray
            if isinstance(query_embedding, list):
                query_embedding = np.array(query_embedding, dtype=np.float32)
            results = self.vector_db.search(query_embedding, top_k)
            logger.info(f"Successfully searched knowledge base for query: {query}")
            return results
        except Exception as e:
            logger.error(f"Error searching knowledge base: {str(e)}")
            raise
        
    def generate_teaching_content(self, course_outline: str, course_name: str, expected_hours: int) -> Dict:
        """根据课程大纲生成教学内容"""
        try:
            # 1. 从大纲中提取关键知识点
            prompt = PromptTemplates.get_knowledge_points_extraction_prompt(
                course_name=course_name,
                course_outline=course_outline
            )
            
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            knowledge_points = [k.strip() for k in response.choices[0].message.content.strip().split('\n') if k.strip()]
            logger.info(f"Extracted {len(knowledge_points)} knowledge points")
            
            # 2. 为每个知识点检索相关内容
            knowledge_base = {}
            for point in knowledge_points:
                if not point:
                    logger.warning(f"跳过空知识点")
                    continue
                try:
                    relevant_docs = self.search_knowledge_base(point)
                except Exception as e:
                    logger.error(f"知识点 '{point}' 检索失败: {e}")
                    relevant_docs = []
                knowledge_base[point] = relevant_docs
            
            # 3. 生成教学内容
            prompt = PromptTemplates.get_teaching_content_generation_prompt(
                course_name=course_name,
                course_outline=course_outline,
                expected_hours=expected_hours,
                knowledge_base=knowledge_base
            )
            
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            
            content = response.choices[0].message.content
            result = json.loads(content)
            logger.info("Successfully generated teaching content")
            return result
            
        except Exception as e:
            logger.error(f"Error generating teaching content: {str(e)}")
            raise
            
    def generate_exercises(self, course_name: str, lesson_content: str, difficulty: str = "medium") -> Dict:
        """生成练习题"""
        try:
            prompt = PromptTemplates.get_exercise_generation_prompt(
                course_name=course_name,
                lesson_content=lesson_content,
                difficulty=difficulty
            )
            
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            
            content = response.choices[0].message.content
            result = json.loads(content)
            logger.info(f"Successfully generated exercises for {course_name}")
            return result
            
        except Exception as e:
            logger.error(f"Error generating exercises: {str(e)}")
            raise
            
    def evaluate_answer(self, question: str, student_answer: str, reference_answer: str) -> Dict:
        """评估学生答案"""
        try:
            prompt = PromptTemplates.get_answer_evaluation_prompt(
                question=question,
                student_answer=student_answer,
                reference_answer=reference_answer
            )
            
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            
            content = response.choices[0].message.content
            result = json.loads(content)
            logger.info("Successfully evaluated student answer")
            return result
            
        except Exception as e:
            logger.error(f"Error evaluating answer: {str(e)}")
            raise 