package dk.dtu.pay.customer.adapter.in.messaging.dto;

import java.util.List;

public record TokenListResult(
        String requestId,
        boolean success,
        String error,
        List<String> tokens
) {}

