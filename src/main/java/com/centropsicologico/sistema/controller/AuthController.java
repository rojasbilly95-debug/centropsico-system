package com.centropsicologico.sistema.controller;

import com.centropsicologico.sistema.config.JwtUtil;
import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            AuditLogService auditLogService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        String email = normalize(request.get("email"));
        String password = request.get("password");

        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            auditLogService.recordSecurity(
                    "AUTENTICACIÓN",
                    "LOGIN FALLIDO",
                    "User",
                    null,
                    "Intento de inicio de sesión con credenciales incompletas. " + getClientInfo(httpRequest),
                    safeEmail(email),
                    "NO_AUTENTICADO",
                    "WARNING",
                    false);

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            auditLogService.recordSecurity(
                    "AUTENTICACIÓN",
                    "LOGIN FALLIDO",
                    "User",
                    null,
                    "Intento de inicio de sesión con correo no registrado: " + email + ". "
                            + getClientInfo(httpRequest),
                    email,
                    "NO_AUTENTICADO",
                    "WARNING",
                    false);

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        User user = optionalUser.get();

        if (!Boolean.TRUE.equals(user.getActive())) {
            auditLogService.recordSecurity(
                    "AUTENTICACIÓN",
                    "USUARIO INACTIVO",
                    "User",
                    user.getId(),
                    "El usuario " + user.getEmail() + " intentó iniciar sesión, pero está inactivo. "
                            + getClientInfo(httpRequest),
                    user.getEmail(),
                    user.getRole(),
                    "CRITICAL",
                    true);

            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inactivo");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            auditLogService.recordSecurity(
                    "AUTENTICACIÓN",
                    "LOGIN FALLIDO",
                    "User",
                    user.getId(),
                    "Intento fallido de inicio de sesión para el usuario " + user.getEmail()
                            + ". Contraseña incorrecta. " + getClientInfo(httpRequest),
                    user.getEmail(),
                    user.getRole(),
                    "WARNING",
                    false);

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        auditLogService.recordSecurity(
                "AUTENTICACIÓN",
                "LOGIN EXITOSO",
                "User",
                user.getId(),
                "El usuario " + user.getEmail() + " inició sesión correctamente. " + getClientInfo(httpRequest),
                user.getEmail(),
                user.getRole(),
                "INFO",
                false);

        return Map.of(
                "token", token,
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "phone", user.getPhone() == null ? "" : user.getPhone(),
                "profileImageUrl", user.getProfileImageUrl() == null ? "" : user.getProfileImageUrl());
    }

    @GetMapping("/validate")
    public Map<String, Object> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            HttpServletRequest request
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return Map.of("valid", false);
        }

        String token = authorizationHeader.substring(7);
        boolean valid = jwtUtil.isTokenValid(token);

        if (!valid) {
            auditLogService.recordSecurity(
                    "SEGURIDAD",
                    "TOKEN INVÁLIDO",
                    "JWT",
                    null,
                    "Se intentó validar un token inválido o expirado. " + getClientInfo(request),
                    "desconocido",
                    "NO_AUTENTICADO",
                    "WARNING",
                    false
            );

            return Map.of("valid", false);
        }

        String email = jwtUtil.getEmailFromToken(token);

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return Map.of("valid", false);
        }

        User user = optionalUser.get();

        if (!Boolean.TRUE.equals(user.getActive())) {
            return Map.of("valid", false);
        }

        return Map.of(
                "valid", true,
                "id", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "phone", user.getPhone() == null ? "" : user.getPhone(),
                "profileImageUrl", user.getProfileImageUrl() == null ? "" : user.getProfileImageUrl()
        );
    }
    @PostMapping("/logout")
    public Map<String, Object> logout(
            Principal principal,
            HttpServletRequest request) {
        String email = principal != null ? principal.getName() : "desconocido";
        String role = getCurrentRole();

        auditLogService.recordSecurity(
                "AUTENTICACIÓN",
                "CIERRE DE SESIÓN",
                "User",
                null,
                "El usuario " + email + " cerró sesión. " + getClientInfo(request),
                email,
                role,
                "INFO",
                false);

        return Map.of("success", true);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    private String safeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "desconocido";
        }

        return email.trim().toLowerCase();
    }

    private String getClientInfo(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "localhost";
        }

        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null || userAgent.isBlank()) {
            userAgent = "No identificado";
        }

        userAgent = simplifyUserAgent(userAgent);

        return "IP: " + ip + ". Navegador: " + userAgent;
    }

    private String simplifyUserAgent(String userAgent) {
        String browser = "Navegador desconocido";
        String os = "Sistema desconocido";

        if (userAgent.contains("Windows NT 10.0")) {
            os = "Windows 10/11";
        } else if (userAgent.contains("Windows")) {
            os = "Windows";
        } else if (userAgent.contains("Android")) {
            os = "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            os = "iOS";
        } else if (userAgent.contains("Mac OS")) {
            os = "macOS";
        } else if (userAgent.contains("Linux")) {
            os = "Linux";
        }

        if (userAgent.contains("Edg/")) {
            browser = "Microsoft Edge";
        } else if (userAgent.contains("Chrome/")) {
            browser = "Google Chrome";
        } else if (userAgent.contains("Firefox/")) {
            browser = "Mozilla Firefox";
        } else if (userAgent.contains("Safari/")) {
            browser = "Safari";
        }

        return browser + " en " + os;
    }

    private String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getAuthorities() == null) {
            return "NO_AUTENTICADO";
        }

        return authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("NO_AUTENTICADO");
    }
}