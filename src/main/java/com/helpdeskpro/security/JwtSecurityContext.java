package com.helpdeskpro.security;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public class JwtSecurityContext implements SecurityContext {
    private final AuthenticatedUser authenticatedUser;
    private final boolean secure;

    public JwtSecurityContext(AuthenticatedUser authenticatedUser, boolean secure) {
        this.authenticatedUser = authenticatedUser;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return authenticatedUser;
    }

    @Override
    public boolean isUserInRole(String role) {
        return authenticatedUser.role().name().equals(role);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
