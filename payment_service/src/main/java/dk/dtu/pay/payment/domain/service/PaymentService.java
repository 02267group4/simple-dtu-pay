package dk.dtu.pay.payment.domain.service;

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

    private final PaymentRepositoryPort payments;
    private final RabbitMQPaymentRequestedPublisher publisher;
    private final BankService bank;

    public PaymentService(PaymentRepositoryPort payments,
                          RabbitMQPaymentRequestedPublisher publisher,
                          BankService bank) {
        this.payments = payments;
        this.publisher = publisher;
        this.bank = bank;
    }

    public Payment pay(PaymentRequest request) {
        // In a microservices architecture, we no longer check the merchant repository directly.
        // Validation would typically happen via an asynchronous message or a shared cache.

        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.amount = request.amount;
        payment.merchantId = request.merchantId;
        payment.merchantBankAccountId = request.merchantBankAccountId;
        payment.token = request.token;
        payment.status = Payment.Status.PENDING;

        payments.add(payment);

        publisher.publishPaymentRequested(payment.id, request.token);

        return payment;
    }

    public List<Payment> getPayments() {
        return payments.all();
    }
    
    public List<Payment> getPaymentsForMerchant(String merchantId) {
        return payments.findByMerchant(merchantId);
    }


    /**
     * Domain-level handling of a validated token: perform bank transfer and update payment.
     */
    public void completePaymentForValidatedToken(String paymentId, String customerId, String customerBankAccountId) {
        Payment p = payments.get(paymentId);
        if (p == null) return;

        p.customerId = customerId;

        // Use the bank account IDs for the transfer
        String merchantAccount = p.merchantBankAccountId;
        String customerAccount = customerBankAccountId;

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