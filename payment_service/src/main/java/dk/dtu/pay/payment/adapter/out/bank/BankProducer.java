package dk.dtu.pay.payment.adapter.out.bank;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class BankProducer {
    @Produces
    public BankService bankService() {
        return new BankService_Service().getBankServicePort();
    }
}
