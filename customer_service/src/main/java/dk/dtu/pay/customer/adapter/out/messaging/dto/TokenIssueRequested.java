package dk.dtu.pay.customer.adapter.out.messaging.dto;

public record TokenIssueRequested(String requestId, String customerId, String bankAccountId, int count) {}