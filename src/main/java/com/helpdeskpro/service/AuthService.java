package com.helpdeskpro.service;

import com.helpdeskpro.dto.auth.AuthResponse;
import com.helpdeskpro.dto.auth.LoginRequest;
import com.helpdeskpro.dto.user.UserResponse;
import com.helpdeskpro.exception.ApiException;
import com.helpdeskpro.model.entity.User;
import com.helpdeskpro.repository.UserRepository;
import com.helpdeskpro.security.JwtService;
import com.helpdeskpro.security.JwtToken;
import com.helpdeskpro.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AuthService {
    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> invalidCredentials());

        if (!passwordService.verify(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        JwtToken jwtToken = jwtService.createToken(user);
        return new AuthResponse(jwtToken.value(), jwtToken.expiresAt().toString(), UserResponse.from(user));
    }

    private ApiException invalidCredentials() {
        return new ApiException(Response.Status.UNAUTHORIZED, "E-mail ou senha invalidos.");
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
