package dk.dtu.pay.token.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.adapter.out.messaging.dto.PaymentRequested;
import dk.dtu.pay.token.adapter.out.messaging.RabbitMQTokenResultPublisher;
import dk.dtu.pay.token.domain.service.TokenService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class RabbitMQPaymentRequestedConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "payment.requested";

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject TokenService tokenService;
    @Inject RabbitMQTokenResultPublisher publisher;

    public RabbitMQPaymentRequestedConsumer() {
        new Thread(this::start).start();
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE, "topic");
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                PaymentRequested event = mapper.readValue(delivery.getBody(), PaymentRequested.class);

                try {
                    String customerId = tokenService.consumeToken(event.token());
                    publisher.publishValidated(event.paymentId(), customerId);
                } catch (Exception e) {
                    publisher.publishRejected(event.paymentId(), e.getMessage());
                }
            }, tag -> {});

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
