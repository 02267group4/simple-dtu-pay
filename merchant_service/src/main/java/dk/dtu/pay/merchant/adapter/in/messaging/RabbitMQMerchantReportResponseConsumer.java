package dk.dtu.pay.merchant.adapter.in.messaging;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.merchant.domain.model.MerchantReportEvents.MerchantReportResponse;
import dk.dtu.pay.merchant.adapter.out.request.MerchantReportStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;

/**
 * Consumes merchant report responses from payment service via RabbitMQ.
 */
@ApplicationScoped
public class RabbitMQMerchantReportResponseConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "merchant.report.response";

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Inject
    MerchantReportStore reportStore;

    @PostConstruct
    void init() {
        System.out.println("RabbitMQMerchantReportResponseConsumer @PostConstruct this@" + System.identityHashCode(this));
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

            System.out.println("RabbitMQMerchantReportResponseConsumer listening on " + ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("Received merchant report response: " + raw);

                try {
                    MerchantReportResponse response = mapper.readValue(raw, MerchantReportResponse.class);

                    if (response != null && response.correlationId != null) {
                        reportStore.complete(response.correlationId, response.payments);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to handle merchant report response: " + e.getMessage());
                    e.printStackTrace();
                }
            }, consumerTag -> {});

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to start merchant report response consumer", e);
        }
    }
}
