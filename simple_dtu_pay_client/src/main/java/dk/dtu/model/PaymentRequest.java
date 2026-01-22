package dk.dtu.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PaymentRequest {
    public String token;
    public String merchantId;
    public String merchantBankAccountId;
    public int amount;
    public String description;

    public PaymentRequest() {
    }

    public PaymentRequest(String token, String merchantId, String merchantBankAccountId, int amount, String description) {
        this.token = token;
        this.merchantId = merchantId;
        this.merchantBankAccountId = merchantBankAccountId;
        this.amount = amount;
        this.description = description;
    }
}