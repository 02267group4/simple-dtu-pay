package dk.dtu.pay.service.api;

import dk.dtu.pay.service.AppContext;
import dk.dtu.pay.service.model.Merchant;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/merchants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantResource {

    @POST
    public Response registerMerchant(Merchant req) {
        Merchant m = AppContext.merchantService.registerMerchant(req);
        return Response.status(201).entity(m).build();
    }

    // Cleanup endpoint for testing (same behavior: void)
    @DELETE
    @Path("{id}")
    public void deleteMerchant(@PathParam("id") String id) {
        AppContext.merchantService.deleteMerchant(id);
    }
}
