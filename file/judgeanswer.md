我来帮你生成这两个控制器的接口说明文档。我会为每个接口提供详细的说明，包括请求方法、URL、参数说明、请求/响应示例等。

# 接口文档

## 1. 手动评分接口文档 (ManualJudgeController)

### 1.1 获取待评分答案
- **接口URL**: `/api/judge/pending`
- **请求方法**: GET
- **接口描述**: 获取需要手动评分的答案列表（非单选题的答案）

#### 请求参数
| 参数名 | 类型 | 是否必须 | 说明 |
|--------|------|----------|------|
| practiceId | Long | 是 | 练习ID |
| submissionId | Long | 否 | 提交记录ID |

#### 响应结果
```json
{
    "code": 200,
    "message": "获取待评分答案成功",
    "data": [
        {
            "id": 1,
            "submissionId": 1,
            "questionId": 2,
            "answerText": "这是学生的答案内容",
            "isJudged": false,
            "correct": null,
            "score": 0
        }
    ]
}
```

### 1.2 提交评分结果
- **接口URL**: `/api/judge/judge`
- **请求方法**: POST
- **接口描述**: 提交教师对答案的评分结果

#### 请求参数
```json
[
    {
        "id": 1,
        "submissionId": 1,
        "questionId": 2,
        "answerText": "学生答案内容",
        "isJudged": true,
        "correct": true,
        "score": 10
    }
]
```

#### 响应结果
```json
{
    "code": 200,
    "message": "评分更新成功",
    "data": null
}
```

## 2. 提交作答接口文档 (SubmissionController)

### 2.1 提交练习答案
- **接口URL**: `/api/submission/submit`
- **请求方法**: POST
- **接口描述**: 学生提交练习答案，系统自动评判单选题

#### 请求参数
| 参数名 | 类型 | 是否必须 | 说明 |
|--------|------|----------|------|
| practiceId | Long | 是 | 练习ID |
| studentId | Long | 是 | 学生ID |

#### 请求体
```json
{
    "1": "A",
    "2": "public int add(int a, int b) { return a + b; }",
    "3": "这是问题3的答案"
}
```
注：请求体是一个Map，key为题目ID，value为答案内容

#### 响应结果
```json
{
    "code": 200,
    "message": "自动评分完成",
    "data": 10  // 自动评分的总分数
}
```

## 注意事项
1. 所有接口返回的数据都封装在 `Result` 对象中，包含以下字段：
   - code: 状态码（200表示成功）
   - message: 提示信息
   - data: 返回的数据内容

2. 评分流程说明：
   - 提交答案时，系统会自动评判单选题
   - 非单选题需要教师通过手动评分接口进行评分
   - 每次评分后系统会自动更新提交记录的总分

3. 错误处理：
   - 当发生错误时，接口会返回相应的错误码和错误信息
   - 请确保所有必填参数都已正确提供

4. 建议的调用顺序：
   - 学生提交答案：调用 `/api/submission/submit`
   - 教师获取待评分答案：调用 `/api/judge/pending`
   - 教师提交评分结果：调用 `/api/judge/judge`

这份接口文档详细说明了每个接口的用途、参数和返回值，可以帮助前端开发人员更好地理解和使用这些接口。如果你需要更详细的说明或者有任何疑问，请告诉我。
