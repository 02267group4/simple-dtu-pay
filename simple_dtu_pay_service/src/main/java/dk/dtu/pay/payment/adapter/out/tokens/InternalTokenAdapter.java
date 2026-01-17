package dk.dtu.pay.payment.adapter.out.tokens;

import dk.dtu.pay.payment.application.port.out.TokenPort;
import dk.dtu.pay.service.AppContext; // Bridging to old context until Token is fully standalone
import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author Nick
 */
@ApplicationScoped
public class InternalTokenAdapter implements TokenPort {
    @Override
    public String validateAndConsumeToken(String token) throws Exception {
        String customerId = AppContext.tokenRepo.consume(token);
        if (customerId == null)
            throw new Exception("Invalid Token");
        return customerId;
    }
}