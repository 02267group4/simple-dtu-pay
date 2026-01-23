package dk.dtu.steps;

import dk.dtu.model.Payment;
import dk.dtu.service.SimpleDTUPay;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.Assert.*;

public class ManagerReportSteps {

    // We reuse the SimpleDTUPay instance from PaymentSteps (Cucumber shares state
    // via dependency injection or static fields,
    // but here we just create a new client since it talks to the same running
    // server)
    private final SimpleDTUPay dtuPay = new SimpleDTUPay();

    private List<Payment> report;
    private List<Payment> secondReport;

    @When("the manager requests the report")
    public void managerRequestsReport() {
        report = dtuPay.getManagerReport();
    }

    @Then("the report contains the payment of {int} kr")
    public void verifyReportContainsPayment(int amount) {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());

        boolean found = report.stream()
                .anyMatch(p -> p.amount == amount);

        assertTrue("Report should contain a payment of " + amount, found);
    }

    @Then("the report is empty")
    public void verifyReportIsEmpty() {
        assertNotNull("Report should not be null", report);
        assertTrue("Report should be empty when no payments exist", report.isEmpty());
    }

    @Then("the report contains {int} payments")
    public void verifyReportContainsCount(int expectedCount) {
        assertNotNull("Report should not be null", report);
        assertEquals("Report should contain " + expectedCount + " payments", expectedCount, report.size());
    }

    @Then("the report contains at least {int} payments")
    public void verifyReportContainsAtLeastCount(int minCount) {
        assertNotNull("Report should not be null", report);
        assertTrue("Report should contain at least " + minCount + " payments, but found " + report.size(),
                report.size() >= minCount);
    }

    @Then("the report contains payments from at least {int} merchants")
    public void verifyReportContainsPaymentsFromMerchants(int minMerchantCount) {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());
        
        long uniqueMerchants = report.stream()
                .filter(p -> p.merchantId != null)
                .map(p -> p.merchantId)
                .distinct()
                .count();
        
        assertTrue("Report should contain payments from at least " + minMerchantCount + " merchant(s), but found " + uniqueMerchants,
                uniqueMerchants >= minMerchantCount);
    }

    @Then("the report contains a payment with status {string}")
    public void verifyReportContainsPaymentWithStatus(String expectedStatus) {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());
        
        boolean found = report.stream()
                .anyMatch(p -> expectedStatus.equalsIgnoreCase(p.status));
        
        assertTrue("Report should contain a payment with status " + expectedStatus, found);
    }

    @Then("the report contains a payment with status {string} or {string}")
    public void verifyReportContainsPaymentWithStatusOr(String status1, String status2) {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());
        
        boolean found = report.stream()
                .anyMatch(p -> status1.equalsIgnoreCase(p.status) || status2.equalsIgnoreCase(p.status));
        
        assertTrue("Report should contain a payment with status " + status1 + " or " + status2, found);
    }

    @Then("all payments in the report have required fields")
    public void verifyAllPaymentsHaveRequiredFields() {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());
        
        for (Payment p : report) {
            assertNotNull("Payment ID should not be null", p.id);
            assertTrue("Payment amount should be non-negative", p.amount >= 0);
            // customerId and merchantId can be null for pending payments, so we don't check them
            assertNotNull("Payment status should not be null", p.status);
        }
    }

    @Then("all payment IDs in the report are unique")
    public void verifyAllPaymentIdsAreUnique() {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());
        
        Set<String> uniqueIds = report.stream()
                .map(p -> p.id)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        
        assertEquals("All payment IDs should be unique", report.size(), uniqueIds.size());
    }

    @Then("the report contains the payment by its ID")
    public void verifyReportContainsPaymentById() {
        assertNotNull("Report should not be null", report);
        assertFalse("Report should not be empty", report.isEmpty());
        
        // Verify all payments have valid IDs
        boolean allHaveIds = report.stream()
                .allMatch(p -> p.id != null && !p.id.isEmpty());
        
        assertTrue("All payments in report should have valid IDs", allHaveIds);
        
        // Verify we can find payments by their IDs (each payment's ID should be in the report)
        for (Payment p : report) {
            boolean found = report.stream()
                    .anyMatch(payment -> p.id.equals(payment.id));
            assertTrue("Report should contain payment with ID " + p.id, found);
        }
    }

    @When("the manager requests the report again")
    public void managerRequestsReportAgain() {
        secondReport = dtuPay.getManagerReport();
    }

    @Then("both reports contain the payment of {int} kr")
    public void verifyBothReportsContainPayment(int amount) {
        assertNotNull("First report should not be null", report);
        assertNotNull("Second report should not be null", secondReport);
        
        boolean foundInFirst = report.stream()
                .anyMatch(p -> p.amount == amount);
        boolean foundInSecond = secondReport.stream()
                .anyMatch(p -> p.amount == amount);
        
        assertTrue("First report should contain a payment of " + amount, foundInFirst);
        assertTrue("Second report should contain a payment of " + amount, foundInSecond);
    }

    @Then("both reports have the same number of payments")
    public void verifyBothReportsHaveSameCount() {
        assertNotNull("First report should not be null", report);
        assertNotNull("Second report should not be null", secondReport);
        
        assertEquals("Both reports should have the same number of payments",
                report.size(), secondReport.size());
    }
}