package dk.dtu.pay.merchant.domain.model;

public class MerchantPaymentReport {
    public int amount;
    public String token;

    public MerchantPaymentReport(int amount, String token) {
        this.amount = amount;
        this.token = token;
    }
}