package com.helpdeskpro.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    @NotBlank(message = "Mensagem e obrigatoria.")
    @Size(max = 5000, message = "Mensagem deve ter no maximo 5000 caracteres.")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
