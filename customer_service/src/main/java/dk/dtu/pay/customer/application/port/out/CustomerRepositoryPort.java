package dk.dtu.pay.customer.application.port.out;

import dk.dtu.pay.customer.domain.model.Customer;
import java.util.Optional;

public interface CustomerRepositoryPort {

    Optional<Customer> findByCustomerId(String customerId);

    void save(Customer customer);

    Customer get(String id);

    void remove(String id);
}
