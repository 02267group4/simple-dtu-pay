package dk.dtu.steps;

import dk.dtu.model.Payment;
import dk.dtu.service.SimpleDTUPay;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.List;
import static org.junit.Assert.*;

public class ManagerReportSteps {

    // We reuse the SimpleDTUPay instance from PaymentSteps (Cucumber shares state
    // via dependency injection or static fields,
    // but here we just create a new client since it talks to the same running
    // server)
    private final SimpleDTUPay dtuPay = new SimpleDTUPay();

    private List<Payment> report;

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
}