package dk.dtu.pay.token.adapter.out.messaging.dto;

public record TokenValidated(String paymentId, String customerId) {}
