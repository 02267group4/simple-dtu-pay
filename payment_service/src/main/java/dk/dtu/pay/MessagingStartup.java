package dk.dtu.pay;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQTokenValidationResultConsumer;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQTokenValidationResultConsumer> tokenValidationResultConsumer;

    void onStart(@Observes StartupEvent ev) {
        RabbitMQTokenValidationResultConsumer res = tokenValidationResultConsumer.get();

        System.out.println("MessagingStartup (Payment Service) onStart — tokenResultConsumer=" +
                (res == null ? "NULL" : "OK@" + System.identityHashCode(res))
        );

        if (res != null) res.startListening();
    }
}
