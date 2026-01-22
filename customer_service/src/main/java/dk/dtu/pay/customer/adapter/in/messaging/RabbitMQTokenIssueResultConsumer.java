package dk.dtu.pay.customer.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.customer.adapter.in.messaging.dto.TokenIssueResult;
import dk.dtu.pay.customer.adapter.out.request.RequestStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class RabbitMQTokenIssueResultConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String RESULT_KEY = "token.issue.result";

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    RequestStore requestStore;

    @PostConstruct
    void init() {
        System.out.println("RabbitMQTokenIssueResultConsumer @PostConstruct this@" + System.identityHashCode(this));
        // startListening() is triggered by MessagingStartup
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
            channel.queueBind(queue, EXCHANGE, RESULT_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                TokenIssueResult ev = mapper.readValue(raw, TokenIssueResult.class);

                // store full envelope as a non-null value (inner map may contain nulls)
                Map<String, Object> envelope = new HashMap<>();
                envelope.put("requestId", ev.requestId());
                envelope.put("success", ev.success());
                envelope.put("error", ev.error()); // may be null on success
                envelope.put("tokens", ev.tokens());

                System.out.println("Completing request " + ev.requestId());
                requestStore.complete(ev.requestId(), envelope);
            }, consumerTag -> {});

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
