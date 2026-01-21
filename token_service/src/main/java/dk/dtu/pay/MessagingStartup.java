package dk.dtu.pay;

import dk.dtu.pay.token.adapter.in.messaging.RabbitMQPaymentRequestedConsumer;
import dk.dtu.pay.token.adapter.in.messaging.RabbitMQTokenIssueRequestConsumer;
import dk.dtu.pay.token.adapter.in.messaging.RabbitMQTokenListRequestConsumer;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQPaymentRequestedConsumer> paymentRequestedConsumer;

    @Inject
    Instance<RabbitMQTokenIssueRequestConsumer> tokenIssueRequestConsumer;

    @Inject
    Instance<RabbitMQTokenListRequestConsumer> tokenListRequestConsumer;

    void onStart(@Observes StartupEvent ev) {

        RabbitMQPaymentRequestedConsumer req = paymentRequestedConsumer.get();
        RabbitMQTokenIssueRequestConsumer issueReq = tokenIssueRequestConsumer.get();
        RabbitMQTokenListRequestConsumer listReq = tokenListRequestConsumer.get();

        System.out.println(
                "MessagingStartup (Token Service) onStart — " +
                        "paymentRequested=" + (req != null) +
                        ", tokenIssueRequest=" + (issueReq != null) +
                        ", tokenListRequest=" + (listReq != null)
        );

        if (req != null) req.startListening();
        if (issueReq != null) issueReq.startListening();
        if (listReq != null) listReq.startListening();
    }
}
