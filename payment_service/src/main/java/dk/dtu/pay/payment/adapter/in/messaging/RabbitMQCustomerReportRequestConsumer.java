package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.adapter.out.messaging.RabbitMQCustomerReportResponsePublisher;
import dk.dtu.pay.payment.adapter.in.messaging.dto.CustomerReportRequest;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.service.PaymentService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RabbitMQCustomerReportRequestConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "customer.report.request";

    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentService paymentService;
    private final RabbitMQCustomerReportResponsePublisher publisher;

    @Inject
    public RabbitMQCustomerReportRequestConsumer(PaymentService paymentService,
                                                  RabbitMQCustomerReportResponsePublisher publisher) {
        this.paymentService = paymentService;
        this.publisher = publisher;
    }

    @PostConstruct
    void init() {
        System.out.println("RabbitMQCustomerReportRequestConsumer @PostConstruct publisher=" +
                (publisher == null ? "NULL" : "OK") + " this@" + System.identityHashCode(this));
    }

    public void startListening() {
        new Thread(this::start).start();
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            String host = System.getenv().getOrDefault("RABBIT_HOST", "localhost");
            factory.setHost(host);

            System.out.println("RabbitMQCustomerReportRequestConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQCustomerReportRequestConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic", true);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("CustomerReportRequest raw body: " + raw);

                CustomerReportRequest request = mapper.readValue(delivery.getBody(), CustomerReportRequest.class);
                
                // Filter payments by customerId
                List<Payment> allPayments = paymentService.getPayments();
                List<Payment> customerPayments = allPayments.stream()
                        .filter(p -> request.customerId().equals(p.customerId))
                        .collect(Collectors.toList());
                
                publisher.publishReportResponse(request.correlationId(), customerPayments);
            }, consumerTag -> {
            });

        } catch (Exception e) {
            System.err.println("RabbitMQCustomerReportRequestConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
