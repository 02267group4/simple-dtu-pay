package dk.dtu.pay.manager.adapter.in.rest;

import dk.dtu.pay.manager.domain.model.PaymentDTO;
import dk.dtu.pay.manager.adapter.out.messaging.RabbitMQManagerReportPublisher;
import dk.dtu.pay.manager.adapter.out.request.ManagerReportStore;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Path("/manager")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ManagerResource {

    @Inject
    RabbitMQManagerReportPublisher reportPublisher;

    @Inject
    ManagerReportStore reportStore;

    @GET
    @Path("/reports")
    public Response getManagerReport() {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<List<PaymentDTO>> future = reportStore.createPending(correlationId);

        try {
            // send request event to payment service
            reportPublisher.publish(correlationId);

            // wait (max 30 seconds) for async reply
            List<PaymentDTO> payments = future.get(30, TimeUnit.SECONDS);
            return Response.ok(payments).build();

        } catch (Exception e) {
            reportStore.remove(correlationId);
            return Response.status(Response.Status.GATEWAY_TIMEOUT)
                    .entity("Timed out while waiting for manager report")
                    .build();
        }
    }
}
