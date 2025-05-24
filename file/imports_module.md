# 学生导入模块说明文档

## 模块结构

该模块在以下目录中实现了相关功能：

```
src/main/java/org/example/edusoft/
├── entity/imports/
│   └── ImportRecord.java          # 导入记录实体类
├── mapper/imports/
│   └── ImportRecordMapper.java    # 导入记录数据访问接口
├── service/imports/
│   ├── ImportService.java         # 导入服务接口
│   └── impl/
│       └── ImportServiceImpl.java  # 导入服务实现类
└── controller/imports/
    └── ImportController.java      # 导入控制器
```

## 功能说明

### 1. 导入记录实体（ImportRecord.java）
- 对应数据库中的import_record表
- 包含导入记录的基本信息：
  - id：记录ID
  - classId：班级ID
  - operatorId：操作人ID
  - fileName：导入文件名（可选）
  - totalCount：总记录数
  - successCount：成功导入数
  - failCount：失败数
  - failReason：失败原因
  - importTime：导入时间
  - importType：导入类型（FILE/MANUAL）
- 使用MyBatis-Plus注解进行ORM映射

### 2. 数据访问层（ImportRecordMapper.java）
- 继承MyBatis-Plus的BaseMapper接口
- 提供基础的CRUD操作
- 自定义查询方法：
  - 根据班级ID获取导入记录列表
  - 根据记录ID获取导入记录详情

### 3. 服务层
#### 接口（ImportService.java）
- 定义导入相关的业务方法：
  - 导入学生数据（统一接口）
  - 获取班级的导入记录列表
  - 获取导入记录详情

#### 实现类（ImportServiceImpl.java）
- 实现ImportService接口定义的所有方法
- 使用事务注解确保数据一致性
- 主要功能：
  - 处理JSON格式的学生数据
  - 验证学生ID的有效性
  - 检查学生是否已在班级中
  - 记录导入结果和失败原因
  - 调用Mapper层进行数据操作

### 4. 控制器（ImportController.java）
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| POST | /api/imports/students | 导入学生数据 |
| GET | /api/imports/records/{classId} | 获取班级的导入记录列表 |
| GET | /api/imports/record/{id} | 获取导入记录详情 |

## 前端接口说明

### 1. 导入学生数据
- 请求方式：POST
- 路径：/api/imports/students
- 请求参数：
  - classId：班级ID
  - operatorId：操作人ID
  - importType：导入类型（FILE/MANUAL）
  - studentData：学生数据JSON数组
```json
[
    {
        "student_id": "1",
        "name": "张三",
        "other_info": "其他信息"
    }
]
```

### 2. 获取导入记录列表
- 请求方式：GET
- 路径：/api/imports/records/{classId}
- 说明：获取指定班级的所有导入记录

### 3. 获取导入记录详情
- 请求方式：GET
- 路径：/api/imports/record/{id}
- 说明：获取指定ID的导入记录详细信息

## 注意事项

1. 所有接口返回统一使用Result类封装
2. 导入操作使用事务管理，确保数据一致性
3. 导入前需要验证学生ID的有效性
4. 已存在于班级中的学生将被跳过，并记录在失败原因中
5. 建议定期查看导入记录，及时处理导入失败的情况

## 错误处理

1. 数据格式错误：检查JSON数据格式是否符合要求
2. 学生ID不存在：确认学生ID是否有效
3. 学生已在班级中：无需重复导入
4. 其他错误：查看具体错误信息进行处理 