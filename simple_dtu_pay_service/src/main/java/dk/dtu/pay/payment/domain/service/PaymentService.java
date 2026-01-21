package dk.dtu.pay.payment.domain.service;

import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import dk.dtu.pay.payment.adapter.out.messaging.RabbitMQPaymentRequestedPublisher;
import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.model.PaymentRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PaymentService {

    private final CustomerRepositoryPort customers;
    private final MerchantRepositoryPort merchants;
    private final PaymentRepositoryPort payments;
    private final RabbitMQPaymentRequestedPublisher publisher;

    public PaymentService(CustomerRepositoryPort customers,
                          MerchantRepositoryPort merchants,
                          PaymentRepositoryPort payments,
                          RabbitMQPaymentRequestedPublisher publisher) {
        this.customers = customers;
        this.merchants = merchants;
        this.payments = payments;
        this.publisher = publisher;
    }

    public Payment pay(PaymentRequest request)
            throws UnknownMerchantException {

        // merchant can still be validated synchronously
        if (merchants.get(request.merchantId) == null) {
            throw new UnknownMerchantException(
                    "merchant with id \"" + request.merchantId + "\" is unknown"
            );
        }

        // create payment FIRST
        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.amount = request.amount;
        payment.merchantId = request.merchantId;
        payment.status = Payment.Status.PENDING; // add if you don't have it

        payments.add(payment);

        // async step: ask token service to validate
        publisher.publishPaymentRequested(payment.id, request.token);

        // return immediately
        return payment;
    }

    public List<Payment> getPayments() {
        return payments.all();
    }
    
    // 🔹 NEW: merchant-specific reporting
    public List<Payment> getPaymentsForMerchant(String merchantId)
            throws UnknownMerchantException {

        // Optional safety: check merchant exists
        if (merchants.get(merchantId) == null) {
            throw new UnknownMerchantException(
                    "merchant with id \"" + merchantId + "\" is unknown"
            );
        }

        // Delegate to the payment repository
        return payments.findByMerchant(merchantId);
    }


    public static class UnknownMerchantException extends Exception {
        public UnknownMerchantException(String msg) { super(msg); }
    }
}
