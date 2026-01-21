package dk.dtu.pay.token.adapter.in.messaging.dto;

public record PaymentRequested(String paymentId, String token) {}