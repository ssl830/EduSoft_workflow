import subprocess
import json
import sys
import time
from datetime import datetime

# 所有接口的配置（极简请求体版）
ENDPOINTS = [
    {
        "name": "健康检查",
        "url": "http://localhost:8000/health",
        "method": "GET",
        "headers": {},
        "data": None
    },
    {
        "name": "生成教学内容详情",
        "url": "http://localhost:8000/rag/detail",
        "method": "POST",
        "headers": {"Content-Type": "application/json"},
        "data": {
            "title": "概述",
            "knowledgePoints": ["定义"],
            "practiceContent": "简述",
            "teachingGuidance": "说明",
            "timePlan": [
                {"content": "介绍", "minutes": 1, "step": "导入"}
            ]
        }
    },
    {
        "name": "生成练习题",
        "url": "http://localhost:8000/rag/generate_exercise",
        "method": "POST",
        "headers": {"Content-Type": "application/json"},
        "data": {
            "course_name": "软工",
            "lesson_content": "简述",
            "difficulty": "easy",
            "choose_count": 1,
            "fill_blank_count": 0,
            "question_count": 1
        }
    },
    {
        "name": "评估主观题答案",
        "url": "http://localhost:8000/rag/evaluate_subjective",
        "method": "POST",
        "headers": {"Content-Type": "application/json"},
        "data": {
            "question": "定义?",
            "student_answer": "答案",
            "reference_answer": "答案",
            "max_score": 1
        }
    },
    {
        "name": "分析练习整体情况",
        "url": "http://localhost:8000/rag/analyze_exercise",
        "method": "POST",
        "headers": {"Content-Type": "application/json"},
        "data": {
            "exercise_questions": [
                {
                    "content": "题目?",
                    "error_rate": 0.0,
                    "type": "single_choice"
                }
            ]
        }
    },
    {
        "name": "在线学习助手",
        "url": "http://localhost:8000/rag/assistant",
        "method": "POST",
        "headers": {"Content-Type": "application/json"},
        "data": {
            "question": "什么?"
        }
    },
    {
        "name": "生成学生自测练习",
        "url": "http://localhost:8000/rag/generate_student_exercise",
        "method": "POST",
        "headers": {"Content-Type": "application/json"},
        "data": {
            "requirements": "1题",
            "knowledge_preferences": "概述"
        }
    },
    {
        "name": "生成课程优化建议",
        "url": "http://localhost:8000/rag/optimize_course",
        "method": "POST",
        "headers": {"Content-Type": "application/json"},
        "data": {
            "courseName": "软工",
            "sectionName": "概述",
            "averageScore": 1,
            "errorRate": 0.0,
            "studentCount": 1
        }
    }
]

REPEAT = 5  # 每个接口测5次

timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
output_file = f"curl_benchmark_{timestamp}.txt"

all_results = []  # 存储每个接口的详细结果

print(f"将对每个接口测试 {REPEAT} 次，统计平均/最小/最大响应时间...\n")
with open(output_file, "w", encoding="utf-8") as f:
    f.write(f"curl接口性能测试报告\n测试时间: {timestamp}\n每个接口测试{REPEAT}次\n\n")
    summary_table = []
    for ep in ENDPOINTS:
        times = []
        header = f"接口: {ep['name']} ({ep['url']})"
        print(header)
        f.write(header + "\n")
        for i in range(REPEAT):
            if ep["method"] == "GET":
                curl_cmd = [
                    "curl", "-w", "%{time_total}", "-s", "-o", "NUL" if sys.platform.startswith("win") else "/dev/null", ep["url"]
                ]
            else:
                data_str = json.dumps(ep["data"], ensure_ascii=False)
                curl_cmd = [
                    "curl", "-w", "%{time_total}", "-s", "-o", "NUL" if sys.platform.startswith("win") else "/dev/null",
                    "-X", ep["method"], ep["url"]
                ]
                for k, v in ep["headers"].items():
                    curl_cmd += ["-H", f"{k}: {v}"]
                curl_cmd += ["-d", data_str]
            try:
                t0 = time.time()
                result = subprocess.run(curl_cmd, capture_output=True, text=True, timeout=120)
                t1 = time.time()
                curl_time = None
                try:
                    curl_time = float(result.stdout.strip())
                except Exception:
                    curl_time = t1 - t0
                times.append(curl_time)
                line = f"  第{i+1}次: {curl_time:.3f} 秒"
                print(line)
                f.write(line + "\n")
            except Exception as e:
                line = f"  第{i+1}次: 发生错误: {e}"
                print(line)
                f.write(line + "\n")
        if times:
            avg = sum(times) / len(times)
            minv = min(times)
            maxv = max(times)
            avg_line = f"  平均响应时间: {avg:.3f} 秒  最小: {minv:.3f} 秒  最大: {maxv:.3f} 秒\n"
            print(avg_line)
            f.write(avg_line)
            summary_table.append((ep['name'], avg, minv, maxv))
        else:
            print("  无有效结果\n")
            f.write("  无有效结果\n")
        all_results.append({"name": ep['name'], "times": times})

    # 输出总览表格
    f.write("\n==================== 总览表 ====================\n")
    f.write("| 接口名称 | 平均响应时间(s) | 最小(s) | 最大(s) |\n")
    f.write("|----------|----------------|--------|--------|\n")
    print("\n==================== 总览表 ====================")
    print("| 接口名称 | 平均响应时间(s) | 最小(s) | 最大(s) |")
    print("|----------|----------------|--------|--------|")
    for name, avg, minv, maxv in summary_table:
        line = f"| {name} | {avg:.3f} | {minv:.3f} | {maxv:.3f} |"
        f.write(line + "\n")
        print(line)

    # 输出详细分接口的每次耗时
    f.write("\n==================== 详细分接口耗时 ====================\n")
    for res in all_results:
        f.write(f"\n{res['name']}\n")
        for idx, t in enumerate(res['times']):
            f.write(f"  第{idx+1}次: {t:.3f} 秒\n")

print(f"\n已将结果保存到: {output_file}") 