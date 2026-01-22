package dk.dtu.pay;

import dk.dtu.pay.manager.adapter.in.messaging.RabbitMQManagerReportResponseConsumer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    RabbitMQManagerReportResponseConsumer reportResponseConsumer;

    void onStart(@Observes StartupEvent ev) {
        System.out.println("MessagingStartup (Manager Service) onStart — Initializing listeners.");
        reportResponseConsumer.startListening();
    }
}
