package dk.dtu.pay.payment.adapter.in.messaging.dto;

public record CustomerReportRequest(
    String correlationId,
    String customerId
) {}
