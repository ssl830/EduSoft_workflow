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
    FOREIGN KEY (teacher_id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE TABLE CourseSection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE
);

-- 四、班级与成员
CREATE TABLE Class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    class_code VARCHAR(20) NOT NULL UNIQUE, -- 班级暗号，学生自己加入班级
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE
);

CREATE TABLE ClassUser (
    class_id BIGINT,
    user_id BIGINT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (class_id, user_id),
    FOREIGN KEY (class_id) REFERENCES Class(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE TABLE CourseClass (
    course_id BIGINT,
    class_id BIGINT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (class_id, course_id),
    FOREIGN KEY (class_id) REFERENCES Class(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE
);

-- 六、题库与练习
CREATE TABLE Question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    creator_id BIGINT NOT NULL,
    type ENUM('singlechoice', 'program', 'fillblank','judge') NOT NULL,
    content TEXT NOT NULL,
    analysis TEXT,
    options TEXT,
    answer TEXT,
    course_id BIGINT,            -- 新增：关联课程
    section_id BIGINT,           -- 新增：关联章节
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES CourseSection(id) ON DELETE CASCADE
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
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES Class(id) ON DELETE CASCADE
);

CREATE TABLE PracticeQuestion (
    practice_id BIGINT,
    question_id BIGINT,
    sort_order BIGINT,
    score INT NOT NULL,
    score_rate DECIMAL(5,4) DEFAULT NULL,
    PRIMARY KEY (practice_id, question_id),
    FOREIGN KEY (practice_id) REFERENCES Practice(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Question(id) ON DELETE CASCADE
);

CREATE TABLE Submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    practice_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    score INT DEFAULT 0,
    is_judged INT DEFAULT 0,
    feedback TEXT,
    FOREIGN KEY (practice_id) REFERENCES Practice(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES User(id) ON DELETE CASCADE
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
    FOREIGN KEY (submission_id) REFERENCES Submission(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Question(id) ON DELETE CASCADE
);

-- 七、学习记录
CREATE TABLE Progress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    section_id BIGINT,
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE,
    FOREIGN KEY (section_id) REFERENCES CourseSection(id) ON DELETE CASCADE
);

CREATE TABLE FavoriteQuestion (
    student_id BIGINT,
    question_id BIGINT,
    PRIMARY KEY (student_id, question_id),
    FOREIGN KEY (student_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Question(id) ON DELETE CASCADE
);

-- 八、通知
CREATE TABLE Notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(200),
    message TEXT,
    type VARCHAR(20) NOT NULL,
    read_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    related_id BIGINT,
    related_type VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
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

    FOREIGN KEY (parent_id) REFERENCES file_node(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES Class(id) ON DELETE CASCADE,
    FOREIGN KEY (uploader_id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE INDEX idx_parent_id ON file_node (parent_id);  -- 创建索引

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
    FOREIGN KEY (class_id) REFERENCES Class(id) ON DELETE CASCADE,
    FOREIGN KEY (operator_id) REFERENCES User(id) ON DELETE CASCADE
);

-- 十一、作业
-- Create Homework table
CREATE TABLE IF NOT EXISTS homework (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    class_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    attachment_url VARCHAR(255),
    object_name VARCHAR(255),
    deadline DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create HomeworkSubmission table
CREATE TABLE IF NOT exists homeworksubmission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    homework_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    file_url VARCHAR(255),
    object_name VARCHAR(255),
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (homework_id) REFERENCES homework(id) ON DELETE CASCADE
);

-- 十二、教学资源
-- Create teaching resource table
CREATE TABLE IF NOT EXISTS teaching_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,           -- 资源标题
    description TEXT,                      -- 资源描述
    course_id BIGINT NOT NULL,             -- 所属课程ID
    chapter_id BIGINT NOT NULL,            -- 所属章节ID
    chapter_name VARCHAR(255) NOT NULL,     -- 章节名称
    resource_type VARCHAR(50) NOT NULL,    -- 资源类型（VIDEO/DOCUMENT等）
    file_url VARCHAR(255) NOT NULL,        -- 文件URL
    object_name VARCHAR(255) NOT NULL,     -- 对象存储中的文件名
    duration INTEGER,                      -- 视频时长（秒）
    created_by BIGINT NOT NULL,            -- 创建者ID
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create learning progress table
CREATE TABLE IF NOT EXISTS learning_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,           -- 教学资源ID
    student_id BIGINT NOT NULL,            -- 学生ID
    progress DECIMAL(5,2) NOT null,       -- 学习进度
    last_position INTEGER NOT NULL,        -- 最后观看位置（秒）
    watch_count INTEGER DEFAULT 0,         -- 观看次数
    last_watch_time DATETIME,              -- 最后观看时间
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_resource_student (resource_id, student_id),  -- 资源和学生的唯一约束
    FOREIGN KEY (resource_id) REFERENCES teaching_resource(id) ON DELETE CASCADE
);



-- 错题库
CREATE TABLE WrongQuestion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    wrong_answer TEXT,
    correct_answer TEXT,
    wrong_count INT DEFAULT 1,
    last_wrong_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Question(id) ON DELETE CASCADE
);

-- 讨论区相关表
CREATE TABLE Discussion (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    creator_num VARCHAR(15) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    is_closed BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES Course(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES Class(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (creator_num) REFERENCES User(user_id) ON DELETE CASCADE
);

CREATE TABLE DiscussionReply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    discussion_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    user_num VARCHAR(15) NOT NULL,
    content TEXT NOT NULL,
    parent_reply_id BIGINT,
    is_teacher_reply BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (discussion_id) REFERENCES Discussion(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE,
    FOREIGN KEY (user_num) REFERENCES User(user_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_reply_id) REFERENCES DiscussionReply(id) ON DELETE CASCADE
);

-- 插入一些用户（一个老师、两个学生）
INSERT INTO User (user_id, username, password_hash, role, email)
VALUES
('T001', '张老师', 'hashed_pwd1', 'teacher', 'teacher@example.com'),
('S001', '小明', 'hashed_pwd2', 'student', 'xiaoming@example.com'),
('S002', '小红', 'hashed_pwd3', 'student', 'xiaohong@example.com');

-- 插入课程（由张老师创建）
INSERT INTO Course (teacher_id, name, code, outline, objective, assessment)
VALUES
(1, '数据库原理', 'CS101', '介绍关系数据库', '掌握SQL', '期末考试+作业');

-- 插入章节
INSERT INTO CourseSection (course_id, title, sort_order)
VALUES
(1, '第一章 数据库基础', 1),
(1, '第二章 SQL语言', 2);

-- 插入班级
INSERT INTO Class (course_id, name, class_code)
VALUES
(1, '数据库-01班', 'DBCLASS01');

-- 插入班级用户
INSERT INTO ClassUser (class_id, user_id)
VALUES
(1, 2), -- 小明加入
(1, 3); -- 小红加入

-- 插入课程与班级关系
INSERT INTO CourseClass (course_id, class_id)
VALUES
(1, 1);

-- 插入题目（由老师创建）
INSERT INTO Question (creator_id, type, content, analysis, options, answer, course_id, section_id)
VALUES
(1, 'singlechoice', '数据库的三大范式是什么？', '详见教材第1章', '1NF|2NF|3NF|BCNF', '1NF|2NF|3NF', 1, 1);

-- 插入练习
INSERT INTO Practice (course_id, class_id, title, start_time, end_time, allow_multiple_submission, created_by)
VALUES
(1, 1, '第一章小测', NOW(), NOW() + INTERVAL 1 DAY, TRUE, 1);

-- 练习关联题目
INSERT INTO PracticeQuestion (practice_id, question_id, score, sort_order)
VALUES
(1, 1, 10, 1);

-- 插入一次提交记录（小明提交）
INSERT INTO Submission (practice_id, student_id, score, is_judged, feedback)
VALUES
(1, 2, 10, 1, '回答正确');

-- 插入答案记录
INSERT INTO Answer (submission_id, question_id, answer_text, is_judged, correct, score, sort_order)
VALUES
(1, 1, '1NF|2NF|3NF', TRUE, TRUE, 10, 1);

-- 插入学习记录
INSERT INTO Progress (student_id, course_id, section_id, completed, completed_at)
VALUES
(2, 1, 1, TRUE, NOW()),
(3, 1, 1, FALSE, NULL);

-- 插入收藏题目
INSERT INTO FavoriteQuestion (student_id, question_id)
VALUES
(2, 1);

-- 插入通知
INSERT INTO Notification (user_id, title, message, type, related_id, related_type)
VALUES
(2, '练习通知', '你有一个新练习待完成', 'practice', 1, 'Practice');

-- 插入文件（课程根目录下的视频文件）
INSERT INTO file_node (file_name, is_dir, parent_id, course_id, class_id, uploader_id, sectiondir_id, file_type, section_id, last_file_version, is_current_version, file_size, visibility, file_url, file_version, object_name)
VALUES
('第一章-视频.mp4', FALSE, NULL, 1, 1, 1, -1, 'VIDEO', -1, 0, TRUE, 102400, 'CLASS_ONLY', '/files/1.mp4', 1, '/course/1/section/1/第一章-视频.mp4');

-- 插入导入记录
INSERT INTO import_record (class_id, operator_id, file_name, total_count, success_count, fail_count, fail_reason, import_time, import_type)
VALUES
(1, 1, 'student_list.xlsx', 2, 2, 0, NULL, NOW(), 'excel');

-- 插入作业
INSERT INTO homework (title, description, class_id, created_by, attachment_url, object_name, deadline)
VALUES
('第一次作业', '请完成ER图设计', 1, 1, '/attachments/hw1.pdf', '/homework/hw1.pdf', NOW() + INTERVAL 7 DAY);

-- 插入作业提交记录（小红提交）
INSERT INTO homeworksubmission (homework_id, student_id, file_url, object_name)

VALUES
    (1, 3, '/attachments/hw1_s2.pdf', '/homework_submissions/s2_hw1.pdf');


-- 插入教学资源
INSERT INTO teaching_resource (title, description, course_id, chapter_id, chapter_name, resource_type, file_url, object_name, duration, created_by)
VALUES
('第一章视频资源', '视频讲解', 1, 1, '第一章 数据库基础', 'VIDEO', '/resource/1.mp4', '/resource/1.mp4', 600, 1);

-- 插入学习进度
INSERT INTO learning_progress (resource_id, student_id, progress, last_position, watch_count)
VALUES
(1, 2, 300, 300, 1),
(1, 3, 120, 120, 1);

-- ###############################################
-- 学生自建练习相关表（AI 自测）
-- ###############################################

-- 学生自建练习主体表
CREATE TABLE SelfPractice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL COMMENT '发起学生 ID',
    title VARCHAR(200) NOT NULL COMMENT '练习标题',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES User(id) ON DELETE CASCADE
);

-- 自建练习与题目关联表
CREATE TABLE SelfPracticeQuestion (
    self_practice_id BIGINT,
    question_id BIGINT,
    sort_order INT DEFAULT 0,
    score INT NOT NULL,
    PRIMARY KEY (self_practice_id, question_id),
    FOREIGN KEY (self_practice_id) REFERENCES SelfPractice(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Question(id) ON DELETE CASCADE
);

-- 进度暂存表
CREATE TABLE SelfProgress (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    self_practice_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    progress_json JSON NOT NULL COMMENT '题目作答进度(JSON)',
    saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (self_practice_id) REFERENCES SelfPractice(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES User(id) ON DELETE CASCADE
);

-- 最终提交表
CREATE TABLE SelfSubmission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    self_practice_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    score INT DEFAULT 0,
    is_judged BOOLEAN DEFAULT FALSE,
    feedback JSON,
    FOREIGN KEY (self_practice_id) REFERENCES SelfPractice(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES User(id) ON DELETE CASCADE
);

-- 提交答案详情表
CREATE TABLE SelfAnswer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    is_judged BOOLEAN DEFAULT FALSE,
    correct BOOLEAN,
    score INT,
    sort_order INT,
    FOREIGN KEY (submission_id) REFERENCES SelfSubmission(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES Question(id) ON DELETE CASCADE
);

-- AI 服务调用日志表
CREATE TABLE AiServiceCallLog (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,              -- 调用者ID (如果可以获取，否则为NULL)
    endpoint VARCHAR(255) NOT NULL, -- 调用的AI服务接口，如 /rag/generate_teaching_content
    duration_ms BIGINT NOT NULL,    -- 耗时，毫秒
    call_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),             -- 调用结果，如 success / fail
    error_message TEXT,             -- 错误信息
    FOREIGN KEY (user_id) REFERENCES User(id) ON DELETE CASCADE
);

-- 教师私密 / 联合知识库配置
CREATE TABLE IF NOT EXISTS TeacherKnowledgeBase (
    teacher_id BIGINT PRIMARY KEY,          -- 教师用户 ID
    paths_json JSON NOT NULL,               -- ["", "D:/KB1", ...]  "" 代表服务器公共库
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                     ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES User(id) ON DELETE CASCADE
);
