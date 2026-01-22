package dk.dtu.pay.customer.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.customer.adapter.in.messaging.dto.TokenListResult;
import dk.dtu.pay.customer.adapter.out.request.RequestStore;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.HashMap;


import java.nio.charset.StandardCharsets;
import java.util.Map;

@ApplicationScoped
public class RabbitMQTokenListResultConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String RESULT_KEY = "token.list.result";

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    RequestStore requestStore;

    @PostConstruct
    void init() {
        System.out.println("RabbitMQTokenListResultConsumer @PostConstruct this@" + System.identityHashCode(this));
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
                System.out.println("TokenListResult raw body: " + raw);

                try {
                    TokenListResult ev = mapper.readValue(raw, TokenListResult.class);

                    Map<String, Object> envelope = new HashMap<>();
                    envelope.put("requestId", ev.requestId());
                    envelope.put("success", ev.success());
                    envelope.put("error", ev.error());
                    envelope.put("tokens", ev.tokens());

                    requestStore.complete(ev.requestId(), envelope);


                    System.out.println(
                            "Completed token-list requestId=" + ev.requestId() +
                                    " tokens=" + (ev.tokens() == null ? "NULL" : ev.tokens().size())
                    );

                } catch (Exception e) {
                    System.err.println("FAILED to decode TokenListResult");
                    e.printStackTrace();

                    // ensure polling never hangs
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> m = mapper.readValue(raw, Map.class);
                        Object rid = m.get("requestId");
                        if (rid != null) {
                            requestStore.complete(
                                    rid.toString(),
                                    Map.of(
                                            "tokens", null,
                                            "error", "Failed to decode token list result"
                                    )
                            );
                        }
                    } catch (Exception ignore) {
                        // nothing more we can do
                    }
                }

            }, consumerTag -> {});

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
