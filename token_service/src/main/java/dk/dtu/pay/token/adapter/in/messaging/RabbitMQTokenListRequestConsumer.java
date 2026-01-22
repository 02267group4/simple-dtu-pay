package dk.dtu.pay.token.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.token.adapter.in.messaging.dto.TokenListRequested;
import dk.dtu.pay.token.adapter.out.messaging.RabbitMQTokenListResultPublisher;
import dk.dtu.pay.token.domain.service.TokenService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;

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

            channel.basicConsume(queue, true, (tag, delivery) -> {

                TokenListRequested ev =
                        mapper.readValue(delivery.getBody(), TokenListRequested.class);

                try {
                    List<String> tokens =
                            tokenService.listTokensForCustomer(ev.customerId());

                    publisher.publish(ev.requestId(), true, null, tokens);

                } catch (Exception e) {

                    publisher.publish(ev.requestId(), false, e.getMessage(), List.of());
                }

            }, consumerTag -> {});

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
