package dk.dtu.pay.payment.adapter.out.messaging.dto;

public record PaymentRequested(String paymentId, String token) {}
