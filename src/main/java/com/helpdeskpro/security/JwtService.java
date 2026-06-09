package com.helpdeskpro.security;

import com.helpdeskpro.config.AppConfig;
import com.helpdeskpro.exception.ApiException;
import com.helpdeskpro.model.entity.User;
import com.helpdeskpro.model.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@ApplicationScoped
public class JwtService {
    @Inject
    AppConfig appConfig;

    public JwtToken createToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(appConfig.jwtExpirationMinutes() * 60);

        String token = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .signWith(signingKey())
                .compact();

        return new JwtToken(token, expiresAt);
    }

    public JwtClaims validate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Number userId = claims.get("userId", Number.class);
            String role = claims.get("role", String.class);
            return new JwtClaims(userId.longValue(), claims.getSubject(), UserRole.valueOf(role));
        } catch (JwtException | IllegalArgumentException | NullPointerException exception) {
            throw new ApiException(Response.Status.UNAUTHORIZED, "Token invalido ou expirado.");
        }
    }

    private SecretKey signingKey() {
        byte[] secret = appConfig.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 bytes.");
        }
        return Keys.hmacShaKeyFor(secret);
    }
}
