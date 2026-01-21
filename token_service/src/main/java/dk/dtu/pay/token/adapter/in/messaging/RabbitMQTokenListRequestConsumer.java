package dk.dtu.pay.token.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.token.adapter.out.messaging.RabbitMQTokenListResultPublisher;
import dk.dtu.pay.token.domain.service.TokenService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import dk.dtu.pay.token.adapter.in.messaging.dto.TokenListRequested;


import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQTokenListRequestConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "token.list.request";

    private final ObjectMapper mapper = new ObjectMapper();
    private final TokenService tokenService;
    private final RabbitMQTokenListResultPublisher publisher;

    @Inject
    public RabbitMQTokenListRequestConsumer(TokenService tokenService,
                                            RabbitMQTokenListResultPublisher publisher) {
        this.tokenService = tokenService;
        this.publisher = publisher;
    }

    @PostConstruct
    void init() {
        System.out.println("RabbitMQTokenListRequestConsumer @PostConstruct publisher=" +
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

            System.out.println("RabbitMQTokenListRequestConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQTokenListRequestConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic", true);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("TokenListRequest raw body: " + raw);

                TokenListRequested ev = mapper.readValue(delivery.getBody(), TokenListRequested.class);
                java.util.List<String> tokens = tokenService.listTokensForCustomer(ev.customerId());
                publisher.publish(ev.requestId(), tokens);
            }, consumerTag -> {
            });

        } catch (Exception e) {
            System.err.println("RabbitMQTokenListRequestConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
