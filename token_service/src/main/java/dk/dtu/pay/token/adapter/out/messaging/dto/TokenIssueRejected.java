package dk.dtu.pay.token.adapter.out.messaging.dto;

public record TokenIssueRejected(String requestId, String reason) {}