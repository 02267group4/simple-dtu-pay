package dk.dtu.steps;

import dk.dtu.model.Payment;
import dk.dtu.service.SimpleDTUPay;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CustomerReportSteps {

    private final SimpleDTUPay dtuPay = new SimpleDTUPay();

    private List<Payment> report;

    @When("the customer requests their report")
    public void customerRequestsReport() {
        report = dtuPay.getCustomerReport(TestContext.customerId);
    }

    @Then("the customer report contains the payment of {int} kr")
    public void verifyReportContainsPayment(int amount) {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        boolean found = report.stream()
                .anyMatch(p -> p.amount == amount);

        assertTrue("Customer report should contain a payment of " + amount, found);
    }

    @Then("the customer report contains the merchant")
    public void verifyReportContainsMerchant() {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        boolean found = report.stream()
                .anyMatch(p -> p.merchantId != null && p.merchantId.equals(TestContext.merchantId));

        assertTrue("Customer report should contain the merchant", found);
    }

    @Then("the customer report contains the token used")
    public void verifyReportContainsToken() {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        boolean found = report.stream()
                .anyMatch(p -> p.token != null && !p.token.isEmpty());

        assertTrue("Customer report should contain payments with tokens", found);
    }

    @Then("the customer report is empty")
    public void verifyReportIsEmpty() {
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be empty", report.isEmpty());
    }

    @Then("the customer report contains {int} payments")
    public void verifyReportContainsPaymentCount(int expectedCount) {
        assertNotNull("Report should not be null", report);
        assertEquals("Customer report should contain " + expectedCount + " payments",
                expectedCount, report.size());
    }

    @Then("the customer report does not contain a payment of {int} kr")
    public void verifyReportDoesNotContainPayment(int amount) {
        assertNotNull("Report should not be null", report);

        boolean found = report.stream()
                .anyMatch(p -> p.amount == amount);

        assertFalse("Customer report should NOT contain a payment of " + amount, found);
    }

    @Then("the customer report contains payments to {int} different merchants")
    public void verifyReportContainsDifferentMerchants(int expectedMerchantCount) {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        Set<String> uniqueMerchants = report.stream()
                .map(p -> p.merchantId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        assertEquals("Customer report should contain payments to " + expectedMerchantCount + " different merchants",
                expectedMerchantCount, uniqueMerchants.size());
    }
}
