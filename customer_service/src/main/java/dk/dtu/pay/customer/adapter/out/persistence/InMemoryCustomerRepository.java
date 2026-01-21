package dk.dtu.pay.customer.adapter.out.persistence;

import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryCustomerRepository implements CustomerRepositoryPort {

    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    @Override
    public Optional<Customer> findByCustomerId(String id) {
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public void save(Customer customer) {
        customers.put(customer.getId(), customer);
    }

    @Override
    public Customer get(String id) {
        return customers.get(id);
    }

    @Override
    public void remove(String id) {
        customers.remove(id);
    }
}