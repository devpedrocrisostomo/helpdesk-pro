package com.helpdeskpro.service;

import com.helpdeskpro.dto.client.ClientRequest;
import com.helpdeskpro.dto.client.ClientResponse;
import com.helpdeskpro.exception.ApiException;
import com.helpdeskpro.model.entity.Client;
import com.helpdeskpro.model.entity.User;
import com.helpdeskpro.model.enums.UserRole;
import com.helpdeskpro.repository.ClientRepository;
import com.helpdeskpro.repository.TicketRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@ApplicationScoped
public class ClientService {
    @Inject
    ClientRepository clientRepository;

    @Inject
    TicketRepository ticketRepository;

    @Inject
    UserService userService;

    @Transactional
    public ClientResponse create(ClientRequest request, SecurityContext securityContext) {
        userService.requireAdmin(securityContext);
        validateUniqueEmail(request.getEmail(), null);
        validateUniqueDocument(request.getDocument(), null);

        Client client = new Client();
        applyRequest(client, request);
        clientRepository.save(client);
        return ClientResponse.from(client);
    }

    public List<ClientResponse> findAll(SecurityContext securityContext) {
        User currentUser = userService.getCurrentUser(securityContext);
        List<Client> clients = currentUser.getRole() == UserRole.CLIENT
                ? clientRepository.findByEmail(currentUser.getEmail()).stream().toList()
                : clientRepository.findAll();

        return clients
                .stream()
                .map(ClientResponse::from)
                .toList();
    }

    public ClientResponse findById(Long id, SecurityContext securityContext) {
        Client client = getClient(id);
        ensureCanRead(client, userService.getCurrentUser(securityContext));
        return ClientResponse.from(client);
    }

    @Transactional
    public ClientResponse update(Long id, ClientRequest request, SecurityContext securityContext) {
        userService.requireAdmin(securityContext);
        Client client = getClient(id);
        validateUniqueEmail(request.getEmail(), client.getId());
        validateUniqueDocument(request.getDocument(), client.getId());

        applyRequest(client, request);
        return ClientResponse.from(clientRepository.save(client));
    }

    @Transactional
    public void delete(Long id, SecurityContext securityContext) {
        userService.requireAdmin(securityContext);
        Client client = getClient(id);
        if (ticketRepository.countByClientId(id) > 0) {
            throw new ApiException(Response.Status.CONFLICT, "Cliente possui chamados vinculados.");
        }
        clientRepository.delete(client);
    }

    Client getClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ApiException(Response.Status.NOT_FOUND, "Cliente nao encontrado."));
    }

    Client getClientForUser(User user) {
        return clientRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ApiException(Response.Status.FORBIDDEN, "Usuario cliente nao possui cadastro de cliente vinculado."));
    }

    private void ensureCanRead(Client client, User currentUser) {
        if (currentUser.getRole() == UserRole.CLIENT && !client.getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            throw new ApiException(Response.Status.FORBIDDEN, "Cliente sem permissao para acessar este cadastro.");
        }
    }

    private void applyRequest(Client client, ClientRequest request) {
        client.setName(request.getName().trim());
        client.setEmail(normalizeEmail(request.getEmail()));
        client.setPhone(blankToNull(request.getPhone()));
        client.setDocument(request.getDocument().trim());
    }

    private void validateUniqueEmail(String email, Long currentClientId) {
        clientRepository.findByEmail(normalizeEmail(email))
                .filter(client -> !client.getId().equals(currentClientId))
                .ifPresent(client -> {
                    throw new ApiException(Response.Status.CONFLICT, "Ja existe cliente com este e-mail.");
                });
    }

    private void validateUniqueDocument(String document, Long currentClientId) {
        clientRepository.findByDocument(document.trim())
                .filter(client -> !client.getId().equals(currentClientId))
                .ifPresent(client -> {
                    throw new ApiException(Response.Status.CONFLICT, "Ja existe cliente com este documento.");
                });
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
