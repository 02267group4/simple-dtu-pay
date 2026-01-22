package dk.dtu.pay.merchant.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.*;
import java.util.concurrent.*;

@ApplicationScoped
public class MerchantReportMessagingClient {

    @Channel("merchant-report-requests")
    Emitter<String> requestEmitter;

    private final ObjectMapper mapper = new ObjectMapper();

    // correlationId -> future waiting for reply JSON
    private final Map<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    // These must match the JSON the payment-service expects / returns
    public static class MerchantReportRequest {
        public String correlationId;
        public String merchantId;
    }

    public static class MerchantReportResponse {
        public String correlationId;
        public List<Object> payments;  
    }

    /** Called by REST to ask payment-service for a report */
    public List<Object> requestReport(String merchantId) {
        try {
            String correlationId = UUID.randomUUID().toString();

            MerchantReportRequest req = new MerchantReportRequest();
            req.correlationId = correlationId;
            req.merchantId = merchantId;

            String json = mapper.writeValueAsString(req);

            CompletableFuture<String> future = new CompletableFuture<>();
            pending.put(correlationId, future);

            requestEmitter.send(json);

            // wait up to 5 seconds for reply
            String replyJson = future.get(5, TimeUnit.SECONDS);

            MerchantReportResponse resp =
                    mapper.readValue(replyJson, MerchantReportResponse.class);

            return resp.payments;

        } catch (Exception e) {
            throw new RuntimeException("Could not get merchant report", e);
        }
    }

    /** Handles replies coming back from payment-service */
    @Incoming("merchant-report-replies")
    void onMerchantReportReply(String rawReply) {
        try {
            MerchantReportResponse resp =
                    mapper.readValue(rawReply, MerchantReportResponse.class);

            CompletableFuture<String> future = pending.remove(resp.correlationId);
            if (future != null) {
                future.complete(rawReply);
            }
        } catch (Exception e) {
            // you can add logging here
        }
    }
}