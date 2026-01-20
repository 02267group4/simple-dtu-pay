package dk.dtu.pay.manager.domain.model;

import dk.dtu.pay.payment.domain.model.Payment;
import java.util.List;

public class ManagerReportEvents {

    // 1. The Request Event (Manager Facade -> PaymentService)
    public static class ManagerReportRequest {
        public String correlationId;
        // Manager requests ALL data, so no specific ID needed here

        public ManagerReportRequest() {
        }

        public ManagerReportRequest(String correlationId) {
            this.correlationId = correlationId;
        }
    }

    // 2. The Response Event (PaymentService -> Manager Facade)
    public static class ManagerReportResponse {
        public String correlationId;
        public List<Payment> payments;

        public ManagerReportResponse() {
        }

        public ManagerReportResponse(String correlationId, List<Payment> payments) {
            this.correlationId = correlationId;
            this.payments = payments;
        }
    }
}