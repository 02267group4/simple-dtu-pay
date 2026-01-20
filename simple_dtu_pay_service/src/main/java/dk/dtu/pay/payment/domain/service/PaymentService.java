// java
package dk.dtu.pay.payment.domain.service;

import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import dk.dtu.pay.merchant.domain.model.Merchant;
import dk.dtu.pay.payment.adapter.out.messaging.RabbitMQPaymentRequestedPublisher;
import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
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
    private final RabbitMQPaymentRequestedPublisher publisher;
    private final BankService bank;

    public PaymentService(CustomerRepositoryPort customers,
                          MerchantRepositoryPort merchants,
                          PaymentRepositoryPort payments,
                          RabbitMQPaymentRequestedPublisher publisher,
                          BankService bank) {
        this.customers = customers;
        this.merchants = merchants;
        this.payments = payments;
        this.publisher = publisher;
        this.bank = bank;
    }

    public Payment pay(PaymentRequest request)
            throws UnknownMerchantException {

        if (merchants.get(request.merchantId) == null) {
            throw new UnknownMerchantException(
                    "merchant with id \"" + request.merchantId + "\" is unknown"
            );
        }

        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.amount = request.amount;
        payment.merchantId = request.merchantId;
        payment.status = Payment.Status.PENDING;

        payments.add(payment);

        publisher.publishPaymentRequested(payment.id, request.token);

        return payment;
    }

    public List<Payment> getPayments() {
        return payments.all();
    }

    /**
     * Domain-level handling of a validated token: perform bank transfer and update payment.
     * Ensures failures from the bank are recorded (FAILED) so tests cannot pass spuriously.
     */
    public void completePaymentForValidatedToken(String paymentId, String customerId) {
        Payment p = payments.get(paymentId);
        if (p == null) return;

        p.customerId = customerId;

        Merchant merchant = merchants.get(p.merchantId);
        Customer customer = customers.get(customerId);

        if (merchant == null || customer == null) {
            p.status = Payment.Status.FAILED;
            p.failureReason = "Unknown merchant or customer";
            payments.update(p);
            return;
        }

        // Merchant uses public fields; customer uses getter
        String merchantAccount = merchant.bankAccountId;
        String customerAccount = customer.getBankAccountId();

        if (merchantAccount == null || customerAccount == null || merchantAccount.isBlank() || customerAccount.isBlank()) {
            p.status = Payment.Status.FAILED;
            p.failureReason = "Missing bank account for merchant or customer";
            payments.update(p);
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(p.amount);
        String description = "Transfer for payment " + p.id;

        try {
            bank.transferMoneyFromTo(customerAccount, merchantAccount, amount, description);
            p.status = Payment.Status.COMPLETED;
            p.failureReason = null;
        } catch (BankServiceException_Exception e) {
            p.status = Payment.Status.FAILED;
            p.failureReason = e.getMessage();
        }

        payments.update(p);
    }

    /**
     * Mark payment failed when token was rejected or other errors occur.
     */
    public void failPayment(String paymentId, String reason) {
        Payment p = payments.get(paymentId);
        if (p == null) return;
        p.status = Payment.Status.FAILED;
        p.failureReason = reason;
        payments.update(p);
    }

    public static class UnknownMerchantException extends Exception {
        public UnknownMerchantException(String msg) { super(msg); }
    }
}
