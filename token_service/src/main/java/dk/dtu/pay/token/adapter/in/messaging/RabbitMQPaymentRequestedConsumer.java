package dk.dtu.pay.token.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
// UPDATED: Now importing from the local dto package within token_service
import dk.dtu.pay.token.adapter.in.messaging.dto.PaymentRequested;
import dk.dtu.pay.token.adapter.out.messaging.RabbitMQTokenResultPublisher;
import dk.dtu.pay.token.domain.service.TokenService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQPaymentRequestedConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "payment.requested";

    private final ObjectMapper mapper = new ObjectMapper();
    private final TokenService tokenService;
    private final RabbitMQTokenResultPublisher publisher;

    @Inject
    public RabbitMQPaymentRequestedConsumer(TokenService tokenService,
                                            RabbitMQTokenResultPublisher publisher) {
        this.tokenService = tokenService;
        this.publisher = publisher;
    }

    @PostConstruct
    void init() {
        System.out.println("RabbitMQPaymentRequestedConsumer @PostConstruct publisher=" +
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

            System.out.println("RabbitMQPaymentRequestedConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQPaymentRequestedConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic");
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                System.out.println("PaymentRequested handler — this@" + System.identityHashCode(this) +
                        " publisher=" + (publisher == null ? "NULL" : ("OK@" + System.identityHashCode(publisher))));

                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("PaymentRequested raw body: " + raw);

                PaymentRequested event = mapper.readValue(delivery.getBody(), PaymentRequested.class);
                try {
                    String customerId = tokenService.consumeToken(event.token());
                    publisher.publishValidated(event.paymentId(), customerId);
                } catch (Exception e) {
                    publisher.publishRejected(event.paymentId(), e.getMessage());
                }
            }, consumerTag -> {});

        } catch (Exception e) {
            System.err.println("RabbitMQPaymentRequestedConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}