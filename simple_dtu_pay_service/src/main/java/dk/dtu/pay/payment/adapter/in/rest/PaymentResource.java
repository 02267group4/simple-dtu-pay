package dk.dtu.pay.payment.adapter.in.rest;

import dk.dtu.pay.payment.domain.service.PaymentService;
import dk.dtu.pay.service.domain.model.PaymentRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Nick
 */
@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    PaymentService paymentService;

    @POST
    public Response pay(PaymentRequest request) {
        try {
            var result = paymentService.pay(request.token, request.merchantId, request.amount, request.description);
            return Response.status(201).entity(result).build();
        } catch (Exception e) {
            return Response.status(400).entity(e.getMessage()).build();
        }
    }
}