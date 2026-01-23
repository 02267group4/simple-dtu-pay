package dk.dtu.pay.customer.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.customer.domain.model.CustomerReportEvents.CustomerReportRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;

/**
 * Publishes customer report requests to payment service via RabbitMQ.
 */
@ApplicationScoped
public class RabbitMQCustomerReportPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "customer.report.request";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publish(String correlationId, String customerId) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);

                CustomerReportRequest payload = new CustomerReportRequest(correlationId, customerId);
                byte[] body = mapper.writeValueAsBytes(payload);

                System.out.println("Publishing CustomerReportRequest: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, ROUTING_KEY, null, body);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish customer report request", e);
        }
    }
}
