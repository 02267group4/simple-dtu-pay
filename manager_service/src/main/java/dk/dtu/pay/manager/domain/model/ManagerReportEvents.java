package dk.dtu.pay.manager.domain.model;

import java.util.List;

public class ManagerReportEvents {

    // Request sent from Manager service -> Payment service
    public static class ManagerReportRequest {
        public String correlationId;

        public ManagerReportRequest() {
        }

        public ManagerReportRequest(String correlationId) {
            this.correlationId = correlationId;
        }
    }

    // Response sent from Payment service -> Manager service
    public static class ManagerReportResponse {
        public String correlationId;
        public List<PaymentDTO> payments;

        public ManagerReportResponse() {
        }

        public ManagerReportResponse(String correlationId, List<PaymentDTO> payments) {
            this.correlationId = correlationId;
            this.payments = payments;
        }
    }
}
