package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Notification;

import java.util.List;

public interface NotificationService {

    Notification createForRole(String title, String message, String type, String targetRole);

    Notification createForUser(String title, String message, String type, String targetEmail);

    List<Notification> findByRole(String role);

    List<Notification> findByUserEmail(String email);

    Long countUnreadByRole(String role);

    Long countUnreadByUserEmail(String email);

    Notification markAsRead(Long id);

    Notification markAsReadForCurrentUser(Long id, String currentRole, String currentEmail);

    void markAllAsReadByRole(String role);

    void markAllAsReadByUserEmail(String email);
}