package dk.dtu.pay.payment.adapter.in.messaging.dto;

public class MerchantReportRequest {
    public String correlationId;
    public String merchantId;

    // Default constructor for Jackson
    public MerchantReportRequest() {
    }

    public MerchantReportRequest(String correlationId, String merchantId) {
        this.correlationId = correlationId;
        this.merchantId = merchantId;
    }
}