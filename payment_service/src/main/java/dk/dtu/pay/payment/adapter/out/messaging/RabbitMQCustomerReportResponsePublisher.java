package dk.dtu.pay.payment.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.domain.model.Payment;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Publishes customer report responses back to customer service via RabbitMQ.
 */
@ApplicationScoped
public class RabbitMQCustomerReportResponsePublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "customer.report.response";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publishReportResponse(String correlationId, List<Payment> payments) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);

                CustomerReportResponse response = new CustomerReportResponse(correlationId, payments);
                byte[] body = mapper.writeValueAsBytes(response);

                System.out.println("Publishing CustomerReportResponse: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, ROUTING_KEY, null, body);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish customer report response", e);
        }
    }

    // Inner DTO for the response
    public record CustomerReportResponse(String correlationId, List<Payment> payments) {}
}
