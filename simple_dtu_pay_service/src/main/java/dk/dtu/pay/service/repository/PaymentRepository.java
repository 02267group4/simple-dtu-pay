package dk.dtu.pay.service.repository;

import dk.dtu.pay.service.model.Payment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PaymentRepository {
    private final List<Payment> payments = new CopyOnWriteArrayList<>();

    public void add(Payment p) {
        payments.add(p);
    }

    public List<Payment> all() {
        return List.copyOf(payments);
    }
}
