package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Notification;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.NotificationRepository;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.WebSocketNotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            WebSocketNotificationService webSocketNotificationService) {
        this.notificationRepository = notificationRepository;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @Override
    public Notification createForRole(String title, String message, String type, String targetRole) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetRole(targetRole);
        notification.setTargetEmail(null);
        notification.setRead(false);
        notification.setActive(true);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        webSocketNotificationService.sendToRole(targetRole, saved);

        return saved;
    }

    @Override
    public Notification createForUser(String title, String message, String type, String targetEmail) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTargetRole("USER");
        notification.setTargetEmail(targetEmail);
        notification.setRead(false);
        notification.setActive(true);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        webSocketNotificationService.sendToUserEmail(targetEmail, saved);

        return saved;
    }

    @Override
    public List<Notification> findByRole(String role) {
        return notificationRepository.findByTargetRoleAndActiveTrueOrderByCreatedAtDesc(role);
    }

    @Override
    public List<Notification> findByUserEmail(String email) {
        return notificationRepository.findByTargetEmailAndActiveTrueOrderByCreatedAtDesc(email);
    }

    @Override
    public Long countUnreadByRole(String role) {
        return notificationRepository.countByTargetRoleAndReadFalseAndActiveTrue(role);
    }

    @Override
    public Long countUnreadByUserEmail(String email) {
        return notificationRepository.countByTargetEmailAndReadFalseAndActiveTrue(email);
    }

    @Override
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    public void markAllAsReadByRole(String role) {
        List<Notification> notifications = findByRole(role);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void markAllAsReadByUserEmail(String email) {
        List<Notification> notifications = findByUserEmail(email);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}