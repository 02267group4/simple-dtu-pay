package dk.dtu.pay.payment.adapter.out.bank;

import dk.dtu.pay.payment.application.port.out.BankPort;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;

/**
 * @author Nick
 */
@ApplicationScoped
public class SoapBankAdapter implements BankPort {
    private final BankService bank = new BankService_Service().getBankServicePort();

    @Override
    public void transfer(String from, String to, BigDecimal amount, String desc) throws Exception {
        bank.transferMoneyFromTo(from, to, amount, desc);
    }
}