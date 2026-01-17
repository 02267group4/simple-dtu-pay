// InMemoryPaymentRepository.java
package dk.dtu.pay.payment.adapter.out.persistence;

import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import dk.dtu.pay.payment.domain.model.Payment;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class InMemoryPaymentRepository implements PaymentRepositoryPort {

    private final List<Payment> payments = new CopyOnWriteArrayList<>();

    @Override
    public void add(Payment p) {
        payments.add(p);
    }

    @Override
    public List<Payment> all() {
        return List.copyOf(payments);
    }
}
