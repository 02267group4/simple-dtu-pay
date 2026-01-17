package dk.dtu.pay.payment.domain.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * @author Nick
 */
@RegisterForReflection
public class Payment {
    public String id;
    public int amount;
    public String customerId;
    public String merchantId;
    public String description;

    public Payment() {
    }

    public Payment(String id, String customerId, String merchantId, int amount, String description) {
        this.id = id;
        this.customerId = customerId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.description = description;
    }
}