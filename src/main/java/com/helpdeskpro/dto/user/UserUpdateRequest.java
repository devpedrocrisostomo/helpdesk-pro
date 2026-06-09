package com.helpdeskpro.dto.user;

import com.helpdeskpro.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserUpdateRequest {
    @NotBlank(message = "Nome e obrigatorio.")
    @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres.")
    private String name;

    @NotBlank(message = "E-mail e obrigatorio.")
    @Email(message = "E-mail invalido.")
    @Size(max = 180, message = "E-mail deve ter no maximo 180 caracteres.")
    private String email;

    @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres.")
    private String password;

    @NotNull(message = "Perfil e obrigatorio.")
    private UserRole role;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
