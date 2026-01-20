package dk.dtu.pay.merchant.adapter.in.rest;

import dk.dtu.pay.merchant.domain.model.Merchant;
import dk.dtu.pay.service.domain.service.PaymentService;
import dk.dtu.pay.service.AppContext;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/merchants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantResource {

    @POST
    public Response register(Merchant req) {
        if (req == null || req.name == null || req.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("name is required")
                    .build();
        }
        Merchant saved = AppContext.merchantService.registerMerchant(req);
        return Response.status(Response.Status.CREATED).entity(saved).build();
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") String id) {
        AppContext.merchantService.deleteMerchant(id);
        return Response.noContent().build();
    }
    
    @GET
    @Path("/{id}/report")
    public Response getMerchantReport(@PathParam("id") String merchantId) {
        try {
            return Response.ok(
                    AppContext.paymentService
                            .getPaymentsForMerchant(merchantId)
                            .stream()
                            .map(p -> new dk.dtu.pay.merchant.domain.model.MerchantPaymentReport(
                                    p.amount,
                                    p.token
                            ))
                            .toList()
            ).build();

        } catch (PaymentService.UnknownMerchantException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }
}