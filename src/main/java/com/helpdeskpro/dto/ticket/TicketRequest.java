package com.helpdeskpro.dto.ticket;

import com.helpdeskpro.model.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class TicketRequest {
    @NotBlank(message = "Titulo e obrigatorio.")
    @Size(max = 180, message = "Titulo deve ter no maximo 180 caracteres.")
    private String title;

    @NotBlank(message = "Descricao e obrigatoria.")
    private String description;

    @NotNull(message = "Prioridade e obrigatoria.")
    private TicketPriority priority;

    @NotNull(message = "Cliente e obrigatorio.")
    @Positive(message = "Cliente deve ser um ID positivo.")
    private Long clientId;

    @Positive(message = "Tecnico deve ser um ID positivo.")
    private Long assignedTechnicianId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(Long assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }
}
