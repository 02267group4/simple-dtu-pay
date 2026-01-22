package dk.dtu.pay.merchant.domain.model;

import java.util.List;

public class MerchantReportEvents {

    // Request sent from Merchant facade -> Payment service
    public static class MerchantReportRequest {
        public String correlationId;
        public String merchantId;

        public MerchantReportRequest() {
        }

        public MerchantReportRequest(String correlationId, String merchantId) {
            this.correlationId = correlationId;
            this.merchantId = merchantId;
        }
    }

    // Response sent from Payment service -> Merchant facade
    public static class MerchantReportResponse {
        public String correlationId;
        public List<PaymentDTO> payments;

        public MerchantReportResponse() {
        }

        public MerchantReportResponse(String correlationId, List<PaymentDTO> payments) {
            this.correlationId = correlationId;
            this.payments = payments;
        }
    }
}