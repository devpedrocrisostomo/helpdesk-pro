package com.helpdeskpro.dto.ticket;

import com.helpdeskpro.dto.client.ClientResponse;
import com.helpdeskpro.dto.user.UserResponse;
import com.helpdeskpro.model.entity.Ticket;
import com.helpdeskpro.model.enums.TicketPriority;
import com.helpdeskpro.model.enums.TicketStatus;

public class TicketResponse {
    private Long id;
    private String title;
    private String description;
    private TicketPriority priority;
    private TicketStatus status;
    private ClientResponse client;
    private UserResponse assignedTechnician;
    private String createdAt;
    private String updatedAt;

    public static TicketResponse from(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTitle(ticket.getTitle());
        response.setDescription(ticket.getDescription());
        response.setPriority(ticket.getPriority());
        response.setStatus(ticket.getStatus());
        response.setClient(ClientResponse.from(ticket.getClient()));
        response.setAssignedTechnician(ticket.getAssignedTechnician() == null ? null : UserResponse.from(ticket.getAssignedTechnician()));
        response.setCreatedAt(ticket.getCreatedAt().toString());
        response.setUpdatedAt(ticket.getUpdatedAt().toString());
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public ClientResponse getClient() {
        return client;
    }

    public void setClient(ClientResponse client) {
        this.client = client;
    }

    public UserResponse getAssignedTechnician() {
        return assignedTechnician;
    }

    public void setAssignedTechnician(UserResponse assignedTechnician) {
        this.assignedTechnician = assignedTechnician;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
