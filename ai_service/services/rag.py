"""
RAG (Retrieval-Augmented Generation) 服务
用于基于本地知识库生成教学内容、习题等
"""
import os
import json
from typing import List, Dict, Optional, Any
from openai import OpenAI
from dotenv import load_dotenv
from .embedding import EmbeddingService
from .faiss_db import FAISSDatabase
from .prompts import PromptTemplates
from utils.logger import rag_logger as logger
from pydantic import BaseModel
import numpy as np
import re
import shutil

load_dotenv()
class TimePlanItem(BaseModel):
    content: str
    minutes: int
    step: str

class RAGService:
    """RAG服务类"""

    def __init__(self, embedding_service=None, vector_db=None):
        """初始化服务"""
        self.embedding_service = embedding_service or EmbeddingService()
        self.vector_db = vector_db or FAISSDatabase(dim=self.embedding_service.dimensions)
        self.client = OpenAI(
            api_key=os.getenv("DEEPSEEK_API_KEY"),
            base_url="https://api.deepseek.com/v1"
        )

        # 加载已有的知识库（自动检测维度是否一致）
        vector_db_path = 'data/vector_db'
        if os.path.exists(vector_db_path):
            try:
                self.vector_db.load(vector_db_path)
                # 检查索引维度
                if hasattr(self.vector_db, "index") and hasattr(self.vector_db.index, "d"):
                    index_dim = self.vector_db.index.d
                    if index_dim != self.embedding_service.dimensions:
                        logger.warning(
                            f"FAISS索引维度({index_dim})与当前embedding维度({self.embedding_service.dimensions})不一致，"
                            f"将自动重建索引并清空原有向量库。"
                        )
                        # 删除旧索引文件夹或文件
                        if os.path.isdir(vector_db_path):
                            shutil.rmtree(vector_db_path)
                        else:
                            os.remove(vector_db_path)
                        # 重新初始化空索引
                        self.vector_db = FAISSDatabase(dim=self.embedding_service.dimensions)
                        # 清空内容缓存，防止内容与索引不一致
                        if hasattr(self.vector_db, "contents"):
                            self.vector_db.contents.clear()
                        if hasattr(self.vector_db, "sources"):
                            self.vector_db.sources.clear()
                logger.info("Successfully loaded existing vector database")
            except Exception as e:
                logger.error(f"Error loading vector database: {str(e)}")
                # 若加载失败也初始化空索引并清空内容缓存
                self.vector_db = FAISSDatabase(dim=self.embedding_service.dimensions)
                if hasattr(self.vector_db, "contents"):
                    self.vector_db.contents.clear()
                if hasattr(self.vector_db, "sources"):
                    self.vector_db.sources.clear()

    def add_to_knowledge_base(self, chunks: List[Dict[str, str]]):
        """将文档添加到知识库"""
        try:
            embeddings, contents, sources = self.embedding_service.get_chunks_embeddings(chunks)
            self.vector_db.add_embeddings(embeddings, contents, sources)
            self.vector_db.save('data/vector_db')
            logger.info(f"Successfully added {len(chunks)} chunks to knowledge base")
        except Exception as e:
            import traceback
            logger.error(f"Error adding chunks to knowledge base: {str(e)}\n{traceback.format_exc()}")
            raise

    def search_knowledge_base(self, query: str, top_k: int = 5) -> List[Dict[str, str]]:
        """搜索知识库，将query编码，查询最相关的top_k个文本块"""
        try:
            # 若知识库为空，直接返回空列表
            if not hasattr(self.vector_db, "contents") or not self.vector_db.contents:
                logger.warning("知识库为空，search_knowledge_base直接返回空列表")
                return []
            query_embedding = self.embedding_service.get_embedding(query)
            # 如果返回的是 list，转为 np.ndarray
            if isinstance(query_embedding, list):
                import numpy as np
                query_embedding = np.array(query_embedding, dtype=np.float32)
            results = self.vector_db.search(query_embedding, top_k)
            logger.info(f"Successfully searched knowledge base for query: {query}")
            return results
        except Exception as e:
            import traceback
            logger.error(f"Error searching knowledge base: {str(e)}\n{traceback.format_exc()}")
            raise

    def safe_json_loads(self, text):
        """
        尝试从文本中提取并解析 JSON。
        """
        try:
            return json.loads(text)
        except Exception as e1:
            try:
                # 尝试用正则提取最外层 JSON 对象
                match = re.search(r'(\{[\s\S]*\})', text)
                if match:
                    json_str = match.group(1)
                    # 去除常见的格式化占位符（如 ... 或 '）
                    json_str = re.sub(r'\\?"[^"]*\\?": ?"[^"]*\.{2,}[^"]*"', '"key": "value"', json_str)
                    return json.loads(json_str)
            except Exception as e2:
                pass
            raise ValueError(f"JSON解析失败，原始内容：{text[:200]}... 错误信息: {e1}")

    def generate_teaching_content(self, course_outline: str, course_name: str, expected_hours: int, constraints: Optional[str] = None) -> Dict:
        """根据课程大纲生成教学内容 (支持 constraints)"""
        try:
            # 1. 调用大模型从大纲中提取关键知识点
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
            logger.info("start generate teaching content\n")
            prompt = PromptTemplates.get_teaching_content_generation_prompt(
                course_name=course_name,
                course_outline=course_outline,
                expected_hours=expected_hours,
                knowledge_base=knowledge_base,
                constraints=constraints
            )
            logger.info("test1\n");
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            logger.info("test2\n")
            content = response.choices[0].message.content
            logger.info("大模型原始返回内容: %s", content)
            try:
                logger.info(f"准备进入 safe_json_loads 解析内容")
                result = self.safe_json_loads(content)
                logger.info(f"safe_json_loads 解析成功，结果类型: {type(result)}")
                # 检查并适配新结构
                lessons = result.get('lessons', [])
                for lesson in lessons:
                    # timePlan 字段应为详细的时间分配列表
                    time_plan = lesson.get('timePlan', [])
                    logger.info(f"课时: {lesson.get('title', '')}，时间分配: {time_plan}")
                    # 其它字段如 knowledgePoints、practiceContent、teachingGuidance 也做日志
                    logger.info(f"知识点: {lesson.get('knowledgePoints', [])}")
                    logger.info(f"实训练习: {lesson.get('practiceContent', '')}")
                    logger.info(f"教学指导: {lesson.get('teachingGuidance', '')}")
            except Exception as e:
                logger.error(f"JSON解析失败，原始内容：{content[:200]}... 错误信息: {e}")
                raise
            logger.info("Successfully generated teaching content")
            return result
        except Exception as e:
            logger.error(f"Error generating teaching content: {str(e)}")
            raise

    def generate_teaching_content_detail(self, title: str, knowledgePoints: List[str], practiceContent: str, teachingGuidance: str, timePlan: List[TimePlanItem], constraints: str | None = None) -> Dict:
        """根据已有教案生成详细教案（内容更丰富，但仅基于传入参数）"""
        try:
            # 构造详细教案生成的提示词
            prompt = PromptTemplates.get_teaching_content_detail_prompt(
                title=title,
                knowledgePoints=knowledgePoints,
                practiceContent=practiceContent,
                teachingGuidance=teachingGuidance,
                timePlan=timePlan,
                constraints=constraints
            )
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            content = response.choices[0].message.content
            logger.info("详细教案 LLM 返回内容: %s", content)
            result = self.safe_json_loads(content)
            logger.info("Successfully generated detailed teaching content")
            return result
        except Exception as e:
            logger.error(f"Error generating teaching content detail: {str(e)}")
            raise

    def regenerate_teaching_content_detail(self, title: str, knowledgePoints: List[str], practiceContent: str, teachingGuidance: str, timePlan: List[TimePlanItem], constraints: str | None = None) -> Dict:
        """根据已有教案生成一版全新的教案（内容充实度保持一致，无需更丰富）"""
        try:
            prompt = PromptTemplates.get_regenerate_teaching_content_prompt(
                title=title,
                knowledgePoints=knowledgePoints,
                practiceContent=practiceContent,
                teachingGuidance=teachingGuidance,
                timePlan=timePlan,
                constraints=constraints
            )
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            content = response.choices[0].message.content
            logger.info("全新教案 LLM 返回内容: %s", content)
            result = self.safe_json_loads(content)
            logger.info("Successfully regenerated teaching content detail")
            return result
        except Exception as e:
            logger.error(f"Error regenerating teaching content detail: {str(e)}")
            raise

    def generate_exercises(self, course_name: str, lesson_content: str, difficulty: str = "medium", choose_count: int = 5, fill_blank_count: int = 5, question_count: int = 2, custom_types: dict = None) -> Dict:
        """生成练习题"""
        try:
            prompt = PromptTemplates.get_exercise_generation_prompt(
                course_name=course_name,
                lesson_content=lesson_content,
                difficulty=difficulty,
                choose_count=choose_count,
                fill_blank_count=fill_blank_count,
                question_count=question_count,
                custom_types=custom_types
            )
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            content = response.choices[0].message.content
            logger.info(f"LLM raw output:\n{content}")
            result = self.safe_json_loads(content)
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

    def evaluate_subjective_answer(self, question: str, student_answer: str, reference_answer: str, max_score: float) -> Dict:
        """评估主观题答案"""
        try:
            # 1. 获取相关知识点的内容
            relevant_docs = self.search_knowledge_base(question)

            # 2. 生成评估提示
            prompt = PromptTemplates.get_subjective_answer_evaluation_prompt(
                question=question,
                student_answer=student_answer,
                reference_answer=reference_answer,
                max_score=max_score
            )

            # 3. 调用大模型进行评估
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )

            # 4. 解析结果
            content = response.choices[0].message.content
            result = self.safe_json_loads(content)

            # 5. 添加知识点位置信息
            if relevant_docs:
                result['knowledge_context'] = relevant_docs

            logger.info("Successfully evaluated subjective answer")
            return result

        except Exception as e:
            logger.error(f"Error evaluating subjective answer: {str(e)}")
            raise

    def analyze_exercise(self, exercise_questions: List[Dict]) -> Dict:
        """分析练习整体情况"""
        try:
            # 1. 提取所有题目的知识点
            all_knowledge_points = []
            for question in exercise_questions:
                relevant_docs = self.search_knowledge_base(question.content)
                all_knowledge_points.extend(relevant_docs)
            # 2. 对象转字典，便于序列化
            exercise_questions_dict = [vars(q) for q in exercise_questions]
            # 3. 生成分析提示
            prompt = PromptTemplates.get_exercise_analysis_prompt(exercise_questions_dict)

            # 4. 调用大模型进行分析
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )

            # 5. 解析结果
            content = response.choices[0].message.content
            result = self.safe_json_loads(content)

            # 6. 添加知识点上下文
            result['knowledge_context'] = all_knowledge_points

            logger.info("Successfully analyzed exercise")
            return result

        except Exception as e:
            logger.error(f"Error analyzing exercise: {str(e)}")
            raise

    def answer_student_question(self, question: str, course_name: Optional[str] = None, chat_history: Optional[List[Dict[str, str]]] = None, top_k: int = 5) -> Dict:
        """在线学习助手 - 回答学生问题

        参数:
            question: 学生提出的问题
            course_name: 课程名称，可选，用于日志或过滤资料
            chat_history: 对话上下文，可选，列表元素格式同 OpenAI messages
            top_k: 检索的文档数量
        返回:
            {{"answer": str, "references": List[Dict], "knowledgePoints": List[str]}}
        """
        try:
            # 1. 检索相关知识库内容
            relevant_docs = self.search_knowledge_base(question, top_k=top_k)

            # 2. 构造提示词
            prompt = PromptTemplates.get_online_assistant_prompt(
                question=question,
                relevant_docs=relevant_docs,
                course_name=course_name or "",
                chat_history=chat_history
            )

            # 3. 调用大模型生成回答
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            content = response.choices[0].message.content
            logger.info("LLM 返回内容: %s", content)

            # 4. 解析JSON
            result = self.safe_json_loads(content)

            # 5. 如果需要，补充引用材料原始信息
            if "references" in result and isinstance(result["references"], list):
                # 可能模型返回的引用仅含摘要，确保包含source
                enriched_refs = []
                for ref in result["references"]:
                    source = ref.get("source")
                    # 查找原 doc content
                    match_doc = next((d for d in relevant_docs if d["source"] == source), None)
                    if match_doc:
                        ref.setdefault("content", match_doc["content"])
                    enriched_refs.append(ref)
                result["references"] = enriched_refs

            logger.info("Successfully answered student question")
            return result
        except Exception as e:
            logger.error(f"Error answering student question: {str(e)}")
            raise


    def generate_student_exercise(self, requirements: str, knowledge_preferences: str, wrong_questions: Optional[List[Dict[str, Any]]] = None) -> Dict:
        """根据学生需求、知识点偏好以及历史错题生成练习题"""
        try:
            prompt = PromptTemplates.get_student_exercise_generation_prompt(
                requirements=requirements,
                knowledge_preferences=knowledge_preferences,
                wrong_questions=wrong_questions or []
            )

            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )

            content = response.choices[0].message.content
            logger.info(f"LLM raw output for student exercise:\n{content}")

            result = self.safe_json_loads(content)
            logger.info("Successfully generated student exercise")
            return result
        except Exception as e:
            logger.error(f"Error generating student exercise: {str(e)}")
            raise

    def generate_course_optimization(
        self,
        course_name: str,
        section_name: str,
        average_score: float,
        error_rate: float,
        student_count: int
    ) -> Dict:
        """生成课程优化建议"""
        try:
            # 1. 检索相关知识库内容
            query = f"{course_name} {section_name}"
            relevant_docs = self.search_knowledge_base(query)

            # 2. 生成优化建议
            prompt = PromptTemplates.get_course_optimization_prompt(
                course_name=course_name,
                section_name=section_name,
                average_score=average_score,
                error_rate=error_rate,
                student_count=student_count,
                relevant_docs=relevant_docs
            )

            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )

            content = response.choices[0].message.content
            result = self.safe_json_loads(content)
            logger.info("Successfully generated course optimization suggestions")
            return result

        except Exception as e:
            logger.error(f"Error generating course optimization suggestions: {str(e)}")
            raise

    def revise_teaching_content(self, original_plan: Dict, feedback: str) -> Dict:
        """根据教师反馈修改教学大纲"""
        try:
            prompt = PromptTemplates.get_teaching_content_feedback_prompt(original_plan, feedback)
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            content = response.choices[0].message.content
            logger.info("教学大纲反馈 LLM 返回内容: %s", content)
            result = self.safe_json_loads(content)
            logger.info("Successfully revised teaching content according to feedback")
            return result
        except Exception as e:
            logger.error(f"Error revising teaching content: {str(e)}")
            raise

    def generate_step_detail(self, lesson_title: str, step_name: str, current_content: str, knowledge_points: List[str]) -> Dict:
        """生成课时中某个环节的详细内容"""
        try:
            prompt = PromptTemplates.get_step_detail_prompt(
                lesson_title=lesson_title,
                step_name=step_name,
                current_content=current_content,
                knowledge_points=knowledge_points
            )
            response = self.client.chat.completions.create(
                model="deepseek-chat",
                messages=[{"role": "user", "content": prompt}]
            )
            content = response.choices[0].message.content
            logger.info("课时环节细节 LLM 返回内容: %s", content)
            result = self.safe_json_loads(content)
            logger.info("Successfully generated step detail")
            return result
        except Exception as e:
            logger.error(f"Error generating step detail: {str(e)}")
            raise
