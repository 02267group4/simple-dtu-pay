// java
package dk.dtu.pay;

import dk.dtu.pay.merchant.adapter.in.messaging.RabbitMQMerchantReportResponseConsumer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    RabbitMQMerchantReportResponseConsumer reportResponseConsumer;

    void onStart(@Observes StartupEvent ev) {
        System.out.println("MessagingStartup (Merchant Service) onStart — Initializing listeners.");
        reportResponseConsumer.startListening();
    }
}
