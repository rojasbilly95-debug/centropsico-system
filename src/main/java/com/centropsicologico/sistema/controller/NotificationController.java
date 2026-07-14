package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.Notification;
import com.centropsicologico.sistema.service.NotificationService;
import org.springframework.security.core.Authentication;
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
    public List<Notification> findByRole(
            @PathVariable String role,
            Authentication authentication
    ) {
        String currentRole = getCurrentRole(authentication);
        validateRequestedRole(role, currentRole);

        return notificationService.findByRole(currentRole);
    }

    @GetMapping("/me")
    public List<Notification> findMyNotifications(Principal principal) {
        return notificationService.findByUserEmail(principal.getName());
    }

    @GetMapping("/role/{role}/unread-count")
    public Long countUnreadByRole(
            @PathVariable String role,
            Authentication authentication
    ) {
        String currentRole = getCurrentRole(authentication);
        validateRequestedRole(role, currentRole);

        return notificationService.countUnreadByRole(currentRole);
    }

    @GetMapping("/me/unread-count")
    public Long countMyUnreadNotifications(Principal principal) {
        return notificationService.countUnreadByUserEmail(principal.getName());
    }

    @PatchMapping("/{id}/read")
    public Notification markAsRead(
            @PathVariable Long id,
            Principal principal,
            Authentication authentication
    ) {
        return notificationService.markAsReadForCurrentUser(
                id,
                getCurrentRole(authentication),
                principal.getName()
        );
    }

    @PatchMapping("/role/{role}/read-all")
    public void markAllAsReadByRole(
            @PathVariable String role,
            Authentication authentication
    ) {
        String currentRole = getCurrentRole(authentication);
        validateRequestedRole(role, currentRole);

        notificationService.markAllAsReadByRole(currentRole);
    }

    @PatchMapping("/me/read-all")
    public void markMyNotificationsAsRead(Principal principal) {
        notificationService.markAllAsReadByUserEmail(principal.getName());
    }

    private String getCurrentRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            throw new RuntimeException("No se pudo identificar el rol del usuario autenticado");
        }

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElseThrow(() -> new RuntimeException("No se pudo identificar el rol del usuario autenticado"));
    }

    private void validateRequestedRole(String requestedRole, String currentRole) {
        if (requestedRole == null || currentRole == null) {
            throw new RuntimeException("Rol no válido");
        }

        if (!requestedRole.trim().equalsIgnoreCase(currentRole.trim())) {
            throw new RuntimeException("No tienes permiso para consultar notificaciones de otro rol");
        }
    }
}