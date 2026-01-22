package dk.dtu.pay.merchant.adapter.out.request;

import dk.dtu.pay.merchant.domain.model.PaymentDTO;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores pending merchant report requests, awaiting async responses from payment service.
 */
@ApplicationScoped
public class MerchantReportStore {

    private final Map<String, CompletableFuture<List<PaymentDTO>>> pendingReports = new ConcurrentHashMap<>();

    public CompletableFuture<List<PaymentDTO>> createPending(String correlationId) {
        CompletableFuture<List<PaymentDTO>> future = new CompletableFuture<>();
        pendingReports.put(correlationId, future);
        return future;
    }

    public void complete(String correlationId, List<PaymentDTO> payments) {
        CompletableFuture<List<PaymentDTO>> future = pendingReports.remove(correlationId);
        if (future != null) {
            future.complete(payments);
        }
    }

    public void remove(String correlationId) {
        pendingReports.remove(correlationId);
    }
}
