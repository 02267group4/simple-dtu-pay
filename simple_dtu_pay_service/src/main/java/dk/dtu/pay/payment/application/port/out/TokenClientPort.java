// TokenClientPort.java
package dk.dtu.pay.payment.application.port.out;

public interface TokenClientPort {
    /** returns customerId, throws if token invalid/used */
    String consumeToken(String token) throws InvalidTokenException;

    class InvalidTokenException extends Exception {
        public InvalidTokenException(String msg) { super(msg); }
    }
}
