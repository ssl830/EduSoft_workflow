package org.example.edusoft.controller.notification;

import org.example.edusoft.common.Result;
import org.example.edusoft.entity.notification.Notification;
import org.example.edusoft.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // getUserNotifications
    @Test
    void testGetUserNotifications_Success() {
        List<Notification> notifications = new ArrayList<>();
        Notification n = new Notification();
        n.setId(1L);
        n.setTitle("通知");
        notifications.add(n);

        when(notificationService.getUserNotifications(100L)).thenReturn(notifications);

        Result<List<Notification>> result = notificationController.getUserNotifications(100L);
        assertEquals(200, result.getCode());
        assertEquals(notifications, result.getData());
    }

    @Test
    void testGetUserNotifications_Failure() {
        when(notificationService.getUserNotifications(100L)).thenThrow(new RuntimeException("fail"));
        Result<List<Notification>> result = notificationController.getUserNotifications(100L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // getUnreadCount
    @Test
    void testGetUnreadCount_Success() {
        when(notificationService.getUnreadCount(100L)).thenReturn(5);
        Result<Map<String, Integer>> result = notificationController.getUnreadCount(100L);
        assertEquals(200, result.getCode());
        assertEquals(5, result.getData().get("count"));
    }

    @Test
    void testGetUnreadCount_Failure() {
        when(notificationService.getUnreadCount(100L)).thenThrow(new RuntimeException("fail"));
        Result<Map<String, Integer>> result = notificationController.getUnreadCount(100L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // markAsRead
    @Test
    void testMarkAsRead_Success() {
        doNothing().when(notificationService).markAsRead(1L);
        Result<Void> result = notificationController.markAsRead(1L);
        assertEquals(200, result.getCode());
        assertEquals("通知已标记为已读", result.getMessage());
    }

    @Test
    void testMarkAsRead_Failure() {
        doThrow(new RuntimeException("fail")).when(notificationService).markAsRead(1L);
        Result<Void> result = notificationController.markAsRead(1L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // markAllAsRead
    @Test
    void testMarkAllAsRead_Success() {
        doNothing().when(notificationService).markAllAsRead(100L);
        Result<Void> result = notificationController.markAllAsRead(100L);
        assertEquals(200, result.getCode());
        assertEquals("所有通知已标记为已读", result.getMessage());
    }

    @Test
    void testMarkAllAsRead_Failure() {
        doThrow(new RuntimeException("fail")).when(notificationService).markAllAsRead(100L);
        Result<Void> result = notificationController.markAllAsRead(100L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }

    // deleteNotification
    @Test
    void testDeleteNotification_Success() {
        doNothing().when(notificationService).deleteNotification(1L);
        Result<Void> result = notificationController.deleteNotification(1L);
        assertEquals(200, result.getCode());
        assertEquals("通知已删除", result.getMessage());
    }

    @Test
    void testDeleteNotification_Failure() {
        doThrow(new RuntimeException("fail")).when(notificationService).deleteNotification(1L);
        Result<Void> result = notificationController.deleteNotification(1L);
        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("fail"));
    }
}