// java
package dk.dtu.pay.customer.domain.service;

import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.adapter.out.messaging.RabbitMQTokenIssueRequestPublisher;
import dk.dtu.pay.customer.adapter.out.messaging.RabbitMQTokenListRequestPublisher;
import dk.dtu.pay.customer.adapter.out.request.RequestStore;
import dk.dtu.pay.customer.domain.model.Customer;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class CustomerService {

    private final CustomerRepositoryPort repo;
    private final RabbitMQTokenIssueRequestPublisher issuePublisher;
    private final RabbitMQTokenListRequestPublisher listPublisher;
    private final RequestStore requestStore;

    public CustomerService(CustomerRepositoryPort repo,
                           RabbitMQTokenIssueRequestPublisher issuePublisher,
                           RabbitMQTokenListRequestPublisher listPublisher,
                           RequestStore requestStore) {
        this.repo = repo;
        this.issuePublisher = issuePublisher;
        this.listPublisher = listPublisher;
        this.requestStore = requestStore;
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
        if (customerNotFound(customerId)) {
            throw new UnknownCustomerException("customer with id \"" + customerId + "\" is unknown");
        }

        issuePublisher.publish(requestId, customerId, count);
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
