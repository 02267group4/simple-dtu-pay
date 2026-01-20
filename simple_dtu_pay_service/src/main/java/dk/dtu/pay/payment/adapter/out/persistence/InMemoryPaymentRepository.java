package dk.dtu.pay.payment.adapter.out.persistence;

import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import dk.dtu.pay.payment.domain.model.Payment;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class InMemoryPaymentRepository implements PaymentRepositoryPort {

    private final ConcurrentMap<String, Payment> byId = new ConcurrentHashMap<>();

    @Override
    public void add(Payment p) {
        byId.put(p.id, p);
    }

    @Override
    public List<Payment> all() {
        return List.copyOf(byId.values());
    }

    @Override
    public Payment get(String id) {
        return byId.get(id);
    }

    @Override
    public void update(Payment p) {
        byId.put(p.id, p);
    }
}
