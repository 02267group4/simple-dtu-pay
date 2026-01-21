package dk.dtu.pay.merchant.domain.service;

import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import dk.dtu.pay.merchant.domain.model.Merchant;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class MerchantService {

    private final MerchantRepositoryPort repo;

    public MerchantService(MerchantRepositoryPort repo) {
        this.repo = repo;
    }

    public Merchant registerMerchant(Merchant req) {
        Merchant m = new Merchant(req.name, req.cprNumber, req.bankAccountId);
        m.id = UUID.randomUUID().toString();
        repo.put(m);
        return m;
    }

    public void deleteMerchant(String id) {
        repo.remove(id);
    }
}
