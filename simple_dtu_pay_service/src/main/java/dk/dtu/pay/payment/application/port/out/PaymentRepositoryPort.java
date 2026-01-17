package dk.dtu.pay.payment.application.port.out;

import dk.dtu.pay.payment.domain.model.Payment;
import java.util.List;

/**
 * @author Nick
 */
public interface PaymentRepositoryPort {
    void add(Payment p);

    List<Payment> all();
}