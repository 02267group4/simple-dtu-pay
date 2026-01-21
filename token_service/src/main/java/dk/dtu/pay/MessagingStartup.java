// java
package dk.dtu.pay;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import dk.dtu.pay.token.adapter.in.messaging.RabbitMQPaymentRequestedConsumer;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQPaymentRequestedConsumer> tokenRequestConsumer;

    void onStart(@Observes StartupEvent ev) {
        // Only initialize the consumer relevant to the Token Service
        RabbitMQPaymentRequestedConsumer req = tokenRequestConsumer.get();

        System.out.println("MessagingStartup (Token Service) onStart — tokenRequestConsumer=" +
                (req == null ? "NULL" : "OK@" + System.identityHashCode(req))
        );

        if (req != null) req.startListening();
    }
}
