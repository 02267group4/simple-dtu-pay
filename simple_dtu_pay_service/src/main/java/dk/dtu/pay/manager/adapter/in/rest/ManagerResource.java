package dk.dtu.pay.manager.adapter.in.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.pay.manager.domain.model.ManagerReportEvents.*;
import dk.dtu.pay.payment.domain.model.Payment;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Path("/manager")
public class ManagerResource {

    @Channel("manager-requests-out")
    Emitter<String> reportRequestEmitter;

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Map<String, CompletableFuture<List<Payment>>> pendingRequests = new ConcurrentHashMap<>();

    @GET
    @Path("/reports")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Payment> getManagerReport() {
        String correlationId = UUID.randomUUID().toString();
        CompletableFuture<List<Payment>> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        System.out.println("MANAGER_FACADE: Requesting full report [CorrId: " + correlationId + "]");

        try {
            String jsonRequest = mapper.writeValueAsString(new ManagerReportRequest(correlationId));
            reportRequestEmitter.send(jsonRequest);
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            pendingRequests.remove(correlationId);
            throw new RuntimeException("Manager report generation failed", e);
        }
    }

    @Incoming("manager-replies-in")
    public void onReportResponse(String rawResponse) {
        try {
            ManagerReportResponse response = mapper.readValue(rawResponse, ManagerReportResponse.class);

            if (response.correlationId == null) {
                return;
            }

            if (response.payments == null) {
                System.err.println("MANAGER_FACADE: Received response with NULL payments. [CorrId: "
                        + response.correlationId + "]");
                return;
            }

            System.out.println("MANAGER_FACADE: Received response [CorrId: " + response.correlationId + "]");

            CompletableFuture<List<Payment>> future = pendingRequests.get(response.correlationId);
            if (future != null) {
                future.complete(response.payments);
                pendingRequests.remove(response.correlationId);
            }
        } catch (IOException e) {
            System.err.println("Failed to handle manager reply: " + e.getMessage());
        }
    }
}