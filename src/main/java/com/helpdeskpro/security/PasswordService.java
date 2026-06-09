package com.helpdeskpro.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordService {
    private static final int COST = 12;

    public String hash(String password) {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray());
    }

    public boolean verify(String password, String passwordHash) {
        return BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified;
    }
}
