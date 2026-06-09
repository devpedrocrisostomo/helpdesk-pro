package com.helpdeskpro.resource;

import com.helpdeskpro.dto.ticket.AssignTicketRequest;
import com.helpdeskpro.dto.ticket.ChangeTicketStatusRequest;
import com.helpdeskpro.dto.ticket.TicketDashboardResponse;
import com.helpdeskpro.dto.ticket.TicketRequest;
import com.helpdeskpro.dto.ticket.TicketResponse;
import com.helpdeskpro.service.TicketService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

@Path("/tickets")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TicketResource {
    @Inject
    TicketService ticketService;

    @GET
    public List<TicketResponse> findAll(@Context SecurityContext securityContext) {
        return ticketService.findAll(securityContext);
    }

    @POST
    public Response create(@NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid TicketRequest request,
                           @Context SecurityContext securityContext,
                           @Context UriInfo uriInfo) {
        TicketResponse response = ticketService.create(request, securityContext);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(response.getId().toString()).build())
                .entity(response)
                .build();
    }

    @GET
    @Path("/dashboard")
    public TicketDashboardResponse dashboard(@Context SecurityContext securityContext) {
        return ticketService.dashboard(securityContext);
    }

    @GET
    @Path("/{id}")
    public TicketResponse findById(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        return ticketService.findById(id, securityContext);
    }

    @PUT
    @Path("/{id}")
    public TicketResponse update(@PathParam("id") Long id,
                                 @NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid TicketRequest request,
                                 @Context SecurityContext securityContext) {
        return ticketService.update(id, request, securityContext);
    }

    @PATCH
    @Path("/{id}/status")
    public TicketResponse changeStatus(@PathParam("id") Long id,
                                       @NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid ChangeTicketStatusRequest request,
                                       @Context SecurityContext securityContext) {
        return ticketService.changeStatus(id, request, securityContext);
    }

    @PATCH
    @Path("/{id}/assignment")
    public TicketResponse assign(@PathParam("id") Long id,
                                 @NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid AssignTicketRequest request,
                                 @Context SecurityContext securityContext) {
        return ticketService.assign(id, request, securityContext);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        ticketService.delete(id, securityContext);
        return Response.noContent().build();
    }
}
