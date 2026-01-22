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
 * Publishes merchant report responses back to merchant service via RabbitMQ.
 */
@ApplicationScoped
public class RabbitMQMerchantReportResponsePublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "merchant.report.response";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publishReportResponse(String correlationId, List<Payment> payments) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);

                MerchantReportResponse response = new MerchantReportResponse(correlationId, payments);
                byte[] body = mapper.writeValueAsBytes(response);

                System.out.println("Publishing MerchantReportResponse: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, ROUTING_KEY, null, body);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish merchant report response", e);
        }
    }

    // Inner DTO for the response
    public record MerchantReportResponse(String correlationId, List<Payment> payments) {}
}
