package dk.dtu.pay.token.adapter.out.persistence;

import dk.dtu.pay.token.application.port.out.TokenRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class InMemoryTokenRepository implements TokenRepositoryPort {

    // token -> customerId
    private final Map<String, String> tokenToCustomer = new ConcurrentHashMap<>();

    @Override
    public void store(String token, String customerId) {
        tokenToCustomer.put(token, customerId);
    }

    @Override
    public Optional<String> consume(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenToCustomer.remove(token));
    }

    @Override
    public boolean contains(String token) {
        return tokenToCustomer.containsKey(token);
    }

    @Override
    public List<String> findByCustomerId(String customerId) {
        return tokenToCustomer.entrySet().stream()
                .filter(e -> customerId.equals(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}