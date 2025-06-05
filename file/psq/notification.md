# 消息提醒模块说明文档

## 模块结构

该模块在以下目录中实现了相关功能：

```
src/main/java/org/example/edusoft/
├── entity/notification/
│   ├── Notification.java        # 通知实体类
│   ├── NotificationType.java    # 通知类型枚举
│   └── TaskReminder.java        # 任务提醒实体类
├── mapper/notification/
│   ├── NotificationMapper.java  # 通知数据访问接口
│   └── TaskReminderMapper.java  # 任务提醒数据访问接口
├── service/notification/
│   ├── NotificationService.java # 服务接口
│   ├── TaskReminderService.java # 任务提醒服务接口
│   └── impl/
│       ├── NotificationServiceImpl.java # 服务实现类
│       └── TaskReminderServiceImpl.java # 任务提醒服务实现类
└── controller/notification/
    ├── NotificationController.java # 控制器
    └── TaskReminderController.java # 任务提醒控制器
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
  - type: 通知类型（课程通知/练习通知/DDL提醒/任务提醒）
  - readFlag: 是否已读
  - createdAt: 创建时间
  - relatedId: 关联ID（如作业ID、任务ID等）
  - relatedType: 关联类型

#### NotificationType.java
- 通知类型枚举
- 包含：
  - COURSE_NOTICE: 课程通知
  - PRACTICE_NOTICE: 练习通知
  - DDL_REMINDER: DDL提醒
  - TASK_REMINDER: 任务提醒
  - HOMEWORK: 作业
  - TASK: 任务
  - PRACTICE: 在线练习

#### TaskReminder.java
- 任务提醒实体类
- 字段说明：
  - id: 任务ID
  - userId: 用户ID
  - title: 任务标题
  - content: 任务内容（可选）
  - createTime: 创建时间
  - deadline: 截止时间
  - priority: 优先级（高/中/低）
  - completed: 是否完成
  - completedTime: 完成时间

### 2. 控制器

#### NotificationController.java
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| GET | /api/notifications | 获取用户的所有通知 |
| GET | /api/notifications/unread | 获取未读通知数量 |
| PUT | /api/notifications/{id}/read | 标记通知为已读 |
| PUT | /api/notifications/read-all | 标记所有通知为已读 |
| DELETE | /api/notifications/{id} | 删除指定通知 |

#### TaskReminderController.java
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| POST | /api/task-reminders | 创建任务提醒 |
| PUT | /api/task-reminders/{id} | 更新任务提醒 |
| DELETE | /api/task-reminders/{id} | 删除任务提醒 |
| GET | /api/task-reminders/{id} | 获取任务提醒详情 |
| GET | /api/task-reminders/user/{userId} | 获取用户的所有任务提醒 |
| GET | /api/task-reminders/user/{userId}/uncompleted | 获取用户未完成的任务提醒 |
| GET | /api/task-reminders/user/{userId}/completed | 获取用户已完成的任务提醒 |
| PUT | /api/task-reminders/{id}/complete | 标记任务为已完成 |
| PUT | /api/task-reminders/{id}/uncomplete | 标记任务为未完成 |

## 前端接口说明

### 1. 通知相关接口

#### 获取用户的所有通知
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
            "type": "PRACTICE_NOTICE",
            "readFlag": false,
            "createdAt": "2024-03-20T10:00:00",
            "relatedId": 1,
            "relatedType": "PRACTICE"
        }
    ]
}
```

#### 获取未读通知数量
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

#### 标记通知为已读
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

#### 标记所有通知为已读
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

#### 删除通知
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

### 2. 任务提醒相关接口

#### 创建任务提醒
- 请求方式：POST
- 路径：/api/task-reminders
- 请求头：需要登录token
- 请求体：
```json
{
    "title": "完成作业",
    "content": "完成第一章练习",
    "deadline": "2024-03-25T23:59:59",
    "priority": "HIGH"
}
```
- 返回：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "title": "完成作业",
        "content": "完成第一章练习",
        "createTime": "2024-03-20T10:00:00",
        "deadline": "2024-03-25T23:59:59",
        "priority": "HIGH",
        "completed": false
    }
}
```

#### 更新任务提醒
- 请求方式：PUT
- 路径：/api/task-reminders/{id}
- 请求头：需要登录token
- 请求体：同上
- 返回：同上

#### 获取任务提醒列表
- 请求方式：GET
- 路径：/api/task-reminders/user/{userId}
- 请求头：需要登录token
- 返回：
```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "title": "完成作业",
            "content": "完成第一章练习",
            "createTime": "2024-03-20T10:00:00",
            "deadline": "2024-03-25T23:59:59",
            "priority": "HIGH",
            "completed": false
        }
    ]
}
```

#### 标记任务完成状态
- 请求方式：PUT
- 路径：/api/task-reminders/{id}/complete 或 /api/task-reminders/{id}/uncomplete
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
   - 任务截止时间前3天（DDL提醒）
3. 通知会实时推送给相关学生
4. 前端需要实现通知的实时更新功能
5. 建议使用WebSocket实现实时通知推送
6. 任务提醒功能支持用户自定义任务，可以设置优先级和截止时间
7. 系统会自动检查未完成的任务，在截止时间前3天发送DDL提醒 