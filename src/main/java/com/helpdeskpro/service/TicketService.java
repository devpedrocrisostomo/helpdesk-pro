package com.helpdeskpro.service;

import com.helpdeskpro.dto.ticket.AssignTicketRequest;
import com.helpdeskpro.dto.ticket.ChangeTicketStatusRequest;
import com.helpdeskpro.dto.ticket.TicketDashboardResponse;
import com.helpdeskpro.dto.ticket.TicketRequest;
import com.helpdeskpro.dto.ticket.TicketResponse;
import com.helpdeskpro.exception.ApiException;
import com.helpdeskpro.model.entity.Ticket;
import com.helpdeskpro.model.entity.TicketHistory;
import com.helpdeskpro.model.entity.User;
import com.helpdeskpro.model.enums.TicketPriority;
import com.helpdeskpro.model.enums.TicketStatus;
import com.helpdeskpro.model.enums.UserRole;
import com.helpdeskpro.repository.TicketHistoryRepository;
import com.helpdeskpro.repository.TicketRepository;
import com.helpdeskpro.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TicketService {
    @Inject
    TicketRepository ticketRepository;

    @Inject
    TicketHistoryRepository ticketHistoryRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    ClientService clientService;

    @Inject
    UserService userService;

    @Transactional
    public TicketResponse create(TicketRequest request, SecurityContext securityContext) {
        User currentUser = userService.getCurrentUser(securityContext);
        Ticket ticket = new Ticket();
        applyRequest(ticket, request, currentUser);
        ticketRepository.save(ticket);
        return TicketResponse.from(ticket);
    }

    @Transactional
    public List<TicketResponse> findAll(SecurityContext securityContext) {
        User currentUser = userService.getCurrentUser(securityContext);
        return visibleTickets(currentUser)
                .stream()
                .map(TicketResponse::from)
                .toList();
    }

    @Transactional
    public TicketResponse findById(Long id, SecurityContext securityContext) {
        Ticket ticket = getTicket(id);
        ensureCanRead(ticket, userService.getCurrentUser(securityContext));
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse update(Long id, TicketRequest request, SecurityContext securityContext) {
        User currentUser = userService.requireAnyRole(securityContext, UserRole.ADMIN, UserRole.TECHNICIAN);
        Ticket ticket = getTicket(id);
        ensureCanWorkOnTicket(ticket, currentUser);
        applyRequest(ticket, request, currentUser);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse changeStatus(Long id, ChangeTicketStatusRequest request, SecurityContext securityContext) {
        Ticket ticket = getTicket(id);
        User currentUser = userService.requireAnyRole(securityContext, UserRole.ADMIN, UserRole.TECHNICIAN);
        ensureCanWorkOnTicket(ticket, currentUser);
        TicketStatus oldStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();

        if (oldStatus != newStatus) {
            ticket.setStatus(newStatus);
            ticketRepository.save(ticket);
            registerStatusHistory(ticket, currentUser, oldStatus, newStatus);
        }

        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse assign(Long id, AssignTicketRequest request, SecurityContext securityContext) {
        userService.requireAdmin(securityContext);
        Ticket ticket = getTicket(id);
        ticket.setAssignedTechnician(findAssignableTechnician(request.getTechnicianId()));
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public void delete(Long id, SecurityContext securityContext) {
        userService.requireAdmin(securityContext);
        ticketRepository.delete(getTicket(id));
    }

    public TicketDashboardResponse dashboard(SecurityContext securityContext) {
        User currentUser = userService.getCurrentUser(securityContext);
        List<Ticket> tickets = visibleTickets(currentUser);
        Map<TicketStatus, Long> byStatus = new LinkedHashMap<>();
        Arrays.stream(TicketStatus.values())
                .forEach(status -> byStatus.put(status, tickets.stream().filter(ticket -> ticket.getStatus() == status).count()));

        Map<TicketPriority, Long> byPriority = new LinkedHashMap<>();
        Arrays.stream(TicketPriority.values())
                .forEach(priority -> byPriority.put(priority, tickets.stream().filter(ticket -> ticket.getPriority() == priority).count()));

        return new TicketDashboardResponse(tickets.size(), byStatus, byPriority);
    }

    Ticket getAuthorizedTicket(Long id, User currentUser) {
        Ticket ticket = getTicket(id);
        ensureCanRead(ticket, currentUser);
        return ticket;
    }

    private Ticket getTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(Response.Status.NOT_FOUND, "Chamado nao encontrado."));
    }

    private void applyRequest(Ticket ticket, TicketRequest request, User currentUser) {
        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setPriority(request.getPriority());

        if (currentUser.getRole() == UserRole.CLIENT) {
            ticket.setClient(clientService.getClientForUser(currentUser));
            ticket.setAssignedTechnician(null);
            return;
        }

        if (currentUser.getRole() == UserRole.TECHNICIAN) {
            ticket.setClient(clientService.getClient(request.getClientId()));
            if (ticket.getId() == null && ticket.getAssignedTechnician() == null) {
                ticket.setAssignedTechnician(currentUser);
            }
            return;
        }

        ticket.setClient(clientService.getClient(request.getClientId()));
        ticket.setAssignedTechnician(findAssignableTechnician(request.getAssignedTechnicianId()));
    }

    private List<Ticket> visibleTickets(User currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return ticketRepository.findAll();
        }
        if (currentUser.getRole() == UserRole.TECHNICIAN) {
            return ticketRepository.findByAssignedTechnicianId(currentUser.getId());
        }
        return ticketRepository.findByClientEmail(currentUser.getEmail());
    }

    private void ensureCanRead(Ticket ticket, User currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (currentUser.getRole() == UserRole.TECHNICIAN
                && ticket.getAssignedTechnician() != null
                && ticket.getAssignedTechnician().getId().equals(currentUser.getId())) {
            return;
        }
        if (currentUser.getRole() == UserRole.CLIENT
                && ticket.getClient().getEmail().equalsIgnoreCase(currentUser.getEmail())) {
            return;
        }
        throw new ApiException(Response.Status.FORBIDDEN, "Usuario sem permissao para acessar este chamado.");
    }

    private void ensureCanWorkOnTicket(Ticket ticket, User currentUser) {
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (currentUser.getRole() == UserRole.TECHNICIAN
                && ticket.getAssignedTechnician() != null
                && ticket.getAssignedTechnician().getId().equals(currentUser.getId())) {
            return;
        }
        throw new ApiException(Response.Status.FORBIDDEN, "Usuario sem permissao para alterar este chamado.");
    }

    private User findAssignableTechnician(Long technicianId) {
        if (technicianId == null) {
            return null;
        }

        User user = userRepository.findById(technicianId)
                .orElseThrow(() -> new ApiException(Response.Status.NOT_FOUND, "Tecnico nao encontrado."));

        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.TECHNICIAN) {
            throw new ApiException(Response.Status.BAD_REQUEST, "Usuario informado nao pode receber chamados.");
        }

        return user;
    }

    private void registerStatusHistory(Ticket ticket, User currentUser, TicketStatus oldStatus, TicketStatus newStatus) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setChangedBy(currentUser);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        ticketHistoryRepository.save(history);
    }
}
