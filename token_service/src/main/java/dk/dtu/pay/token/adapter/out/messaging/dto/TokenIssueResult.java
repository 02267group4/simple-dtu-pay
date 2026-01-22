package dk.dtu.pay.token.adapter.out.messaging.dto;

import java.util.List;

public record TokenIssueResult(String requestId, boolean success, String error, List<String> tokens) {}
