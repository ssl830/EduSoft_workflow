package org.example.edusoft.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.example.edusoft.entity.notification.Notification;
import org.example.edusoft.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationUtilsTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationUtils notificationUtils;

    @BeforeEach
    void setUp() {
        // 重置mock对象
        reset(notificationService);
    }

    @Test
    void testCreatePracticeNotification_Success() {
        // 准备测试数据
        Long practiceId = 1L;
        String practiceTitle = "测试练习";
        Long courseId = 1L;
        Long classId = 1L;
        List<Long> studentIds = Arrays.asList(1L, 2L, 3L);

        // 执行测试
        notificationUtils.createPracticeNotification(practiceId, practiceTitle, courseId, classId, studentIds);

        // 验证结果 - 应该为每个学生创建通知
        verify(notificationService, times(3)).createNotification(any(Notification.class));
    }

    @Test
    void testCreatePracticeNotification_EmptyStudentList() {
        // 准备测试数据 - 空的学生列表
        Long practiceId = 1L;
        String practiceTitle = "测试练习";
        Long courseId = 1L;
        Long classId = 1L;
        List<Long> studentIds = Collections.emptyList();

        // 执行测试
        notificationUtils.createPracticeNotification(practiceId, practiceTitle, courseId, classId, studentIds);

        // 验证结果 - 不应该创建任何通知
        verify(notificationService, never()).createNotification(any(Notification.class));
    }

    @Test
    void testCreateHomeworkNotification_Success() {
        // 准备测试数据
        Long homeworkId = 1L;
        String homeworkTitle = "测试作业";
        Long courseId = 1L;
        Long classId = 1L;
        List<Long> studentIds = Arrays.asList(1L, 2L);

        // 执行测试
        notificationUtils.createHomeworkNotification(homeworkId, homeworkTitle, courseId, classId, studentIds);

        // 验证结果 - 应该为每个学生创建通知
        verify(notificationService, times(2)).createNotification(any(Notification.class));
    }

    @Test
    void testCreateHomeworkNotification_NullStudentList() {
        // 准备测试数据 - null的学生列表
        Long homeworkId = 1L;
        String homeworkTitle = "测试作业";
        Long courseId = 1L;
        Long classId = 1L;
        List<Long> studentIds = null;

        // 执行测试 - 这里会抛出NullPointerException
        try {
            notificationUtils.createHomeworkNotification(homeworkId, homeworkTitle, courseId, classId, studentIds);
        } catch (NullPointerException e) {
            // 预期抛出异常
        }

        // 验证结果 - 不应该创建任何通知
        verify(notificationService, never()).createNotification(any(Notification.class));
    }

    @Test
    void testCreateTaskNotification_Success() {
        // 准备测试数据
        Long taskId = 1L;
        String taskTitle = "测试任务";
        Long courseId = 1L;
        Long classId = 1L;
        List<Long> studentIds = Arrays.asList(1L);

        // 执行测试
        notificationUtils.createTaskNotification(taskId, taskTitle, courseId, classId, studentIds);

        // 验证结果 - 应该为每个学生创建通知
        verify(notificationService, times(1)).createNotification(any(Notification.class));
    }

    @Test
    void testCreateTaskNotification_SingleStudent() {
        // 准备测试数据 - 单个学生
        Long taskId = 1L;
        String taskTitle = "测试任务";
        Long courseId = 1L;
        Long classId = 1L;
        List<Long> studentIds = Arrays.asList(1L);

        // 执行测试
        notificationUtils.createTaskNotification(taskId, taskTitle, courseId, classId, studentIds);

        // 验证结果 - 应该为每个学生创建通知
        verify(notificationService, times(1)).createNotification(any(Notification.class));
    }
}

