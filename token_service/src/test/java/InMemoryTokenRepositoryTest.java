// java
package dk.dtu.pay.token.adapter.out.persistence;

import dk.dtu.pay.token.domain.model.TokenInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTokenRepositoryTest {

    @Test
    void store_contains_and_consume_behaviour() {
        InMemoryTokenRepository repo = new InMemoryTokenRepository();

        assertFalse(repo.contains("t1"));
        repo.store("t1", "cust1", "bank1");
        assertTrue(repo.contains("t1"));

        Optional<TokenInfo> consumed = repo.consume("t1");
        assertTrue(consumed.isPresent());
        assertEquals("cust1", consumed.get().customerId());
        assertEquals("bank1", consumed.get().bankAccountId());

        // consumed is single-use
        Optional<TokenInfo> second = repo.consume("t1");
        assertTrue(second.isEmpty());
        assertFalse(repo.contains("t1"));
    }

    @Test
    void findByCustomerId_returns_all_tokens() {
        InMemoryTokenRepository repo = new InMemoryTokenRepository();
        repo.store("a", "c1", "bank1");
        repo.store("b", "c1", "bank1");
        repo.store("c", "c2", "bank2");

        List<String> tokens = repo.findByCustomerId("c1");
        assertTrue(tokens.contains("a"));
        assertTrue(tokens.contains("b"));
        assertEquals(2, tokens.size());
    }

    @Test
    void consume_null_returnsEmpty() {
        InMemoryTokenRepository repo = new InMemoryTokenRepository();
        Optional<TokenInfo> r = repo.consume(null);
        assertTrue(r.isEmpty());
    }
}
