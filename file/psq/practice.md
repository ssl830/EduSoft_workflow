# 在线练习模块说明文档

## 模块结构

该模块在以下目录中实现了相关功能：

```
src/main/java/org/example/edusoft/
├── entity/practice/
│   ├── Practice.java          # 练习实体类
│   ├── Question.java          # 题目实体类
│   ├── PracticeQuestion.java  # 练习-题目关联实体类
│   └── Submission.java        # 提交记录实体类
├── mapper/practice/
│   ├── PracticeMapper.java    # 练习数据访问接口
│   ├── QuestionMapper.java    # 题目数据访问接口
│   └── SubmissionMapper.java  # 提交记录数据访问接口
├── service/practice/
│   ├── PracticeService.java   # 练习服务接口
│   ├── QuestionService.java   # 题目服务接口
│   └── impl/
│       ├── PracticeServiceImpl.java
│       └── QuestionServiceImpl.java
└── controller/practice/
    ├── PracticeController.java # 练习控制器
    └── QuestionController.java # 题目控制器
```

## 功能说明

### 1. 实体类
#### Practice.java
- 对应数据库中的Practice表
- 包含练习的基本信息：标题、开始时间、结束时间、是否允许多次提交等

#### Question.java
- 对应数据库中的Question表
- 包含题目的基本信息：类型、内容、答案、选项等
- 选项处理：
  - 数据库中存储为TEXT类型，使用"|||"分隔符连接多个选项
  - 实体类中提供optionsList字段用于前端交互
  - 自动处理选项的转换（字符串与列表互转）

#### PracticeQuestion.java
- 对应数据库中的PracticeQuestion表
- 用于关联练习和题目，包含题目分值

#### Submission.java
- 对应数据库中的Submission表
- 记录学生的提交信息

### 2. 数据访问层
#### PracticeMapper.java
- 练习相关的数据库操作
- 主要方法：
  - 创建练习
  - 更新练习信息
  - 查询练习列表
  - 获取练习详情

#### QuestionMapper.java
- 题目相关的数据库操作
- 主要方法：
  - 创建题目
  - 批量导入题目
  - 查询题目列表
  - 获取题目详情

#### SubmissionMapper.java
- 提交记录相关的数据库操作
- 主要方法：
  - 记录提交
  - 查询提交历史
  - 更新提交状态

### 3. 服务层
#### PracticeService.java
- 练习相关的业务逻辑
- 主要功能：
  - 创建练习
  - 设置练习时间
  - 管理练习题目
  - 查看练习详情

#### QuestionService.java
- 题目相关的业务逻辑
- 主要功能：
  - 创建题目
  - 从题库导入题目
  - 管理题目章节
  - 题目评分
  - 自动处理选项的转换

### 4. 控制器
#### PracticeController.java
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| POST | /api/practice/create | 创建练习 |
| PUT | /api/practice/{id} | 更新练习信息 |
| GET | /api/practice/list | 获取练习列表 |
| GET | /api/practice/{id} | 获取练习详情 |
| POST | /api/practice/{id}/questions | 添加题目到练习 |
| DELETE | /api/practice/{id}/questions/{questionId} | 从练习中移除题目 |

#### QuestionController.java
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| POST | /api/practice/question/create | 创建题目 |
| POST | /api/practice/question/import | 从题库导入题目 |
| GET | /api/practice/question/list | 获取题目列表 |
| GET | /api/practice/question/{id} | 获取题目详情 |
| PUT | /api/practice/question/{id} | 更新题目信息 |

## 前端接口说明

### 1. 创建练习
- 请求方式：POST
- 路径：/api/practice/create
- 请求体：
```json
{
    "title": "练习标题",
    "classId": 1,
    "startTime": "2024-03-20T10:00:00",
    "endTime": "2024-03-27T10:00:00",
    "allowMultipleSubmission": true,
    "createdBy": 1
}
```
- 响应：
```json
{
    "code": 200,
    "message": "练习创建成功",
    "data": {
        "practiceId": 1
    }
}
```

### 2. 创建题目
- 请求方式：POST
- 路径：/api/practice/question/create
- 请求体：
```json
{
    "type": "singlechoice",
    "content": "题目内容",
    "options": ["选项A", "选项B", "选项C", "选项D"],
    "answer": "A",
    "analysis": "解析",
    "courseId": 1,
    "sectionId": 1,
    "creatorId": 1
}
```
- 响应：
```json
{
    "code": 200,
    "message": "题目创建成功",
    "data": {
        "questionId": 1
    }
}
```

### 3. 从题库导入题目
- 请求方式：POST
- 路径：/api/practice/question/import
- 请求体：
```json
{
    "practiceId": 1,
    "questionIds": [1, 2, 3],
    "scores": [10, 10, 10]
    //
}
```

### 4. 获取题目列表
- 请求方式：GET
- 路径：/api/practice/question/list
- 参数：
  - teacherId: 教师ID
  - courseId: 课程ID
  - sectionId: 章节ID
- 响应：
```json
{
    "code": 200,
    "message": "获取题目列表成功",
    "data": {
        "questions": [
            {
                "id": 1,
                "name": "题目内容",
                "course_id": 1,
                "section_id": 1,
                "teacher_id": 1,
                "type": "singlechoice",
                "options": [
                    {
                        "key": "A",
                        "text": "选项A"
                    },
                    {
                        "key": "B",
                        "text": "选项B"
                    }
                ],
                "answer": "A"
            }
        ]
    }
}
```

### 4. 获取练习列表
- 请求方式：GET
- 路径：/api/practice/list
- 参数：
  - classId: 班级ID

### 5. 获取学生练习列表
- 请求方式：GET
- 路径：/api/practice/student/list
- 参数：
  - studentId: 学生ID
  - classId: 班级ID
- 响应示例：
```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "id": 1,
            "courseId": 1,
            "classId": 1,
            "title": "第1章在线练习",
            "startTime": "2024-03-20T10:00:00",
            "endTime": "2024-03-27T10:00:00",
            "allowMultipleSubmission": true,
            "createdBy": 1,
            "createdAt": "2024-03-20T10:00:00",
            "isCompleted": true,
            "submissionCount": 2,
            "score": 85
        },
        {
            "id": 2,
            "courseId": 1,
            "classId": 1,
            "title": "第2章在线练习",
            "startTime": "2024-03-21T10:00:00",
            "endTime": "2024-03-28T10:00:00",
            "allowMultipleSubmission": true,
            "createdBy": 1,
            "createdAt": "2024-03-21T10:00:00",
            "isCompleted": false,
            "submissionCount": 0,
            "score": null
        }
    ]
}
```

### 6. 获取练习详情
- 请求方式：GET
- 路径：/api/practice/{id}
- 返回：练习详细信息，包含题目列表 