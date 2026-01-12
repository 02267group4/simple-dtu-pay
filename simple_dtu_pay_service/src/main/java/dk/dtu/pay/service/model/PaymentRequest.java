package dk.dtu.pay.service.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PaymentRequest {
    public int amount;
    public String customerId;
    public String merchantId;

    public PaymentRequest() {
    }
}