package dk.dtu.pay.manager.adapter.in.messaging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.manager.domain.model.ManagerReportEvents.ManagerReportResponse;
import dk.dtu.pay.manager.adapter.out.request.ManagerReportStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;

/**
 * Consumes manager report responses from payment service via RabbitMQ.
 */
@ApplicationScoped
public class RabbitMQManagerReportResponseConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "manager.report.reply";

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Inject
    ManagerReportStore reportStore;

    @PostConstruct
    void init() {
        System.out.println("RabbitMQManagerReportResponseConsumer @PostConstruct this@" + System.identityHashCode(this));
    }

    public void startListening() {
        new Thread(this::start).start();
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE, "topic", true);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            System.out.println("RabbitMQManagerReportResponseConsumer listening on " + ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("Received manager report response: " + raw);

                try {
                    ManagerReportResponse response = mapper.readValue(raw, ManagerReportResponse.class);

                    if (response != null && response.correlationId != null) {
                        reportStore.complete(response.correlationId, response.payments);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to handle manager report response: " + e.getMessage());
                    e.printStackTrace();
                }
            }, consumerTag -> {});

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start manager report response consumer", e);
        }
    }
}
