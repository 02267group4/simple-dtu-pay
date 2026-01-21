package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.dtu.pay.manager.domain.model.ManagerReportEvents.*;
import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import dk.dtu.pay.payment.domain.model.Payment;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
public class PaymentReportProcessor {

    @Inject
    PaymentRepositoryPort paymentRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Incoming("manager-requests-in")
    @Outgoing("manager-replies-out")
    public String processManagerRequest(String rawRequest) {
        try {
            ManagerReportRequest request = mapper.readValue(rawRequest, ManagerReportRequest.class);

            if (request.correlationId == null) {
                return null;
            }

            System.out.println(
                    "PAYMENT_SERVICE: Received MANAGER report request [CorrId: " + request.correlationId + "]");

            List<Payment> allPayments = paymentRepository.all();
            ManagerReportResponse response = new ManagerReportResponse(request.correlationId, allPayments);

            return mapper.writeValueAsString(response);

        } catch (IOException e) {
            System.err.println("Failed to process manager report request: " + e.getMessage());
            return null;
        }
    }
}