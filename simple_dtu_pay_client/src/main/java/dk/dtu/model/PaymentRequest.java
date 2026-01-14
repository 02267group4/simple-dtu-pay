package dk.dtu.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PaymentRequest {
    public String token;
    public String merchantId;
    public int amount;
    public String description;

    public PaymentRequest() {
    }

    public PaymentRequest(String token, String merchantId, int amount, String description) {
        this.token = token;
        this.merchantId = merchantId;
        this.amount = amount;
        this.description = description;
    }
}