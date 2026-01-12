package dk.dtu.pay.service.service;

import dk.dtu.pay.service.model.Merchant;
import dk.dtu.pay.service.repository.MerchantRepository;

import java.util.UUID;

public class MerchantService {

    private final MerchantRepository repo;

    public MerchantService(MerchantRepository repo) {
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
