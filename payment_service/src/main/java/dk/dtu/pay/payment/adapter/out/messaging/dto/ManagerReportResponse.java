package dk.dtu.pay.payment.adapter.out.messaging.dto;

import dk.dtu.pay.payment.domain.model.Payment;
import java.util.List;

public record ManagerReportResponse(String correlationId, List<Payment> payments) {}
