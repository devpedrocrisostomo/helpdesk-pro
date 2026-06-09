package com.helpdeskpro.service;

import com.helpdeskpro.dto.comment.CommentRequest;
import com.helpdeskpro.dto.comment.CommentResponse;
import com.helpdeskpro.exception.ApiException;
import com.helpdeskpro.model.entity.Comment;
import com.helpdeskpro.model.entity.Ticket;
import com.helpdeskpro.model.entity.User;
import com.helpdeskpro.model.enums.UserRole;
import com.helpdeskpro.repository.CommentRepository;
import com.helpdeskpro.repository.TicketRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

@ApplicationScoped
public class CommentService {
    @Inject
    CommentRepository commentRepository;

    @Inject
    TicketRepository ticketRepository;

    @Inject
    UserService userService;

    @Transactional
    public List<CommentResponse> findByTicketId(Long ticketId, SecurityContext securityContext) {
        Ticket ticket = ensureTicketExists(ticketId);
        ensureCanReadTicket(ticket, userService.getCurrentUser(securityContext));
        return commentRepository.findByTicketId(ticketId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse create(Long ticketId, CommentRequest request, SecurityContext securityContext) {
        Ticket ticket = ensureTicketExists(ticketId);
        User author = userService.getCurrentUser(securityContext);
        ensureCanReadTicket(ticket, author);

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setMessage(request.getMessage().trim());
        commentRepository.save(comment);

        return CommentResponse.from(comment);
    }

    @Transactional
    public void delete(Long ticketId, Long commentId, SecurityContext securityContext) {
        Ticket ticket = ensureTicketExists(ticketId);
        User currentUser = userService.getCurrentUser(securityContext);
        ensureCanReadTicket(ticket, currentUser);
        Comment comment = commentRepository.findById(commentId)
                .filter(found -> found.getTicket().getId().equals(ticketId))
                .orElseThrow(() -> new ApiException(Response.Status.NOT_FOUND, "Comentario nao encontrado."));

        boolean isAuthor = comment.getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        if (!isAuthor && !isAdmin) {
            throw new ApiException(Response.Status.FORBIDDEN, "Somente o autor ou um administrador pode remover este comentario.");
        }

        commentRepository.delete(comment);
    }

    private Ticket ensureTicketExists(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ApiException(Response.Status.NOT_FOUND, "Chamado nao encontrado."));
    }

    private void ensureCanReadTicket(Ticket ticket, User currentUser) {
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
        throw new ApiException(Response.Status.FORBIDDEN, "Usuario sem permissao para acessar comentarios deste chamado.");
    }
}
