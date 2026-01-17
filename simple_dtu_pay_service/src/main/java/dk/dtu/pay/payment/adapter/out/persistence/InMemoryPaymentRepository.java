package dk.dtu.pay.payment.adapter.out.persistence;

import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Nick
 */
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