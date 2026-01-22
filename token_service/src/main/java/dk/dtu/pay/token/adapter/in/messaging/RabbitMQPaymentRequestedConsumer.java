package dk.dtu.pay.token.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import dk.dtu.pay.token.adapter.in.messaging.dto.PaymentRequested;
import dk.dtu.pay.token.adapter.out.messaging.RabbitMQTokenValidationResultPublisher;
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
    private final RabbitMQTokenValidationResultPublisher publisher;

    @Inject
    public RabbitMQPaymentRequestedConsumer(TokenService tokenService,
                                            RabbitMQTokenValidationResultPublisher publisher) {
        this.tokenService = tokenService;
        this.publisher = publisher;
    }

    @PostConstruct
    void init() {
        System.out.println("RabbitMQPaymentRequestedConsumer @PostConstruct publisher=" +
                (publisher == null ? "NULL" : "OK") + " this@" + System.identityHashCode(this));
        // Do NOT start the thread here; startup bean will explicitly call
        // startListening()
    }

    public void startListening() {
        new Thread(this::start).start();
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();

            String host = firstNonBlank(
                    System.getenv("RABBIT_HOST"),
                    System.getenv("QUARKUS_RABBITMQ_HOST"),
                    "rabbitmq",
                    "localhost"
            );
            factory.setHost(host);
            factory.setPort(5672);

            System.out.println("RabbitMQPaymentRequestedConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQPaymentRequestedConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic", true);
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
            }, consumerTag -> {
            });

        } catch (Exception e) {
            System.err.println("RabbitMQPaymentRequestedConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String firstNonBlank(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) return c;
        }
        return null;
    }
}