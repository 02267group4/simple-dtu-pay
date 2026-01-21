package dk.dtu.pay.token.adapter.out.messaging.dto;

public record TokenRejected(String paymentId, String reason) {}
