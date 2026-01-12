package dk.dtu.pay.service.service;

import dk.dtu.pay.service.model.Customer;
import dk.dtu.pay.service.repository.CustomerRepository;

import java.util.UUID;

public class CustomerService {

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public Customer registerCustomer(Customer req) {
        Customer c = new Customer(req.name, req.cprNumber, req.bankAccountId);
        c.id = UUID.randomUUID().toString();
        repo.put(c);
        return c;
    }

    public void deleteCustomer(String id) {
        repo.remove(id);
    }
}
