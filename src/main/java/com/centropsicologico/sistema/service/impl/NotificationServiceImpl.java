package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.Notification;
import com.centropsicologico.sistema.exception.BusinessRuleException;
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
            WebSocketNotificationService webSocketNotificationService
    ) {
        this.notificationRepository = notificationRepository;
        this.webSocketNotificationService = webSocketNotificationService;
    }

    @Override
    public Notification createForRole(String title, String message, String type, String targetRole) {
        Notification notification = new Notification();

        notification.setTitle(clean(title));
        notification.setMessage(clean(message));
        notification.setType(clean(type));
        notification.setTargetRole(clean(targetRole));
        notification.setTargetEmail(null);
        notification.setRead(false);
        notification.setActive(true);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        webSocketNotificationService.sendToRole(saved.getTargetRole(), saved);

        return saved;
    }

    @Override
    public Notification createForUser(String title, String message, String type, String targetEmail) {
        Notification notification = new Notification();

        notification.setTitle(clean(title));
        notification.setMessage(clean(message));
        notification.setType(clean(type));
        notification.setTargetRole("USER");
        notification.setTargetEmail(cleanEmail(targetEmail));
        notification.setRead(false);
        notification.setActive(true);
        notification.setCreatedAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);

        webSocketNotificationService.sendToUserEmail(saved.getTargetEmail(), saved);

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
    public Notification markAsReadForCurrentUser(Long id, String currentRole, String currentEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con id: " + id));

        if (Boolean.FALSE.equals(notification.getActive())) {
            throw new ResourceNotFoundException("Notificación no encontrada");
        }

        if (!belongsToCurrentUser(notification, currentRole, currentEmail)) {
            throw new BusinessRuleException("No tienes permiso para leer esta notificación");
        }

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    @Override
    public void markAllAsReadByRole(String role) {
        List<Notification> notifications = findByRole(role);

        notifications.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    @Override
    public void markAllAsReadByUserEmail(String email) {
        List<Notification> notifications = findByUserEmail(email);

        notifications.forEach(notification -> notification.setRead(true));

        notificationRepository.saveAll(notifications);
    }

    private boolean belongsToCurrentUser(
            Notification notification,
            String currentRole,
            String currentEmail
    ) {
        boolean belongsToRole = notification.getTargetRole() != null
                && currentRole != null
                && notification.getTargetRole().equalsIgnoreCase(currentRole);

        boolean belongsToEmail = notification.getTargetEmail() != null
                && currentEmail != null
                && notification.getTargetEmail().equalsIgnoreCase(currentEmail);

        return belongsToRole || belongsToEmail;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isEmpty() ? null : cleaned;
    }

    private String cleanEmail(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim().toLowerCase();

        return cleaned.isEmpty() ? null : cleaned;
    }
}