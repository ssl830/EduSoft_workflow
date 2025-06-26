"""
提示词管理模块
"""
from typing import List, Dict

class PromptTemplates:
    """提示词模板管理"""
    
    @staticmethod
    def get_knowledge_points_extraction_prompt(course_name: str, course_outline: str) -> str:
        """提取知识点的提示词"""
        return f"""
        作为一名经验丰富的教育专家，请分析以下课程大纲，提取关键知识点：
        
        课程名称：{course_name}
        课程大纲：
        {course_outline}
        
        请列出所有关键知识点，要求：
        1. 每个知识点独占一行
        2. 知识点应该具体且可操作
        3. 知识点的粒度要适中，既不要太大也不要太小
        4. 知识点之间应该有逻辑关联
        5. 确保覆盖大纲中的所有重要内容
        """

    @staticmethod
    def get_teaching_content_generation_prompt(
        course_name: str,
        course_outline: str,
        expected_hours: int,
        knowledge_base: Dict[str, List[Dict[str, str]]]
    ) -> str:
        """生成教学内容的提示词"""
        return f"""
        作为一名资深教育专家，请根据以下信息生成详细的教学内容：
        
        课程名称：{course_name}
        预期课时：{expected_hours}
        课程大纲：
        {course_outline}
        
        参考资料：
        {PromptTemplates._format_knowledge_base(knowledge_base)}
        
        请生成一个完整的教学方案，包含以下内容：
        
        1. 课时规划：
           - 将内容合理分配到{expected_hours}个课时
           - 每个课时的时长应该合适，通常为45-90分钟
           - 课时安排要循序渐进，难度逐步提升
        
        2. 每个课时必须包含：
           - 标题：简洁清晰地概括本课时内容
           - 知识点讲解：详细的教学内容，包括概念解释、原理分析、案例说明等
           - 实训练习：针对本课时内容设计的实践任务，帮助学生巩固所学知识
           - 教学指导建议：包括教学方法、重难点提示、可能的学生疑问及解答等
        
        3. 整体教学建议：
           - 总体时间分配策略
           - 教学方法和教具使用建议
           - 学生参与度提升建议
           - 课程难点突破建议
        
        请以JSON格式返回，结构如下：
        {
            "lessons": [
                {
                    "title": "课时标题",
                    "content": "知识点讲解内容",
                    "practiceContent": "实训练习内容",
                    "teachingGuidance": "教学指导建议",
                    "suggestedHours": "建议课时数",
                    "knowledgeSources": ["知识点来源1", "知识点来源2"]
                }
            ],
            "totalHours": "总课时数",
            "timeDistribution": "时间分配建议",
            "teachingAdvice": "整体教学建议"
        }
        
        注意事项：
        1. 内容要具体且可操作，避免空泛的描述
        2. 充分利用参考资料中的内容，确保生成的内容与资料保持一致
        3. 实训练习要贴近实际，难度适中
        4. 教学指导要具体实用，便于教师参考
        5. 确保总课时数与预期课时相符
        """

    @staticmethod
    def get_exercise_generation_prompt(
        course_name: str,
        lesson_content: str,
        difficulty: str = "medium"
    ) -> str:
        """生成练习题的提示词"""
        return f"""
        作为一名专业的教育测评专家，请根据以下课程内容生成练习题：
        
        课程名称：{course_name}
        课程内容：
        {lesson_content}
        难度要求：{difficulty}
        
        请生成多种类型的练习题，包括：
        1. 概念理解题
        2. 应用实践题
        3. 分析思考题
        4. 综合案例题
        
        每道题目必须包含：
        1. 题目描述
        2. 参考答案
        3. 解题思路
        4. 相关知识点
        
        请以JSON格式返回，结构如下：
        {
            "exercises": [
                {
                    "type": "题目类型",
                    "question": "题目描述",
                    "answer": "参考答案",
                    "explanation": "解题思路",
                    "knowledge_points": ["相关知识点1", "相关知识点2"]
                }
            ]
        }
        """

    @staticmethod
    def get_answer_evaluation_prompt(
        question: str,
        student_answer: str,
        reference_answer: str
    ) -> str:
        """评估学生答案的提示词"""
        return f"""
        作为一名经验丰富的教育评估专家，请评估学生的答案：
        
        题目：
        {question}
        
        标准答案：
        {reference_answer}
        
        学生答案：
        {student_answer}
        
        请提供以下评估内容：
        1. 得分（0-100分）
        2. 答案正确性分析
        3. 思路完整性分析
        4. 表达准确性分析
        5. 具体改进建议
        
        请以JSON格式返回，结构如下：
        {
            "score": "得分",
            "correctness": "正确性分析",
            "completeness": "完整性分析",
            "accuracy": "准确性分析",
            "suggestions": ["改进建议1", "改进建议2"]
        }
        """

    @staticmethod
    def _format_knowledge_base(knowledge_base: Dict[str, List[Dict[str, str]]]) -> str:
        """格式化知识库内容"""
        formatted = []
        for point, docs in knowledge_base.items():
            formatted.append(f"知识点：{point}")
            for doc in docs:
                formatted.append(f"- 来源：{doc['source']}")
                formatted.append(f"  内容：{doc['content']}\n")
        return "\n".join(formatted) 