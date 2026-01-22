package dk.dtu.pay.token.domain.service;

import dk.dtu.pay.token.application.port.out.TokenRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    private TokenRepositoryPort repo;
    private TokenService service;

    @BeforeEach
    void setUp() {
        repo = mock(TokenRepositoryPort.class);
        service = new TokenService(repo);
    }

    @Test
    void issueToken_storesAndReturnsToken() {
        String customerId = "cust-1";
        String token = service.issueToken(customerId);
        assertNotNull(token);
        verify(repo, times(1)).store(eq(token), eq(customerId));
    }

    @Test
    void issueTokens_invalidCount_throws() {
        assertThrows(TokenService.InvalidTokenException.class, () -> service.issueTokens("c", 0));
        assertThrows(TokenService.InvalidTokenException.class, () -> service.issueTokens("c", 6));
    }

    @Test
    void issueTokens_respectsExistingLimits() throws Exception {
        when(repo.findByCustomerId("c")).thenReturn(List.of());
        List<String> tokens = service.issueTokens("c", 3);
        assertEquals(3, tokens.size());
        verify(repo, times(3)).store(anyString(), eq("c"));
    }

    @Test
    void issueTokens_tooManyExisting_throws() {
        when(repo.findByCustomerId("c")).thenReturn(List.of("a","b"));
        assertThrows(TokenService.InvalidTokenException.class, () -> service.issueTokens("c", 1));
    }

    @Test
    void consumeToken_success_returnsCustomer() throws Exception {
        when(repo.consume("t")).thenReturn(Optional.of("cust"));
        String cid = service.consumeToken("t");
        assertEquals("cust", cid);
        verify(repo, times(1)).consume("t");
    }

    @Test
    void consumeToken_null_throws() {
        assertThrows(TokenService.InvalidTokenException.class, () -> service.consumeToken(null));
    }

    @Test
    void consumeToken_invalid_throws() {
        when(repo.consume("t")).thenReturn(Optional.empty());
        assertThrows(TokenService.InvalidTokenException.class, () -> service.consumeToken("t"));
    }
}
