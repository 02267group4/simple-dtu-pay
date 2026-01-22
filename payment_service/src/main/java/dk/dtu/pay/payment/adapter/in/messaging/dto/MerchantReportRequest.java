package dk.dtu.pay.payment.adapter.in.messaging.dto;

public record MerchantReportRequest(
    String correlationId,
    String merchantId
) {}
