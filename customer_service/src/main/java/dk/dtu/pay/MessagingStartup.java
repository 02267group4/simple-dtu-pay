// java
package dk.dtu.pay;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class MessagingStartup {

    void onStart(@Observes StartupEvent ev) {
        // Customer Service currently has no RabbitMQ consumers to start
        System.out.println("MessagingStartup (Customer Service) onStart — No listeners to initialize.");
    }
}
