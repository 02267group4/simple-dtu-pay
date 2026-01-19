package dk.dtu.pay.payment.domain.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PaymentRequest {
    public String token;
    public String merchantId;
    public int amount;
    public String description;

    public PaymentRequest() {
    }
}
