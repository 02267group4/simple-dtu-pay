package dk.dtu.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PaymentRequest {
    public int amount;
    public String customerId;
    public String merchantId;

    public PaymentRequest() {
    }

    public PaymentRequest(int amount, String customerId, String merchantId) {
        this.amount = amount;
        this.customerId = customerId;
        this.merchantId = merchantId;
    }
}