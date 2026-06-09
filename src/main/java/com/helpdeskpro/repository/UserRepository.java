package com.helpdeskpro.repository;

import com.helpdeskpro.model.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRepository {
    @PersistenceContext(unitName = "helpdeskPU")
    EntityManager entityManager;

    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
            return user;
        }
        return entityManager.merge(user);
    }

    public Optional<User> findById(Long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    public Optional<User> findByEmail(String email) {
        try {
            User user = entityManager
                    .createQuery("select u from User u where lower(u.email) = lower(:email)", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException exception) {
            return Optional.empty();
        }
    }

    public List<User> findAll() {
        return entityManager
                .createQuery("select u from User u order by u.createdAt desc", User.class)
                .getResultList();
    }

    public boolean existsByEmail(String email) {
        Long total = entityManager
                .createQuery("select count(u) from User u where lower(u.email) = lower(:email)", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return total > 0;
    }

    public void delete(User user) {
        entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
    }
}
