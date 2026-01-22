package dk.dtu.pay;

import dk.dtu.pay.customer.adapter.in.messaging.RabbitMQTokenListResultConsumer;
import dk.dtu.pay.customer.adapter.in.messaging.RabbitMQTokenIssueResultConsumer;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import dk.dtu.pay.customer.adapter.out.request.RequestStore;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQTokenListResultConsumer> tokenListResultConsumer;

    @Inject
    Instance<RabbitMQTokenIssueResultConsumer> tokenIssueResultConsumer;

    void onStart(@Observes StartupEvent ev) {

        RabbitMQTokenListResultConsumer listConsumer = null;
        if (tokenListResultConsumer != null && tokenListResultConsumer.isResolvable()) {
            listConsumer = tokenListResultConsumer.get();
        }

        RabbitMQTokenIssueResultConsumer issueConsumer = null;
        if (tokenIssueResultConsumer != null && tokenIssueResultConsumer.isResolvable()) {
            issueConsumer = tokenIssueResultConsumer.get();
        }

        System.out.println(
                "MessagingStartup (Customer Service) onStart — " +
                        "tokenListResultConsumer=" + (listConsumer != null) +
                        ", tokenIssueResultConsumer=" + (issueConsumer != null)
        );

        if (issueConsumer != null) {
            System.out.println("Starting RabbitMQTokenIssueResultConsumer listener");
            issueConsumer.startListening();
        }

        if (listConsumer != null) {
            System.out.println("Starting RabbitMQTokenListResultConsumer listener");
            listConsumer.startListening();
        }
    }
}

