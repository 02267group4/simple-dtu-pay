package dk.dtu.pay.payment.adapter.in.rest;

import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/manager/reports")
@Produces(MediaType.APPLICATION_JSON)
public class ManagerReportResource {

    @Inject
    PaymentService paymentService;

    @GET
    public List<Payment> getReport() {
        // Manager report is currently equivalent to "all payments".
        return paymentService.getPayments();
    }
}