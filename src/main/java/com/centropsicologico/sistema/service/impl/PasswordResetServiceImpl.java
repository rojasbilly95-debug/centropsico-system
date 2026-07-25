package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.PasswordResetToken;
import com.centropsicologico.sistema.entity.User;

import com.centropsicologico.sistema.exception.BusinessRuleException;

import com.centropsicologico.sistema.repository.PasswordResetTokenRepository;
import com.centropsicologico.sistema.repository.UserRepository;

import com.centropsicologico.sistema.service.AuditLogService;
import com.centropsicologico.sistema.service.EmailService;
import com.centropsicologico.sistema.service.PasswordResetService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.time.LocalDateTime;

import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class PasswordResetServiceImpl
        implements PasswordResetService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PasswordResetServiceImpl.class
            );

    private static final String GENERIC_RESPONSE =
            "Si existe una cuenta asociada al correo, "
                    + "recibirás un enlace para restablecer "
                    + "tu contraseña.";

    private static final String INVALID_TOKEN_MESSAGE =
            "El enlace de recuperación es inválido, "
                    + "ya fue utilizado o ha expirado.";

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    @Value(
            "${app.password-reset.expiration-minutes:15}"
    )
    private long expirationMinutes;

    @Value(
            "${app.password-reset.cooldown-seconds:60}"
    )
    private long cooldownSeconds;

    public PasswordResetServiceImpl(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            AuditLogService auditLogService
    ) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    /*
     * =========================================================
     * SOLICITAR RECUPERACIÓN
     * =========================================================
     */

    @Override
    @Transactional
    public String requestPasswordReset(
            String email,
            String requestContext
    ) {
        String normalizedEmail =
                normalizeEmail(email);

        Optional<User> optionalUser =
                userRepository.findByEmailIgnoreCase(
                        normalizedEmail
                );

        /*
         * Siempre se devuelve el mismo mensaje.
         * Así no se revela si el correo está registrado.
         */
        if (optionalUser.isEmpty()) {
            return GENERIC_RESPONSE;
        }

        User user = optionalUser.get();

        if (!Boolean.TRUE.equals(user.getActive())) {
            return GENERIC_RESPONSE;
        }

        LocalDateTime now =
                LocalDateTime.now();

        /*
         * Evita solicitudes repetidas en pocos segundos.
         */
        Optional<PasswordResetToken> lastToken =
                tokenRepository
                        .findTopByUserIdOrderByCreatedAtDesc(
                                user.getId()
                        );

        if (
                lastToken.isPresent()
                && lastToken
                        .get()
                        .getCreatedAt()
                        .isAfter(
                                now.minusSeconds(
                                        cooldownSeconds
                                )
                        )
        ) {
            log.info(
                    "Solicitud de recuperación omitida "
                            + "por tiempo de espera para {}",
                    user.getEmail()
            );

            return GENERIC_RESPONSE;
        }

        /*
         * Los enlaces anteriores dejan de ser válidos.
         */
        tokenRepository.revokeActiveTokensByUserId(
                user.getId(),
                now
        );

        String rawToken =
                generateUniqueRawToken();

        String tokenHash =
                hashToken(rawToken);

        PasswordResetToken resetToken =
                new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setTokenHash(tokenHash);
        resetToken.setCreatedAt(now);
        resetToken.setExpiresAt(
                now.plusMinutes(
                        expirationMinutes
                )
        );
        resetToken.setUsed(false);
        resetToken.setRevoked(false);

        tokenRepository.save(resetToken);

        /*
         * El token original solo se utiliza para construir
         * el enlace. Nunca se guarda en la base de datos.
         */
        emailService.sendPasswordResetEmail(
                user,
                rawToken,
                expirationMinutes
        );

        auditLogService.recordSecurity(
                "AUTENTICACIÓN",
                "SOLICITUD DE RECUPERACIÓN",
                "User",
                user.getId(),
                "Se solicitó un enlace para restablecer "
                        + "la contraseña. "
                        + safeContext(requestContext),
                user.getEmail(),
                safeRole(user.getRole()),
                "INFO",
                false
        );

        log.info(
                "Enlace de recuperación generado para {}",
                user.getEmail()
        );

        return GENERIC_RESPONSE;
    }

    /*
     * =========================================================
     * VALIDAR TOKEN
     * =========================================================
     */

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(
            String rawToken
    ) {
        if (
                rawToken == null
                || rawToken.isBlank()
                || rawToken.length() > 200
        ) {
            return false;
        }

        String tokenHash =
                hashToken(rawToken);

        Optional<PasswordResetToken> optionalToken =
                tokenRepository.findByTokenHash(
                        tokenHash
                );

        if (optionalToken.isEmpty()) {
            return false;
        }

        return isUsable(
                optionalToken.get(),
                LocalDateTime.now()
        );
    }

    /*
     * =========================================================
     * RESTABLECER CONTRASEÑA
     * =========================================================
     */

    @Override
    @Transactional
    public void resetPassword(
            String rawToken,
            String newPassword,
            String confirmPassword,
            String requestContext
    ) {
        validatePassword(
                newPassword,
                confirmPassword
        );

        PasswordResetToken resetToken =
                findUsableToken(rawToken);

        User user =
                resetToken.getUser();

        if (
                passwordEncoder.matches(
                        newPassword,
                        user.getPassword()
                )
        ) {
            throw new BusinessRuleException(
                    "La nueva contraseña debe ser "
                            + "diferente de la contraseña actual"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        user.setPassword(
                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(user);

        resetToken.setUsed(true);
        resetToken.setUsedAt(now);

        tokenRepository.save(resetToken);

        /*
         * Invalida cualquier otro enlace que pudiera existir.
         */
        tokenRepository.revokeActiveTokensByUserId(
                user.getId(),
                now
        );

        auditLogService.recordSecurity(
                "AUTENTICACIÓN",
                "CONTRASEÑA RESTABLECIDA",
                "User",
                user.getId(),
                "La contraseña fue restablecida mediante "
                        + "un enlace de recuperación. "
                        + safeContext(requestContext),
                user.getEmail(),
                safeRole(user.getRole()),
                "INFO",
                false
        );

        /*
         * Se envía un aviso de seguridad.
         */
        try {
            emailService
                    .sendPasswordChangedConfirmation(
                            user
                    );

        } catch (Exception exception) {
            /*
             * La contraseña ya fue modificada.
             * Un fallo del correo no debe revertir el cambio.
             */
            log.warn(
                    "No se pudo enviar la confirmación "
                            + "de contraseña para {}",
                    user.getEmail(),
                    exception
            );
        }

        log.info(
                "Contraseña restablecida correctamente para {}",
                user.getEmail()
        );
    }

    /*
     * =========================================================
     * BUSCAR TOKEN UTILIZABLE
     * =========================================================
     */

    private PasswordResetToken findUsableToken(
            String rawToken
    ) {
        if (
                rawToken == null
                || rawToken.isBlank()
                || rawToken.length() > 200
        ) {
            throw new BusinessRuleException(
                    INVALID_TOKEN_MESSAGE
            );
        }

        String tokenHash =
                hashToken(rawToken);

        PasswordResetToken resetToken =
                tokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new BusinessRuleException(
                                        INVALID_TOKEN_MESSAGE
                                )
                        );

        if (
                !isUsable(
                        resetToken,
                        LocalDateTime.now()
                )
        ) {
            throw new BusinessRuleException(
                    INVALID_TOKEN_MESSAGE
            );
        }

        return resetToken;
    }

    private boolean isUsable(
            PasswordResetToken resetToken,
            LocalDateTime now
    ) {
        if (
                resetToken == null
                || resetToken.isUsed()
                || resetToken.isRevoked()
                || resetToken.getExpiresAt() == null
                || !resetToken.getExpiresAt().isAfter(now)
        ) {
            return false;
        }

        User user =
                resetToken.getUser();

        return user != null
                && Boolean.TRUE.equals(
                        user.getActive()
                );
    }

    /*
     * =========================================================
     * POLÍTICA DE CONTRASEÑA
     * =========================================================
     */

    private void validatePassword(
            String newPassword,
            String confirmPassword
    ) {
        if (
                newPassword == null
                || newPassword.isBlank()
        ) {
            throw new BusinessRuleException(
                    "La nueva contraseña es obligatoria"
            );
        }

        if (
                confirmPassword == null
                || confirmPassword.isBlank()
        ) {
            throw new BusinessRuleException(
                    "Debe confirmar la nueva contraseña"
            );
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessRuleException(
                    "Las contraseñas no coinciden"
            );
        }

        if (
                newPassword.length() < 10
                || newPassword.length() > 72
        ) {
            throw new BusinessRuleException(
                    "La contraseña debe tener entre "
                            + "10 y 72 caracteres"
            );
        }

        if (!newPassword.equals(newPassword.trim())) {
            throw new BusinessRuleException(
                    "La contraseña no debe comenzar "
                            + "ni terminar con espacios"
            );
        }

        boolean hasUppercase =
                newPassword
                        .chars()
                        .anyMatch(
                                Character::isUpperCase
                        );

        boolean hasLowercase =
                newPassword
                        .chars()
                        .anyMatch(
                                Character::isLowerCase
                        );

        boolean hasNumber =
                newPassword
                        .chars()
                        .anyMatch(
                                Character::isDigit
                        );

        boolean hasSpecial =
                newPassword
                        .chars()
                        .anyMatch(character ->
                                !Character.isLetterOrDigit(character)
                                && !Character.isWhitespace(character)
                        );

        if (
                !hasUppercase
                || !hasLowercase
                || !hasNumber
                || !hasSpecial
        ) {
            throw new BusinessRuleException(
                    "La contraseña debe incluir una mayúscula, "
                            + "una minúscula, un número y "
                            + "un carácter especial"
            );
        }
    }

    /*
     * =========================================================
     * GENERACIÓN Y HASH DEL TOKEN
     * =========================================================
     */

    private String generateUniqueRawToken() {
        String rawToken;
        String tokenHash;

        do {
            byte[] randomBytes =
                    new byte[32];

            secureRandom.nextBytes(
                    randomBytes
            );

            rawToken =
                    Base64
                            .getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(
                                    randomBytes
                            );

            tokenHash =
                    hashToken(rawToken);

        } while (
                tokenRepository.existsByTokenHash(
                        tokenHash
                )
        );

        return rawToken;
    }

    private String hashToken(
            String rawToken
    ) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "No se pudo generar el hash "
                            + "del token de recuperación",
                    exception
            );
        }
    }

    /*
     * =========================================================
     * UTILIDADES
     * =========================================================
     */

    private String normalizeEmail(
            String email
    ) {
        return email == null
                ? ""
                : email.trim().toLowerCase();
    }

    private String safeRole(
            String role
    ) {
        return role == null || role.isBlank()
                ? "SIN_ROL"
                : role.trim();
    }

    private String safeContext(
            String requestContext
    ) {
        return requestContext == null
                || requestContext.isBlank()
                ? "Origen no identificado."
                : requestContext.trim();
    }
}