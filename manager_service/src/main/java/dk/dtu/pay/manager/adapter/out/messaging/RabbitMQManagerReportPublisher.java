package dk.dtu.pay.manager.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.manager.domain.model.ManagerReportEvents.ManagerReportRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;

/**
 * Publishes manager report requests to payment service via RabbitMQ.
 */
@ApplicationScoped
public class RabbitMQManagerReportPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "manager.report.request";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publish(String correlationId) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);

                ManagerReportRequest payload = new ManagerReportRequest(correlationId);
                byte[] body = mapper.writeValueAsBytes(payload);

                System.out.println("Publishing ManagerReportRequest: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, ROUTING_KEY, null, body);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish manager report request", e);
        }
    }
}
