
# 接口文档


## 0. 概述
- **数据库相关表格**
-- 六、题库与练习
CREATE TABLE Question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT NOT NULL,
    type ENUM('singlechoice', 'program', 'fillblank') NOT NULL,
    content TEXT NOT NULL,
    analysis TEXT,
    options TEXT,
    answer TEXT,
    course_id BIGINT,            -- 新增：关联课程
    section_id BIGINT,           -- 新增：关联章节
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES User(id),
    FOREIGN KEY (course_id) REFERENCES Course(id),
    FOREIGN KEY (section_id) REFERENCES CourseSection(id)
);

CREATE TABLE Practice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    allow_multiple_submission BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES Course(id),
    FOREIGN KEY (created_by) REFERENCES User(id),
    FOREIGN KEY (class_id) REFERENCES Class(id)
);

CREATE TABLE PracticeQuestion (
    practice_id BIGINT,
    question_id BIGINT,
    score INT NOT NULL,
    sort_order BIGINT,
    PRIMARY KEY (practice_id, question_id),
    FOREIGN KEY (practice_id) REFERENCES Practice(id),
    FOREIGN KEY (question_id) REFERENCES Question(id)
);

CREATE TABLE Submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    practice_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    score INT DEFAULT 0,
    is_judged INT DEFAULT 0, 
    feedback TEXT,
    FOREIGN KEY (practice_id) REFERENCES Practice(id),
    FOREIGN KEY (student_id) REFERENCES User(id)
);

CREATE TABLE Answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    is_judged BOOLEAN DEFAULT FALSE,
    correct BOOLEAN,
    score INT,
    sort_order BIGINT,
    FOREIGN KEY (submission_id) REFERENCES Submission(id),
    FOREIGN KEY (question_id) REFERENCES Question(id)
); 

- **说明**：这部分是提交练习的答案和进行自动评分、手动评分的接口文档，
在练习部分中，创建一个练习对应的是实体类中的Practice类和数据库中的practice表，题目对应的是实体类中的Question类和数据库中的question表，练习和题目是多对多关系，练习和题目的关系在数据库中的practicequestion表中，对应的是实体类中的PracticeQuestion类。
而学生作答时，会给出练习id、学生id和一个表示练习中所有题目答案的字符串数组，练习中有多个题目，每个题目的答案用一个字符串表示，一一对应，生成多个Answer实体类和anwser表表项，生成一个提交submission，对应Submission实体类和数据库中submission表，多个answer表项通过submission_id关联到一个submission表项，submission和answer是一对多的关系，一个submission对应多个answer。

对于下面接口的实现逻辑进行说明
1. 获取某个练习的待批改的提交的列表
练习是以班级为单位发布的，如果前端传来的参数只有班级ID，则返回该班级下所有待批改的提交信息的列表，在提交信息中，包括提交ID、提交学生的姓名和所提交的练习的名称，通过班级id可以在practice表中找到所有这个班级的练习的id和练习的名称，然后在提交表中找到练习id对应的提交信息，包含提交学生的id，在student表中找到学生id对应的学生姓名，最后将studentName"、practiceName"、"submissionId"三者返回给前端。而如果前端传来的参数有practiceId、 classId，返回班级下对应练习的所有提交即可。

2. 获取指定提交的答案内容详情
获取到待批改提交列表之后，前端已经拿到了每个提交的ID，现在老师想要查看这个提交中的具体内容，通过提交ID，在submission表中找到提交信息，得到练习id和学生id，学生id用于在student表中找到学生姓名（返回给前端的信息1,studentName），练习id用于在practicequestion表中找到所有这个练习中的题目id和题目在这个练习中的分值（返回给前端的maxScore），题目id用于在question表中找到题目，筛选出type非singlechoice的题目，将内容content作为返回给前端的questionName信息（返回给前端的questionName），用已经获取到的提交id和题目id在answer表中找到 answer_text（返回给前端的answerText信息），此时已经获取到返回给前端的所有信息，返回给前端。
注意：数组中内容的顺序要按照practicequestion表中sort_order字段的顺序来从小到大一道一道返回给前端，且只需要返回question表中type类型为singlechoice的回问题对应的回答。

3. 批改练习中主观题
老师获取到指定提交的所有题目的答案内容详情，遍历前端传来的questions数组，挨个答案修改老师评的score分数。用前端传来的提交id和sort_order共同在answer表中找到对应的那个答案，将答案的分数置为前端给的分数，将answer表中is_judged置为1，表示该条答案已经judged了。完成提交中所有答案的批改后，将提交表中对应提交id的is_judged置为1，表示该条提交中的答案已经全部judged了。同时在给提交中每个主观题答案评分的过程中，将分数累加，最后将主观题总分数也加到在提交表对应提交项的score字段中（在答案提交时已经有所有的客观题分数累加到score字段中了）。最后返回给前端响应成功信息即可。

4. 学生提交练习答案，系统自动评判单选题
学生完成练习后，提交答案，在submission表中插入新的条目，id为提交自增主键，practice_id和student_id对应前端传来的内容，submission_time为当前时间，score初始化为0，is_judges为0，feedback为空。然后用practiceId作为practice_id在practicequestion表中找到所有的question_id，然后按照practicequestion表中sort_order从一增大的顺序，在answer表中插入新的条目，id为answer自增主键，submission_id和question_id分别置为submission表中新增条目的id和现在处理的question_id的值，sort_order属性与practicequestion表中sort_order相同，answer_text属性为前端传来的answers数组中第sort_order-1个字符串（answers数组索引从0开始，sort_order从1开始），使用question_id在question表中确定type，如果type为singlechoice，将answer表中answer_text属性与question表中对应的问题的answer属性比较，两者相同就将answer表中的correct置为1，is_judged置为1，score置为从practicequestion获取的score值（即这道题目得满分），否则将correct置为0，is_judged置为1，score置为0；如果type不为单选题，将is_judeged置为0，score置为0,correct置为0。在处理每个answer的过程中，累加单选题所得的分值，处理完前端传来的所有answer后，将单选题的得分保存在submission表刚刚新增的条目的score中，如果之前处理的题型都是单选题，submission新增条目的is_judged置为1。最后返回给前端响应信息和submissionId即可。


{
  "practiceId": 1,
  "studentId": 1,
  "answers": ["A", "C", "B", "D", "Hello,   World!"]
}

#### 响应结果
```json
{
    "code": 0,
    "message": "string",
    "data": {
        "submissionId": 0
    }
}


## 1. 手动评分接口文档 (ManualJudgeController)

### 1.1 获取某个练习的待批改的提交的列表
- **接口URL**: `/api/judge/pendinglist`
- **请求方法**: POST
- **接口描述**: 获取包含需要手动评分的题目的练习的提交的列表

#### Body Params请求参数
| 参数名 | 类型 | 是否必须 | 说明 |
|--------|------|----------|------|
| practiceId | Long | 否 | 练习ID |
| classId | Long | 是 | 提交记录ID |

#### 响应结果
```json
{
    "code": 0,
    "message": "string",
    "data": [
        {
            "studentName": "string",
            "practiceName": "string",
            "submissionId": "string"
        }
    ]
}


```

### 1.2 获取指定提交的答案内容详情
- **接口URL**: `/api/judge/pending`
- **请求方法**: POST
- **接口描述**: 提交教师对答案的评分结果

#### Body Params请求参数
| 参数名 | 类型 | 是否必须 | 说明 |
|--------|------|----------|------|
| submissionId | Long | 是 | 提交的ID |

#### 响应结果
```json
{
    "code": 0,
    "message": "string",
    "data": [
        {
            "questionName": "string",
            "answerText": "string",
            "maxScore": 0,
            "studentName": "string"，
            "sortOrder": 0
        }
    ]
}
```

### 1.3 批改练习
- **接口URL**: `/api/judge/judge`
- **请求方法**: POST
- **接口描述**: 老师批改非客观题

#### Body 请求参数
| 参数名 | 类型 | 是否必须 | 说明 |
|--------|------|----------|------|
| submissionId | Long | 是 | 提交ID |
| question | array[object{3}] | 是 | 提交中包含的学生的答案 |
        | answerText | string | 是 | 作答的文本 |
        | score | Long | 是 | 老师给这道主观题的评分 |
        | maxScore | Long | 是 | 这道主观题的分值 |

#### 请求示例
```
POST /api/judge/judge
Content-Type: application/json

{
    "submissionId": 0,
    "question": [
        {
            "answerText": "string",
            "score": 0,
            "maxScore": 0,
            "sortOrder": 0
        }
    ]
}
```


#### 响应结果
```json
{
    "code": 0,
    "message": "string",
    "data": null
}
```



## 2. 提交作答接口文档 (SubmissionController)

### 2.1 提交练习答案
- **接口URL**: `/api/submission/submit`
- **请求方法**: POST
- **接口描述**: 学生提交练习答案，系统自动评判单选题

#### Body 请求参数
| 参数名 | 类型 | 是否必须 | 说明 |
|--------|------|----------|------|
| practiceId | Long | 是 | 练习ID |
| studentId | Long | 是 | 学生ID |
| answers | array[string] | 是 | 学生对指定练习的答案 |

#### 请求示例
```
POST /api/submission/submit
Content-Type: application/json

{
  "practiceId": 1,
  "studentId": 1,
  "answers": ["A", "C", "B", "D", "Hello,   World!"]
}
```


#### 响应结果
```json
{
    "code": 0,
    "message": "string",
    "data": {
        "submissionId": 0
    }
}
```

