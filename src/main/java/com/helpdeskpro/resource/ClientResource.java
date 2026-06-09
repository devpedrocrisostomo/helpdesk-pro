package com.helpdeskpro.resource;

import com.helpdeskpro.dto.client.ClientRequest;
import com.helpdeskpro.dto.client.ClientResponse;
import com.helpdeskpro.service.ClientService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
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

@Path("/clients")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ClientResource {
    @Inject
    ClientService clientService;

    @GET
    public List<ClientResponse> findAll(@Context SecurityContext securityContext) {
        return clientService.findAll(securityContext);
    }

    @POST
    public Response create(@NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid ClientRequest request,
                           @Context SecurityContext securityContext,
                           @Context UriInfo uriInfo) {
        ClientResponse response = clientService.create(request, securityContext);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(response.getId().toString()).build())
                .entity(response)
                .build();
    }

    @GET
    @Path("/{id}")
    public ClientResponse findById(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        return clientService.findById(id, securityContext);
    }

    @PUT
    @Path("/{id}")
    public ClientResponse update(@PathParam("id") Long id,
                                 @NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid ClientRequest request,
                                 @Context SecurityContext securityContext) {
        return clientService.update(id, request, securityContext);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        clientService.delete(id, securityContext);
        return Response.noContent().build();
    }
}
