package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.Notification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendToRole(String role, Notification notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/role/" + role,
                notification
        );
    }

    public void sendToUserEmail(String email, Notification notification) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/user/" + email,
                notification
        );
    }
}