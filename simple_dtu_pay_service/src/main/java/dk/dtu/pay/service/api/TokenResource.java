package dk.dtu.pay.service.api;

import dk.dtu.pay.service.AppContext;
import dk.dtu.pay.service.model.Token;
import dk.dtu.pay.service.model.TokenRequest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tokens")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TokenResource {

    @POST
    public Response createToken(TokenRequest req) {
        if (req == null || req.customerId == null || req.customerId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("customerId is required")
                    .build();
        }

        try {
            String token = AppContext.tokenService.issueToken(req.customerId);
            return Response.status(Response.Status.CREATED)
                    .entity(new Token(token))
                    .build();
        } catch (IllegalArgumentException e) {
            // customer not found
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
