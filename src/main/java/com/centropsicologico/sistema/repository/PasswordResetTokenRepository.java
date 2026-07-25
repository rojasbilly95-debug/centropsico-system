package com.centropsicologico.sistema.repository;

import com.centropsicologico.sistema.entity.PasswordResetToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(
            String tokenHash
    );

    boolean existsByTokenHash(
            String tokenHash
    );

    Optional<PasswordResetToken>
    findTopByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    /*
     * Invalida todos los enlaces anteriores
     * que todavía no fueron utilizados.
     */
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE PasswordResetToken token
               SET token.revoked = true,
                   token.revokedAt = :revokedAt
             WHERE token.user.id = :userId
               AND token.used = false
               AND token.revoked = false
            """)
    int revokeActiveTokensByUserId(
            @Param("userId") Long userId,
            @Param("revokedAt") LocalDateTime revokedAt
    );
}