package dk.dtu.steps;

import dk.dtu.model.Payment;
import dk.dtu.service.SimpleDTUPay;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
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
    }
}
