package dk.dtu.pay.token.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.token.adapter.out.messaging.dto.TokenIssueRejected;
import dk.dtu.pay.token.adapter.out.messaging.dto.TokenIssueValidated;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQTokenIssueResultPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String VALIDATED_KEY = "token.issue.validated";
    private static final String REJECTED_KEY = "token.issue.rejected";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publishValidated(String requestId, java.util.List<String> tokens) {
        publish(VALIDATED_KEY, new TokenIssueValidated(requestId, tokens));
    }

    public void publishRejected(String requestId, String reason) {
        publish(REJECTED_KEY, new TokenIssueRejected(requestId, reason));
    }

    private void publish(String routingKey, Object payload) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);
                byte[] body = mapper.writeValueAsBytes(payload);

                System.out.println("Publishing to " + routingKey + " payload: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, routingKey, null, body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}