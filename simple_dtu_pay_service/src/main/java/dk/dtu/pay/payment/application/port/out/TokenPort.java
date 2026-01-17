package dk.dtu.pay.payment.application.port.out;

/**
 * @author Nick
 */
public interface TokenPort {
    /** Returns customerId if valid, otherwise throws exception */
    String validateAndConsumeToken(String token) throws Exception;
}