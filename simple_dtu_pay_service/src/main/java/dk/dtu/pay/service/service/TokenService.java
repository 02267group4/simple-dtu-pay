package dk.dtu.pay.service.service;

import dk.dtu.pay.service.repository.CustomerRepository;
import dk.dtu.pay.service.repository.TokenRepository;

import java.util.UUID;

public class TokenService {

    private final CustomerRepository customerRepo;
    private final TokenRepository tokenRepo;

    public TokenService(CustomerRepository customerRepo, TokenRepository tokenRepo) {
        this.customerRepo = customerRepo;
        this.tokenRepo = tokenRepo;
    }

    public String issueToken(String customerId) {
        if (customerRepo.get(customerId) == null) {
            throw new IllegalArgumentException("Customer not found: " + customerId);
        }

        String token = UUID.randomUUID().toString();
        tokenRepo.store(token, customerId);
        return token;
    }
}
