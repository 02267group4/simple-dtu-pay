package dk.dtu.pay.service.repository;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import dk.dtu.pay.service.domain.model.Payment;

public class PaymentRepository {
    private final List<Payment> payments = new CopyOnWriteArrayList<>();

    public void add(Payment p) {
        payments.add(p);
    }

    public List<Payment> all() {
        return List.copyOf(payments);
    }

 // Get all payments for a specific merchant
    public List<Payment> findByMerchant(String merchantId) {
        return payments.stream()
                .filter(p -> p.merchantId.equals(merchantId))
                .collect(Collectors.toList());
    
    }
}