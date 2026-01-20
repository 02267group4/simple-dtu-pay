package dk.dtu.pay.payment.adapter.in.messaging;

import dk.dtu.pay.manager.domain.model.ManagerReportEvents.*; // <--- Make sure this is imported
import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import dk.dtu.pay.payment.domain.model.Payment;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class PaymentReportProcessor {

    @Inject
    PaymentRepositoryPort paymentRepository;

    // --- MANAGER REPORT LOGIC ---
    @Incoming("manager-requests")
    @Outgoing("manager-replies")
    public ManagerReportResponse processManagerRequest(ManagerReportRequest request) {
        System.out.println("PAYMENT_SERVICE: Received MANAGER report request");

        List<Payment> allPayments = paymentRepository.all();
        return new ManagerReportResponse(request.correlationId, allPayments);
    }
}