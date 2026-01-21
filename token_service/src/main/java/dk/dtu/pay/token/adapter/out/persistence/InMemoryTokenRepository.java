// java
package dk.dtu.pay.token.adapter.out.persistence;

import dk.dtu.pay.token.application.port.out.TokenRepositoryPort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
        // handle null token safely: return empty instead of causing NPE
        if (token == null) {
            return Optional.empty();
        }
        // single-use: remove on read
        return Optional.ofNullable(tokenToCustomer.remove(token));
    }

    @Override
    public boolean contains(String token) {
        return tokenToCustomer.containsKey(token);
    }
}
