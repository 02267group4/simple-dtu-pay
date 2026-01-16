package dk.dtu.pay.service;

import dk.dtu.pay.customer.adapter.out.persistence.InMemoryCustomerRepository;
import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.domain.service.CustomerService;
import dk.dtu.pay.service.repository.*;
import dk.dtu.pay.service.service.*;
import dtu.ws.fastmoney.*;

public final class AppContext {

    // Shared in-memory storage (your "DB")
    public static final CustomerRepositoryPort customerRepo = new InMemoryCustomerRepository();
    public static final MerchantRepository merchantRepo = new MerchantRepository();
    public static final PaymentRepository paymentRepo   = new PaymentRepository();

    public static final TokenRepository tokenRepo = new TokenRepository();
    public static final TokenService tokenService = new TokenService(customerRepo, tokenRepo);

    // Shared services
    public static final CustomerService customerService = new CustomerService(customerRepo);
    public static final MerchantService merchantService = new MerchantService(merchantRepo);
    // Bank client
    public static final BankService bank = new BankService_Service().getBankServicePort();

    // Payment service depends on repos + bank
    public static final PaymentService paymentService =
            new PaymentService(customerRepo, merchantRepo, paymentRepo, tokenRepo, bank);

    private AppContext() {}
}
