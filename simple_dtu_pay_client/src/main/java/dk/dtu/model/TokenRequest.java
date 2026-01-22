package dk.dtu.model;

public class TokenRequest {
    public String customerId;
    public String bankAccountId;

    public TokenRequest() {}

    public TokenRequest(String customerId) {
        this.customerId = customerId;
    }

    public TokenRequest(String customerId, String bankAccountId) {
        this.customerId = customerId;
        this.bankAccountId = bankAccountId;
    }
}
