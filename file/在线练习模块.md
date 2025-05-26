# 练习模块说明文档

## 模块结构

该模块在以下目录中实现了相关功能：

```
src/main/java/org/example/edusoft/
├── controller/practice/
│   └── practiceController.java   # 练习控制器
├── service/practice/
│   ├── PracticeService.java      # 练习服务接口
│   └── impl/
│       └── PracticeServiceImpl.java # 练习服务实现类
└── mapper/practice/
    └── PracticeMapper.java       # 练习数据访问接口
```

## 功能说明

### 1. 控制器（practiceController.java）
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| POST | /api/practice/questions/{questionId}/favorite | 收藏题目 |
| DELETE | /api/practice/questions/{questionId}/favorite | 取消收藏题目 |
| GET | /api/practice/questions/favorites | 获取收藏的题目列表 |
| POST | /api/practice/questions/{questionId}/wrong | 添加错题 |
| GET | /api/practice/questions/wrong | 获取错题列表 |
| GET | /api/practice/questions/wrong/course/{courseId} | 获取某个课程的错题列表 |
| DELETE | /api/practice/questions/{questionId}/wrong | 删除错题 |
| GET | /api/practice/course/{courseId} | 获取课程的所有练习 |

## 前端接口说明

### 1. 收藏题目
- 请求方式：POST
- 路径：/api/practice/questions/{questionId}/favorite
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

### 2. 取消收藏题目
- 请求方式：DELETE
- 路径：/api/practice/questions/{questionId}/favorite
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

### 3. 获取收藏的题目列表
- 请求方式：GET
- 路径：/api/practice/questions/favorites
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "practice_title": "第一章练习",
            "section_title": "第一章：软件工程导论",
            "answer": "需求分析",
            "course_name": "软件工程基础",
            "options": "[\"需求分析\", \"编码\", \"测试\", \"部署\"]",
            "id": 1,
            "type": "singlechoice",
            "content": "软件工程的第一步是什么？"
        }
    ]
}
```

### 4. 添加错题
- 请求方式：POST
- 路径：/api/practice/questions/{questionId}/wrong
- 请求头：需要登录token
- 请求体：
```json
{
    "wrongAnswer": "编码"
}
```
- 返回：
```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

### 5. 获取错题列表
- 注意：重做错题功能需要
- 请求方式：GET
- 路径：/api/practice/questions/wrong
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "practice_title": "第一章练习",
            "section_title": "第一章：软件工程导论",
            "course_name": "软件工程基础",
            "correct_answer": "需求分析",
            "options": "[\"需求分析\", \"编码\", \"测试\", \"部署\"]",
            "id": 1,
            "wrong_count": 1,
            "last_wrong_time": "2025-05-24T18:15:32.000+00:00",
            "type": "singlechoice",
            "wrong_answer": "学生的错误答案",
            "content": "软件工程的第一步是什么？"
        }
    ]
}
```

### 6. 获取某个课程的错题列表
- 请求方式：GET
- 路径：/api/practice/questions/wrong/course/{courseId}
- 请求头：需要登录token
- 返回：同上

### 7. 删除错题
- 请求方式：DELETE
- 路径：/api/practice/questions/{questionId}/wrong
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "success",
    "data": true
}
```

### 8. 获取课程的所有练习
- 可以根据提交次数确定是否完成过
- 请求方式：GET
- 路径：/api/practice/course/{courseId}
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "question_count": 1,
            "start_time": "2025-05-24T23:57:11",
            "section_title": "第一章：软件工程导论",
            "section_id": 1,
            "submission_count": 1,
            "end_time": "2025-05-31T23:57:11",
            "created_at": "2025-05-24T15:57:11.000+00:00",
            "last_score": 5,
            "id": 1,
            "title": "第一章练习",
            "allow_multiple_submission": true
        }
    ]
}
```

注意事项：
1. 所有接口都需要用户登录
2. 如果未登录，所有接口都会返回错误信息
3. 如果发生错误，接口会返回相应的错误信息和状态码
4. 收藏和错题功能只对学生用户开放
5. 题目相关的操作需要确保题目ID存在且有效 