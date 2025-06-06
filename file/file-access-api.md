# 文件访问接口文档

## 1. 获取文件下载URL

### 请求方式
```
GET /api/resources/{resourceId}/download-url
```

### 请求参数
- **URL Path**
  - `resourceId`: 文件ID (int)

### 响应格式
```json
{
  "code": 0,
  "message": "获取下载链接成功",
  "data": {
    "url": "https://edusoft-file.oss-cn-beijing.aliyuncs.com/example.pdf?Expires=xxx&OSSAccessKeyId=xxx&Signature=xxx",
    "fileName": "example.pdf",
    "expiresIn": 3600
  }
}
```

## 2. 获取文件预览URL

### 请求方式
```
GET /api/resources/{resourceId}/preview-url
```

### 请求参数
- **URL Path**
  - `resourceId`: 文件ID (int)

### 响应格式
```json
{
  "code": 0,
  "message": "获取预览链接成功",
  "data": {
    "url": "https://edusoft-file.oss-cn-beijing.aliyuncs.com/example.pdf?Expires=xxx&OSSAccessKeyId=xxx&Signature=xxx",
    "fileName": "example.pdf",
    "fileType": "PDF",
    "expiresIn": 3600
  }
}
```

## 实现说明

### 文件结构
```
org.example.edusoft
├── controller.file
│   └── FileController.java (修改)
├── service.file
│   ├── FileAccessService.java (新增)
│   └── impl
│       └── FileAccessServiceImpl.java (新增)
├── entity.file
│   └── FileAccessDTO.java (新增)
```

### 注意事项
1. 返回的URL有效期为1小时
2. 文件下载URL会自动添加下载头
3. 文件预览URL适用于浏览器直接打开预览
4. 所有的resourceId参数都是int类型 