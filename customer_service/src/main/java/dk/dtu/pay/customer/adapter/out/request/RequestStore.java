package dk.dtu.pay.customer.adapter.out.request;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@ApplicationScoped
public class RequestStore {

    private final Map<String, Object> results = new ConcurrentHashMap<>();
    private final Map<String, Boolean> started = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        System.out.println(
                "RequestStore initialized this@" +
                        System.identityHashCode(this)
        );
    }

    public void start(String requestId) {
        started.put(requestId, Boolean.TRUE);
        System.out.println(
                "RequestStore.start(" + requestId + ") this@" +
                        System.identityHashCode(this)
        );
    }

    public void complete(String requestId, Object result) {
        started.putIfAbsent(requestId, true);
        results.put(requestId, result);
        System.out.println(
                "RequestStore.complete(" + requestId + ") this@" +
                        System.identityHashCode(this)
        );
    }

    public boolean isKnown(String requestId) {
        return started.containsKey(requestId);
    }

    public Optional<Object> getResult(String requestId) {
        Object result = results.get(requestId);
        System.out.println(
                "RequestStore.getResult(" + requestId + ") -> " +
                        (result == null ? "NOT READY" : "READY") +
                        " this@" + System.identityHashCode(this)
        );
        return Optional.ofNullable(result);
    }
}
