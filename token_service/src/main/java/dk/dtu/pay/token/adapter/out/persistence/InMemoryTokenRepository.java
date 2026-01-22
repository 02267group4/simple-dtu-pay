package dk.dtu.pay.token.adapter.out.persistence;

import dk.dtu.pay.token.application.port.out.TokenRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import dk.dtu.pay.token.domain.model.TokenInfo;

@ApplicationScoped
public class InMemoryTokenRepository implements TokenRepositoryPort {

    // token -> TokenInfo
    private final Map<String, TokenInfo> tokenToInfo = new ConcurrentHashMap<>();

    @Override
    public void store(String token, String customerId, String bankAccountId) {
        tokenToInfo.put(token, new TokenInfo(customerId, bankAccountId));
    }

    @Override
    public Optional<TokenInfo> consume(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenToInfo.remove(token));
    }

    @Override
    public boolean contains(String token) {
        return tokenToInfo.containsKey(token);
    }

    @Override
    public List<String> findByCustomerId(String customerId) {
        return tokenToInfo.entrySet().stream()
                .filter(e -> customerId.equals(e.getValue().customerId()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}