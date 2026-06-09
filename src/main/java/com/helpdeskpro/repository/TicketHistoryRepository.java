package com.helpdeskpro.repository;

import com.helpdeskpro.model.entity.TicketHistory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class TicketHistoryRepository {
    @PersistenceContext(unitName = "helpdeskPU")
    EntityManager entityManager;

    public TicketHistory save(TicketHistory history) {
        entityManager.persist(history);
        return history;
    }

    public List<TicketHistory> findByTicketId(Long ticketId) {
        return entityManager
                .createQuery("select h from TicketHistory h where h.ticket.id = :ticketId order by h.createdAt desc", TicketHistory.class)
                .setParameter("ticketId", ticketId)
                .getResultList();
    }

    public long countByChangedById(Long userId) {
        return entityManager
                .createQuery("select count(h) from TicketHistory h where h.changedBy.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }
}
