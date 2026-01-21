package dk.dtu.pay.payment.application.port.out;

import dk.dtu.pay.payment.domain.model.Payment;

import java.util.List;

public interface PaymentRepositoryPort {
    void add(Payment p);
    List<Payment> all();

    Payment get(String id);
    void update(Payment p);
    
    List<Payment> findByMerchant(String merchantId);
}
