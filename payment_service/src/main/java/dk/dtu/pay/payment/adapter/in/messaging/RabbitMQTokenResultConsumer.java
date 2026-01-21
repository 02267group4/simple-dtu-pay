// java
package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.domain.service.PaymentService;

// ONLY these two local imports for the DTOs:
import dk.dtu.pay.payment.adapter.in.messaging.dto.TokenRejected;
import dk.dtu.pay.payment.adapter.in.messaging.dto.TokenValidated;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
public class RabbitMQTokenResultConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String VALIDATED_KEY = "token.validated";
    private static final String REJECTED_KEY = "token.rejected";

    private final ObjectMapper mapper = new ObjectMapper();

    private final PaymentService paymentService;

    @Inject
    public RabbitMQTokenResultConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostConstruct
    void init() {
        System.out.println("RabbitMQTokenResultConsumer @PostConstruct paymentService=" +
                (paymentService == null ? "NULL" : "OK") + " this@" + System.identityHashCode(this));
    }

    public void startListening() {
        new Thread(this::start).start();
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            String host = System.getenv().getOrDefault("RABBIT_HOST", "localhost");
            factory.setHost(host);

            System.out.println("RabbitMQTokenResultConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQTokenResultConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic");

            String qValidated = channel.queueDeclare().getQueue();
            channel.queueBind(qValidated, EXCHANGE, VALIDATED_KEY);

            String qRejected = channel.queueDeclare().getQueue();
            channel.queueBind(qRejected, EXCHANGE, REJECTED_KEY);

            channel.basicConsume(qValidated, true, (tag, delivery) -> {
                System.out.println("TokenValidated handler — this@" + System.identityHashCode(this));

                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("TokenValidated raw body: " + raw);

                TokenValidated ev = mapper.readValue(delivery.getBody(), TokenValidated.class);
                paymentService.completePaymentForValidatedToken(ev.paymentId(), ev.customerId());
            }, consumerTag -> {});

            channel.basicConsume(qRejected, true, (tag, delivery) -> {
                System.out.println("TokenRejected handler — this@" + System.identityHashCode(this));

                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("TokenRejected raw body: " + raw);

                TokenRejected ev = mapper.readValue(delivery.getBody(), TokenRejected.class);
                paymentService.failPayment(ev.paymentId(), ev.reason());
            }, consumerTag -> {});

        } catch (Exception e) {
            System.err.println("RabbitMQTokenResultConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
