package dk.dtu.pay.customer.domain.model;

import java.util.List;

public class CustomerReportEvents {

    // Request sent from Customer facade -> Payment service
    public static class CustomerReportRequest {
        public String correlationId;
        public String customerId;

        public CustomerReportRequest() {
        }

        public CustomerReportRequest(String correlationId, String customerId) {
            this.correlationId = correlationId;
            this.customerId = customerId;
        }
    }

    // Response sent from Payment service -> Customer facade
    public static class CustomerReportResponse {
        public String correlationId;
        public List<PaymentDTO> payments;

        public CustomerReportResponse() {
        }

        public CustomerReportResponse(String correlationId, List<PaymentDTO> payments) {
            this.correlationId = correlationId;
            this.payments = payments;
        }
    }
}
