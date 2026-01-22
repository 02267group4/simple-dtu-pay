package dk.dtu.pay.merchant.adapter.in.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.pay.merchant.domain.model.Merchant;
import dk.dtu.pay.merchant.domain.model.MerchantReportEvents.MerchantReportRequest;
import dk.dtu.pay.merchant.domain.model.MerchantReportEvents.MerchantReportResponse;
import dk.dtu.pay.merchant.domain.service.MerchantService;
import dk.dtu.pay.payment.domain.model.Payment;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Path("/merchants")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantResource {

    @Inject
    MerchantService merchantService;

    // Outgoing request channel → payment-service
    // Matches: mp.messaging.outgoing.merchant-report-requests-out...
    @Channel("merchant-report-requests-out")
    Emitter<String> reportRequestEmitter;

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // correlationId -> Future waiting for report reply
    private static final Map<String, CompletableFuture<List<Payment>>> pendingReports =
            new ConcurrentHashMap<>();

    // ----------------- BASIC MERCHANT API -----------------

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

    // ----------------- MERCHANT REPORT (RabbitMQ) -----------------

    @GET
    @Path("/{id}/reports")
    public Response getMerchantReport(@PathParam("id") String merchantId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<List<Payment>> future = new CompletableFuture<>();

        pendingReports.put(correlationId, future);

        try {
            // Create request JSON
            String json = mapper.writeValueAsString(
                    new MerchantReportRequest(correlationId, merchantId)
            );

            // Send RabbitMQ request → payment-service
            reportRequestEmitter.send(json);

            // Wait (up to 5 seconds) for async reply
            List<Payment> payments = future.get(5, TimeUnit.SECONDS);

            return Response.ok(payments).build();

        } catch (Exception e) {
            pendingReports.remove(correlationId);
            return Response.status(Response.Status.GATEWAY_TIMEOUT)
                    .entity("Timed out while waiting for merchant report")
                    .build();
        }
    }

    // Incoming reply from payment-service
    // Matches: mp.messaging.incoming.merchant-report-replies-in...
    @Incoming("merchant-report-replies-in")
    public void onMerchantReportReply(String rawResponse) {
        try {
            MerchantReportResponse response =
                    mapper.readValue(rawResponse, MerchantReportResponse.class);

            if (response == null || response.correlationId == null) {
                return;
            }

            CompletableFuture<List<Payment>> future =
                    pendingReports.remove(response.correlationId);

            if (future != null) {
                future.complete(response.payments);
            }

        } catch (IOException e) {
            System.err.println("Failed to handle merchant report reply: " + e.getMessage());
        }
    }
}