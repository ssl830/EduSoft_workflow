package org.example.edusoft.entity.notification;

/**
 * 通知类型枚举
 */
public enum NotificationType {
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