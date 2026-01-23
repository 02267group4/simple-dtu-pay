package dk.dtu.pay.token.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.token.adapter.in.messaging.dto.TokenIssueRequested;
import dk.dtu.pay.token.adapter.out.messaging.RabbitMQTokenIssueResultPublisher;
import dk.dtu.pay.token.domain.service.TokenService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;

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
        // startListening is invoked by MessagingStartup
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
                System.out.println("TokenIssueRequest raw body: " + raw);

                TokenIssueRequested ev = mapper.readValue(delivery.getBody(), TokenIssueRequested.class);
                try {
                    List<String> tokens = tokenService.issueTokens(ev.customerId(), ev.bankAccountId(), ev.count());
                    // success -> publish explicit envelope with success=true, error=null, tokens=list
                    publisher.publish(ev.requestId(), true, null, tokens);
                } catch (Exception e) {
                    System.err.println("Token issue failed for requestId=" + ev.requestId() + ": " + e.getMessage());
                    // failure -> publish explicit envelope with success=false, error=message, tokens=[]
                    publisher.publish(ev.requestId(), false, e.getMessage(), java.util.List.of());
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
