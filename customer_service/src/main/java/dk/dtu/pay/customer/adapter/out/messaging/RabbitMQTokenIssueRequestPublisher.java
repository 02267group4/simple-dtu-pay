package dk.dtu.pay.customer.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.customer.adapter.out.messaging.dto.TokenIssueRequested;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQTokenIssueRequestPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "token.issue.request";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publish(String requestId, String customerId, int count) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);

                TokenIssueRequested payload = new TokenIssueRequested(requestId, customerId, count);
                byte[] body = mapper.writeValueAsBytes(payload);

                System.out.println("Publishing TokenIssueRequested.java payload: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, ROUTING_KEY, null, body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}