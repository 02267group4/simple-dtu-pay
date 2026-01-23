package dk.dtu.pay;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQTokenValidationResultConsumer;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQManagerReportRequestConsumer;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQMerchantReportRequestConsumer;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQCustomerReportRequestConsumer;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQTokenValidationResultConsumer> tokenValidationResultConsumer;

    @Inject
    Instance<RabbitMQManagerReportRequestConsumer> managerReportRequestConsumer;

    @Inject
    Instance<RabbitMQMerchantReportRequestConsumer> merchantReportRequestConsumer;

    @Inject
    Instance<RabbitMQCustomerReportRequestConsumer> customerReportRequestConsumer;

    void onStart(@Observes StartupEvent ev) {
        RabbitMQTokenValidationResultConsumer res = tokenValidationResultConsumer.get();

        System.out.println("MessagingStartup (Payment Service) onStart — tokenResultConsumer=" +
                (res == null ? "NULL" : "OK@" + System.identityHashCode(res))
        );

        if (res != null) res.startListening();

        // Start Manager Report request consumer (handles requests from manager_service)
        RabbitMQManagerReportRequestConsumer managerReqConsumer = managerReportRequestConsumer.get();
        System.out.println("MessagingStartup (Payment Service) onStart — managerReportRequestConsumer=" +
                (managerReqConsumer == null ? "NULL" : "OK@" + System.identityHashCode(managerReqConsumer))
        );
        if (managerReqConsumer != null) managerReqConsumer.startListening();

        // Start Merchant Report request consumer (handles requests from merchant_service)
        RabbitMQMerchantReportRequestConsumer merchantReqConsumer = merchantReportRequestConsumer.get();
        System.out.println("MessagingStartup (Payment Service) onStart — merchantReportRequestConsumer=" +
                (merchantReqConsumer == null ? "NULL" : "OK@" + System.identityHashCode(merchantReqConsumer))
        );
        if (merchantReqConsumer != null) merchantReqConsumer.startListening();

        // Start Customer Report request consumer (handles requests from customer_service)
        RabbitMQCustomerReportRequestConsumer customerReqConsumer = customerReportRequestConsumer.get();
        System.out.println("MessagingStartup (Payment Service) onStart — customerReportRequestConsumer=" +
                (customerReqConsumer == null ? "NULL" : "OK@" + System.identityHashCode(customerReqConsumer))
        );
        if (customerReqConsumer != null) customerReqConsumer.startListening();
    }
}
