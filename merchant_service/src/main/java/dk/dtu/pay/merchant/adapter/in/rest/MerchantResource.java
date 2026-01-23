package dk.dtu.pay.merchant.adapter.in.rest;

import dk.dtu.pay.merchant.domain.model.Merchant;
import dk.dtu.pay.merchant.domain.model.PaymentDTO;
import dk.dtu.pay.merchant.domain.service.MerchantService;
import dk.dtu.pay.merchant.adapter.out.messaging.RabbitMQMerchantReportPublisher;
import dk.dtu.pay.merchant.adapter.out.request.MerchantReportStore;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Path("/merchants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MerchantResource {

    @Inject
    MerchantService merchantService;

    @Inject
    RabbitMQMerchantReportPublisher reportPublisher;

    @Inject
    MerchantReportStore reportStore;

    // ---------- basic merchant API ----------

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

    // ---------- merchant report over RabbitMQ ----------

    @GET
    @Path("/{id}/report")
    public Response getMerchantReport(@PathParam("id") String merchantId) {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<List<PaymentDTO>> future = reportStore.createPending(correlationId);

        try {
            // send request event to payment service
            reportPublisher.publish(correlationId, merchantId);

            // wait (max 5 seconds) for async reply
            List<PaymentDTO> payments = future.get(5, TimeUnit.SECONDS);
            return Response.ok(payments).build();

        } catch (Exception e) {
            reportStore.remove(correlationId);
            return Response.status(Response.Status.GATEWAY_TIMEOUT)
                    .entity("Timed out while waiting for merchant report")
                    .build();
        }
    }
}
