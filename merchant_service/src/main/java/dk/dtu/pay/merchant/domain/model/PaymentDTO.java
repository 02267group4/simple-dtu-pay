package dk.dtu.pay.merchant.domain.model;

/**
 * Local DTO mirroring Payment from payment_service for JSON deserialization.
 * Microservices must not share code directly - each has its own DTOs.
 */
public class PaymentDTO {
    public String id;
    public int amount;
    public String customerId;
    public String merchantId;
    public String merchantBankAccountId;
    public String token;
    public String status;
    public String failureReason;

    public PaymentDTO() {
    }
}
