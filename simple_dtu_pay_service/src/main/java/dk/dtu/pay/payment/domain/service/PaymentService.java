package dk.dtu.pay.payment.domain.service;

import dk.dtu.pay.payment.application.port.out.*;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * @author Nick
 */
@ApplicationScoped
public class PaymentService {

    @Inject
    PaymentRepositoryPort paymentRepo;
    @Inject
    CustomerRepositoryPort customerRepo;
    @Inject
    MerchantRepositoryPort merchantRepo;

    // MASTER CLASS: Use your ports, not AppContext!
    @Inject
    BankPort bankPort;
    @Inject
    TokenPort tokenPort;

    public Payment pay(String token, String merchantId, int amount, String description) throws Exception {
        // 1. Use the TokenPort to validate
        String customerId = tokenPort.validateAndConsumeToken(token);

        // 2. Load Domain Entities from their respective ports
        var customer = customerRepo.get(customerId);
        var merchant = merchantRepo.get(merchantId);

        if (customer == null || merchant == null)
            throw new Exception("Participant not found");

        // 3. Use the BankPort to transfer
        bankPort.transfer(
                customer.getBankAccountId(),
                merchant.bankAccountId,
                BigDecimal.valueOf(amount),
                description);

        // 4. Record Payment
        Payment p = new Payment(UUID.randomUUID().toString(), customerId, merchantId, amount, description);
        paymentRepo.add(p);
        return p;
    }
}