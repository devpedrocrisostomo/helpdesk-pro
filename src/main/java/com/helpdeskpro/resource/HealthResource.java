package com.helpdeskpro.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.Map;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {
    @GET
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "HelpDesk Pro",
                "timestamp", Instant.now().toString()
        );
    }
}
