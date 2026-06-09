package com.helpdeskpro.service;

import com.helpdeskpro.config.AppConfig;
import com.helpdeskpro.model.entity.User;
import com.helpdeskpro.model.enums.UserRole;
import com.helpdeskpro.repository.UserRepository;
import com.helpdeskpro.security.PasswordService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DatabaseSeeder {
    @Inject
    AppConfig appConfig;

    @Inject
    UserRepository userRepository;

    @Inject
    PasswordService passwordService;

    @Transactional
    public void seedAdmin(@Observes @Initialized(ApplicationScoped.class) Object event) {
        String email = appConfig.adminEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = new User();
        admin.setName(appConfig.adminName());
        admin.setEmail(email);
        admin.setPasswordHash(passwordService.hash(appConfig.adminPassword()));
        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
    }
}
