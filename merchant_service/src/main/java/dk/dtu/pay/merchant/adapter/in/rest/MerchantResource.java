package dk.dtu.pay.merchant.adapter.in.rest;

import dk.dtu.pay.merchant.domain.model.Merchant;
import dk.dtu.pay.merchant.domain.service.MerchantService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/merchants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantResource {

    @Inject
    MerchantService merchantService;

    @POST
    public Response register(Merchant req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("name is required")
                    .build();
        }
        Merchant saved = merchantService.registerMerchant(req);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        merchantService.deleteMerchant(id);
        return Response.noContent().build();
    }
}
