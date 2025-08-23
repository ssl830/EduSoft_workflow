-- 创建视频摘要表
CREATE TABLE `video_summary` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '摘要ID',
  `resource_id` bigint NOT NULL COMMENT '关联的教学资源ID',
  `video_title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '视频标题',
  `course_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '课程名称',
  `summary` text COLLATE utf8mb4_unicode_ci COMMENT '视频内容概述',
  `transcript` longtext COLLATE utf8mb4_unicode_ci COMMENT '视频转写文本',
  `duration` int DEFAULT 0 COMMENT '视频时长(秒)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_id` (`resource_id`),
  KEY `idx_course_name` (`course_name`),
  CONSTRAINT `fk_video_summary_resource` FOREIGN KEY (`resource_id`) REFERENCES `teaching_resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频摘要表';

-- 创建视频摘要阶段表
CREATE TABLE `video_summary_stage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '阶段ID',
  `summary_id` bigint NOT NULL COMMENT '关联的摘要ID',
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '阶段标题',
  `start_time` int NOT NULL DEFAULT 0 COMMENT '开始时间(秒)',
  `end_time` int NOT NULL DEFAULT 0 COMMENT '结束时间(秒)',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '阶段描述',
  `sort_order` int DEFAULT 1 COMMENT '排序序号',
  PRIMARY KEY (`id`),
  KEY `idx_summary_id` (`summary_id`),
  KEY `idx_start_time` (`start_time`),
  CONSTRAINT `fk_stage_summary` FOREIGN KEY (`summary_id`) REFERENCES `video_summary` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频摘要阶段表';

-- 创建视频知识点表
CREATE TABLE `video_summary_keypoint` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
  `summary_id` bigint NOT NULL COMMENT '关联的摘要ID',
  `point` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '知识点内容',
  `timestamp` int DEFAULT 0 COMMENT '相关时间点(秒)',
  `importance` enum('high','medium','low') COLLATE utf8mb4_unicode_ci DEFAULT 'medium' COMMENT '重要程度',
  PRIMARY KEY (`id`),
  KEY `idx_summary_id` (`summary_id`),
  KEY `idx_timestamp` (`timestamp`),
  KEY `idx_importance` (`importance`),
  CONSTRAINT `fk_keypoint_summary` FOREIGN KEY (`summary_id`) REFERENCES `video_summary` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频知识点表';

-- 创建视频重要时间点表
CREATE TABLE `video_summary_timestamp` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '时间点ID',
  `summary_id` bigint NOT NULL COMMENT '关联的摘要ID',
  `time` int NOT NULL COMMENT '时间点(秒)',
  `event` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '重要事件或内容描述',
  `type` enum('concept','example','summary','question') COLLATE utf8mb4_unicode_ci DEFAULT 'concept' COMMENT '事件类型',
  PRIMARY KEY (`id`),
  KEY `idx_summary_id` (`summary_id`),
  KEY `idx_time` (`time`),
  KEY `idx_type` (`type`),
  CONSTRAINT `fk_timestamp_summary` FOREIGN KEY (`summary_id`) REFERENCES `video_summary` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频重要时间点表';
