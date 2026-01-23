package dk.dtu.pay.token.application.port.out;

import dk.dtu.pay.token.domain.model.TokenInfo;
import java.util.List;
import java.util.Optional;

public interface TokenRepositoryPort {

    void store(String token, String customerId, String bankAccountId);

    // single-use: returns TokenInfo and removes mapping
    Optional<TokenInfo> consume(String token);

    boolean contains(String token);

    // list all tokens owned by a customer
    List<String> findByCustomerId(String customerId);
}