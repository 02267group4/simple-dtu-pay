package dk.dtu.pay.merchant.application.port.out;

import dk.dtu.pay.merchant.domain.model.Merchant;

import java.util.Optional;

public interface MerchantRepositoryPort {

    Optional<Merchant> findByMerchantId(String merchantId);

    void put(Merchant merchant);

    Merchant get(String id);

    void remove(String id);
}
