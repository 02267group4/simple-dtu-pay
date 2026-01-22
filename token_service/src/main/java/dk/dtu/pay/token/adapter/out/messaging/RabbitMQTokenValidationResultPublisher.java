// java
package dk.dtu.pay.token.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.token.adapter.out.messaging.dto.TokenValidationRejected;
import dk.dtu.pay.token.adapter.out.messaging.dto.TokenValidationValidated;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQTokenValidationResultPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String VALIDATED_KEY = "token.validated";
    private static final String REJECTED_KEY = "token.rejected";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publishValidated(String paymentId, String customerId) {
        publish(VALIDATED_KEY, new TokenValidationValidated(paymentId, customerId));
    }

    public void publishRejected(String paymentId, String reason) {
        publish(REJECTED_KEY, new TokenValidationRejected(paymentId, reason));
    }

    private void publish(String routingKey, Object payload) {
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
                byte[] body = mapper.writeValueAsBytes(payload);

                System.out.println(
                        "Publishing to " + routingKey + " payload: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, routingKey, null, body);
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