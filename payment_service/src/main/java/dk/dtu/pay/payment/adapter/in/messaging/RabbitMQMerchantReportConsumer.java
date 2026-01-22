package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.service.PaymentService;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class RabbitMQMerchantReportConsumer {

    @Inject
    PaymentService paymentService;

    // Outgoing reply channel → merchant-service
    // Matches: mp.messaging.outgoing.merchant-report-replies-out...
    @Channel("merchant-report-replies-out")
    Emitter<String> replyEmitter;

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // local DTOs – they just need to match the JSON fields from merchant-service
    public static class MerchantReportRequest {
        public String correlationId;
        public String merchantId;

        public MerchantReportRequest() {}
    }

    public static class MerchantReportResponse {
        public String correlationId;
        public List<Payment> payments;

        public MerchantReportResponse() {}

        public MerchantReportResponse(String correlationId, List<Payment> payments) {
            this.correlationId = correlationId;
            this.payments = payments;
        }
    }

    // Incoming request channel from merchant-service
    // Matches: mp.messaging.incoming.merchant-report-requests-in...
    @Incoming("merchant-report-requests-in")
    public void onMerchantReportRequest(String rawRequest) {
        try {
            MerchantReportRequest request =
                    mapper.readValue(rawRequest, MerchantReportRequest.class);

            if (request == null || request.merchantId == null) {
                System.err.println("Merchant report request missing merchantId");
                return;
            }

            // Use PaymentService method to fetch payments for this merchant
            List<Payment> payments =
                    paymentService.getPaymentsForMerchant(request.merchantId);

            MerchantReportResponse response =
                    new MerchantReportResponse(request.correlationId, payments);

            String json = mapper.writeValueAsString(response);
            replyEmitter.send(json);

            System.out.println("Sent merchant report reply for merchant " + request.merchantId +
                    " with correlationId " + request.correlationId);

        } catch (IOException e) {
            System.err.println("Failed to handle merchant report request: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in merchant report handler: " + e.getMessage());
            e.printStackTrace();
        }
    }
}