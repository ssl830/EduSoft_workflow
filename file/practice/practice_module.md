# 练习批改模块说明文档

## 1. 模块概述
本模块主要实现练习的提交、自动评判和手动批改功能。包括学生提交练习答案、系统自动评判单选题、教师查看待批改列表、查看答案详情和批改主观题等功能。

## 2. 目录结构
```
src/main/java/org/example/edusoft/
├── controller/practice/
│   ├── SubmissionController.java    // 处理学生提交答案相关请求
│   └── ManualJudgeController.java   // 处理教师批改相关请求
├── service/practice/
│   ├── SubmissionService.java       // 处理提交和自动评判逻辑
│   └── ManualJudgeService.java      // 处理教师批改相关逻辑
├── mapper/practice/
│   ├── SubmissionMapper.java        // 提交记录数据库操作
│   ├── AnswerMapper.java            // 答案数据库操作
│   ├── PracticeQuestionMapper.java  // 练习题目关联操作
│   └── QuestionMapper.java          // 题目数据库操作
├── entity/practice/
│   ├── Submission.java              // 提交记录实体
│   ├── Answer.java                  // 答案实体
│   ├── Practice.java                // 练习实体
│   ├── Question.java                // 题目实体
│   └── PracticeQuestion.java        // 练习-题目关联实体
└── dto/practice/
    ├── SubmissionAnswersDTO.java    // 提交答案详情传输对象
    └── QuestionAnswerDTO.java       // 题目答案传输对象

```

## 3. API接口说明

### 3.1 提交练习答案
- **URL**: `/api/submission/submit`
- **方法**: POST
- **请求参数**:
  ```json
  {
    "practiceId": 1,
    "studentId": 1,
    "answers": ["A", "C", "B", "D", "Hello, World!"]
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "提交成功",
    "data": {
      "submissionId": 1
    }
  }
  ```

### 3.2 获取待批改列表
- **URL**: `/api/judge/pendinglist`
- **方法**: POST
- **请求参数**:
  ```json
  {
    "practiceId": 1,
    "classId": 1
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "获取成功",
    "data": [
      {
        "studentName": "张三",
        "practiceName": "第一章测试",
        "submissionId": "1"
      }
    ]
  }
  ```

### 3.3 获取提交答案详情
- **URL**: `/api/judge/pending`
- **方法**: POST
- **请求参数**:
  ```json
  {
    "submissionId": 1
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "获取成功",
    "data": [
      {
        "questionName": "编程题1",
        "answerText": "代码内容",
        "maxScore": 10,
        "studentName": "张三",
        "sortOrder": 1
      }
    ]
  }
  ```

### 3.4 批改练习
- **URL**: `/api/judge/judge`
- **方法**: POST
- **请求参数**:
  ```json
  {
    "submissionId": 1,
    "question": [
      {
        "answerText": "代码内容",
        "score": 8,
        "maxScore": 10,
        "sortOrder": 1
      }
    ]
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "批改成功",
    "data": null
  }
  ```

## 4. 数据库表说明
模块涉及以下数据库表：
- Question: 题目表
- Practice: 练习表
- PracticeQuestion: 练习-题目关联表
- Submission: 提交记录表
- Answer: 答案表

详细的表结构见 coursePlatform.sql 文件。 