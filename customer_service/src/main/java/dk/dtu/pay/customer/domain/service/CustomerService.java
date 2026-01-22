package dk.dtu.pay.customer.domain.service;

import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.adapter.out.messaging.RabbitMQTokenIssueRequestPublisher;
import dk.dtu.pay.customer.adapter.out.messaging.RabbitMQTokenListRequestPublisher;
import dk.dtu.pay.customer.domain.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CustomerService {

    private final CustomerRepositoryPort repo;
    private final RabbitMQTokenIssueRequestPublisher issuePublisher;
    private final RabbitMQTokenListRequestPublisher listPublisher;

    public CustomerService(CustomerRepositoryPort repo,
                           RabbitMQTokenIssueRequestPublisher issuePublisher,
                           RabbitMQTokenListRequestPublisher listPublisher) {
        this.repo = repo;
        this.issuePublisher = issuePublisher;
        this.listPublisher = listPublisher;
    }

    public Customer registerCustomer(Customer req) {
        Customer c = new Customer(req.getName(), req.getCprNumber(), req.getBankAccountId());
        c.setId(UUID.randomUUID().toString());
        repo.save(c);
        return c;
    }

    public void deleteCustomer(String id) {
        repo.remove(id);
    }

    public void requestTokenIssue(String requestId, String customerId, int count) throws UnknownCustomerException {
        Optional<Customer> customerOpt = repo.findByCustomerId(customerId);
        if (customerOpt.isEmpty()) {
            throw new UnknownCustomerException("customer with id \"" + customerId + "\" is unknown");
        }
        Customer customer = customerOpt.get();
        issuePublisher.publish(requestId, customerId, customer.getBankAccountId(), count);
    }

    public void requestTokenList(String requestId, String customerId) throws UnknownCustomerException {
        if (customerNotFound(customerId)) {
            throw new UnknownCustomerException("customer with id \"" + customerId + "\" is unknown");
        }
        listPublisher.publish(requestId, customerId);
    }

    private boolean customerNotFound(String customerId) {
        return repo.findByCustomerId(customerId).isEmpty();
    }

    public static class UnknownCustomerException extends Exception {
        public UnknownCustomerException(String msg) { super(msg); }
    }
}
