-- 一、创建数据库
DROP DATABASE IF EXISTS CoursePlatform;
CREATE DATABASE CoursePlatform DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE CoursePlatform;

-- 二、用户表
CREATE TABLE User (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(15) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('student', 'teacher', 'tutor') NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 三、课程与章节
CREATE TABLE Course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    teacher_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,   -- 课程代码（类似于ISBN）
    outline TEXT,
    objective TEXT,
    assessment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES User(id)
);

CREATE TABLE CourseSection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (course_id) REFERENCES Course(id)
);

-- 四、班级与成员
CREATE TABLE Class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    class_code VARCHAR(20) NOT NULL UNIQUE, -- 班级暗号，学生自己加入班级
    FOREIGN KEY (course_id) REFERENCES Course(id)
);

CREATE TABLE ClassUser (
    class_id BIGINT,
    user_id BIGINT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (class_id, user_id),
    FOREIGN KEY (class_id) REFERENCES Class(id),
    FOREIGN KEY (user_id) REFERENCES User(id)
);

CREATE TABLE CourseClass (
    course_id BIGINT,
    class_id BIGINT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (class_id, course_id),
    FOREIGN KEY (class_id) REFERENCES Class(id),
    FOREIGN KEY (course_id) REFERENCES Course(id)
);

-- 六、题库与练习
CREATE TABLE Question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT NOT NULL,
    type ENUM('singlechoice', 'program', 'fillblank') NOT NULL,
    content TEXT NOT NULL,
    options JSON,
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
    title VARCHAR(200) NOT NULL,
    start_time DATETIME,
    end_time DATETIME,
    allow_multiple_submission BOOLEAN DEFAULT TRUE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES Course(id),
    FOREIGN KEY (created_by) REFERENCES User(id)
);

CREATE TABLE PracticeQuestion (
    practice_id BIGINT,
    question_id BIGINT,
    score INT NOT NULL,
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
    feedback TEXT,
    FOREIGN KEY (practice_id) REFERENCES Practice(id),
    FOREIGN KEY (student_id) REFERENCES User(id)
);

CREATE TABLE Answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    correct BOOLEAN,
    score INT,
    FOREIGN KEY (submission_id) REFERENCES Submission(id),
    FOREIGN KEY (question_id) REFERENCES Question(id)
);

-- 七、学习记录
CREATE TABLE Progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    section_id BIGINT,
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES User(id),
    FOREIGN KEY (course_id) REFERENCES Course(id),
    FOREIGN KEY (section_id) REFERENCES CourseSection(id)
);

CREATE TABLE FavoriteQuestion (
    student_id BIGINT,
    question_id BIGINT,
    PRIMARY KEY (student_id, question_id),
    FOREIGN KEY (student_id) REFERENCES User(id),
    FOREIGN KEY (question_id) REFERENCES Question(id)
);

-- 八、通知
CREATE TABLE Notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    message TEXT,
    read_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES User(id)
);

-- 九、文件管理
CREATE TABLE file_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, 
    file_name VARCHAR(255) NOT NULL,             -- 节点名称
    is_dir BOOLEAN NOT NULL DEFAULT FALSE,  -- 是否为文件夹
    parent_id BIGINT NULL,                  -- 父节点ID（根节点为NULL）
    course_id BIGINT NOT NULL,              -- 所属课程
    class_id BIGINT NULL,                   -- 所属班级（非必须，用于权限控制）
    uploader_id BIGINT NOT NULL,            -- 上传者
    sectiondir_id BIGINT default -1,            -- 是班级根文件夹中的章节文件夹时，这个字段等于它内部的文件的section_id
    file_type ENUM('VIDEO', 'PPT', 'CODE', 'PDF', 'OTHER') NOT NULL,
    section_id BIGINT default -1,     -- 所属章节，章节文件夹和所有的课程班级根文件夹中的文件的section_id都为-1
    last_file_version BIGINT NOT NULL DEFAULT 0,   -- 指向该文件的上一个版本的id，如果没有上一个版本默认为0
    is_current_version BOOLEAN NOT NULL DEFAULT TRUE, -- 是否是一个文件的最新版本，没有多个版本时默认为true
    file_size BIGINT NOT NULL,
    visibility ENUM('PUBLIC', 'PRIVATE', 'CLASS_ONLY') NOT NULL DEFAULT 'CLASS_ONLY',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    file_url VARCHAR(1024),      -- 文件存储路径，在云对象库中 
    file_version int,     -- 文件版本号           
    object_name VARCHAR(255),  -- 对象存储中的文件路径到文件名，如 /course/1/section/1/file.txt

    FOREIGN KEY (parent_id) REFERENCES file_node(id),
    FOREIGN KEY (course_id) REFERENCES Course(id),
    FOREIGN KEY (class_id) REFERENCES Class(id),
    FOREIGN KEY (uploader_id) REFERENCES User(id)
);

-- 十、导入记录
CREATE TABLE import_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    class_id BIGINT NOT NULL,
    operator_id BIGINT NOT NULL,
    file_name VARCHAR(255),
    total_count INT NOT NULL,
    success_count INT NOT NULL,
    fail_count INT NOT NULL,
    fail_reason TEXT,
    import_time DATETIME NOT NULL,
    import_type VARCHAR(20) NOT NULL,
    FOREIGN KEY (class_id) REFERENCES Class(id),
    FOREIGN KEY (operator_id) REFERENCES User(id)
);

CREATE INDEX idx_parent_id ON file_node (parent_id);  -- 创建索引

-- 数据插入部分（修复 INSERT）
INSERT INTO User (user_id, username, password_hash, role, email) 
VALUES 
('U001', 'teacher_zhang', 'hash123456', 'teacher', 'zhang@example.com'),
('U002', 'student_li', 'hash789012', 'student', 'li@example.com');

INSERT INTO Course (teacher_id, name, code, outline, objective, assessment)
VALUES (1, '软件工程基础', 'SE101', '介绍软件开发流程', '掌握基础知识', '作业+项目+考试');

INSERT INTO CourseSection (course_id, title, sort_order)
VALUES 
(1, '第一章：软件工程导论', 1),
(1, '第二章：需求分析', 2);

INSERT INTO Class (course_id, name, class_code)
VALUES (1, '软工A班', 'CLASS_A_101');

INSERT INTO ClassUser (class_id, user_id)
VALUES (1, 2);

INSERT INTO Question (creator_id, type, content, options, answer)
VALUES 
(1, 'singlechoice', '软件工程的第一步是什么？', 
    JSON_ARRAY('需求分析', '编码', '测试', '部署'), 
    '需求分析');

INSERT INTO Practice (course_id, title, start_time, end_time, allow_multiple_submission, created_by)
VALUES 
(1, '第一章练习', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), TRUE, 1);

INSERT INTO PracticeQuestion (practice_id, question_id, score)
VALUES (1, 1, 5);

INSERT INTO Submission (practice_id, student_id, score, feedback)
VALUES (1, 2, 5, '回答正确');

INSERT INTO Answer (submission_id, question_id, answer_text, correct, score)
VALUES (1, 1, '需求分析', TRUE, 5);

INSERT INTO Progress (student_id, course_id, section_id, completed, completed_at)
VALUES (2, 1, 1, TRUE, NOW());

INSERT INTO FavoriteQuestion (student_id, question_id)
VALUES (2, 1);

INSERT INTO Notification (user_id, title, message)
VALUES (2, '练习反馈已出', '第一章练习已批改，请查看得分');
