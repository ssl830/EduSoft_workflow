# 作业管理模块说明文档

## 1. 模块结构
### 1.1 目录结构
```
src/main/java/org/example/edusoft/
├── controller/homework/
│   └── HomeworkController.java      # 作业控制器，处理HTTP请求
├── service/homework/
│   └── HomeworkService.java         # 作业服务，处理业务逻辑
├── entity/homework/
│   ├── Homework.java                # 作业实体类
│   └── HomeworkSubmission.java      # 作业提交实体类
├── mapper/homework/
│   ├── HomeworkMapper.java          # 作业数据访问接口
│   └── HomeworkSubmissionMapper.java # 作业提交数据访问接口
```
```
```
###  请求/响应格式
#### 创建作业
POST   /api/homework/create     # 创建作业
- 请求：
  ``` 
  Content-Type: multipart/form-data
  - class_id: number，班级ID（必选）
  - title: 作业名称(必选)
  - description: 作业简介（可选）
  - end_time: 截止时间（可选）
  - file: 附件文件（可选）
  ```
- 响应：作业ID 和 code、message响应信息

#### 提交作业
POST   /api/homework/submit/{homeworkId}     # 提交作业
- 请求：
  ```
  Content-Type: multipart/form-data
  - student_id: 学生ID（必选）
  - file: 提交的文件（必选）
  ```
- 响应：提交记录ID 和 code、message响应信息

#### 获取作业列表
POST   /api/homework/list 
- 请求：
  ```
  Query Params: class_id=number
  Header Params: Content-Type: application/json
  ```
- 响应：
  ```
  {
    "success": true,
    "message": "culpa cillum commodo Ut qui",
    "data": [
        {
            "homeworkId": 31,
            "course_id": 95,
            "title": "平常由于横穿联盟托架耶耶耶快切难道在",
            "description": "VIDEO、、、yuj",
            "fileUrl": "https://www.baidu.com/",
            "fileName": "CLASS_ONLY",
            "end_time": "2024-09-14 09:22:59"
        },
        {
            "homeworkId": 1,
            "course_id": 95,
            "title": "平常由于横穿联盟托架耶耶耶快切难道在",
            "description": "VIDEO===-",
            "fileUrl": "https://www.baidu.com/",
            "fileName": "CLASS_ONLY",
            "end_time": "2024-09-14 09:22:59"
        }
    ]
}
```
```
#### 获取学生完成作业后的提交的列表
GET    /api/homework/submissions/{homeworkId}
- 请求：
  ```
  Path Params: homeworkId=number
  ```
- 响应：
  ```
  {
    "code": 0,
    "message": "string",
    "data": [
        {
            "submissionId": 0,
            "studentId": "string",
            "studentName": "string",
            "fileUrl": "string",
            "fileName": "string",
            "submit_time": "string"
        }
    ]
  }

```
```

