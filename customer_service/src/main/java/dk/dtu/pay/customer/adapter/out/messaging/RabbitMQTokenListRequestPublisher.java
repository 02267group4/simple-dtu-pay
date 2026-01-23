package dk.dtu.pay.customer.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.customer.adapter.out.messaging.dto.TokenListRequested;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQTokenListRequestPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "token.list.request";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publish(String requestId, String customerId) {
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

                TokenListRequested payload = new TokenListRequested(requestId, customerId);
                byte[] body = mapper.writeValueAsBytes(payload);

                System.out.println("Publishing TokenListRequest payload: " +
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