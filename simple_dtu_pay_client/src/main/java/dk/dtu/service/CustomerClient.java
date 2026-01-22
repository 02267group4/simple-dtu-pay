package dk.dtu.service;

import dk.dtu.model.Customer;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

public class CustomerClient {

    /**
     * Customer service base URL (matches docker-compose port mappings).
     * Override with env var: CUSTOMER_SERVICE_URL
     */
    private static final String CUSTOMER_BASE_URL =
            System.getenv().getOrDefault("CUSTOMER_SERVICE_URL", "http://localhost:8081");

    // JAX-RS client used for all HTTP interactions
    private final Client client = ClientBuilder.newClient();
    private final WebTarget target = client.target(CUSTOMER_BASE_URL);

    public String register(Customer customer) {
        // POST /customers — synchronous customer creation
        try (Response response = target.path("customers")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(customer, MediaType.APPLICATION_JSON))) {

            // Backend contract: customer creation must return 201
            if (response.getStatus() != 201) {
                throw new RuntimeException(
                        "Customer registration failed: HTTP " + response.getStatus());
            }

            // Backend returns the created Customer with generated id
            return response.readEntity(Customer.class).id;
        }
    }

    public void unregister(String customerId) {
        // DELETE /customers/{id} — best-effort cleanup for tests
        try (Response r = target.path("customers/" + customerId)
                .request()
                .delete()) {
            // Intentionally ignore status; cleanup should not fail tests
            r.getStatus();
        }
    }

    public String requestTokens(String customerId, int count) {
        // POST /customers/{id}/tokens?count=N
        // This only triggers the async workflow and returns a requestId
        try (Response response = target
                .path("customers/" + customerId + "/tokens")
                .queryParam("count", count)
                .request(MediaType.APPLICATION_JSON)
                .post(null)) {

            // Backend contract: async requests always return 202 Accepted
            if (response.getStatus() != 202) {
                throw new RuntimeException(
                        "Token request failed: HTTP " + response.getStatus());
            }

            // Response body: { "requestId": "<uuid>" }
            Map<String, String> body = response.readEntity(Map.class);
            return body.get("requestId");
        }
    }

    public String requestTokenList(String customerId) {
        // GET /customers/{id}/tokens
        // Same async pattern as token issuance: returns requestId only
        try (Response response = target
                .path("customers/" + customerId + "/tokens")
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() != 202) {
                throw new RuntimeException(
                        "Token list request failed: HTTP " + response.getStatus());
            }

            Map<String, String> body = response.readEntity(Map.class);
            return body.get("requestId");
        }
    }

    public void close() {
        // Explicit cleanup to avoid leaking HTTP connections in test runs
        client.close();
    }
}
