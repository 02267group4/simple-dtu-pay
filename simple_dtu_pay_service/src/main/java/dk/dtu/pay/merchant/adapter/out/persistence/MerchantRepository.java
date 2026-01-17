package dk.dtu.pay.merchant.adapter.out.persistence;

import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import dk.dtu.pay.merchant.domain.model.Merchant;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MerchantRepository implements MerchantRepositoryPort {

    private final Map<String, Merchant> merchants = new ConcurrentHashMap<>();

    @Override
    public Merchant get(String id) {
        return merchants.get(id);
    }

    @Override
    public void put(Merchant m) {
        merchants.put(m.id, m);
    }

    @Override
    public void remove(String id) {
        merchants.remove(id);
    }

    @Override
    public Optional<Merchant> findByMerchantId(String merchantId) {
        return Optional.ofNullable(merchants.get(merchantId));
    }
}
