package dk.dtu.pay.payment.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.adapter.out.messaging.dto.ManagerReportRequest;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQManagerReportRequestPublisher {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "manager.report.request";

    private final ObjectMapper mapper = new ObjectMapper();

    public void publishReportRequest(String correlationId) {
        publish(new ManagerReportRequest(correlationId));
    }

    private void publish(ManagerReportRequest request) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {

                channel.exchangeDeclare(EXCHANGE, "topic", true);
                byte[] body = mapper.writeValueAsBytes(request);

                System.out.println("Publishing to " + ROUTING_KEY + " payload: " + new String(body, StandardCharsets.UTF_8));

                channel.basicPublish(EXCHANGE, ROUTING_KEY, null, body);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
