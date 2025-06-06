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
    type VARCHAR(20) NOT NULL,
    read_flag BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    related_id BIGINT,
    related_type VARCHAR(50),
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
    FOREIGN KEY (class_id) REFERENCES Class(id),
    FOREIGN KEY (operator_id) REFERENCES User(id)
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
CREATE TABLE IF NOT EXISTShomework_submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    homework_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    submission_type VARCHAR(50) NOT NULL,
    file_url VARCHAR(255),
    object_name VARCHAR(255),
    submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (homework_id) REFERENCES homework(id)
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
    progress INTEGER NOT NULL,             -- 学习进度（秒）
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
    FOREIGN KEY (student_id) REFERENCES User(id),
    FOREIGN KEY (question_id) REFERENCES Question(id)
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
    FOREIGN KEY (course_id) REFERENCES Course(id),
    FOREIGN KEY (class_id) REFERENCES Class(id),
    FOREIGN KEY (creator_id) REFERENCES User(id),
    FOREIGN KEY (creator_num) REFERENCES User(user_id)
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
    FOREIGN KEY (discussion_id) REFERENCES Discussion(id),
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (user_num) REFERENCES User(user_id),
    FOREIGN KEY (parent_reply_id) REFERENCES DiscussionReply(id)
); 

-- 数据插入部分（修复 INSERT）
INSERT INTO User (user_id, username, password_hash, role, email) VALUES
('20230001', '张三', '$2a$10$xJwL5v5zL3j2VJ5V5X5X5u', 'student', 'zhangsan@example.com'),
('20230002', '李四', '$2a$10$xJwL5v5zL3j2VJ5V5X5X5u', 'student', 'lisi@example.com'),
('T2023001', '王教授', '$2a$10$xJwL5v5zL3j2VJ5V5X5X5u', 'teacher', 'wang@example.com'),
('T2023002', '李教授', '$2a$10$xJwL5v5zL3j2VJ5V5X5X5u', 'teacher', 'li@example.com'),
('TA202301', '助教刘', '$2a$10$xJwL5v5zL3j2VJ5V5X5X5u', 'tutor', 'liu@example.com');

-- 2. 课程表数据
INSERT INTO Course (teacher_id, name, code, outline, objective, assessment) VALUES
(3, '数据库系统原理', 'CS101', '本课程介绍数据库系统的基本概念和原理', '掌握SQL语言和数据库设计', '平时作业30%，期末考试70%'),
(3, '算法设计与分析', 'CS102', '本课程介绍常用算法和复杂度分析', '掌握基本算法设计和分析方法', '编程作业40%，期末考试60%'),
(4, '计算机网络', 'CS201', '本课程介绍计算机网络基本原理', '理解网络协议和体系结构', '实验30%，期末考试70%'),
(4, '操作系统', 'CS202', '本课程介绍操作系统核心概念', '理解进程管理、内存管理等', '项目40%，期末考试60%'),
(3, '软件工程', 'CS301', '本课程介绍软件开发流程和方法', '掌握软件生命周期和设计模式', '团队项目60%，个人作业40%');

-- 3. 课程章节数据
INSERT INTO CourseSection (course_id, title, sort_order) VALUES
(1, '数据库概述', 1),
(1, '关系数据库', 2),
(1, 'SQL语言', 3),
(2, '算法基础', 1),
(2, '排序算法', 2),
(3, '网络体系结构', 1),
(4, '进程管理', 1),
(5, '软件生命周期', 1);

-- 4. 班级数据
INSERT INTO Class (course_id, name, class_code) VALUES
(1, '数据库系统2023秋季班', 'DB2023FALL'),
(1, '数据库系统2023春季班', 'DB2023SPR'),
(2, '算法设计2023班', 'ALGO2023'),
(3, '计算机网络2023班', 'NET2023'),
(4, '操作系统2023班', 'OS2023');

-- 5. 班级成员数据
INSERT INTO ClassUser (class_id, user_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 5),
(2, 1), (2, 3),
(3, 1), (3, 2), (3, 4),
(4, 2), (4, 4),
(5, 1), (5, 4);

-- 6. 课程班级关联数据
INSERT INTO CourseClass (course_id, class_id) VALUES
(1, 1), (1, 2),
(2, 3),
(3, 4),
(4, 5);

-- 7. 题库数据
INSERT INTO Question (creator_id, type, content, analysis, options, answer, course_id, section_id) VALUES
(3, 'singlechoice', 'SQL中用于查询数据的关键字是？', 'SELECT用于查询数据', '["INSERT", "SELECT", "UPDATE", "DELETE"]', 'SELECT', 1, 3),
(3, 'fillblank', '关系数据库的三大范式分别是___、___和___', '第一范式、第二范式、第三范式', NULL, '第一范式,第二范式,第三范式', 1, 2),
(4, 'program', '实现快速排序算法', '分治思想的应用', NULL, 'function quickSort(arr) {...}', 2, 2),
(3, 'singlechoice', '以下哪个不是数据库事务的特性？', '事务特性是ACID', '["原子性", "一致性", "隔离性", "持久性", "可靠性"]', '可靠性', 1, 2),
(4, 'fillblank', 'TCP/IP模型中，传输层的主要协议是___和___', 'TCP和UDP', NULL, 'TCP,UDP', 3, 1);

-- 8. 练习数据
INSERT INTO Practice (course_id, class_id, title, start_time, end_time, allow_multiple_submission, created_by) VALUES
(1, 1, 'SQL基础练习', '2023-09-01 00:00:00', '2023-09-30 23:59:59', TRUE, 3),
(1, 1, '数据库设计作业', '2023-10-01 00:00:00', '2023-10-15 23:59:59', FALSE, 3),
(2, 3, '排序算法实现', '2023-09-15 00:00:00', '2023-09-30 23:59:59', TRUE, 4),
(3, 4, '网络协议测试', '2023-10-01 00:00:00', '2023-10-31 23:59:59', TRUE, 4),
(4, 5, '进程同步练习', '2023-11-01 00:00:00', '2023-11-30 23:59:59', FALSE, 4);

-- 9. 练习题目关联数据
INSERT INTO PracticeQuestion (practice_id, question_id, score) VALUES
(1, 1, 10), (1, 2, 20),
(2, 4, 15),
(3, 3, 30),
(4, 5, 20),
(5, 3, 25);

-- 10. 提交数据
INSERT INTO Submission (practice_id, student_id, score, is_judged, feedback) VALUES
(1, 1, 25, 1, '完成得很好'),
(1, 2, 20, 1, '第二题需要改进'),
(2, 1, 15, 1, '正确'),
(3, 1, 30, 1, '算法实现正确'),
(4, 2, 20, 1, '回答完整');

-- 11. 答案数据
INSERT INTO Answer (submission_id, question_id, answer_text, is_judged, correct, score) VALUES
(1, 1, 'SELECT', TRUE, TRUE, 10),
(1, 2, '第一范式,第二范式,第三范式', TRUE, TRUE, 15),
(2, 1, 'SELECT', TRUE, TRUE, 10),
(2, 2, '第一范式,第二范式', TRUE, FALSE, 10),
(3, 4, '可靠性', TRUE, TRUE, 15),
(4, 3, 'function quickSort(arr) {...}', TRUE, TRUE, 30),
(5, 5, 'TCP,UDP', TRUE, TRUE, 20);

-- 12. 学习进度数据
INSERT INTO Progress (student_id, course_id, section_id, completed, completed_at) VALUES
(1, 1, 1, TRUE, '2023-09-10 10:00:00'),
(1, 1, 2, TRUE, '2023-09-15 11:00:00'),
(2, 1, 1, TRUE, '2023-09-12 09:00:00'),
(1, 2, 1, TRUE, '2023-09-20 14:00:00'),
(2, 3, 1, FALSE, NULL);

-- 13. 收藏题目数据
INSERT INTO FavoriteQuestion (student_id, question_id) VALUES
(1, 1), (1, 3),
(2, 2), (2, 5),
(1, 4);

-- 14. 通知数据
INSERT INTO Notification (user_id, title, message, type, read_flag, related_id, related_type) VALUES
(1, '作业发布', 'SQL基础练习已发布', 'practice', FALSE, 1, 'practice'),
(2, '作业发布', 'SQL基础练习已发布', 'practice', FALSE, 1, 'practice'),
(3, '学生提问', '学生李四在讨论区提问', 'discussion', FALSE, 1, 'discussion'),
(1, '成绩公布', '数据库设计作业成绩已公布', 'submission', FALSE, 3, 'submission'),
(5, '助教任务', '请批改算法作业', 'task', FALSE, 3, 'practice');

-- 15. 文件节点数据
INSERT INTO file_node (file_name, is_dir, parent_id, course_id, class_id, uploader_id, file_type, file_size, visibility, file_url, object_name) VALUES
('课件', TRUE, NULL, 1, 1, 3, 'OTHER', 0, 'CLASS_ONLY', NULL, NULL),
('第一章', TRUE, 1, 1, 1, 3, 'OTHER', 0, 'CLASS_ONLY', NULL, NULL),
('数据库概述.pdf', FALSE, 2, 1, 1, 3, 'PDF', 1024, 'CLASS_ONLY', '/files/db_intro.pdf', 'course/1/section/1/db_intro.pdf'),
('实验指导', TRUE, NULL, 1, 1, 5, 'OTHER', 0, 'CLASS_ONLY', NULL, NULL),
('实验1.docx', FALSE, 4, 1, 1, 5, 'OTHER', 2048, 'CLASS_ONLY', '/files/lab1.docx', 'course/1/section/1/lab1.docx');

-- 16. 导入记录数据
INSERT INTO import_record (class_id, operator_id, file_name, total_count, success_count, fail_count, fail_reason, import_time, import_type) VALUES
(1, 3, 'students.xlsx', 50, 48, 2, '2名学生学号格式错误', '2023-09-01 10:00:00', 'student'),
(2, 3, 'students.xlsx', 30, 30, 0, NULL, '2023-03-01 09:30:00', 'student'),
(3, 4, 'questions.xlsx', 20, 18, 2, '2道题目格式不符合要求', '2023-09-15 14:00:00', 'question'),
(4, 4, 'students.xlsx', 40, 40, 0, NULL, '2023-09-10 11:00:00', 'student'),
(5, 4, 'resources.zip', 15, 15, 0, NULL, '2023-10-01 16:00:00', 'resource');

-- 17. 作业数据
INSERT INTO homework (title, description, class_id, created_by, attachment_url, object_name, deadline) VALUES
('数据库设计作业', '设计一个学生选课系统的ER图', 1, 3, '/homeworks/db_design.pdf', 'course/1/homework/db_design.pdf', '2023-10-15 23:59:59'),
('算法实现作业', '实现归并排序算法', 3, 4, '/homeworks/merge_sort.pdf', 'course/2/homework/merge_sort.pdf', '2023-09-30 23:59:59'),
('网络协议分析', '分析TCP三次握手过程', 4, 4, '/homeworks/tcp_analysis.pdf', 'course/3/homework/tcp_analysis.pdf', '2023-10-31 23:59:59'),
('进程同步实验', '使用信号量解决生产者消费者问题', 5, 4, '/homeworks/producer_consumer.pdf', 'course/4/homework/producer_consumer.pdf', '2023-11-30 23:59:59'),
('软件需求分析', '编写一个软件的需求文档', 1, 3, '/homeworks/requirements.pdf', 'course/1/homework/requirements.pdf', '2023-11-15 23:59:59');

-- 18. 作业提交数据
INSERT INTO homework_submission (homework_id, student_id, submission_type, file_url, object_name) VALUES
(1, 1, 'file', '/submissions/db_design_zhangsan.pdf', 'course/1/submission/db_design_zhangsan.pdf'),
(1, 2, 'file', '/submissions/db_design_lisi.docx', 'course/1/submission/db_design_lisi.docx'),
(2, 1, 'file', '/submissions/merge_sort_zhangsan.zip', 'course/2/submission/merge_sort_zhangsan.zip'),
(3, 2, 'file', '/submissions/tcp_analysis_lisi.pdf', 'course/3/submission/tcp_analysis_lisi.pdf'),
(5, 1, 'file', '/submissions/requirements_zhangsan.docx', 'course/1/submission/requirements_zhangsan.docx');

-- 19. 教学资源数据
INSERT INTO teaching_resource (title, description, course_id, chapter_id, chapter_name, resource_type, file_url, object_name, duration, created_by) VALUES
('数据库概述视频', '数据库系统介绍视频', 1, 1, '数据库概述', 'VIDEO', '/resources/db_intro.mp4', 'course/1/section/1/db_intro.mp4', 1800, 3),
('SQL学习文档', 'SQL语言详细教程', 1, 3, 'SQL语言', 'DOCUMENT', '/resources/sql_tutorial.pdf', 'course/1/section/3/sql_tutorial.pdf', NULL, 3),
('算法基础视频', '算法设计与分析基础', 2, 1, '算法基础', 'VIDEO', '/resources/algo_basic.mp4', 'course/2/section/1/algo_basic.mp4', 2400, 4),
('网络协议PPT', 'TCP/IP协议详解', 3, 1, '网络体系结构', 'PPT', '/resources/tcp_ip.pptx', 'course/3/section/1/tcp_ip.pptx', NULL, 4),
('进程管理实验', '进程同步实验指导', 4, 1, '进程管理', 'DOCUMENT', '/resources/process_lab.pdf', 'course/4/section/1/process_lab.pdf', NULL, 4);

-- 20. 学习进度数据
INSERT INTO learning_progress (resource_id, student_id, progress, last_position, watch_count, last_watch_time) VALUES
(1, 1, 1800, 1800, 2, '2023-09-10 10:30:00'),
(1, 2, 1200, 1200, 1, '2023-09-12 09:30:00'),
(3, 1, 2400, 2400, 1, '2023-09-20 14:30:00'),
(2, 1, 0, 0, 0, NULL),
(4, 2, 600, 600, 1, '2023-10-05 15:00:00');

-- 21. 错题库数据
INSERT INTO WrongQuestion (student_id, question_id, wrong_answer, correct_answer, wrong_count) VALUES
(1, 2, '第一范式,第二范式', '第一范式,第二范式,第三范式', 2),
(2, 1, 'INSERT', 'SELECT', 1),
(1, 4, '一致性', '可靠性', 1),
(2, 5, 'TCP,IP', 'TCP,UDP', 3),
(1, 3, 'function bubbleSort(arr) {...}', 'function quickSort(arr) {...}', 1);

-- 22. 讨论区数据
INSERT INTO Discussion (course_id, class_id, creator_id, creator_num, title, content, is_pinned, view_count, reply_count) VALUES
(1, 1, 1, '20230001', '关于第一范式的疑问', '第一范式的定义中，属性不可再分是什么意思？', FALSE, 15, 3),
(1, 1, 3, 'T2023001', '重要通知：作业提交延期', '因系统维护，作业提交截止时间延长至10月20日', TRUE, 30, 0),
(2, 3, 2, '20230002', '快速排序时间复杂度问题', '为什么快速排序的平均时间复杂度是O(nlogn)?', FALSE, 20, 5),
(3, 4, 4, 'T2023002', '实验环境配置指南', '请同学们按照以下步骤配置实验环境...', TRUE, 25, 2),
(1, 1, 5, 'TA202301', 'SQL作业常见问题解答', '以下是同学们在SQL作业中遇到的常见问题...', FALSE, 18, 4);

-- 23. 讨论回复数据
INSERT INTO DiscussionReply (discussion_id, user_id, user_num, content, parent_reply_id, is_teacher_reply) VALUES
(1, 3, 'T2023001', '指的是属性不能再分解为更小的数据项', NULL, TRUE),
(1, 5, 'TA202301', '例如，地址属性可以再分为省、市、街道等', NULL, TRUE),
(1, 2, '20230002', '明白了，谢谢老师！', 1, FALSE),
(3, 4, 'T2023002', '这是因为快速排序采用了分治策略', NULL, TRUE),
(5, 1, '20230001', '关于第三题，JOIN和WHERE的区别是什么？', NULL, FALSE);

-- 24. 讨论点赞数据
INSERT INTO DiscussionLike (discussion_id, user_id) VALUES
(1, 2), (1, 5),
(3, 1), (3, 2), (3, 5),
(5, 1), (5, 2);