package dk.dtu.pay.merchant.application.port.out;

import dk.dtu.pay.merchant.domain.model.Merchant;

public interface MerchantRepositoryPort {
    Merchant get(String id);
    void put(Merchant merchant);
    void remove(String id);
}