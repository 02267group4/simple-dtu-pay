package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.adapter.out.messaging.dto.ManagerReportResponse;
import dk.dtu.pay.payment.domain.model.Payment;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RabbitMQManagerReportResponseConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "manager.report.reply";

    private final ObjectMapper mapper = new ObjectMapper();
    
    private final Map<String, CompletableFuture<List<Payment>>> pendingRequests = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        System.out.println("RabbitMQManagerReportResponseConsumer @PostConstruct this@" + System.identityHashCode(this));
    }

    public void startListening() {
        new Thread(this::start).start();
    }

    public CompletableFuture<List<Payment>> registerPendingRequest(String correlationId) {
        CompletableFuture<List<Payment>> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);
        return future;
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            String host = System.getenv().getOrDefault("RABBIT_HOST", "localhost");
            factory.setHost(host);

            System.out.println("RabbitMQManagerReportResponseConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQManagerReportResponseConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic", true);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("ManagerReportResponse raw body: " + raw);

                ManagerReportResponse response = mapper.readValue(delivery.getBody(), ManagerReportResponse.class);
                
                CompletableFuture<List<Payment>> future = pendingRequests.remove(response.correlationId());
                if (future != null) {
                    future.complete(response.payments());
                } else {
                    System.err.println("No pending request found for correlationId: " + response.correlationId());
                }
            }, consumerTag -> {
            });

        } catch (Exception e) {
            System.err.println("RabbitMQManagerReportResponseConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
