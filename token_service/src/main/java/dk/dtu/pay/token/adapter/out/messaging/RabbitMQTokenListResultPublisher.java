package dk.dtu.pay.token.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.token.adapter.out.messaging.dto.TokenListResult;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.util.List;   // ✅ REQUIRED

@ApplicationScoped
public class RabbitMQTokenListResultPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String RESULT_KEY = "token.list.result";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publish(String requestId, boolean success, String error, List<String> tokens) {
        publishResult(new TokenListResult(requestId, success, error, tokens));
    }

    private void publishResult(Object payload) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);
                byte[] body = mapper.writeValueAsBytes(payload);

                System.out.println(
                        "Publishing TokenListResult payload: " +
                                new String(body, StandardCharsets.UTF_8)
                );

                channel.basicPublish(EXCHANGE, RESULT_KEY, null, body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
