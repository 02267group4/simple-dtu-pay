package dk.dtu.pay.token.application.port.out;

import java.util.Optional;

public interface TokenRepositoryPort {

    void store(String token, String customerId);

    // single-use: returns customerId and removes mapping
    Optional<String> consume(String token);

    boolean contains(String token);
}
