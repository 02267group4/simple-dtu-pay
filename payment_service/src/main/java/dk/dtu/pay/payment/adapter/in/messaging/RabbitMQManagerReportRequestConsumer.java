package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.adapter.out.messaging.RabbitMQManagerReportResponsePublisher;
import dk.dtu.pay.payment.adapter.out.messaging.dto.ManagerReportRequest;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.service.PaymentService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;

@ApplicationScoped
public class RabbitMQManagerReportRequestConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "manager.report.request";

    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentService paymentService;
    private final RabbitMQManagerReportResponsePublisher publisher;

    @Inject
    public RabbitMQManagerReportRequestConsumer(PaymentService paymentService,
                                                 RabbitMQManagerReportResponsePublisher publisher) {
        this.paymentService = paymentService;
        this.publisher = publisher;
    }

    @PostConstruct
    void init() {
        System.out.println("RabbitMQManagerReportRequestConsumer @PostConstruct publisher=" +
                (publisher == null ? "NULL" : "OK") + " this@" + System.identityHashCode(this));
    }

    public void startListening() {
        new Thread(this::start).start();
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            String host = System.getenv().getOrDefault("RABBIT_HOST", "localhost");
            factory.setHost(host);

            System.out.println("RabbitMQManagerReportRequestConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQManagerReportRequestConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic", true);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("ManagerReportRequest raw body: " + raw);

                ManagerReportRequest request = mapper.readValue(delivery.getBody(), ManagerReportRequest.class);
                
                List<Payment> payments = paymentService.getPayments();
                publisher.publishReportResponse(request.correlationId(), payments);
            }, consumerTag -> {
            });

        } catch (Exception e) {
            System.err.println("RabbitMQManagerReportRequestConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
