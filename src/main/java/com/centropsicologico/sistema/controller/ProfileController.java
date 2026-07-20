package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final long MAX_PHOTO_SIZE = 2 * 1024 * 1024;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public ProfileController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/me")
    public Map<String, Object> getMyProfile(Principal principal) {
        User user = getAuthenticatedUser(principal);
        return buildProfileResponse(user);
    }

    @PutMapping("/me")
    public Map<String, Object> updateMyProfile(
            Principal principal,
            @RequestBody Map<String, String> request
    ) {
        User user = getAuthenticatedUser(principal);

        String firstName = clean(request.get("firstName"));
        String lastName = clean(request.get("lastName"));
        String phone = clean(request.get("phone"));

        if (firstName == null || firstName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El apellido es obligatorio");
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        auditLogService.recordSecurity(
                "PERFIL",
                "ACTUALIZACIÓN DE PERFIL",
                "User",
                saved.getId(),
                "El usuario actualizó sus datos de perfil.",
                saved.getEmail(),
                saved.getRole(),
                "INFO",
                false
        );

        return buildProfileResponse(saved);
    }

    @PutMapping("/change-password")
    public Map<String, Object> changePassword(
            Principal principal,
            @RequestBody Map<String, String> request
    ) {
        User user = getAuthenticatedUser(principal);

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa tu contraseña actual");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresa la nueva contraseña");
        }

        if (newPassword.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe tener al menos 6 caracteres");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Las contraseñas no coinciden");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            auditLogService.recordSecurity(
                    "PERFIL",
                    "CAMBIO DE CONTRASEÑA FALLIDO",
                    "User",
                    user.getId(),
                    "El usuario intentó cambiar su contraseña, pero ingresó una contraseña actual incorrecta.",
                    user.getEmail(),
                    user.getRole(),
                    "WARNING",
                    false
            );

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        auditLogService.recordSecurity(
                "PERFIL",
                "CAMBIO DE CONTRASEÑA",
                "User",
                saved.getId(),
                "El usuario cambió su contraseña correctamente.",
                saved.getEmail(),
                saved.getRole(),
                "INFO",
                false
        );

        return Map.of(
                "success", true,
                "message", "Contraseña actualizada correctamente"
        );
    }

    /*
     * Foto de perfil persistente.
     * Antes se guardaba en /uploads/profiles y se perdía al redeplegar en Render.
     * Ahora se convierte a Base64 y se guarda en la base de datos Aiven MySQL.
     */
    @PostMapping("/photo")
    public Map<String, Object> uploadProfilePhoto(
            Principal principal,
            @RequestParam("photo") MultipartFile photo
    ) {
        User user = getAuthenticatedUser(principal);

        if (photo == null || photo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen");
        }

        if (photo.getSize() > MAX_PHOTO_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen no debe superar 2MB");
        }

        String contentType = photo.getContentType();

        if (contentType == null || !isAllowedImage(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten imágenes JPG, PNG o WEBP");
        }

        try {
            String base64 = Base64.getEncoder().encodeToString(photo.getBytes());
            String base64Image = "data:" + contentType + ";base64," + base64;

            user.setProfileImageBase64(base64Image);

            /*
             * Limpiamos la URL antigua para evitar que el sistema intente cargar
             * una imagen física que podría desaparecer en Render.
             */
            user.setProfileImageUrl(null);
            user.setUpdatedAt(LocalDateTime.now());

            User saved = userRepository.save(user);

            auditLogService.recordSecurity(
                    "PERFIL",
                    "ACTUALIZACIÓN DE FOTO",
                    "User",
                    saved.getId(),
                    "El usuario actualizó su foto de perfil y fue almacenada en la base de datos.",
                    saved.getEmail(),
                    saved.getRole(),
                    "INFO",
                    false
            );

            return buildProfileResponse(saved);

        } catch (IOException error) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo procesar la imagen"
            );
        }
    }

    /*
     * Endpoint opcional por si luego quieres eliminar la foto.
     */
    @DeleteMapping("/photo")
    public Map<String, Object> deleteProfilePhoto(Principal principal) {
        User user = getAuthenticatedUser(principal);

        user.setProfileImageBase64(null);
        user.setProfileImageUrl(null);
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        auditLogService.recordSecurity(
                "PERFIL",
                "ELIMINACIÓN DE FOTO",
                "User",
                saved.getId(),
                "El usuario eliminó su foto de perfil.",
                saved.getEmail(),
                saved.getRole(),
                "INFO",
                false
        );

        return buildProfileResponse(saved);
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private Map<String, Object> buildProfileResponse(User user) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", user.getId());
        response.put("firstName", safe(user.getFirstName()));
        response.put("lastName", safe(user.getLastName()));
        response.put("email", safe(user.getEmail()));
        response.put("role", safe(user.getRole()));
        response.put("phone", safe(user.getPhone()));
        response.put("profileImageUrl", safe(user.getProfileImageUrl()));
        response.put("profileImageBase64", safe(user.getProfileImageBase64()));
        response.put("active", Boolean.TRUE.equals(user.getActive()));

        return response;
    }

    private boolean isAllowedImage(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/webp");
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}