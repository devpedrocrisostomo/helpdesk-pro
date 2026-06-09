package com.helpdeskpro.config;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppConfig {
    private static final String DEFAULT_JWT_SECRET = "dev-helpdesk-pro-jwt-secret-change-me-32-bytes-minimum";

    public String jwtSecret() {
        return env("JWT_SECRET", DEFAULT_JWT_SECRET);
    }

    public long jwtExpirationMinutes() {
        return Long.parseLong(env("JWT_EXPIRATION_MINUTES", "120"));
    }

    public String adminName() {
        return env("ADMIN_NAME", "HelpDesk Admin");
    }

    public String adminEmail() {
        return env("ADMIN_EMAIL", "admin@helpdeskpro.local");
    }

    public String adminPassword() {
        return env("ADMIN_PASSWORD", "admin123");
    }

    public String corsAllowedOrigins() {
        return env("CORS_ALLOWED_ORIGINS", "*");
    }

    private String env(String key, String fallback) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value;
    }
}
