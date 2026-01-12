package dk.dtu.steps;

import dk.dtu.model.Customer;
import dk.dtu.model.Merchant;
import dk.dtu.service.SimpleDTUPay;
import dtu.ws.fastmoney.*; // Generated Bank Code

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class PaymentSteps {

    // === CHANGE THIS KEY ===
    private static final String BANK_API_KEY = System.getenv("BANK_API_KEY");
    // =======================

    private final SimpleDTUPay dtuPay = new SimpleDTUPay();
    private final BankService bank = new BankService_Service().getBankServicePort();

    private String customerId; // DTU Pay ID
    private String merchantId; // DTU Pay ID

    // We need to store Bank Account IDs to clean them up later
    private String customerBankId;
    private String merchantBankId;
    private final List<String> accountsToRetire = new ArrayList<>();

    private boolean paymentSuccess;

    @Given("a customer with name {string}, CPR {string}, and balance {int}")
    public void createCustomer(String name, String cpr, int balance) throws Exception {
        // 1. Create User object for Bank
        User user = new User();
        user.setFirstName(name);
        user.setLastName("Test");
        user.setCprNumber(cpr);

        // 2. Create Account at Bank
        // Note: We might need to handle "Account already exists" in a real scenario
        // but for now we assume fresh CPRs or cleanup works.
        try {
            customerBankId = bank.createAccountWithBalance(BANK_API_KEY, user, BigDecimal.valueOf(balance));
            accountsToRetire.add(customerBankId);
        } catch (BankServiceException_Exception e) {
            // If account exists, try to reuse it (optional logic) or fail
            throw e;
        }

        // 3. Register with DTU Pay
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

    @When("the merchant initiates a payment for {int} kr by the customer")
    public void makePayment(int amount) {
        try {
            paymentSuccess = dtuPay.pay(amount, customerId, merchantId);
        } catch (Exception e) {
            paymentSuccess = false;
        }
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

    /***
    @Then("this should fail")
    public void thisShouldFail() {
        fail("Forced failure");
    }
    ***/

    @After
    public void cleanup() {
        // 1. Cleanup DTU Pay
        if (customerId != null)
            dtuPay.unregisterCustomer(customerId);
        if (merchantId != null)
            dtuPay.unregisterMerchant(merchantId);

        // 2. Cleanup Bank (Crucial!)
        for (String accId : accountsToRetire) {
            try {
                bank.retireAccount(BANK_API_KEY, accId);
            } catch (Exception e) {
                System.err.println("Failed to retire bank account: " + accId);
            }
        }
    }
}