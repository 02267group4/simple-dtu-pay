package dk.dtu.pay.token.adapter.out.messaging.dto;

public record TokenValidationRejected(String paymentId, String reason) {}
