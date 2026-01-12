package dk.dtu.pay.service.repository;

import dk.dtu.pay.service.model.Customer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomerRepository {
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();

    public Customer get(String id) {
        return customers.get(id);
    }

    public void put(Customer c) {
        customers.put(c.id, c);
    }

    public void remove(String id) {
        customers.remove(id);
    }
}
