package dk.dtu.pay.service.repository;

import dk.dtu.pay.service.model.Merchant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MerchantRepository {
    private final Map<String, Merchant> merchants = new ConcurrentHashMap<>();

    public Merchant get(String id) {
        return merchants.get(id);
    }

    public void put(Merchant m) {
        merchants.put(m.id, m);
    }

    public void remove(String id) {
        merchants.remove(id);
    }
}
