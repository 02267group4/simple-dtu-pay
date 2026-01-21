package dk.dtu.pay.token.adapter.out.messaging.dto;

import java.util.List;

public record TokenListResult(String requestId, List<String> tokens) {}