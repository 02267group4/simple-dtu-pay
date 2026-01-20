package dk.dtu.pay.service.domain.service;

import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import dk.dtu.pay.merchant.domain.model.Merchant;
import dk.dtu.pay.service.domain.model.Payment;
import dk.dtu.pay.service.domain.model.PaymentRequest;
import dk.dtu.pay.service.repository.PaymentRepository;
import dk.dtu.pay.service.repository.TokenRepository;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PaymentService {

    private final CustomerRepositoryPort customers;
    private final MerchantRepositoryPort merchants;
    private final PaymentRepository payments;
    private final TokenRepository tokens;
    private final BankService bank;

    public PaymentService(CustomerRepositoryPort customers,
                          MerchantRepositoryPort merchants,
                          PaymentRepository payments,
                          TokenRepository tokens,
                          BankService bank) {
        this.customers = customers;
        this.merchants = merchants;
        this.payments = payments;
        this.tokens = tokens;
        this.bank = bank;
    }

    
    // Process a payment
    
    public Payment pay(PaymentRequest request)
            throws UnknownCustomerException, UnknownMerchantException, BankFailureException {

        // Validate token
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

        // Call FastMoney bank system
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

        // Create payment object
        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.amount = request.amount;
        payment.customerId = customerId;
        payment.merchantId = request.merchantId;
        payment.token = request.token;

        payments.add(payment);
        return payment;
    }

     
    // Get all payments
     
    public List<Payment> getPayments() {
        return payments.all();
    }
 
    // Get  payments for one merchant
     
    public List<Payment> getPaymentsForMerchant(String merchantId)
            throws UnknownMerchantException {

        Merchant m = merchants.get(merchantId);
        if (m == null) {
            throw new UnknownMerchantException(
                    "merchant with id \"" + merchantId + "\" is unknown"
            );
        }

        return payments.findByMerchant(merchantId);
    }

    
    // Custom exceptions
    
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