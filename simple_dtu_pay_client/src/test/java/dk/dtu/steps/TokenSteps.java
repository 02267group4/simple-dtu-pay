package dk.dtu.steps;

import dk.dtu.service.CustomerClient;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static org.junit.Assert.*;

public class TokenSteps {

    private static final String TOKEN_SERVICE_URL =
            System.getenv().getOrDefault("TOKEN_SERVICE_URL", "http://localhost:8084");

    private final CustomerClient customerClient = new CustomerClient();
    private String requestId;
    private String createdToken;

    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(int count) {
        assertNotNull("customerId must be set by Given step", TestContext.customerId);
        requestId = customerClient.requestTokens(TestContext.customerId, count);
        assertNotNull(requestId);
        TestContext.requestId = requestId;
    }

    @When("the customer requests their token list")
    public void theCustomerRequestsTheirTokenList() {
        assertNotNull("customerId must be set by Given step", TestContext.customerId);
        requestId = customerClient.requestTokenList(TestContext.customerId);
        assertNotNull(requestId);
        TestContext.requestId = requestId;
    }

    @When("the test creates a token for the customer")
    public void the_test_creates_a_token_for_the_customer() {
        assertNotNull("customerId must be set by Given step", TestContext.customerId);

        Client client = ClientBuilder.newClient();
        try (Response resp = client.target(TOKEN_SERVICE_URL)
                .path("tokens")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(Map.of("customerId", TestContext.customerId), MediaType.APPLICATION_JSON))) {

            if (resp.getStatus() != 201) {
                throw new RuntimeException("Token creation failed: HTTP " + resp.getStatus());
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> body = resp.readEntity(Map.class);
            Object t = body.get("token");
            if (t != null) {
                createdToken = t.toString();
            }
        } finally {
            client.close();
        }

        assertNotNull("created token must not be null", createdToken);
    }

    @Then("the request is accepted")
    public void theRequestIsAccepted() {
        assertNotNull(requestId);
    }

    @Then("a request id is returned")
    public void aRequestIdIsReturned() {
        assertNotNull(requestId);
    }

    @Then("a token is returned")
    public void a_token_is_returned() {
        assertNotNull(createdToken);
        assertFalse(createdToken.isBlank());
    }

    @After
    public void cleanup() {
        if (TestContext.customerId != null) {
            customerClient.unregister(TestContext.customerId);
        }
        customerClient.close();

        TestContext.customerId = null;
        TestContext.requestId = null;
    }
}