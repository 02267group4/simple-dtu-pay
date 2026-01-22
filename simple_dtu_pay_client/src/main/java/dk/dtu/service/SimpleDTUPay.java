package dk.dtu.service;

import dk.dtu.model.*;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

public class SimpleDTUPay {

    // Microservice endpoints
    private static final String CUSTOMER_SERVICE_URL = "http://localhost:8081";
    private static final String MERCHANT_SERVICE_URL = "http://localhost:8082";
    private static final String PAYMENT_SERVICE_URL = "http://localhost:8083";
    private static final String TOKEN_SERVICE_URL = "http://localhost:8084";

    private final Client client = ClientBuilder.newClient();
    private final WebTarget customerTarget = client.target(CUSTOMER_SERVICE_URL);
    private final WebTarget merchantTarget = client.target(MERCHANT_SERVICE_URL);
    private final WebTarget paymentTarget = client.target(PAYMENT_SERVICE_URL);
    private final WebTarget tokenTarget = client.target(TOKEN_SERVICE_URL);

    public String register(Customer customer) {
        try (Response response = customerTarget.path("customers")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(customer, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() != 201)
                throw new RuntimeException("Reg failed");
            return response.readEntity(Customer.class).id;
        }
    }

    public String register(Merchant merchant) {
        try (Response response = merchantTarget.path("merchants")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(merchant, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() != 201)
                throw new RuntimeException("Reg failed");
            return response.readEntity(Merchant.class).id;
        }
    }

    public String requestToken(String customerId) {
        TokenRequest req = new TokenRequest(customerId);

        try (Response response = tokenTarget.path("tokens")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(req, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() == 404) {
                throw new NotFoundException(response.readEntity(String.class));
            }

            if (response.getStatus() != 201)
                throw new RuntimeException("Token request failed: HTTP " + response.getStatus());

            return response.readEntity(Token.class).token;
        }
    }

    // Async payment initiation
    // returns paymentId (or throws)
    public String payAsync(String token, String merchantId, int amount, String description) {
        PaymentRequest req = new PaymentRequest(token, merchantId, amount, description);

        try (Response response = paymentTarget.path("payments")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(req, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() == 404) {
                throw new NotFoundException(response.readEntity(String.class));
            }

            if (response.getStatus() != 202) {
                throw new RuntimeException("Payment request failed: HTTP " + response.getStatus());
            }

            Payment p = response.readEntity(Payment.class);
            return p.id;
        }
    }

    // Poll until COMPLETED or FAILED, true if COMPLETED
    public boolean waitForPaymentCompleted(String paymentId, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;

        while (System.currentTimeMillis() < deadline) {
            Payment p = findPayment(paymentId);
            if (p != null && p.status != null) {
                if ("COMPLETED".equalsIgnoreCase(p.status)) return true;
                if ("FAILED".equalsIgnoreCase(p.status)) return false;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }

    private Payment findPayment(String paymentId) {
        List<Payment> all = getPayments();
        for (Payment p : all) {
            if (p != null && paymentId.equals(p.id)) return p;
        }
        return null;
    }

    public List<Payment> getPayments() {
        return paymentTarget.path("payments")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Payment>>() {});
    }

    public void unregisterCustomer(String id) {
        try (Response r = customerTarget.path("customers/" + id).request().delete()) {
            r.getStatus();
        }
    }

    public void unregisterMerchant(String id) {
        try (Response r = merchantTarget.path("merchants/" + id).request().delete()) {
            r.getStatus();
        }
    }

    public List<Payment> getManagerReport() {
        return paymentTarget.path("manager/reports")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Payment>>() {
                });
    }

    public void close() {
        client.close();
    }
}
