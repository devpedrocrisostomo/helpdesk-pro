package com.helpdeskpro.resource;

import com.helpdeskpro.dto.comment.CommentRequest;
import com.helpdeskpro.dto.comment.CommentResponse;
import com.helpdeskpro.service.CommentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;

@Path("/tickets/{ticketId}/comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommentResource {
    @Inject
    CommentService commentService;

    @GET
    public List<CommentResponse> findByTicketId(@PathParam("ticketId") Long ticketId,
                                                @Context SecurityContext securityContext) {
        return commentService.findByTicketId(ticketId, securityContext);
    }

    @POST
    public Response create(@PathParam("ticketId") Long ticketId,
                           @NotNull(message = "Corpo da requisicao e obrigatorio.") @Valid CommentRequest request,
                           @Context SecurityContext securityContext,
                           @Context UriInfo uriInfo) {
        CommentResponse response = commentService.create(ticketId, request, securityContext);
        return Response.created(uriInfo.getAbsolutePathBuilder().path(response.getId().toString()).build())
                .entity(response)
                .build();
    }

    @DELETE
    @Path("/{commentId}")
    public Response delete(@PathParam("ticketId") Long ticketId,
                           @PathParam("commentId") Long commentId,
                           @Context SecurityContext securityContext) {
        commentService.delete(ticketId, commentId, securityContext);
        return Response.noContent().build();
    }
}
