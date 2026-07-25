package com.centropsicologico.sistema.service;

public interface PasswordResetService {

    String requestPasswordReset(
            String email,
            String requestContext
    );

    boolean isTokenValid(
            String rawToken
    );

    void resetPassword(
            String rawToken,
            String newPassword,
            String confirmPassword,
            String requestContext
    );
}