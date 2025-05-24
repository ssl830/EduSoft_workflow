package org.example.edusoft.service.notification.impl;

import org.example.edusoft.entity.notification.Notification;
import org.example.edusoft.mapper.notification.NotificationMapper;
import org.example.edusoft.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void createNotification(Notification notification) {
        notificationMapper.insert(notification);
    }

    @Override
    public List<Notification> getUserNotifications(Long userId) {
        return notificationMapper.findByUserId(userId);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        notificationMapper.markAsRead(id);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        notificationMapper.deleteById(id);
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationMapper.findById(id);
    }
} 