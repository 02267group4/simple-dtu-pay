package dk.dtu.pay.payment.adapter.in.rest;

import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQManagerReportResponseConsumer;
import dk.dtu.pay.payment.adapter.out.messaging.RabbitMQManagerReportRequestPublisher;
import dk.dtu.pay.payment.domain.model.Payment;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Path("/manager")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ManagerResource {

    @Inject
    RabbitMQManagerReportRequestPublisher requestPublisher;

    @Inject
    RabbitMQManagerReportResponseConsumer responseConsumer;

    @GET
    @Path("/reports")
    public Response getManagerReport() {
        String correlationId = UUID.randomUUID().toString();
        
        // Register for the response before publishing
        CompletableFuture<List<Payment>> future = responseConsumer.registerPendingRequest(correlationId);
        
        // Publish the request
        requestPublisher.publishReportRequest(correlationId);
        
        try {
            // Wait for response with timeout
            List<Payment> payments = future.get(30, TimeUnit.SECONDS);
            return Response.ok(payments).build();
        } catch (TimeoutException e) {
            return Response.status(Response.Status.GATEWAY_TIMEOUT)
                    .entity("Manager report request timed out")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error retrieving manager report: " + e.getMessage())
                    .build();
        }
    }
}
