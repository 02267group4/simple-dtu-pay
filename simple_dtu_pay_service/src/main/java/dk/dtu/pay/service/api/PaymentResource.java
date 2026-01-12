package dk.dtu.pay.service.api;

import dk.dtu.pay.service.AppContext;
import dk.dtu.pay.service.model.Payment;
import dk.dtu.pay.service.model.PaymentRequest;
import dk.dtu.pay.service.service.PaymentService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @POST
    public Response pay(PaymentRequest request) {
        try {
            Payment payment = AppContext.paymentService.pay(request);
            return Response.status(201).entity(payment).build();

        } catch (PaymentService.UnknownCustomerException | PaymentService.UnknownMerchantException e) {
            return Response.status(404).entity(e.getMessage()).build();

        } catch (PaymentService.BankFailureException e) {
            return Response.status(409).entity(e.getMessage()).build();
        }
    }

    @GET
    public List<Payment> getPayments() {
        return AppContext.paymentService.getPayments();
    }
}
