# 教学资源接口文档

## 获取课程资源及学习进度

### 接口说明
获取指定课程的教学资源列表及对应的学习进度信息，可选择按章节筛选。

### 请求方法
POST

### 请求路径
`/api/resources/chapter/{courseId}`

### 请求头
```
Content-Type: application/json
```

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| courseId | Long | 是 | 课程ID |

### 请求体
```json
{
    "studentId": 0,  // 学生ID
    "chapterId": 0   // 章节ID，-1表示不筛选章节
}
```

### 响应结果
```json
{
    "code": 0,
    "message": "string",
    "data": [
        {
            "learningrecordId": 0,    // 学习记录ID
            "resourceId": 0,          // 资源ID
            "studentId": 0,           // 学生ID
            "title": "string",        // 资源标题
            "description": "string",   // 资源描述
            "courseId": 0,            // 课程ID
            "chapterId": 0,           // 章节ID
            "chapterName": "string",   // 章节名称
            "fileUrl": "string",       // 带签名的临时访问URL
            "duration": 0,             // 视频时长（秒）
            "lastWatch": "string",     // 最后观看时间
            "progress": 0,             // 学习进度（秒）
            "lastPosition": 0,         // 最后观看位置（秒）
            "watchCount": 0,           // 观看次数
            "lastWatchTime": "string", // 最后观看时间
            "createdAt": "string",     // 创建时间
            "updatedAt": "string",     // 更新时间
            "version": 0               // 版本号
        }
    ]
}
```

### 实现说明
1. 接口实现在 `TeachingResourceController` 中的 `getCourseResourcesWithProgress` 方法
2. 业务逻辑在 `TeachingResourceService` 中的 `getCourseResourcesWithProgress` 方法
3. 数据访问在 `TeachingResourceMapper` 中的 `selectByCourseAndChapter` 方法

### 文件结构
```
org.example.edusoft
├── controller.resource
│   └── TeachingResourceController.java
├── service.resource
│   ├── TeachingResourceService.java
│   └── impl
│       └── TeachingResourceServiceImpl.java
├── mapper.resource
│   ├── TeachingResourceMapper.java
│   └── LearningProgressMapper.java
└── entity.resource
    ├── TeachingResource.java
    ├── LearningProgress.java
    ├── ResourceProgressDTO.java
    └── ChapterResourceRequest.java
```

### 注意事项
1. `fileUrl` 返回的是带签名的临时访问URL，有效期为1小时
2. 如果 `chapterId` 为 -1，则返回课程的所有资源
3. 如果学生没有观看记录，相关的学习进度字段将为null 