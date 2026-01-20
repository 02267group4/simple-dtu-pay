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
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic");

                PaymentRequested event = new PaymentRequested(paymentId, token);

                byte[] body = mapper.writeValueAsBytes(event);

                // <-- ADDED: log the raw JSON payload
                System.out.println("Publishing PaymentRequested payload: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(
                        EXCHANGE,
                        ROUTING_KEY,
                        null,
                        body
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
