package com.helpdeskpro.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClientRequest {
    @NotBlank(message = "Nome e obrigatorio.")
    @Size(max = 140, message = "Nome deve ter no maximo 140 caracteres.")
    private String name;

    @NotBlank(message = "E-mail e obrigatorio.")
    @Email(message = "E-mail invalido.")
    @Size(max = 180, message = "E-mail deve ter no maximo 180 caracteres.")
    private String email;

    @Size(max = 30, message = "Telefone deve ter no maximo 30 caracteres.")
    private String phone;

    @NotBlank(message = "Documento e obrigatorio.")
    @Size(max = 40, message = "Documento deve ter no maximo 40 caracteres.")
    private String document;

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
}
