// PaymentService.java
package dk.dtu.pay.payment.domain.service;

import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import dk.dtu.pay.merchant.domain.model.Merchant;
import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import dk.dtu.pay.payment.application.port.out.TokenClientPort;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.model.PaymentRequest;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PaymentService {

    private final CustomerRepositoryPort customers;
    private final MerchantRepositoryPort merchants;
    private final PaymentRepositoryPort payments;
    private final TokenClientPort tokens;
    private final BankService bank;

    public PaymentService(CustomerRepositoryPort customers,
                          MerchantRepositoryPort merchants,
                          PaymentRepositoryPort payments,
                          TokenClientPort tokens,
                          BankService bank) {
        this.customers = customers;
        this.merchants = merchants;
        this.payments = payments;
        this.tokens = tokens;
        this.bank = bank;
    }

    public Payment pay(PaymentRequest request)
            throws UnknownCustomerException, UnknownMerchantException, BankFailureException {

        final String customerId;
        try {
            customerId = tokens.consumeToken(request.token);
        } catch (TokenClientPort.InvalidTokenException e) {
            throw new UnknownCustomerException(e.getMessage());
        }

        Customer c = customers.get(customerId);
        Merchant m = merchants.get(request.merchantId);

        if (c == null) throw new UnknownCustomerException("customer with id \"" + customerId + "\" is unknown");
        if (m == null) throw new UnknownMerchantException("merchant with id \"" + request.merchantId + "\" is unknown");

        try {
            bank.transferMoneyFromTo(
                    c.getBankAccountId(),
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

    public List<Payment> getPayments() {
        return payments.all();
    }

    public static class UnknownCustomerException extends Exception { public UnknownCustomerException(String msg) { super(msg); } }
    public static class UnknownMerchantException extends Exception { public UnknownMerchantException(String msg) { super(msg); } }
    public static class BankFailureException extends Exception { public BankFailureException(String msg) { super(msg); } }
}
