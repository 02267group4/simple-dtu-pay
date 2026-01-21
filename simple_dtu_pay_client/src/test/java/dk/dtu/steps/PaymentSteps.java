// java
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

    private static final String BANK_API_KEY = System.getenv("BANK_API_KEY");

    private final SimpleDTUPay dtuPay = new SimpleDTUPay();
    private final BankService bank = new BankService_Service().getBankServicePort();

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
        TestContext.customerId = dtuPay.register(c);
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
        TestContext.merchantId = dtuPay.register(m);
    }

    @Given("the customer has a valid token")
    public void customerHasValidToken() {
        customerToken = dtuPay.requestToken(TestContext.customerId);

        assertNotNull("Token must not be null", customerToken);
        assertFalse("Token must not be empty", customerToken.isBlank());
    }

    @When("the merchant initiates a payment for {int} kr by the customer using the token")
    public void makePayment(int amount) {
        paymentId = dtuPay.payAsync(customerToken, TestContext.merchantId, amount, paymentDescription);
        assertNotNull("paymentId must not be null", paymentId);

        paymentSuccess = dtuPay.waitForPaymentCompleted(paymentId, 5000);
    }

    @When("the merchant initiates a payment for {int} kr by the customer without a token")
    public void makePaymentWithoutToken(int amount) {
        try {
            paymentId = dtuPay.payAsync(null, TestContext.merchantId, amount, paymentDescription);
            paymentSuccess = dtuPay.waitForPaymentCompleted(paymentId, 3000);
        } catch (Exception e) {
            paymentSuccess = false;
        }
    }

    @Then("the payment is successful")
    public void verifySuccess() {
        assertTrue("Payment should be successful", paymentSuccess);
    }

    @Then("the payment is rejected")
    public void verifyRejected() {
        assertFalse("Payment should be rejected", paymentSuccess);
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

    @After
    public void cleanup() {
        if (TestContext.customerId != null) dtuPay.unregisterCustomer(TestContext.customerId);
        if (TestContext.merchantId != null) dtuPay.unregisterMerchant(TestContext.merchantId);

        for (String accId : accountsToRetire) {
            try {
                bank.retireAccount(BANK_API_KEY, accId);
            } catch (Exception e) {
                System.err.println("Failed to retire bank account: " + accId);
            }
        }

        TestContext.customerId = null;
        TestContext.merchantId = null;
        TestContext.requestId = null;
    }
}
