# 文件访问模块改动说明

## 1. 背景
当前文件访问服务(FileAccessService)只支持通过文件ID获取文件的下载和预览URL。但在作业模块中，文件是直接存储在对象存储中的，没有对应的file_node记录，只有objectName。因此需要扩展FileAccessService，支持通过objectName访问文件。

## 2. 改动内容

### 2.1 接口改动
在`org.example.edusoft.service.file.FileAccessService`中新增两个方法：
```java
FileAccessDTO getDownloadUrlByObjectName(String objectName);
FileAccessDTO getPreviewUrlByObjectName(String objectName);
```

### 2.2 实现类改动
在`org.example.edusoft.service.file.impl.FileAccessServiceImpl`中实现这两个方法。

### 2.3 涉及文件
- src/main/java/org/example/edusoft/service/file/FileAccessService.java
- src/main/java/org/example/edusoft/service/file/impl/FileAccessServiceImpl.java

## 3. 实现说明
1. 新增的方法直接使用objectName生成签名URL，不需要查询数据库
2. 复用现有的OSS客户端创建和URL生成逻辑
3. 保持与现有方法相同的过期时间(1小时)和错误处理机制

## 4. 注意事项
1. objectName必须是有效的OSS对象名
2. 生成的URL有效期为1小时
3. 下载URL会强制下载，预览URL会根据文件类型设置合适的Content-Type 