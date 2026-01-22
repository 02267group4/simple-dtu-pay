package dk.dtu.pay.token.domain.model;

public class TokenRequest {
    public String customerId;
    public String bankAccountId;

    public TokenRequest() {}
    public TokenRequest(String customerId) {
        this.customerId = customerId;
    }
}