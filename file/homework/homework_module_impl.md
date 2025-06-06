# 作业模块实现说明文档

## 1. 模块结构

### 1.1 目录结构
```
src/main/java/org/example/edusoft/
├── controller/homework/
│   └── HomeworkController.java          # 作业控制器
├── service/homework/
│   ├── HomeworkService.java             # 作业服务接口
│   └── impl/
│       └── HomeworkServiceImpl.java      # 作业服务实现
├── entity/homework/
│   ├── Homework.java                    # 作业实体类
│   └── HomeworkSubmission.java          # 作业提交实体类
├── mapper/homework/
│   ├── HomeworkMapper.java              # 作业数据访问接口
│   └── HomeworkSubmissionMapper.java    # 作业提交数据访问接口
└── dto/homework/
    ├── HomeworkDTO.java                 # 作业数据传输对象
    └── HomeworkSubmissionDTO.java       # 作业提交数据传输对象
```

### 1.2 功能说明

#### 1.2.1 创建作业
- 接口：POST /api/homework/create
- 功能：创建新作业，支持上传附件
- 实现：
  1. 接收前端传入的作业信息和附件文件
  2. 如果有附件，使用 OSS 存储并生成访问 URL
  3. 保存作业信息到数据库

#### 1.2.2 提交作业
- 接口：POST /api/homework/submit/{homeworkId}
- 功能：学生提交作业文件
- 实现：
  1. 验证作业是否存在且在截止时间内
  2. 上传提交的文件到 OSS
  3. 保存提交记录到数据库

#### 1.2.3 获取作业列表
- 接口：POST /api/homework/list
- 功能：获取指定班级的作业列表
- 实现：
  1. 根据班级 ID 查询作业列表
  2. 对每个作业的附件 URL 生成带签名的访问链接
  3. 转换为 DTO 返回给前端

#### 1.2.4 获取作业提交列表
- 接口：GET /api/homework/submissions/{homeworkId}
- 功能：获取指定作业的所有提交记录
- 实现：
  1. 根据作业 ID 查询所有提交记录
  2. 对每个提交文件的 URL 生成带签名的访问链接
  3. 转换为 DTO 返回给前端

## 2. 数据库表结构

### 2.1 homework 表
```sql
CREATE TABLE IF NOT EXISTS homework (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    class_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    attachment_url VARCHAR(255),
    object_name VARCHAR(255),
    deadline DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 2.2 homeworksubmission 表
```sql
CREATE TABLE IF NOT EXISTS homeworksubmission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    homework_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    file_url VARCHAR(255),
    object_name VARCHAR(255),
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (homework_id) REFERENCES homework(id)
);
```

## 3. 接口说明

### 3.1 创建作业
```http
POST /api/homework/create
Content-Type: multipart/form-data

请求参数：
- class_id: number (必选) - 班级ID
- title: string (必选) - 作业标题
- description: string (可选) - 作业描述
- end_time: string (可选) - 截止时间，格式：yyyy-MM-dd HH:mm:ss
- file: file (可选) - 附件文件

响应：
{
    "code": 0,
    "message": "创建成功",
    "data": {
        "homeworkId": 123
    }
}
```

### 3.2 提交作业
```http
POST /api/homework/submit/{homeworkId}
Content-Type: multipart/form-data

请求参数：
- student_id: number (必选) - 学生ID
- file: file (必选) - 提交的文件

响应：
{
    "code": 0,
    "message": "提交成功",
    "data": {
        "submissionId": 456
    }
}
```

### 3.3 获取作业列表
```http
POST /api/homework/list
Content-Type: application/json

请求参数：
- class_id: number (必选) - 班级ID

响应：
{
    "code": 0,
    "message": "获取成功",
    "data": [
        {
            "homeworkId": 123,
            "course_id": 456,
            "title": "作业标题",
            "description": "作业描述",
            "fileUrl": "https://xxx.com/file?signature=xxx", // 带签名的URL
            "fileName": "file.pdf",
            "end_time": "2024-12-31 23:59:59"
        }
    ]
}
```

### 3.4 获取提交列表
```http
GET /api/homework/submissions/{homeworkId}

响应：
{
    "code": 0,
    "message": "获取成功",
    "data": [
        {
            "submissionId": 789,
            "studentId": "10001",
            "studentName": "张三",
            "fileUrl": "https://xxx.com/file?signature=xxx", // 带签名的URL
            "fileName": "submission.pdf",
            "submit_time": "2024-12-30 10:00:00"
        }
    ]
}
```

## 4. 实现要点

1. 文件访问 URL 处理
   - 使用 FileAccessService 生成带签名的访问 URL
   - URL 有效期为 1 小时
   - 下载时设置 Content-Disposition: attachment
   - 预览时设置 Content-Disposition: inline

2. 数据传输对象
   - 使用 DTO 对象与前端交互，避免直接暴露实体类
   - 在 Service 层进行实体类与 DTO 的转换

3. 异常处理
   - 统一使用 BusinessException 处理业务异常
   - 在 Controller 层统一处理异常并返回友好的错误信息

4. 文件存储
   - 使用阿里云 OSS 存储文件
   - 文件路径格式：
     - 作业附件：homework/{classId}/{fileName}
     - 提交文件：homework/submission/{homeworkId}/{studentId}_{fileName} 