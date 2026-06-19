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
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final long MAX_PHOTO_SIZE = 5 * 1024 * 1024;
    private static final String UPLOAD_DIR = "uploads/profiles";

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

    @PostMapping("/photo")
    public Map<String, Object> uploadProfilePhoto(
            Principal principal,
            @RequestParam("photo") MultipartFile photo
    ) {
        System.out.println("====================================");
        System.out.println(">>> ENTRÓ A SUBIR FOTO DE PERFIL");
        System.out.println("====================================");

        User user = getAuthenticatedUser(principal);

        System.out.println(">>> Usuario autenticado: " + user.getEmail());

        if (photo == null || photo.isEmpty()) {
            System.out.println(">>> ERROR: No se recibió imagen.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona una imagen");
        }

        System.out.println(">>> Archivo recibido: " + photo.getOriginalFilename());
        System.out.println(">>> Tipo recibido: " + photo.getContentType());
        System.out.println(">>> Tamaño recibido: " + photo.getSize());

        if (photo.getSize() > MAX_PHOTO_SIZE) {
            System.out.println(">>> ERROR: Imagen supera 5MB.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen no debe superar 5MB");
        }

        String contentType = photo.getContentType();

        if (contentType == null || !isAllowedImage(contentType)) {
            System.out.println(">>> ERROR: Tipo de imagen no permitido: " + contentType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten imágenes JPG, PNG o WEBP");
        }

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();

            System.out.println(">>> Carpeta destino: " + uploadPath);

            Files.createDirectories(uploadPath);

            String extension = getExtension(contentType);
            String fileName = "user-" + user.getId() + "-" + UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(fileName).normalize();

            System.out.println(">>> Archivo final: " + filePath);

            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String publicUrl = "/uploads/profiles/" + fileName;

            user.setProfileImageUrl(publicUrl);
            user.setUpdatedAt(LocalDateTime.now());

            User saved = userRepository.save(user);

            System.out.println(">>> Foto guardada en BD: " + saved.getProfileImageUrl());

            auditLogService.recordSecurity(
                    "PERFIL",
                    "ACTUALIZACIÓN DE FOTO",
                    "User",
                    saved.getId(),
                    "El usuario actualizó su foto de perfil.",
                    saved.getEmail(),
                    saved.getRole(),
                    "INFO",
                    false
            );

            return buildProfileResponse(saved);

        } catch (IOException error) {
            System.out.println(">>> ERROR AL GUARDAR IMAGEN:");
            error.printStackTrace();

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar la imagen"
            );
        }
    }

    private User getAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no autenticado");
        }

        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private Map<String, Object> buildProfileResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "firstName", safe(user.getFirstName()),
                "lastName", safe(user.getLastName()),
                "email", safe(user.getEmail()),
                "role", safe(user.getRole()),
                "phone", safe(user.getPhone()),
                "profileImageUrl", safe(user.getProfileImageUrl()),
                "active", Boolean.TRUE.equals(user.getActive())
        );
    }

    private boolean isAllowedImage(String contentType) {
        return contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/webp");
    }

    private String getExtension(String contentType) {
        if (contentType.equalsIgnoreCase("image/png")) {
            return ".png";
        }

        if (contentType.equalsIgnoreCase("image/webp")) {
            return ".webp";
        }

        return ".jpg";
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}