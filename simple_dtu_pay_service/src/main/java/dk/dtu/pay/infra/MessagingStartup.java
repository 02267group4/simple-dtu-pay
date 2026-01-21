// java
package dk.dtu.pay.infra;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import dk.dtu.pay.token.adapter.in.messaging.RabbitMQPaymentRequestedConsumer;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQTokenValidationResultConsumer;
import dk.dtu.pay.token.adapter.in.messaging.RabbitMQTokenIssueRequestConsumer;
import dk.dtu.pay.token.adapter.in.messaging.RabbitMQTokenListRequestConsumer;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQPaymentRequestedConsumer> tokenRequestConsumer;

    @Inject
    Instance<RabbitMQTokenValidationResultConsumer> tokenResultConsumer;

    // New consumers — force instantiation/startup
    @Inject
    Instance<RabbitMQTokenIssueRequestConsumer> tokenIssueRequestConsumer;

    @Inject
    Instance<RabbitMQTokenListRequestConsumer> tokenListRequestConsumer;

    void onStart(@Observes StartupEvent ev) {
        // Force actual bean instantiation and explicitly start listeners
        RabbitMQPaymentRequestedConsumer req = tokenRequestConsumer.get();
        RabbitMQTokenValidationResultConsumer res = tokenResultConsumer.get();
        RabbitMQTokenIssueRequestConsumer issueReq = tokenIssueRequestConsumer.get();
        RabbitMQTokenListRequestConsumer listReq = tokenListRequestConsumer.get();

        System.out.println("MessagingStartup onStart — tokenRequestConsumer=" +
                (req == null ? "NULL" : "OK@" + System.identityHashCode(req)) +
                " tokenResultConsumer=" +
                (res == null ? "NULL" : "OK@" + System.identityHashCode(res)) +
                " tokenIssueRequestConsumer=" +
                (issueReq == null ? "NULL" : "OK@" + System.identityHashCode(issueReq)) +
                " tokenListRequestConsumer=" +
                (listReq == null ? "NULL" : "OK@" + System.identityHashCode(listReq))
        );

        if (req != null) req.startListening();
        if (res != null) res.startListening();
        if (issueReq != null) issueReq.startListening();
        if (listReq != null) listReq.startListening();
    }
}
