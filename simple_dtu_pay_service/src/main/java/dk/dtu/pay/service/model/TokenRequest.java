package dk.dtu.pay.service.model;

public class TokenRequest {
    public String customerId;

    public TokenRequest() {}
    public TokenRequest(String customerId) {
        this.customerId = customerId;
    }
}