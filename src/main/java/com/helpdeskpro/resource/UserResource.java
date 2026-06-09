package com.helpdeskpro.resource;

import com.helpdeskpro.dto.user.UserCreateRequest;
import com.helpdeskpro.dto.user.UserResponse;
import com.helpdeskpro.dto.user.UserUpdateRequest;
import com.helpdeskpro.service.UserService;
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

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
    @Inject
    UserService userService;

    @GET
    public List<UserResponse> findAll(@Context SecurityContext securityContext) {
        return userService.findAll(securityContext);
    }

    @POST
    public Response create(@NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid UserCreateRequest request,
                           @Context SecurityContext securityContext,
                           @Context UriInfo uriInfo) {
        UserResponse response = userService.create(request, securityContext);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(response.getId().toString()).build())
                .entity(response)
                .build();
    }

    @GET
    @Path("/{id}")
    public UserResponse findById(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        return userService.findById(id, securityContext);
    }

    @PUT
    @Path("/{id}")
    public UserResponse update(@PathParam("id") Long id,
                               @NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid UserUpdateRequest request,
                               @Context SecurityContext securityContext) {
        return userService.update(id, request, securityContext);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        userService.delete(id, securityContext);
        return Response.noContent().build();
    }
}
