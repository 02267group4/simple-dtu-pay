package dk.dtu.pay.token.domain.service;

import dk.dtu.pay.token.application.port.out.TokenRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
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

    public List<String> issueTokens(String customerId, int count) throws InvalidTokenException {
        if (count < 1 || count > 5) {
            throw new InvalidTokenException("Requested token count must be between 1 and 5");
        }
        List<String> existing = tokenRepo.findByCustomerId(customerId);
        int existingCount = existing == null ? 0 : existing.size();

        if (existingCount > 1) {
            throw new InvalidTokenException("Customer already has more than 1 token");
        }
        if (existingCount + count > 6) {
            throw new InvalidTokenException("Would exceed maximum of 6 tokens");
        }

        List<String> issued = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String token = UUID.randomUUID().toString();
            tokenRepo.store(token, customerId);
            issued.add(token);
        }
        return issued;
    }

    public String consumeToken(String token) throws InvalidTokenException {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null");
        }
        return tokenRepo.consume(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or already used token"));
    }

    public List<String> listTokensForCustomer(String customerId) {
        return tokenRepo.findByCustomerId(customerId);
    }

    public static class InvalidTokenException extends Exception {
        public InvalidTokenException(String msg) {
            super(msg);
        }
    }
}