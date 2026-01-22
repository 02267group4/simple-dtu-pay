// java
package dk.dtu.pay.token.adapter.out.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTokenRepositoryTest {

    @Test
    void store_contains_and_consume_behaviour() {
        InMemoryTokenRepository repo = new InMemoryTokenRepository();

        assertFalse(repo.contains("t1"));
        repo.store("t1", "cust1");
        assertTrue(repo.contains("t1"));

        Optional<String> consumed = repo.consume("t1");
        assertTrue(consumed.isPresent());
        assertEquals("cust1", consumed.get());

        // consumed is single-use
        Optional<String> second = repo.consume("t1");
        assertTrue(second.isEmpty());
        assertFalse(repo.contains("t1"));
    }

    @Test
    void findByCustomerId_returns_all_tokens() {
        InMemoryTokenRepository repo = new InMemoryTokenRepository();
        repo.store("a", "c1");
        repo.store("b", "c1");
        repo.store("c", "c2");

        List<String> tokens = repo.findByCustomerId("c1");
        assertTrue(tokens.contains("a"));
        assertTrue(tokens.contains("b"));
        assertEquals(2, tokens.size());
    }

    @Test
    void consume_null_returnsEmpty() {
        InMemoryTokenRepository repo = new InMemoryTokenRepository();
        Optional<String> r = repo.consume(null);
        assertTrue(r.isEmpty());
    }
}
