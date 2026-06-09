package com.helpdeskpro.repository;

import com.helpdeskpro.model.entity.Comment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CommentRepository {
    @PersistenceContext(unitName = "helpdeskPU")
    EntityManager entityManager;

    public Comment save(Comment comment) {
        entityManager.persist(comment);
        return comment;
    }

    public List<Comment> findByTicketId(Long ticketId) {
        return entityManager
                .createQuery("select c from Comment c where c.ticket.id = :ticketId order by c.createdAt asc", Comment.class)
                .setParameter("ticketId", ticketId)
                .getResultList();
    }

    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Comment.class, id));
    }

    public long countByAuthorId(Long authorId) {
        return entityManager
                .createQuery("select count(c) from Comment c where c.author.id = :authorId", Long.class)
                .setParameter("authorId", authorId)
                .getSingleResult();
    }

    public void delete(Comment comment) {
        entityManager.remove(entityManager.contains(comment) ? comment : entityManager.merge(comment));
    }
}
