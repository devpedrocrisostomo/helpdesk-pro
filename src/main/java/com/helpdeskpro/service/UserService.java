package com.helpdeskpro.service;

import com.helpdeskpro.exception.ApiException;
import com.helpdeskpro.dto.user.UserCreateRequest;
import com.helpdeskpro.dto.user.UserResponse;
import com.helpdeskpro.dto.user.UserUpdateRequest;
import com.helpdeskpro.model.entity.Client;
import com.helpdeskpro.model.entity.User;
import com.helpdeskpro.model.enums.UserRole;
import com.helpdeskpro.repository.ClientRepository;
import com.helpdeskpro.repository.CommentRepository;
import com.helpdeskpro.repository.TicketHistoryRepository;
import com.helpdeskpro.repository.TicketRepository;
import com.helpdeskpro.repository.UserRepository;
import com.helpdeskpro.security.AuthenticatedUser;
import com.helpdeskpro.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class UserService {
    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    @Inject
    TicketRepository ticketRepository;

    @Inject
    CommentRepository commentRepository;

    @Inject
    TicketHistoryRepository ticketHistoryRepository;

    @Inject
    ClientRepository clientRepository;

    public User getCurrentUser(SecurityContext securityContext) {
        if (securityContext == null || !(securityContext.getUserPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new ApiException(Response.Status.UNAUTHORIZED, "Usuario nao autenticado.");
        }

        return userRepository.findById(authenticatedUser.id())
                .orElseThrow(() -> new ApiException(Response.Status.UNAUTHORIZED, "Usuario nao encontrado."));
    }

    public List<UserResponse> findAll(SecurityContext securityContext) {
        requireAdmin(securityContext);
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(Long id, SecurityContext securityContext) {
        requireAdmin(securityContext);
        return UserResponse.from(getUser(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request, SecurityContext securityContext) {
        requireAdmin(securityContext);
        validateUniqueEmail(request.getEmail(), null);

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setPasswordHash(passwordService.hash(request.getPassword()));
        user.setRole(request.getRole());

        userRepository.save(user);
        ensureClientProfile(user);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request, SecurityContext securityContext) {
        requireAdmin(securityContext);
        User user = getUser(id);
        validateUniqueEmail(request.getEmail(), user.getId());

        user.setName(request.getName().trim());
        user.setEmail(normalizeEmail(request.getEmail()));
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordService.hash(request.getPassword()));
        }

        ensureClientProfile(user);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id, SecurityContext securityContext) {
        User currentUser = requireAdmin(securityContext);
        if (currentUser.getId().equals(id)) {
            throw new ApiException(Response.Status.BAD_REQUEST, "Usuario autenticado nao pode remover a propria conta.");
        }
        if (ticketRepository.countByAssignedTechnicianId(id) > 0
                || commentRepository.countByAuthorId(id) > 0
                || ticketHistoryRepository.countByChangedById(id) > 0) {
            throw new ApiException(Response.Status.CONFLICT, "Usuario possui registros vinculados.");
        }
        userRepository.delete(getUser(id));
    }

    User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(Response.Status.NOT_FOUND, "Usuario nao encontrado."));
    }

    public User requireAdmin(SecurityContext securityContext) {
        User currentUser = getCurrentUser(securityContext);
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new ApiException(Response.Status.FORBIDDEN, "Acesso restrito a administradores.");
        }
        return currentUser;
    }

    public User requireAnyRole(SecurityContext securityContext, UserRole... roles) {
        User currentUser = getCurrentUser(securityContext);
        boolean allowed = Arrays.stream(roles).anyMatch(role -> role == currentUser.getRole());
        if (!allowed) {
            throw new ApiException(Response.Status.FORBIDDEN, "Perfil sem permissao para esta acao.");
        }
        return currentUser;
    }

    public boolean hasRole(User user, UserRole role) {
        return user != null && user.getRole() == role;
    }

    private void ensureClientProfile(User user) {
        if (user.getRole() != UserRole.CLIENT) {
            return;
        }

        String document = "USER-" + user.getId();
        Client client = clientRepository.findByDocument(document)
                .or(() -> clientRepository.findByEmail(user.getEmail()))
                .orElseGet(() -> {
                    Client created = new Client();
                    created.setDocument(document);
                    return created;
                });

        client.setName(user.getName());
        client.setEmail(user.getEmail());
        if (client.getDocument() == null || client.getDocument().isBlank()) {
            client.setDocument(document);
        }
        clientRepository.save(client);
    }

    private void validateUniqueEmail(String email, Long currentUserId) {
        userRepository.findByEmail(normalizeEmail(email))
                .filter(user -> !user.getId().equals(currentUserId))
                .ifPresent(user -> {
                    throw new ApiException(Response.Status.CONFLICT, "Ja existe usuario com este e-mail.");
                });
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
