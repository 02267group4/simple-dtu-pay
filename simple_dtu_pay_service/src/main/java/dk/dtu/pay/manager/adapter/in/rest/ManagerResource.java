package dk.dtu.pay.manager.adapter.in.rest;

import dk.dtu.pay.manager.domain.model.ManagerReportEvents.*;
import dk.dtu.pay.payment.domain.model.Payment;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Path("/manager")
public class ManagerResource {

    // Sending to "manager-requests" channel
    @Channel("manager-requests")
    Emitter<ManagerReportRequest> reportRequestEmitter;

    // Map to hold pending requests. Key = Correlation ID
    private static final Map<String, CompletableFuture<List<Payment>>> pendingRequests = new ConcurrentHashMap<>();

    @GET
    @Path("/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Payment> getManagerReport() {
        // 1. Generate Correlation ID
        String correlationId = UUID.randomUUID().toString();

        // 2. Create Future
        CompletableFuture<List<Payment>> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        // 3. Publish Event
        System.out.println("MANAGER_FACADE: Requesting full report [CorrId: " + correlationId + "]");
        reportRequestEmitter.send(new ManagerReportRequest(correlationId));

        try {
            // 4. Wait for response (5s timeout)
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Manager report generation failed", e);
        } finally {
            // Always clean up the map to avoid memory leaks
            pendingRequests.remove(correlationId);
        }
    }

    // Listening to "manager-replies" channel
    @Incoming("manager-replies")
    public void onReportResponse(ManagerReportResponse response) {
        System.out.println("MANAGER_FACADE: Received response [CorrId: " + response.correlationId + "]");

        CompletableFuture<List<Payment>> future = pendingRequests.get(response.correlationId);
        if (future != null) {
            future.complete(response.payments);
        }
    }
}