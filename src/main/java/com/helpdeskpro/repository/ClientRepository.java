package com.helpdeskpro.repository;

import com.helpdeskpro.model.entity.Client;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ClientRepository {
    @PersistenceContext(unitName = "helpdeskPU")
    EntityManager entityManager;

    public Client save(Client client) {
        if (client.getId() == null) {
            entityManager.persist(client);
            return client;
        }
        return entityManager.merge(client);
    }

    public Optional<Client> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Client.class, id));
    }

    public List<Client> findAll() {
        return entityManager
                .createQuery("select c from Client c order by c.createdAt desc", Client.class)
                .getResultList();
    }

    public Optional<Client> findByEmail(String email) {
        try {
            Client client = entityManager
                    .createQuery("select c from Client c where lower(c.email) = lower(:email)", Client.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(client);
        } catch (NoResultException exception) {
            return Optional.empty();
        }
    }

    public Optional<Client> findByDocument(String document) {
        try {
            Client client = entityManager
                    .createQuery("select c from Client c where c.document = :document", Client.class)
                    .setParameter("document", document)
                    .getSingleResult();
            return Optional.of(client);
        } catch (NoResultException exception) {
            return Optional.empty();
        }
    }

    public void delete(Client client) {
        entityManager.remove(entityManager.contains(client) ? client : entityManager.merge(client));
    }
}
