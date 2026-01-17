package dk.dtu.pay.token.domain.service;

import dk.dtu.pay.token.application.port.out.TokenRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class TokenService {

    private final TokenRepositoryPort tokenRepo;

    public TokenService(TokenRepositoryPort tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    public String issueToken(String customerId) {
        String token = UUID.randomUUID().toString();
        tokenRepo.store(token, customerId);
        return token;
    }
}
