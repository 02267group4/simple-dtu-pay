package dk.dtu.pay.payment.adapter.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import dk.dtu.pay.customer.application.port.out.CustomerRepositoryPort;
import dk.dtu.pay.customer.domain.model.Customer;
import dk.dtu.pay.merchant.application.port.out.MerchantRepositoryPort;
import dk.dtu.pay.merchant.domain.model.Merchant;
import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.token.adapter.out.messaging.dto.TokenRejected;
import dk.dtu.pay.token.adapter.out.messaging.dto.TokenValidated;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;

@ApplicationScoped
public class RabbitMQTokenResultConsumer {

    private static final String EXCHANGE = "dtu.pay";
    private static final String VALIDATED_KEY = "token.validated";
    private static final String REJECTED_KEY = "token.rejected";

    private final ObjectMapper mapper = new ObjectMapper();

    @Inject PaymentRepositoryPort payments;
    @Inject CustomerRepositoryPort customers;
    @Inject MerchantRepositoryPort merchants;
    @Inject BankService bank;

    public RabbitMQTokenResultConsumer() {
        new Thread(this::start).start();
    }

    private void start() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(System.getenv().getOrDefault("RABBIT_HOST", "localhost"));

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE, "topic");

            String qValidated = channel.queueDeclare().getQueue();
            channel.queueBind(qValidated, EXCHANGE, VALIDATED_KEY);

            String qRejected = channel.queueDeclare().getQueue();
            channel.queueBind(qRejected, EXCHANGE, REJECTED_KEY);

            channel.basicConsume(qValidated, true, (tag, delivery) -> {
                TokenValidated ev = mapper.readValue(delivery.getBody(), TokenValidated.class);
                handleValidated(ev);
            }, tag -> {});

            channel.basicConsume(qRejected, true, (tag, delivery) -> {
                TokenRejected ev = mapper.readValue(delivery.getBody(), TokenRejected.class);
                handleRejected(ev);
            }, tag -> {});

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleValidated(TokenValidated ev) {
        // YOU MUST HAVE: payments.get(id) and payments.update(payment)
        Payment p = payments.get(ev.paymentId());
        if (p == null) return;

        p.customerId = ev.customerId();

        Customer c = customers.get(p.customerId);
        Merchant m = merchants.get(p.merchantId);
        if (c == null || m == null) {
            p.status = Payment.Status.FAILED;
            p.failureReason = "Unknown customer/merchant after token validation";
            payments.update(p);
            return;
        }

        try {
            bank.transferMoneyFromTo(
                    c.getBankAccountId(),
                    m.bankAccountId,
                    BigDecimal.valueOf(p.amount),
                    "DTU Pay: " + p.customerId + " -> " + p.merchantId
            );
            p.status = Payment.Status.COMPLETED;
            p.failureReason = null;
            payments.update(p);

        } catch (BankServiceException_Exception e) {
            p.status = Payment.Status.FAILED;
            p.failureReason = "Bank failed: " + e.getMessage();
            payments.update(p);
        }
    }

    private void handleRejected(TokenRejected ev) {
        Payment p = payments.get(ev.paymentId());
        if (p == null) return;

        p.status = Payment.Status.FAILED;
        p.failureReason = ev.reason();
        payments.update(p);
    }
}
