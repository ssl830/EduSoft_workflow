# 练习记录模块说明文档

## 模块结构

该模块在以下目录中实现了相关功能：

```
src/main/java/org/example/edusoft/
├── entity/record/
│   ├── PracticeRecord.java      # 练习记录实体类
│   ├── StudyRecord.java         # 学习记录实体类
│   └── QuestionRecord.java      # 题目记录实体类
├── mapper/record/
│   └── PracticeRecordMapper.java # 记录数据访问接口
├── service/record/
│   ├── RecordService.java       # 服务接口
│   └── impl/
│       └── RecordServiceImpl.java # 服务实现类
└── controller/record/
    └── RecordController.java    # 控制器
```

## 功能说明

### 1. 实体类
#### PracticeRecord.java
- 练习记录实体类，包含练习基本信息、得分、反馈等
#### StudyRecord.java
- 学习记录实体类，记录学习进度和完成情况
#### QuestionRecord.java
- 题目记录实体类，包含题目内容、答案、得分等

### 2. 控制器（RecordController.java）
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| GET | /api/record/study | 获取所有学习记录 |
| GET | /api/record/study/course/{courseId} | 获取指定课程的学习记录 |
| GET | /api/record/practice | 获取所有练习记录 |
| GET | /api/record/practice/course/{courseId} | 获取指定课程的练习记录 |
| GET | /api/record/submission/{submissionId}/report | 获取练习提交报告 |
| GET | /api/record/study/export | 导出所有学习记录 |
| GET | /api/record/study/course/{courseId}/export | 导出指定课程的学习记录 |
| GET | /api/record/practice/export | 导出所有练习记录 |
| GET | /api/record/practice/course/{courseId}/export | 导出指定课程的练习记录 |
| GET | /api/record/submission/{submissionId}/export-report | 导出练习提交报告 |

## 前端接口说明

### 1. 获取所有学习记录
- 请求方式：GET
- 路径：/api/record/study
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "courseId": 1,
            "courseName": "软件工程基础",
            "sectionId": 1,
            "sectionTitle": "第一章：软件工程导论",
            "completed": true,
            "completedAt": "2025-05-25T10:00:00"
        }
    ]
}
```

### 2. 获取指定课程的学习记录
- 请求方式：GET
- 路径：/api/record/study/course/{courseId}
- 请求头：需要登录token
- 返回：同上

### 3. 获取所有练习记录
- 请求方式：GET
- 路径：/api/record/practice
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "practiceId": 1,
            "practiceTitle": "第一章练习",
            "courseName": "软件工程基础",
            "className": "软工A班",
            "submittedAt": "2025-05-25T10:00:00",
            "score": 85,
            "feedback": "完成得很好",
            "questions": [
                {
                    "id": 1,
                    "content": "软件工程的第一步是什么？",
                    "type": "singlechoice",
                    "options": ["需求分析", "编码", "测试", "部署"],
                    "studentAnswer": "需求分析",
                    "correctAnswer": "需求分析",
                    "isCorrect": true,
                    "score": 5
                }
            ]
        }
    ]
}
```

### 4. 获取指定课程的练习记录
- 请求方式：GET
- 路径：/api/record/practice/course/{courseId}
- 请求头：需要登录token
- 返回：同上

### 5. 获取练习提交报告
- 请求方式：GET
- 路径：/api/record/submission/{submissionId}/report
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "submission": {
            "id": 1,
            "practiceId": 1,
            "practiceTitle": "第一章练习",
            "courseName": "软件工程基础",
            "className": "软工A班",
            "submittedAt": "2025-05-25T10:00:00",
            "score": 85,
            "feedback": "完成得很好"
        },
        "questions": [
            {
                "id": 1,
                "content": "软件工程的第一步是什么？",
                "type": "singlechoice",
                "options": ["需求分析", "编码", "测试", "部署"],
                "studentAnswer": "需求分析",
                "correctAnswer": "需求分析",
                "isCorrect": true,
                "score": 5,
                "analysis": "需求分析是软件工程的第一步，它帮助确定系统的功能和约束"
            }
        ],
        "rank": 5,
        "totalStudents": 30,
        "scoreDistribution": [
            {
                "score_range": "80-89",
                "count": 10,
                "percentage": 33.33
            }
        ]
    }
}
```

### 6. 导出功能
所有导出接口（/api/record/*/export）都返回文件下载，需要在前端处理文件下载逻辑。

- 学习记录导出：返回 Excel 文件
- 练习记录导出：返回 Excel 文件
- 练习报告导出：返回 PDF 文件

注意事项：
1. 所有接口都需要用户登录
2. 导出接口需要处理文件下载
3. 如果未登录，所有接口都会返回错误信息
4. 如果发生错误，接口会返回相应的错误信息和状态码 