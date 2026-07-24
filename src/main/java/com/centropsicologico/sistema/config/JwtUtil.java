package com.centropsicologico.sistema.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationTime;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:1800000}") long expirationTime) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET no está configurado.");
        }

        byte[] secretBytes;

        try {
            secretBytes = Decoders.BASE64.decode(
                    secret.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET no tiene un formato Base64 válido.",
                    exception);
        }

        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 bytes.");
        }

        this.key = Keys.hmacShaKeyFor(secretBytes);

        this.expirationTime = expirationTime;
    }

    public String generateToken(
            String email,
            String role) {
        Date now = new Date();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer("centropsico")
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(
                        new Date(
                                now.getTime() + expirationTime))
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public String getTokenId(String token) {
        return getClaims(token).getId();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = getClaims(token);

            return "centropsico".equals(
                    claims.getIssuer()) && claims.getExpiration().after(new Date());

        } catch (Exception exception) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer("centropsico")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}