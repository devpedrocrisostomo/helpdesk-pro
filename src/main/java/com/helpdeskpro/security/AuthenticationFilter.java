package com.helpdeskpro.security;

import com.helpdeskpro.exception.ApiException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final Set<String> PUBLIC_PATHS = Set.of("auth/login", "health");

    @Inject
    JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (isPublicRequest(requestContext)) {
            return;
        }

        String authorization = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(Response.Status.UNAUTHORIZED, "Token Bearer obrigatorio.");
        }

        String token = authorization.substring("Bearer ".length()).trim();
        JwtClaims claims = jwtService.validate(token);
        SecurityContext currentContext = requestContext.getSecurityContext();

        requestContext.setSecurityContext(new JwtSecurityContext(
                new AuthenticatedUser(claims.userId(), claims.email(), claims.role()),
                currentContext != null && currentContext.isSecure()
        ));
    }

    private boolean isPublicRequest(ContainerRequestContext requestContext) {
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return true;
        }
        String path = requestContext.getUriInfo().getPath(false);
        String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        return PUBLIC_PATHS.contains(normalizedPath)
                || normalizedPath.endsWith("/auth/login")
                || normalizedPath.endsWith("/health");
    }
}
