package com.helpdeskpro.security;

import com.helpdeskpro.model.enums.UserRole;

import java.security.Principal;

public record AuthenticatedUser(Long id, String email, UserRole role) implements Principal {
    @Override
    public String getName() {
        return email;
    }
}
