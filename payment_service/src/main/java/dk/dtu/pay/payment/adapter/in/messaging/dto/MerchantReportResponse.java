package dk.dtu.pay.payment.adapter.in.messaging.dto;

import java.util.List;
import dk.dtu.pay.payment.domain.model.Payment;

public class MerchantReportResponse {
    public String correlationId;
    public List<Payment> payments;

    // Default constructor for Jackson
    public MerchantReportResponse() {
    }

    public MerchantReportResponse(String correlationId, List<Payment> payments) {
        this.correlationId = correlationId;
        this.payments = payments;
    }
}