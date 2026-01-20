// java
package dk.dtu.pay.infra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import dk.dtu.pay.token.adapter.in.messaging.RabbitMQPaymentRequestedConsumer;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQTokenResultConsumer;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQPaymentRequestedConsumer> tokenRequestConsumer;

    @Inject
    Instance<RabbitMQTokenResultConsumer> tokenResultConsumer;

    void onStart(@Observes StartupEvent ev) {
        // Force actual bean instantiation and explicitly start listeners
        RabbitMQPaymentRequestedConsumer req = tokenRequestConsumer.get();
        RabbitMQTokenResultConsumer res = tokenResultConsumer.get();

        System.out.println("MessagingStartup onStart — tokenRequestConsumer=" +
                (req == null ? "NULL" : "OK@" + System.identityHashCode(req)) +
                " tokenResultConsumer=" +
                (res == null ? "NULL" : "OK@" + System.identityHashCode(res))
        );

        if (req != null) req.startListening();
        if (res != null) res.startListening();
    }
}
