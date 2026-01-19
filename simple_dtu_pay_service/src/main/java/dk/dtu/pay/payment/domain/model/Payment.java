package dk.dtu.pay.payment.domain.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Payment {
    public String id;
    public int amount;
    public String customerId;
    public String merchantId;

    public Status status;
    public String failureReason;

    public Payment() {
    }

    public enum Status {
        PENDING,
        COMPLETED,
        FAILED
    }
}
