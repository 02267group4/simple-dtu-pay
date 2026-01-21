package dk.dtu.model;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Payment {
    public String id;
    public int amount;
    public String customerId;
    public String merchantId;

    public String status;        // "PENDING", "COMPLETED", "FAILED"
    public String failureReason; // null if ok

    public Payment() {
    }
}
