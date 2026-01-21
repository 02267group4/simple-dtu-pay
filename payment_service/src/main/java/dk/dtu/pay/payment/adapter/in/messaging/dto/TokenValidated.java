package dk.dtu.pay.payment.adapter.in.messaging.dto;

public record TokenValidated(String paymentId, String customerId) {}