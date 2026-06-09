package com.helpdeskpro.resource;

import com.helpdeskpro.dto.auth.AuthResponse;
import com.helpdeskpro.dto.auth.LoginRequest;
import com.helpdeskpro.dto.user.UserResponse;
import com.helpdeskpro.service.AuthService;
import com.helpdeskpro.service.UserService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    AuthService authService;

    @Inject
    UserService userService;

    @POST
    @Path("/login")
    public AuthResponse login(@NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid LoginRequest request) {
        return authService.login(request);
    }

    @GET
    @Path("/me")
    public UserResponse me(@Context SecurityContext securityContext) {
        return UserResponse.from(userService.getCurrentUser(securityContext));
    }
}
