package dk.dtu.pay.service.service;

import dk.dtu.pay.service.model.*;
import dk.dtu.pay.service.repository.*;
import dtu.ws.fastmoney.*;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentService {

    private final CustomerRepository customers;
    private final MerchantRepository merchants;
    private final PaymentRepository payments;
    private final TokenRepository tokens;
    private final BankService bank;

    public PaymentService(CustomerRepository customers,
                          MerchantRepository merchants,
                          PaymentRepository payments,
                          TokenRepository tokens,
                          BankService bank) {
        this.customers = customers;
        this.merchants = merchants;
        this.payments = payments;
        this.tokens = tokens;
        this.bank = bank;
    }

    public Payment pay(PaymentRequest request)
            throws UnknownCustomerException, UnknownMerchantException, BankFailureException {

        String customerId = tokens.consume(request.token);
        if (customerId == null) {
            throw new UnknownCustomerException("Invalid or already used token");
        }

        Customer c = customers.get(customerId);
        Merchant m = merchants.get(request.merchantId);

        if (c == null) {
            throw new UnknownCustomerException("customer with id \"" + customerId + "\" is unknown");
        }
        if (m == null) {
            throw new UnknownMerchantException("merchant with id \"" + request.merchantId + "\" is unknown");
        }

        try {
            bank.transferMoneyFromTo(
                    c.bankAccountId,
                    m.bankAccountId,
                    BigDecimal.valueOf(request.amount),
                    "DTU Pay: " + customerId + " -> " + request.merchantId
            );
        } catch (BankServiceException_Exception e) {
            throw new BankFailureException("Bank failed: " + e.getMessage());
        }

        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.amount = request.amount;
        payment.customerId = customerId;
        payment.merchantId = request.merchantId;

        payments.add(payment);
        return payment;
    }

    public java.util.List<Payment> getPayments() {
        return payments.all();
    }

    // simple typed exceptions so resources can map to status codes cleanly
    public static class UnknownCustomerException extends Exception {
        public UnknownCustomerException(String msg) { super(msg); }
    }
    public static class UnknownMerchantException extends Exception {
        public UnknownMerchantException(String msg) { super(msg); }
    }
    public static class BankFailureException extends Exception {
        public BankFailureException(String msg) { super(msg); }
    }
}
