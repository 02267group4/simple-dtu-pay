package dk.dtu.pay.token.adapter.in.messaging.dto;

public record TokenIssueRequested(String requestId, String customerId, int count) {}
