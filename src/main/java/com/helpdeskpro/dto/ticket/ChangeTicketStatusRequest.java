package com.helpdeskpro.dto.ticket;

import com.helpdeskpro.model.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class ChangeTicketStatusRequest {
    @NotNull(message = "Status e obrigatorio.")
    private TicketStatus status;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
