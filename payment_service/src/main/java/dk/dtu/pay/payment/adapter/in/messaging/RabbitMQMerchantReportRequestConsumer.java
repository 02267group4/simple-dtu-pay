package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.payment.adapter.out.messaging.RabbitMQMerchantReportResponsePublisher;
import dk.dtu.pay.payment.adapter.in.messaging.dto.MerchantReportRequest;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.service.PaymentService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class RabbitMQMerchantReportRequestConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String ROUTING_KEY = "merchant.report.request";

    private final ObjectMapper mapper = new ObjectMapper();
    private final PaymentService paymentService;
    private final RabbitMQMerchantReportResponsePublisher publisher;

    @Inject
    public RabbitMQMerchantReportRequestConsumer(PaymentService paymentService,
                                                  RabbitMQMerchantReportResponsePublisher publisher) {
        this.paymentService = paymentService;
        this.publisher = publisher;
    }

    @PostConstruct
    void init() {
        System.out.println("RabbitMQMerchantReportRequestConsumer @PostConstruct publisher=" +
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

            System.out.println("RabbitMQMerchantReportRequestConsumer starting — will connect to: " + host);

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            System.out.println("RabbitMQMerchantReportRequestConsumer connected to RabbitMQ");

            channel.exchangeDeclare(EXCHANGE, "topic", true);
            String queue = channel.queueDeclare().getQueue();
            channel.queueBind(queue, EXCHANGE, ROUTING_KEY);

            channel.basicConsume(queue, true, (tag, delivery) -> {
                String raw = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println("MerchantReportRequest raw body: " + raw);

                MerchantReportRequest request = mapper.readValue(delivery.getBody(), MerchantReportRequest.class);
                
                // Filter payments by merchantId
                List<Payment> allPayments = paymentService.getPayments();
                List<Payment> merchantPayments = allPayments.stream()
                        .filter(p -> request.merchantId().equals(p.merchantId))
                        .collect(Collectors.toList());
                
                publisher.publishReportResponse(request.correlationId(), merchantPayments);
            }, consumerTag -> {
            });

        } catch (Exception e) {
            System.err.println("RabbitMQMerchantReportRequestConsumer failed to start:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
