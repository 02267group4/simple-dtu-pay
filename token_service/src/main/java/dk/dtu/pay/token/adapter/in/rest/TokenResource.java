package dk.dtu.pay.token.adapter.in.rest;

import dk.dtu.pay.token.domain.model.Token;
import dk.dtu.pay.token.domain.model.TokenRequest;
import dk.dtu.pay.token.domain.service.TokenService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tokens")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TokenResource {

    @Inject
    TokenService tokenService;

    @POST
    public Response createToken(TokenRequest req) {
        if (req == null || req.customerId == null || req.customerId.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("customerId is required")
                    .build();
        }

        String token = tokenService.issueToken(req.customerId, req.bankAccountId);
        return Response.status(Response.Status.CREATED)
                .entity(new Token(token))
                .build();
    }
}
