package dk.dtu.pay.service.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TokenRepository {

    // token -> customerId
    private final Map<String, String> tokenToCustomer = new ConcurrentHashMap<>();

    public void store(String token, String customerId) {
        tokenToCustomer.put(token, customerId);
    }

    /** returns customerId or null if token unknown */
    public String consume(String token) {
        // single-use: remove on read
        return tokenToCustomer.remove(token);
    }

    public boolean contains(String token) {
        return tokenToCustomer.containsKey(token);
    }
}
