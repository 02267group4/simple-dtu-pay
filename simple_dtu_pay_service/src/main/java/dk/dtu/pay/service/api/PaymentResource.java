package dk.dtu.pay.service.api;

import java.util.List;

import dk.dtu.pay.service.AppContext;
import dk.dtu.pay.service.domain.model.Payment;
import dk.dtu.pay.service.domain.model.PaymentRequest;
import dk.dtu.pay.service.domain.service.PaymentService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @POST
    public Response pay(PaymentRequest request) {
        try {
            Payment payment = AppContext.paymentService.pay(request);
            return Response.status(Response.Status.CREATED).entity(payment).build();

        } catch (PaymentService.UnknownCustomerException | PaymentService.UnknownMerchantException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();

        } catch (PaymentService.BankFailureException e) {
            return Response.status(Response.Status.BAD_GATEWAY).entity(e.getMessage()).build();
        }
    }

    @GET
    public List<Payment> getPayments() {
        return AppContext.paymentService.getPayments();
    }
    
    // GET /payments/merchant/{merchantId}
    @GET
    @Path("/merchant/{merchantId}")
    public Response getPaymentsForMerchant(@PathParam("merchantId") String merchantId) {
        try {
            List<Payment> results = AppContext.paymentService.getPaymentsForMerchant(merchantId);
            return Response.ok(results).build();

        } catch (PaymentService.UnknownMerchantException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }
}