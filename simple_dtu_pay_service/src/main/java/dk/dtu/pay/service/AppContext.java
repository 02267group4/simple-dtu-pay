package dk.dtu.pay.service;

import dk.dtu.pay.customer.adapter.out.persistence.InMemoryCustomerRepository;
import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.domain.service.CustomerService;

import dk.dtu.pay.merchant.adapter.out.persistence.MerchantRepository;
import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import dk.dtu.pay.merchant.domain.service.MerchantService;

import dk.dtu.pay.service.repository.PaymentRepository;
import dk.dtu.pay.service.domain.service.PaymentService;

import dk.dtu.pay.token.application.port.out.TokenRepositoryPort;
import dk.dtu.pay.token.adapter.out.persistence.InMemoryTokenRepository;
import dk.dtu.pay.token.domain.service.TokenService;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankService_Service;

public final class AppContext {

    // Shared in-memory storage
    public static final CustomerRepositoryPort customerRepo =
            new InMemoryCustomerRepository();

    public static final MerchantRepositoryPort merchantRepo =
            new MerchantRepository();

    public static final PaymentRepository paymentRepo =
            new PaymentRepository();

    public static final TokenRepositoryPort tokenRepo =
            new InMemoryTokenRepository();

    // Shared services
    public static final TokenService tokenService =
            new TokenService(tokenRepo);

    public static final CustomerService customerService =
            new CustomerService(customerRepo);

    public static final MerchantService merchantService =
            new MerchantService(merchantRepo);

    // Bank client
    public static final BankService bank =
            new BankService_Service().getBankServicePort();

    // Payment service
    public static final PaymentService paymentService =
            new PaymentService(customerRepo, merchantRepo, paymentRepo, tokenRepo, bank);

    private AppContext() {}
}
