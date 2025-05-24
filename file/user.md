# 用户管理模块说明文档

## 模块结构

该模块在以下目录中实现了相关功能：

```
src/main/java/org/example/edusoft/
├── entity/user/
│   ├── User.java                 # 实体类
|    —— UserUpdate.java           # 更新用的输入实体类
├── mapper/user/
│   └── UserMapper.java           # 成员数据访问接口
├── service/user/
│   ├── UserService.java          # 服务接口
│   └── impl/
│       └── UserServiceImpl.java  # 服务实现类
└── Controller/user/
    └── UserController.java       # 控制器
```

## 功能说明

### 1. 实体类
#### User.java
- 对应数据库中的User表，包含用户的基本信息
#### UserUpdate.java
- 更新操作使用的实体类，只有部分变量可以更新，所以前端输入进来的只有用户名和邮箱属性即可

### 2. 数据访问层
#### UserMapper.java
- 基本的增删改查
- 查询方法：
  - 根据ID获取
  - 根据userId获取用户

### 3. 服务层
#### 接口（UserService.java）
- 定义用户相关的业务方法：
  - User findById(Long id);查询
  - User findByUserId(String userId);学号查询
  - void save(User user);保存更新
  - void deactivateAccount(Long id);注销账号

#### 实现类（UserServiceImpl.java）
- 实现UserService接口定义的所有方法
- 调用Mapper层进行数据操作

### 4. 控制器（UserController.java）
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| POST | /api/user/register | 注册用户 |
| POST | /api/user/login | 用户登录 |
| POST | /api/user/logout | 退出登录 |
| GET  | /api/user/info | 获取用户信息 |
| POST | /api/user/update | 更新用户信息 |
| POST | /api/user/changePassword | 更改密码 |
| DELETE| /api/user/deactivate | 注销当前用户 |

## 前端接口说明

### 1. 注册用户
- 请求方式：POST
- 路径：/api/user/register
- 请求体：
```json
{
    "username": "testuser1",
    "passwordHash": "password123",
    "role": "student",
    "email": "test@example.com",
    "name": "Test User",
    "userId": "u111"
}
```
- 返回
```json
{
    "code": 200,
    "msg": "注册成功",
    "data": null
}
```

### 2. 登录
- 请求方式：POST
- 路径：/api/user/login
- 参数：

    String userId

    String password
- 返回：token和用户信息
```json
{
    "code": 200,
    "msg": "登录成功",
    "data": {
        "userInfo": {
            "role": "student",
            "id": 4,
            "userid": "u111",
            "email": "test@example.com",
            "username": "testuser1"
        },
        "token": "fdfebfb7-6527-4644-aa1a-e971af08bd12"
    }
}
```
### 3. 退出登录
- 请求方式：POST
- 路径：/api/user/logout
- header：token
- 返回
``` json
{
    "code": 200,
    "msg": "退出登录成功",
    "data": null
}
```
### 4. 获取当前用户信息
- 请求方式：GET
- 路径：/api/user/info
- header：token
- 返回：
``` json
{
    "code": 200,
    "msg": "获取成功",
    "data": {
        "id": 4,
        "userId": "u111",
        "username": "testuser1",
        "passwordHash": null,
        "role": "student",
        "email": "test@example.com",
        "createdAt": "2025-05-22T17:42:48",
        "updatedAt": "2025-05-22T17:42:48"
    }
}
```

### 5. 更新用户信息
- 请求方式：POST
- 路径：/api/user/update
- 参数：
```json
{
    "email": "test1@example.com",
    "username": "Test1"
}
```
- 返回
``` json
{
    "code": 200,
    "msg": "更新成功",
    "data": null
}
```
### 6. 修改密码
- 请求方式：POST
- 路径：/api/user/changePassword
- header：token
- 参数：

      String oldPassword
      
      String newPassword
- 返回
``` json
{
    "code": 200,
    "msg": "密码修改成功",
    "data": null
}
```  
### 7. 注销当前用户
- 请求方式：POST
- 路径：/api/user/deactivate{userId}
- header：token
- 参数：输入密码

    String password

