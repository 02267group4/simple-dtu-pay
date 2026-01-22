package dk.dtu.pay;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQTokenValidationResultConsumer;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQManagerReportRequestConsumer;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQManagerReportResponseConsumer;
import dk.dtu.pay.payment.adapter.in.messaging.RabbitMQMerchantReportRequestConsumer;

@ApplicationScoped
public class MessagingStartup {

    @Inject
    Instance<RabbitMQTokenValidationResultConsumer> tokenValidationResultConsumer;

    @Inject
    Instance<RabbitMQManagerReportRequestConsumer> managerReportRequestConsumer;

    @Inject
    Instance<RabbitMQManagerReportResponseConsumer> managerReportResponseConsumer;

    @Inject
    Instance<RabbitMQMerchantReportRequestConsumer> merchantReportRequestConsumer;

    void onStart(@Observes StartupEvent ev) {
        RabbitMQTokenValidationResultConsumer res = tokenValidationResultConsumer.get();

        System.out.println("MessagingStartup (Payment Service) onStart — tokenResultConsumer=" +
                (res == null ? "NULL" : "OK@" + System.identityHashCode(res))
        );

        if (res != null) res.startListening();

        // Start Manager Report consumers
        RabbitMQManagerReportRequestConsumer managerReqConsumer = managerReportRequestConsumer.get();
        System.out.println("MessagingStartup (Payment Service) onStart — managerReportRequestConsumer=" +
                (managerReqConsumer == null ? "NULL" : "OK@" + System.identityHashCode(managerReqConsumer))
        );
        if (managerReqConsumer != null) managerReqConsumer.startListening();

        RabbitMQManagerReportResponseConsumer managerRespConsumer = managerReportResponseConsumer.get();
        System.out.println("MessagingStartup (Payment Service) onStart — managerReportResponseConsumer=" +
                (managerRespConsumer == null ? "NULL" : "OK@" + System.identityHashCode(managerRespConsumer))
        );
        if (managerRespConsumer != null) managerRespConsumer.startListening();

        // Start Merchant Report consumer
        RabbitMQMerchantReportRequestConsumer merchantReqConsumer = merchantReportRequestConsumer.get();
        System.out.println("MessagingStartup (Payment Service) onStart — merchantReportRequestConsumer=" +
                (merchantReqConsumer == null ? "NULL" : "OK@" + System.identityHashCode(merchantReqConsumer))
        );
        if (merchantReqConsumer != null) merchantReqConsumer.startListening();
    }
}
