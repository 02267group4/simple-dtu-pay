package dk.dtu.pay.token.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.token.adapter.out.messaging.RabbitMQTokenIssueResultPublisher;
import dk.dtu.pay.token.domain.service.TokenService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dk.dtu.pay.token.adapter.in.messaging.dto.TokenIssueRequested;


import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQTokenIssueRequestConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "token.issue.request";

    private final ObjectMapper mapper = new ObjectMapper();
    private final TokenService tokenService;
    private final RabbitMQTokenIssueResultPublisher publisher;

    @Inject
    public RabbitMQTokenIssueRequestConsumer(TokenService tokenService,
                                             RabbitMQTokenIssueResultPublisher publisher) {
        this.tokenService = tokenService;
        this.publisher = publisher;
    }

    @PostConstruct
    void init() {
        System.out.println("RabbitMQTokenIssueRequestConsumer @PostConstruct publisher=" +
                (publisher == null ? "NULL" : "OK") + " this@" + System.identityHashCode(this));
        // start the consumer thread on startup
        // startListening();
    }

    public void startListening() {
        new Thread(this::start).start();
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            String host = System.getenv().getOrDefault("RABBIT_HOST", "localhost");
            factory.setHost(host);

            System.out.println("RabbitMQTokenIssueRequestConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQTokenIssueRequestConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic", true);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("TokenIssueRequested.java raw body: " + raw);

                TokenIssueRequested ev = mapper.readValue(delivery.getBody(), TokenIssueRequested.class);
                try {
                    java.util.List<String> tokens = tokenService.issueTokens(ev.customerId(), ev.count());
                    publisher.publishValidated(ev.requestId(), tokens);
                } catch (Exception e) {
                    publisher.publishRejected(ev.requestId(), e.getMessage());
                }
            }, consumerTag -> {
            });

        } catch (Exception e) {
            System.err.println("RabbitMQTokenIssueRequestConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
