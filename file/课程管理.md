# 课程管理模块说明文档

## 模块结构

该模块在以下目录中实现了相关功能：

```
src/main/java/org/example/edusoft/
├── entity/course/
│   └── Course.java                 # 课程实体类
├── mapper/course/
│   └── CourseMapper.java          # 课程数据访问接口
├── service/course/
│   ├── CourseService.java         # 课程服务接口
│   └── impl/
│       └── CourseServiceImpl.java  # 课程服务实现类
└── controller/course/
    └── CourseController.java      # 课程控制器
```

## 功能说明

### 1. 课程实体（Course.java）
- 对应数据库中的Course表
- 包含课程的基本信息：ID、教师ID、名称、代码、大纲、目标、考核方式等
- 使用MyBatis-Plus注解进行ORM映射

关键代码：
```java
@Data
@TableName("Course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teacherId;
    private String name;
    private String code;
    private String outline;
    private String objective;
    private String assessment;
    private LocalDateTime createdAt;
}
```

### 2. 数据访问层（CourseMapper.java）
- 继承MyBatis-Plus的BaseMapper接口
- 提供基础的CRUD操作
- 自定义查询方法：根据用户ID获取课程列表（支持教师查看自己创建的课程，学生查看加入的课程）

关键代码：
```java
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
    @Select("SELECT c.* FROM Course c " +
            "LEFT JOIN ClassUser cu ON c.id = cu.class_id " +
            "WHERE c.teacher_id = #{userId} OR cu.user_id = #{userId}")
    List<Course> getCoursesByUserId(Long userId);
}
```

### 3. 服务层
#### 接口（CourseService.java）
- 定义课程相关的业务方法：
  - 创建课程
  - 获取课程列表
  - 获取课程详情
  - 更新课程信息
  - 删除课程

关键代码：
```java
public interface CourseService {
    Course createCourse(Course course);
    List<Course> getCoursesByUserId(Long userId);
    Course getCourseById(Long id);
    Course updateCourse(Course course);
    boolean deleteCourse(Long id);
}
```

#### 实现类（CourseServiceImpl.java）
- 实现CourseService接口定义的所有方法
- 使用事务注解确保数据一致性
- 调用Mapper层进行数据操作

关键代码：
```java
@Service
public class CourseServiceImpl implements CourseService {
    @Autowired
    private CourseMapper courseMapper;

    @Override
    @Transactional
    public Course createCourse(Course course) {
        courseMapper.insert(course);
        return course;
    }

    @Override
    public List<Course> getCoursesByUserId(Long userId) {
        return courseMapper.getCoursesByUserId(userId);
    }

    @Override
    public Course getCourseById(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    @Transactional
    public Course updateCourse(Course course) {
        courseMapper.updateById(course);
        return course;
    }

    @Override
    @Transactional
    public boolean deleteCourse(Long id) {
        return courseMapper.deleteById(id) > 0;
    }
}
```

### 4. 控制器（CourseController.java）
提供以下RESTful API接口：

| 请求方法 | 路径 | 功能说明 |
|---------|------|---------|
| POST | /api/courses | 创建新课程 |
| GET | /api/courses/user/{userId} | 获取用户的课程列表 |
| GET | /api/courses/{id} | 获取课程详情 |
| PUT | /api/courses/{id} | 更新课程信息 |
| DELETE | /api/courses/{id} | 删除课程 |

关键代码：
```java
@RestController
@RequestMapping("/api/courses")
public class CourseController {
    @Autowired
    private CourseService courseService;

    @PostMapping
    public Result<Course> createCourse(@RequestBody Course course) {
        return Result.success(courseService.createCourse(course));
    }

    @GetMapping("/user/{userId}")
    public Result<List<Course>> getCoursesByUserId(@PathVariable Long userId) {
        return Result.success(courseService.getCoursesByUserId(userId));
    }

    @GetMapping("/{id}")
    public Result<Course> getCourseById(@PathVariable Long id) {
        return Result.success(courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    public Result<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        return Result.success(courseService.updateCourse(course));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> deleteCourse(@PathVariable Long id) {
        return Result.success(courseService.deleteCourse(id));
    }
}
```

## 前端接口说明

### 1. 创建课程
- 请求方式：POST
- 路径：/api/courses
- 请求体：
```json
{
    "teacherId": "教师ID",
    "name": "课程名称",
    "code": "课程代码",
    "outline": "课程大纲",
    "objective": "教学目标",
    "assessment": "考核方式"
}
```

### 2. 获取课程列表
- 请求方式：GET
- 路径：/api/courses/user/{userId}
- 说明：根据用户ID获取课程列表，教师可以看到自己创建的课程，学生可以看到加入的课程

### 3. 获取课程详情
- 请求方式：GET
- 路径：/api/courses/{id}
- 说明：获取指定ID的课程详细信息

### 4. 更新课程
- 请求方式：PUT
- 路径：/api/courses/{id}
- 请求体：同创建课程的请求体

### 5. 删除课程
- 请求方式：DELETE
- 路径：/api/courses/{id}

## 注意事项

1. 所有接口返回统一使用Result类封装
2. 课程代码（code）需要保证唯一性
3. 删除课程时需要确保没有关联的班级和学生
4. 更新课程信息时，部分字段（如teacherId）不允许修改 