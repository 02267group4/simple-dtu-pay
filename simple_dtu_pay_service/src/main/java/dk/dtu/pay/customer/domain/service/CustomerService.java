package dk.dtu.pay.customer.domain.service;

import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;

import java.util.UUID;

public class CustomerService {

    private final CustomerRepositoryPort repo;

    public CustomerService(CustomerRepositoryPort repo) {
        this.repo = repo;
    }

    public Customer registerCustomer(Customer req) {
        Customer c = new Customer(req.getName(), req.getCprNumber(), req.getBankAccountId());
        c.setId(UUID.randomUUID().toString());
        repo.save(c);
        return c;
    }

    public void deleteCustomer(String id) {
        // Implementation depends on whether you added delete to CustomerRepositoryPort
    }
}