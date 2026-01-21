package dk.dtu.steps;

import dk.dtu.model.Customer;
import dk.dtu.model.Merchant;
import dk.dtu.service.SimpleDTUPay;
import dtu.ws.fastmoney.*;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PaymentSteps {
    // Update these URIs to match your new service ports
    private String customerServiceUri = "http://localhost:8081";
    private String merchantServiceUri = "http://localhost:8082";
    private String paymentServiceUri = "http://localhost:8083";
    private String tokenServiceUri = "http://localhost:8084";

    // Ensure the rest of the code uses these specific URIs for their respective calls

    private static final String BANK_API_KEY = System.getenv("BANK_API_KEY");

    private final SimpleDTUPay dtuPay = new SimpleDTUPay();
    private final BankService bank = new BankService_Service().getBankServicePort();

    private String customerId;
    private String merchantId;

    private String customerBankId;
    private String merchantBankId;
    private final List<String> accountsToRetire = new ArrayList<>();

    private String customerToken;
    private String paymentDescription = "cucumber-test-payment";

    private String paymentId;
    private boolean paymentSuccess;

    @Given("a customer with name {string}, CPR {string}, and balance {int}")
    public void createCustomer(String name, String cpr, int balance) throws Exception {
        User user = new User();
        user.setFirstName(name);
        user.setLastName("Test");
        user.setCprNumber(cpr);

        customerBankId = bank.createAccountWithBalance(BANK_API_KEY, user, BigDecimal.valueOf(balance));
        accountsToRetire.add(customerBankId);

        Customer c = new Customer(name, cpr, customerBankId);
        customerId = dtuPay.register(c);
    }

    @Given("a merchant with name {string}, CPR {string}, and balance {int}")
    public void createMerchant(String name, String cpr, int balance) throws Exception {
        User user = new User();
        user.setFirstName(name);
        user.setLastName("Test");
        user.setCprNumber(cpr);

        merchantBankId = bank.createAccountWithBalance(BANK_API_KEY, user, BigDecimal.valueOf(balance));
        accountsToRetire.add(merchantBankId);

        Merchant m = new Merchant(name, cpr, merchantBankId);
        merchantId = dtuPay.register(m);
    }

    @Given("the customer has a valid token")
    public void customerHasValidToken() {
        customerToken = dtuPay.requestToken(customerId);

        assertNotNull("Token must not be null", customerToken);
        assertFalse("Token must not be empty", customerToken.isBlank());
    }

    @When("the merchant initiates a payment for {int} kr by the customer using the token")
    public void makePayment(int amount) {
        paymentId = dtuPay.payAsync(customerToken, merchantId, amount, paymentDescription);
        assertNotNull("paymentId must not be null", paymentId);

        // async: wait for completion
        paymentSuccess = dtuPay.waitForPaymentCompleted(paymentId, 5000);
    }

    @Then("the payment is successful")
    public void verifySuccess() {
        assertTrue("Payment should be successful", paymentSuccess);
    }

    @Then("the balance of the customer at the bank is {int} kr")
    public void verifyCustomerBalance(int expectedBalance) throws Exception {
        Account account = bank.getAccount(customerBankId);
        assertEquals(BigDecimal.valueOf(expectedBalance), account.getBalance());
    }

    @Then("the balance of the merchant at the bank is {int} kr")
    public void verifyMerchantBalance(int expectedBalance) throws Exception {
        Account account = bank.getAccount(merchantBankId);
        assertEquals(BigDecimal.valueOf(expectedBalance), account.getBalance());
    }

    @When("the merchant initiates a payment for {int} kr by the customer without a token")
    public void makePaymentWithoutToken(int amount) {
        try {
            // token missing -> should end up FAILED
            paymentId = dtuPay.payAsync(null, merchantId, amount, paymentDescription);
            paymentSuccess = dtuPay.waitForPaymentCompleted(paymentId, 3000);
        } catch (Exception e) {
            paymentSuccess = false;
        }
    }

    @Then("the payment is rejected")
    public void verifyRejected() {
        assertFalse("Payment should be rejected", paymentSuccess);
    }

    @After
    public void cleanup() {
        if (customerId != null) dtuPay.unregisterCustomer(customerId);
        if (merchantId != null) dtuPay.unregisterMerchant(merchantId);

        for (String accId : accountsToRetire) {
            try {
                bank.retireAccount(BANK_API_KEY, accId);
            } catch (Exception e) {
                System.err.println("Failed to retire bank account: " + accId);
            }
        }
    }
}
