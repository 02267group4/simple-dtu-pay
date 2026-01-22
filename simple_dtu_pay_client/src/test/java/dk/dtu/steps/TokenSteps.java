// java
package dk.dtu.steps;

import dk.dtu.service.CustomerClient;
import io.cucumber.java.After;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TokenSteps {

    private static final String GATEWAY_BASE = "http://localhost:8080";
    // Poll the customer service request endpoint (RequestStore lives in customer service)
    private static final String TOKEN_SERVICE_BASE = "http://localhost:8081";

    private final CustomerClient customerClient = new CustomerClient();
    private final Client client = ClientBuilder.newClient();

    // state tracked between When/Then steps
    private String lastRequestId;
    private boolean lastRequestRejected;
    private List<String> lastTokenList;

    @When("the customer requests {int} tokens")
    public void theCustomerRequestsTokens(int count) {
        lastRequestRejected = false;
        lastTokenList = null;
        String reqId = customerClient.requestTokens(TestContext.customerId, count);
        lastRequestId = reqId;
        TestContext.requestId = reqId;
    }

    @When("the customer requests {int} token")
    public void the_customer_requests_token(int count) {
        theCustomerRequestsTokens(count);
    }

    @When("the customer requests their token list")
    public void theCustomerRequestsTheirTokenList() {
        lastRequestRejected = false;

        String reqId = customerClient.requestTokenList(TestContext.customerId);
        lastRequestId = reqId;

        Map<String, Object> result = pollRequestResult(reqId);
        Object tokens = result.get("tokens");

        assertNotNull("Expected tokens list, got null", tokens);
        assertTrue("Expected tokens to be a List", tokens instanceof List);

        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) tokens;
        lastTokenList = list;
    }



    private Map<String, Object> pollRequestResult(String requestId) {
        long deadline = System.currentTimeMillis() + 20000; // wait up to 20s
        while (System.currentTimeMillis() < deadline) {
            try (Response r = client.target(TOKEN_SERVICE_BASE)
                    .path("requests")
                    .path(requestId)
                    .request()
                    .get()) {

                int status = r.getStatus();
                if (status == 202) {
                    // still processing
                } else if (status == 200) {
                    // completed: body is { success, token, error } where tokens are either an empty or populated list
                    @SuppressWarnings("unchecked")
                    Map<String, Object> body = r.readEntity(Map.class);
                    return body;
                } else {
                    // unexpected status -> treat as completed with a marker
                    return Map.of("tokens", null);
                }
            } catch (Exception e) {
                // swallow and retry until deadline
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        fail("Timed out waiting for async request result for requestId=" + requestId);
        return Map.of(); // unreachable
    }

    @Then("the token request is rejected")
    public void the_token_request_is_rejected() {
        Map<String, Object> result = pollRequestResult(lastRequestId);

        Object success = result.get("success");
        assertNotNull("Expected success field", success);
        assertEquals("Expected request to be rejected", false, success);

        lastRequestRejected = true;
    }


    @Then("the token request is accepted")
    public void theRequestIsAccepted() {
        Map<String, Object> result = pollRequestResult(lastRequestId);

        Object success = result.get("success");
        assertNotNull("Expected success field", success);
        assertEquals("Expected request to be accepted", true, success);

        Object tokens = result.get("tokens");
        assertNotNull("Expected tokens list", tokens);
        assertTrue("Expected tokens to be a List", tokens instanceof List);

        @SuppressWarnings("unchecked")
        List<String> list = (List<String>) tokens;
        lastTokenList = list;
        lastRequestRejected = false;
    }


    @Then("{int} tokens are returned")
    public void tokens_are_returned(int expectedCount) {
        assertNotNull("No tokens available", lastTokenList);
        assertEquals(expectedCount, lastTokenList.size());
    }

    @Then("list of {int} tokens are returned")
    public void list_of_tokens_are_returned(int expectedCount) {
        tokens_are_returned(expectedCount);
    }

    @After
    public void cleanup() {
        if (TestContext.customerId != null) {
            customerClient.unregister(TestContext.customerId);
        }
        customerClient.close();

        TestContext.customerId = null;
        TestContext.requestId = null;
        lastRequestId = null;
        lastRequestRejected = false;
        lastTokenList = null;
    }
}
