
package dk.dtu.pay.payment.adapter.in.rest;

import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.model.PaymentRequest;
import dk.dtu.pay.payment.domain.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    PaymentService paymentService;

    @POST
    public Response pay(PaymentRequest request) {
        // Since we decoupled the service, validation happens asynchronously.
        // We simply accept the payment request and return 202 Accepted.
        Payment payment = paymentService.pay(request);

        return Response.status(Response.Status.ACCEPTED)
                .entity(payment)
                .build();
    }

    @GET
    public List<Payment> getPayments() {
        return paymentService.getPayments();
    }
}