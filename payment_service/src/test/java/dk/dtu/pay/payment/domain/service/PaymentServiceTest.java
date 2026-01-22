

package dk.dtu.pay.payment.domain.service;

import dk.dtu.pay.payment.adapter.out.messaging.RabbitMQPaymentRequestedPublisher;
import dk.dtu.pay.payment.application.port.out.PaymentRepositoryPort;
import dk.dtu.pay.payment.domain.model.Payment;
import dk.dtu.pay.payment.domain.model.PaymentRequest;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Payment service unit tests.
 *
 * @author Aleksander Sonder (s185289)
 */

class PaymentServiceTest {

    @Test
    void pay_createsPendingPayment_storesIt_andPublishesPaymentRequested() {
        PaymentRepositoryPort payments = mock(PaymentRepositoryPort.class);
        RabbitMQPaymentRequestedPublisher publisher = mock(RabbitMQPaymentRequestedPublisher.class);
        BankService bank = mock(BankService.class);

        PaymentService service = new PaymentService(payments, publisher, bank);

        PaymentRequest req = new PaymentRequest();
        req.amount = 100;
        req.merchantId = "merchant-bank-account-123";
        req.token = "token-abc";

        Payment created = service.pay(req);

        assertNotNull(created);
        assertNotNull(created.id);
        assertFalse(created.id.isBlank());
        assertEquals(100, created.amount);
        assertEquals("merchant-bank-account-123", created.merchantId);
        assertEquals(Payment.Status.PENDING, created.status);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(payments).add(paymentCaptor.capture());
        assertEquals(created.id, paymentCaptor.getValue().id);

        verify(publisher).publishPaymentRequested(created.id, "token-abc");
        verifyNoInteractions(bank);
    }

    @Test
    void completePaymentForValidatedToken_whenBankSucceeds_marksCompleted_andUpdates() throws Exception {
        PaymentRepositoryPort payments = mock(PaymentRepositoryPort.class);
        RabbitMQPaymentRequestedPublisher publisher = mock(RabbitMQPaymentRequestedPublisher.class);
        BankService bank = mock(BankService.class);

        PaymentService service = new PaymentService(payments, publisher, bank);

        Payment existing = new Payment();
        existing.id = "payment-1";
        existing.amount = 50;
        existing.merchantId = "merchant-bank-1";
        existing.status = Payment.Status.PENDING;

        when(payments.get("payment-1")).thenReturn(existing);

        service.completePaymentForValidatedToken("payment-1", "customer-bank-1");

        verify(bank).transferMoneyFromTo(
                eq("customer-bank-1"),
                eq("merchant-bank-1"),
                eq(BigDecimal.valueOf(50)),
                anyString()
        );

        ArgumentCaptor<Payment> updatedCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(payments).update(updatedCaptor.capture());

        Payment updated = updatedCaptor.getValue();
        assertEquals("payment-1", updated.id);
        assertEquals("customer-bank-1", updated.customerId);
        assertEquals(Payment.Status.COMPLETED, updated.status);
        assertNull(updated.failureReason);
    }

    @Test
    void completePaymentForValidatedToken_whenBankThrows_marksFailed_andStoresReason() throws Exception {
        PaymentRepositoryPort payments = mock(PaymentRepositoryPort.class);
        RabbitMQPaymentRequestedPublisher publisher = mock(RabbitMQPaymentRequestedPublisher.class);
        BankService bank = mock(BankService.class);

        PaymentService service = new PaymentService(payments, publisher, bank);

        Payment existing = new Payment();
        existing.id = "payment-2";
        existing.amount = 999;
        existing.merchantId = "merchant-bank-2";
        existing.status = Payment.Status.PENDING;

        when(payments.get("payment-2")).thenReturn(existing);

        BankServiceException_Exception ex = mock(BankServiceException_Exception.class);
        when(ex.getMessage()).thenReturn("Insufficient funds");

        doThrow(ex)
                .when(bank)
                .transferMoneyFromTo(anyString(), anyString(), any(BigDecimal.class), anyString());

        service.completePaymentForValidatedToken("payment-2", "customer-bank-2");

        ArgumentCaptor<Payment> updatedCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(payments).update(updatedCaptor.capture());

        Payment updated = updatedCaptor.getValue();
        assertEquals(Payment.Status.FAILED, updated.status);
        assertEquals("Insufficient funds", updated.failureReason);
    }
}