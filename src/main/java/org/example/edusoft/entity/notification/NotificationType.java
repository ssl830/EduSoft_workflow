package org.example.edusoft.entity.notification;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    COURSE_NOTICE("课程通知"),
    PRACTICE_NOTICE("练习通知"),
    DDL_REMINDER("DDL提醒"),
    TASK_REMINDER("任务提醒"),
    HOMEWORK("作业"),
    TASK("任务"),
    PRACTICE("在线练习");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 