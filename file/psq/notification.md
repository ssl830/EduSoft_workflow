# 消息提醒模块说明文档

## 模块结构

该模块在以下目录中实现了相关功能：

```
src/main/java/org/example/edusoft/
├── entity/notification/
│   ├── Notification.java        # 通知实体类
│   └── NotificationType.java    # 通知类型枚举
├── mapper/notification/
│   └── NotificationMapper.java  # 通知数据访问接口
├── service/notification/
│   ├── NotificationService.java # 服务接口
│   └── impl/
│       └── NotificationServiceImpl.java # 服务实现类
└── controller/notification/
    └── NotificationController.java # 控制器
```

## 功能说明

### 1. 实体类
#### Notification.java
- 通知实体类，包含通知的基本信息
- 字段说明：
  - id: 通知ID
  - userId: 接收用户ID
  - title: 通知标题
  - message: 通知内容
  - type: 通知类型（作业/任务/在线练习）
  - readFlag: 是否已读
  - createdAt: 创建时间
  - relatedId: 关联ID（如作业ID、任务ID等）
  - relatedType: 关联类型

#### NotificationType.java
- 通知类型枚举
- 包含：HOMEWORK（作业）、TASK（任务）、PRACTICE（在线练习）

### 2. 控制器（NotificationController.java）
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| GET | /api/notifications | 获取用户的所有通知 |
| GET | /api/notifications/unread | 获取未读通知数量 |
| PUT | /api/notifications/{id}/read | 标记通知为已读 |
| PUT | /api/notifications/read-all | 标记所有通知为已读 |
| DELETE | /api/notifications/{id} | 删除指定通知 |

## 前端接口说明

### 1. 获取用户的所有通知
- 请求方式：GET
- 路径：/api/notifications
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "title": "新作业提醒",
            "message": "老师发布了新的作业：第一章练习",
            "type": "HOMEWORK",
            "readFlag": false,
            "createdAt": "2024-03-20T10:00:00",
            "relatedId": 1,
            "relatedType": "PRACTICE"
        }
    ]
}
```

### 2. 获取未读通知数量
- 请求方式：GET
- 路径：/api/notifications/unread
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "count": 5
    }
}
```

### 3. 标记通知为已读
- 请求方式：PUT
- 路径：/api/notifications/{id}/read
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功"
}
```

### 4. 标记所有通知为已读
- 请求方式：PUT
- 路径：/api/notifications/read-all
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功"
}
```

### 5. 删除通知
- 请求方式：DELETE
- 路径：/api/notifications/{id}
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功"
}
```

## 注意事项
1. 所有接口都需要用户登录
2. 通知会在以下情况自动创建：
   - 老师发布新作业时
   - 老师发布新任务时
   - 老师发布在线练习时
3. 通知会实时推送给相关学生
4. 前端需要实现通知的实时更新功能
5. 建议使用WebSocket实现实时通知推送 