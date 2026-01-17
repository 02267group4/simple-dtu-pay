package dk.dtu.pay.payment.application.port.out;

import java.math.BigDecimal;

/**
 * @author Nick
 */
public interface BankPort {
    void transfer(String from, String to, BigDecimal amount, String desc) throws Exception;
}