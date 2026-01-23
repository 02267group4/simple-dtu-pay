// java
package dk.dtu.pay.payment.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.adapter.out.messaging.dto.PaymentRequested;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQPaymentRequestedPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "payment.requested";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publishPaymentRequested(String paymentId, String token) {
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

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);

                PaymentRequested event = new PaymentRequested(paymentId, token);
                byte[] body = mapper.writeValueAsBytes(event);

                System.out.println("Publishing PaymentRequested payload: " +
                        new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, ROUTING_KEY, null, body);
            }
        } catch (Exception e) {
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