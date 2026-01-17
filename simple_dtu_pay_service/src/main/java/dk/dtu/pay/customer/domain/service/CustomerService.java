package dk.dtu.pay.customer.domain.service;

import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.domain.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
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
        repo.remove(id); // make sure your port+repo actually has remove/delete
    }
}
