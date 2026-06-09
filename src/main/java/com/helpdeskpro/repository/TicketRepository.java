package com.helpdeskpro.repository;

import com.helpdeskpro.model.entity.Ticket;
import com.helpdeskpro.model.enums.TicketPriority;
import com.helpdeskpro.model.enums.TicketStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class TicketRepository {
    @PersistenceContext(unitName = "helpdeskPU")
    EntityManager entityManager;

    public Ticket save(Ticket ticket) {
        if (ticket.getId() == null) {
            entityManager.persist(ticket);
            return ticket;
        }
        return entityManager.merge(ticket);
    }

    public Optional<Ticket> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Ticket.class, id));
    }

    public List<Ticket> findAll() {
        return entityManager
                .createQuery("select t from Ticket t order by t.createdAt desc", Ticket.class)
                .getResultList();
    }

    public List<Ticket> findByAssignedTechnicianId(Long technicianId) {
        return entityManager
                .createQuery("select t from Ticket t where t.assignedTechnician.id = :technicianId order by t.createdAt desc", Ticket.class)
                .setParameter("technicianId", technicianId)
                .getResultList();
    }

    public List<Ticket> findByClientEmail(String email) {
        return entityManager
                .createQuery("select t from Ticket t where lower(t.client.email) = lower(:email) order by t.createdAt desc", Ticket.class)
                .setParameter("email", email)
                .getResultList();
    }

    public long countByClientId(Long clientId) {
        return entityManager
                .createQuery("select count(t) from Ticket t where t.client.id = :clientId", Long.class)
                .setParameter("clientId", clientId)
                .getSingleResult();
    }

    public long countByAssignedTechnicianId(Long technicianId) {
        return entityManager
                .createQuery("select count(t) from Ticket t where t.assignedTechnician.id = :technicianId", Long.class)
                .setParameter("technicianId", technicianId)
                .getSingleResult();
    }

    public void delete(Ticket ticket) {
        entityManager.remove(entityManager.contains(ticket) ? ticket : entityManager.merge(ticket));
    }

    public long countAll() {
        return entityManager
                .createQuery("select count(t) from Ticket t", Long.class)
                .getSingleResult();
    }

    public long countByStatus(TicketStatus status) {
        return entityManager
                .createQuery("select count(t) from Ticket t where t.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    public long countByPriority(TicketPriority priority) {
        return entityManager
                .createQuery("select count(t) from Ticket t where t.priority = :priority", Long.class)
                .setParameter("priority", priority)
                .getSingleResult();
    }
}
