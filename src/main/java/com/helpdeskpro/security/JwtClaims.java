package com.helpdeskpro.security;

import com.helpdeskpro.model.enums.UserRole;

public record JwtClaims(Long userId, String email, UserRole role) {
}
