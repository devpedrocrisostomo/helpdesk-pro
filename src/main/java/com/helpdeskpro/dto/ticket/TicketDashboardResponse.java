package com.helpdeskpro.dto.ticket;

import com.helpdeskpro.model.enums.TicketPriority;
import com.helpdeskpro.model.enums.TicketStatus;

import java.util.Map;

public class TicketDashboardResponse {
    private long total;
    private Map<TicketStatus, Long> byStatus;
    private Map<TicketPriority, Long> byPriority;

    public TicketDashboardResponse() {
    }

    public TicketDashboardResponse(long total, Map<TicketStatus, Long> byStatus, Map<TicketPriority, Long> byPriority) {
        this.total = total;
        this.byStatus = byStatus;
        this.byPriority = byPriority;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Map<TicketStatus, Long> getByStatus() {
        return byStatus;
    }

    public void setByStatus(Map<TicketStatus, Long> byStatus) {
        this.byStatus = byStatus;
    }

    public Map<TicketPriority, Long> getByPriority() {
        return byPriority;
    }

    public void setByPriority(Map<TicketPriority, Long> byPriority) {
        this.byPriority = byPriority;
    }
}
