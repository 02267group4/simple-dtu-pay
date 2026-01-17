// InProcessTokenClient.java
package dk.dtu.pay.payment.adapter.out;

import dk.dtu.pay.payment.application.port.out.TokenClientPort;
import dk.dtu.pay.token.domain.service.TokenService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InProcessTokenClient implements TokenClientPort {

    private final TokenService tokenService;

    public InProcessTokenClient(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public String consumeToken(String token) throws InvalidTokenException {
        try {
            return tokenService.consumeToken(token);
        } catch (TokenService.InvalidTokenException e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }
}
