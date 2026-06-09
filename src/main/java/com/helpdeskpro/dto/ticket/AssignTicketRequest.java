package com.helpdeskpro.dto.ticket;

import jakarta.validation.constraints.Positive;

public class AssignTicketRequest {
    @Positive(message = "Tecnico deve ser um ID positivo.")
    private Long technicianId;

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }
}
