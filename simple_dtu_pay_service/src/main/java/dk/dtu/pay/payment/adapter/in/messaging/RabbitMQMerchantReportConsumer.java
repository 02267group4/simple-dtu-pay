package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.dtu.pay.merchant.domain.model.MerchantReportEvents.MerchantReportRequest;
import dk.dtu.pay.merchant.domain.model.MerchantReportEvents.MerchantReportResponse;
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

    @Channel("merchant-report-replies-out")
    Emitter<String> replyEmitter;

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Incoming("merchant-report-requests-in")
    public void onMerchantReportRequest(String rawRequest) {
        try {
            MerchantReportRequest request =
                    mapper.readValue(rawRequest, MerchantReportRequest.class);

            List<Payment> payments =
                    paymentService.getPaymentsForMerchant(request.merchantId);

            MerchantReportResponse response =
                    new MerchantReportResponse(request.correlationId, payments);

            String json = mapper.writeValueAsString(response);
            replyEmitter.send(json);

        } catch (PaymentService.UnknownMerchantException e) {
            System.err.println("Unknown merchant in report request: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Failed to handle merchant report request: " + e.getMessage());
        }
    }
}