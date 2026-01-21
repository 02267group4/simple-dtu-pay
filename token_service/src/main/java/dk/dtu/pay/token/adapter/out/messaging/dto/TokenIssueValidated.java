package dk.dtu.pay.token.adapter.out.messaging.dto;

import java.util.List;

public record TokenIssueValidated(String requestId, List<String> tokens) {}