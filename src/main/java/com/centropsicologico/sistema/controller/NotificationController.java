package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.Notification;
import com.centropsicologico.sistema.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/role/{role}")
    public List<Notification> findByRole(@PathVariable String role) {
        return notificationService.findByRole(role);
    }

    @GetMapping("/me")
    public List<Notification> findMyNotifications(Principal principal) {
        return notificationService.findByUserEmail(principal.getName());
    }

    @GetMapping("/role/{role}/unread-count")
    public Long countUnreadByRole(@PathVariable String role) {
        return notificationService.countUnreadByRole(role);
    }

    @GetMapping("/me/unread-count")
    public Long countMyUnreadNotifications(Principal principal) {
        return notificationService.countUnreadByUserEmail(principal.getName());
    }

    @PatchMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    @PatchMapping("/role/{role}/read-all")
    public void markAllAsReadByRole(@PathVariable String role) {
        notificationService.markAllAsReadByRole(role);
    }

    @PatchMapping("/me/read-all")
    public void markMyNotificationsAsRead(Principal principal) {
        notificationService.markAllAsReadByUserEmail(principal.getName());
    }
}