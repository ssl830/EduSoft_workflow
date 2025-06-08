---
title: EduSoft
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# EduSoft

Base URLs:

# Authentication

* API Key (apikey-undefined-free-fs-token)
    - Parameter Name: **free-fs-token**, in: header. 

# 讨论区fkq/回复管理

## POST 创建回复

POST /api/discussion-reply/discussion/3

参数里面可以设置父回复的id，没有就会null

> Body 请求参数

```
content: 这是一条测试回复
parentReplyId: null

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|
|body|body|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "id": 6,
  "discussionId": 3,
  "userId": 4,
  "userNum": "u333",
  "content": "这是一条测试回复",
  "parentReplyId": null,
  "isTeacherReply": true,
  "createdAt": "2025-06-05T11:22:26",
  "updatedAt": "2025-06-05T11:22:26"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» discussionId|integer|true|none||none|
|» userId|integer|true|none||none|
|» userNum|string|true|none||none|
|» content|string|true|none||none|
|» parentReplyId|null|true|none||none|
|» isTeacherReply|boolean|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取讨论的所有回复

GET /api/discussion-reply/discussion/3

参数是discussionid

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 6,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是更新后的回复内容",
    "parentReplyId": null,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:22:27",
    "updatedAt": "2025-06-05T11:28:06"
  },
  {
    "id": 7,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是一条测试回复",
    "parentReplyId": 6,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:29:19",
    "updatedAt": "2025-06-05T11:29:19"
  },
  {
    "id": 8,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是一条测试回复",
    "parentReplyId": 6,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:29:23",
    "updatedAt": "2025-06-05T11:29:23"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» discussionId|integer|true|none||none|
|» userId|integer|true|none||none|
|» userNum|string|true|none||none|
|» content|string|true|none||none|
|» parentReplyId|integer¦null|true|none||none|
|» isTeacherReply|boolean|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## DELETE 删除讨论的所有回复

DELETE /api/discussion-reply/discussion/3

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## PUT 更新回复

PUT /api/discussion-reply/4

> Body 请求参数

```
content: 这是更新后的回复内容

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|
|body|body|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "id": 6,
  "discussionId": 3,
  "userId": 4,
  "userNum": "u333",
  "content": "这是更新后的回复内容",
  "parentReplyId": null,
  "isTeacherReply": true,
  "createdAt": "2025-06-05T11:22:27",
  "updatedAt": "2025-06-05T11:28:05"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» discussionId|integer|true|none||none|
|» userId|integer|true|none||none|
|» userNum|string|true|none||none|
|» content|string|true|none||none|
|» parentReplyId|null|true|none||none|
|» isTeacherReply|boolean|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## DELETE 删除回复

DELETE /api/discussion-reply/2

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 获取回复详情

GET /api/discussion-reply/6

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "id": 6,
  "discussionId": 3,
  "userId": 4,
  "userNum": "u333",
  "content": "这是更新后的回复内容",
  "parentReplyId": null,
  "isTeacherReply": true,
  "createdAt": "2025-06-05T11:22:27",
  "updatedAt": "2025-06-05T11:28:06"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» discussionId|integer|true|none||none|
|» userId|integer|true|none||none|
|» userNum|string|true|none||none|
|» content|string|true|none||none|
|» parentReplyId|null|true|none||none|
|» isTeacherReply|boolean|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取用户的所有回复

GET /api/discussion-reply/user/4

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 8,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是一条测试回复",
    "parentReplyId": 6,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:29:23",
    "updatedAt": "2025-06-05T11:29:23"
  },
  {
    "id": 7,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是一条测试回复",
    "parentReplyId": 6,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:29:19",
    "updatedAt": "2025-06-05T11:29:19"
  },
  {
    "id": 6,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是更新后的回复内容",
    "parentReplyId": null,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:22:27",
    "updatedAt": "2025-06-05T11:28:06"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» discussionId|integer|true|none||none|
|» userId|integer|true|none||none|
|» userNum|string|true|none||none|
|» content|string|true|none||none|
|» parentReplyId|integer¦null|true|none||none|
|» isTeacherReply|boolean|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取讨论的顶层回复

GET /api/discussion-reply/discussion/3/top-level

参数是DiscussionReply的id

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 6,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是更新后的回复内容",
    "parentReplyId": null,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:22:27",
    "updatedAt": "2025-06-05T11:28:06"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|false|none||none|
|» discussionId|integer|false|none||none|
|» userId|integer|false|none||none|
|» userNum|string|false|none||none|
|» content|string|false|none||none|
|» parentReplyId|null|false|none||none|
|» isTeacherReply|boolean|false|none||none|
|» createdAt|string|false|none||none|
|» updatedAt|string|false|none||none|

## GET 获取指定回复的子回复

GET /api/discussion-reply/parent/1

参数是DiscussionReply的id

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 7,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是一条测试回复",
    "parentReplyId": 6,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:29:19",
    "updatedAt": "2025-06-05T11:29:19"
  },
  {
    "id": 8,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是一条测试回复",
    "parentReplyId": 6,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:29:23",
    "updatedAt": "2025-06-05T11:29:23"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» discussionId|integer|true|none||none|
|» userId|integer|true|none||none|
|» userNum|string|true|none||none|
|» content|string|true|none||none|
|» parentReplyId|integer|true|none||none|
|» isTeacherReply|boolean|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取讨论的教师回复

GET /api/discussion-reply/discussion/3/teacher

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 6,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是更新后的回复内容",
    "parentReplyId": null,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:22:27",
    "updatedAt": "2025-06-05T11:28:06"
  },
  {
    "id": 7,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是一条测试回复",
    "parentReplyId": 6,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:29:19",
    "updatedAt": "2025-06-05T11:29:19"
  },
  {
    "id": 8,
    "discussionId": 3,
    "userId": 4,
    "userNum": "u333",
    "content": "这是一条测试回复",
    "parentReplyId": 6,
    "isTeacherReply": true,
    "createdAt": "2025-06-05T11:29:23",
    "updatedAt": "2025-06-05T11:29:23"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» discussionId|integer|true|none||none|
|» userId|integer|true|none||none|
|» userNum|string|true|none||none|
|» content|string|true|none||none|
|» parentReplyId|integer¦null|true|none||none|
|» isTeacherReply|boolean|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取讨论的回复数量

GET /api/discussion-reply/discussion/3/count

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```
3
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 获取用户的回复数量

GET /api/discussion-reply/user/4/count

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```
3
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 讨论区fkq/讨论管理

## POST 创建讨论

POST /api/discussion/course/1/class/1

路径参数是courseid和班级id

> Body 请求参数

```
title: 测试讨论
content: 这是一个测试讨论

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|
|body|body|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "id": 13,
  "courseId": 1,
  "classId": 1,
  "creatorId": 4,
  "creatorNum": "u333",
  "title": "测试讨论",
  "content": "这是一个测试讨论",
  "isPinned": false,
  "isClosed": false,
  "viewCount": 0,
  "replyCount": 0,
  "createdAt": "2025-06-05T11:08:21.1017203",
  "updatedAt": "2025-06-05T11:08:21.1017203"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» courseId|integer|true|none||none|
|» classId|integer|true|none||none|
|» creatorId|integer|true|none||none|
|» creatorNum|string|true|none||none|
|» title|string|true|none||none|
|» content|string|true|none||none|
|» isPinned|boolean|true|none||none|
|» isClosed|boolean|true|none||none|
|» viewCount|integer|true|none||none|
|» replyCount|integer|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取课程和班级的讨论

GET /api/discussion/course/1/class/1

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 13,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T11:08:21",
    "updatedAt": "2025-06-05T11:08:21"
  },
  {
    "id": 11,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T11:00:09",
    "updatedAt": "2025-06-05T11:00:09"
  },
  {
    "id": 10,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:52:56",
    "updatedAt": "2025-06-05T10:52:56"
  },
  {
    "id": 9,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:52:53",
    "updatedAt": "2025-06-05T10:52:53"
  },
  {
    "id": 8,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:49:20",
    "updatedAt": "2025-06-05T10:49:20"
  },
  {
    "id": 7,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:52:25",
    "updatedAt": "2025-05-25T15:08:06"
  },
  {
    "id": 6,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "更新后的讨论",
    "content": "这是更新后的内容",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 10,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:51:58",
    "updatedAt": "2025-06-05T11:13:06"
  },
  {
    "id": 5,
    "courseId": 1,
    "classId": 1,
    "creatorId": 3,
    "creatorNum": "u222",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": true,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:50:28",
    "updatedAt": "2025-05-25T14:57:45"
  },
  {
    "id": 3,
    "courseId": 1,
    "classId": 1,
    "creatorId": 3,
    "creatorNum": "u222",
    "title": "更新后的讨论",
    "content": "这是更新后的内容",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 9,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:48:50",
    "updatedAt": "2025-06-05T11:14:59"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» courseId|integer|true|none||none|
|» classId|integer|true|none||none|
|» creatorId|integer|true|none||none|
|» creatorNum|string|true|none||none|
|» title|string|true|none||none|
|» content|string|true|none||none|
|» isPinned|boolean|true|none||none|
|» isClosed|boolean|true|none||none|
|» viewCount|integer|true|none||none|
|» replyCount|integer|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## PUT 更新讨论

PUT /api/discussion/6

路径参数是discussionid

> Body 请求参数

```
title: 更新后的讨论
content: 这是更新后的内容

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|
|body|body|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "id": 6,
  "courseId": 1,
  "classId": 1,
  "creatorId": 4,
  "creatorNum": "u333",
  "title": "更新后的讨论",
  "content": "这是更新后的内容",
  "isPinned": false,
  "isClosed": false,
  "viewCount": 10,
  "replyCount": 0,
  "createdAt": "2025-05-25T14:51:58",
  "updatedAt": "2025-06-05T11:13:05"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» courseId|integer|true|none||none|
|» classId|integer|true|none||none|
|» creatorId|integer|true|none||none|
|» creatorNum|string|true|none||none|
|» title|string|true|none||none|
|» content|string|true|none||none|
|» isPinned|boolean|true|none||none|
|» isClosed|boolean|true|none||none|
|» viewCount|integer|true|none||none|
|» replyCount|integer|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## DELETE 删除讨论

DELETE /api/discussion/12

参数是discussionid

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "message": "讨论删除成功"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» message|string|true|none||none|

## GET 获取讨论详情

GET /api/discussion/3

路径参数是disscussionid

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "id": 3,
  "courseId": 1,
  "classId": 1,
  "creatorId": 3,
  "creatorNum": "u222",
  "title": "更新后的讨论",
  "content": "这是更新后的内容",
  "isPinned": false,
  "isClosed": false,
  "viewCount": 6,
  "replyCount": 0,
  "createdAt": "2025-05-25T14:48:50",
  "updatedAt": "2025-06-05T11:11:42"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» courseId|integer|true|none||none|
|» classId|integer|true|none||none|
|» creatorId|integer|true|none||none|
|» creatorNum|string|true|none||none|
|» title|string|true|none||none|
|» content|string|true|none||none|
|» isPinned|boolean|true|none||none|
|» isClosed|boolean|true|none||none|
|» viewCount|integer|true|none||none|
|» replyCount|integer|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取课程讨论列表

GET /api/discussion/course/1

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 13,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T11:08:21",
    "updatedAt": "2025-06-05T11:08:21"
  },
  {
    "id": 11,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T11:00:09",
    "updatedAt": "2025-06-05T11:00:09"
  },
  {
    "id": 10,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:52:56",
    "updatedAt": "2025-06-05T10:52:56"
  },
  {
    "id": 9,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:52:53",
    "updatedAt": "2025-06-05T10:52:53"
  },
  {
    "id": 8,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:49:20",
    "updatedAt": "2025-06-05T10:49:20"
  },
  {
    "id": 7,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:52:25",
    "updatedAt": "2025-05-25T15:08:06"
  },
  {
    "id": 6,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "更新后的讨论",
    "content": "这是更新后的内容",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 10,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:51:58",
    "updatedAt": "2025-06-05T11:13:06"
  },
  {
    "id": 5,
    "courseId": 1,
    "classId": 1,
    "creatorId": 3,
    "creatorNum": "u222",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": true,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:50:28",
    "updatedAt": "2025-05-25T14:57:45"
  },
  {
    "id": 3,
    "courseId": 1,
    "classId": 1,
    "creatorId": 3,
    "creatorNum": "u222",
    "title": "更新后的讨论",
    "content": "这是更新后的内容",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 8,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:48:50",
    "updatedAt": "2025-06-05T11:14:07"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» courseId|integer|true|none||none|
|» classId|integer|true|none||none|
|» creatorId|integer|true|none||none|
|» creatorNum|string|true|none||none|
|» title|string|true|none||none|
|» content|string|true|none||none|
|» isPinned|boolean|true|none||none|
|» isClosed|boolean|true|none||none|
|» viewCount|integer|true|none||none|
|» replyCount|integer|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取班级讨论列表

GET /api/discussion/class/1

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 13,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T11:08:21",
    "updatedAt": "2025-06-05T11:08:21"
  },
  {
    "id": 11,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T11:00:09",
    "updatedAt": "2025-06-05T11:00:09"
  },
  {
    "id": 10,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:52:56",
    "updatedAt": "2025-06-05T10:52:56"
  },
  {
    "id": 9,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:52:53",
    "updatedAt": "2025-06-05T10:52:53"
  },
  {
    "id": 8,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:49:20",
    "updatedAt": "2025-06-05T10:49:20"
  },
  {
    "id": 7,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:52:25",
    "updatedAt": "2025-05-25T15:08:06"
  },
  {
    "id": 6,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "更新后的讨论",
    "content": "这是更新后的内容",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 10,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:51:58",
    "updatedAt": "2025-06-05T11:13:06"
  },
  {
    "id": 5,
    "courseId": 1,
    "classId": 1,
    "creatorId": 3,
    "creatorNum": "u222",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": true,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:50:28",
    "updatedAt": "2025-05-25T14:57:45"
  },
  {
    "id": 3,
    "courseId": 1,
    "classId": 1,
    "creatorId": 3,
    "creatorNum": "u222",
    "title": "更新后的讨论",
    "content": "这是更新后的内容",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 9,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:48:50",
    "updatedAt": "2025-06-05T11:14:59"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» courseId|integer|true|none||none|
|» classId|integer|true|none||none|
|» creatorId|integer|true|none||none|
|» creatorNum|string|true|none||none|
|» title|string|true|none||none|
|» content|string|true|none||none|
|» isPinned|boolean|true|none||none|
|» isClosed|boolean|true|none||none|
|» viewCount|integer|true|none||none|
|» replyCount|integer|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## GET 获取用户创建的讨论

GET /api/discussion/creator/4

路径参数是当前用户的id

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
[
  {
    "id": 13,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T11:08:21",
    "updatedAt": "2025-06-05T11:08:21"
  },
  {
    "id": 11,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T11:00:09",
    "updatedAt": "2025-06-05T11:00:09"
  },
  {
    "id": 10,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:52:56",
    "updatedAt": "2025-06-05T10:52:56"
  },
  {
    "id": 9,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:52:53",
    "updatedAt": "2025-06-05T10:52:53"
  },
  {
    "id": 8,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-06-05T10:49:20",
    "updatedAt": "2025-06-05T10:49:20"
  },
  {
    "id": 7,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "测试讨论",
    "content": "这是一个测试讨论",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 0,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:52:25",
    "updatedAt": "2025-05-25T15:08:06"
  },
  {
    "id": 6,
    "courseId": 1,
    "classId": 1,
    "creatorId": 4,
    "creatorNum": "u333",
    "title": "更新后的讨论",
    "content": "这是更新后的内容",
    "isPinned": false,
    "isClosed": false,
    "viewCount": 10,
    "replyCount": 0,
    "createdAt": "2025-05-25T14:51:58",
    "updatedAt": "2025-06-05T11:13:06"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» courseId|integer|true|none||none|
|» classId|integer|true|none||none|
|» creatorId|integer|true|none||none|
|» creatorNum|string|true|none||none|
|» title|string|true|none||none|
|» content|string|true|none||none|
|» isPinned|boolean|true|none||none|
|» isClosed|boolean|true|none||none|
|» viewCount|integer|true|none||none|
|» replyCount|integer|true|none||none|
|» createdAt|string|true|none||none|
|» updatedAt|string|true|none||none|

## PUT 置顶讨论

PUT /api/discussion/6/pin

需要老师身份

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|isPinned|query|string| 是 |none|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "message": "讨论已置顶"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» message|string|true|none||none|

## PUT 关闭讨论

PUT /api/discussion/5/close

需要老师身份

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|isClosed|query|string| 是 |none|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "message": "讨论已关闭"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» message|string|true|none||none|

## GET 获取课程讨论数量

GET /api/discussion/course/1/count

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```
9
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 获取班级讨论数量

GET /api/discussion/class/1/count

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|satoken|header|string| 是 |none|

> 返回示例

> 200 Response

```
9
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 讨论区fkq/点赞功能

## POST 点赞讨论

POST /api/discussion-like/discussion/3

> 返回示例

> 200 Response

```json
{
  "id": 4,
  "discussionId": 3,
  "userId": 4,
  "createdAt": "2025-06-05T11:35:37"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|true|none||none|
|» discussionId|integer|true|none||none|
|» userId|integer|true|none||none|
|» createdAt|string|true|none||none|

## DELETE 删除点赞

DELETE /api/discussion-like/discussion/3

> Body 请求参数

```
string

```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|string| 否 |none|

> 返回示例

> 200 Response

```json
{
  "message": "取消点赞成功"
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» message|string|true|none||none|

## GET 获取讨论的点赞列表

GET /api/discussion-like/discussion/3

> 返回示例

> 200 Response

```json
[
  {
    "id": 5,
    "discussionId": 3,
    "userId": 4,
    "createdAt": "2025-06-05T11:40:59"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|false|none||none|
|» discussionId|integer|false|none||none|
|» userId|integer|false|none||none|
|» createdAt|string|false|none||none|

## GET 获取用户的点赞列表

GET /api/discussion-like/user/4

> 返回示例

> 200 Response

```json
[
  {
    "id": 5,
    "discussionId": 3,
    "userId": 4,
    "createdAt": "2025-06-05T11:40:59"
  }
]
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» id|integer|false|none||none|
|» discussionId|integer|false|none||none|
|» userId|integer|false|none||none|
|» createdAt|string|false|none||none|

## GET 获取讨论的点赞数

GET /api/discussion-like/discussion/3/count

> 返回示例

> 200 Response

```
1
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 获取用户的点赞数

GET /api/discussion-like/user/4/count

> 返回示例

> 200 Response

```
1
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET 检查用户是否已点赞

GET /api/discussion-like/discussion/3/check

路径参数是discussionid

> 返回示例

> 200 Response

```
true
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 数据模型

