// java
package dk.dtu.pay;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQTokenResultConsumer;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQTokenResultConsumer> tokenResultConsumer;

    void onStart(@Observes StartupEvent ev) {
        // Only initialize the consumer relevant to the Payment Service
        RabbitMQTokenResultConsumer res = tokenResultConsumer.get();

        System.out.println("MessagingStartup (Payment Service) onStart — tokenResultConsumer=" +
                (res == null ? "NULL" : "OK@" + System.identityHashCode(res))
        );

        if (res != null) res.startListening();
    }
}
