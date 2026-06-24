package com.chicamax.sentinella.payments.application.internal.commandservices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chicamax.sentinella.payments.domain.model.aggregates.Payment;
import com.chicamax.sentinella.payments.domain.model.aggregates.Plan;
import com.chicamax.sentinella.payments.domain.model.commands.CreateCheckoutCommand;
import com.chicamax.sentinella.payments.domain.model.valueobjects.PaymentStatus;
import com.chicamax.sentinella.payments.infrastructure.messaging.PaymentCompletedRabbitPublisher;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.PaymentRepository;
import com.chicamax.sentinella.payments.infrastructure.persistence.jpa.PlanRepository;
import com.chicamax.sentinella.payments.infrastructure.stripe.StripeCheckoutService;
import com.chicamax.sentinella.payments.infrastructure.stripe.StripeProperties;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class PaymentCommandServiceImplTest {

    @Mock
    private PlanRepository planRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentCompletedRabbitPublisher paymentCompletedRabbitPublisher;
    @Mock
    private StripeCheckoutService stripeCheckoutService;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private PaymentCommandServiceImpl paymentCommandService;

    @BeforeEach
    void setUp() {
        StripeProperties stripeProperties = new StripeProperties(
                "",
                "",
                "http://localhost:3000/profile?billing=success",
                "http://localhost:3000/profile?billing=cancel",
                "http://localhost:3000/profile?billing=1",
                null
        );
        paymentCommandService = new PaymentCommandServiceImpl(
                planRepository,
                paymentRepository,
                paymentCompletedRabbitPublisher,
                stripeCheckoutService,
                stripeProperties,
                rabbitTemplate
        );
    }

    @Test
    void createCheckout_withoutStripe_returnsPendingPaymentWithoutUrl() {
        UUID userId = UUID.randomUUID();
        UUID planId = UUID.fromString("11111111-1111-1111-1111-111111111201");
        Plan plan = mock(Plan.class);
        when(plan.isActive()).thenReturn(true);
        when(plan.getPriceCents()).thenReturn(9000L);
        when(plan.getSetupPriceCents()).thenReturn(10000L);
        when(plan.getCurrency()).thenReturn("USD");
        when(plan.getId()).thenReturn(planId);

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = paymentCommandService.createCheckout(new CreateCheckoutCommand(userId, planId, "user@example.com"));

        assertThat(result.checkoutUrl()).isNull();
        assertThat(result.payment().getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(stripeCheckoutService, never()).createSubscriptionCheckout(any(), any(), any());
    }

    @Test
    void confirmPayment_isIdempotent() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.createPending(
                paymentId,
                UUID.randomUUID(),
                UUID.fromString("11111111-1111-1111-1111-111111111201"),
                19000,
                "USD"
        );
        payment.complete();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        Payment confirmed = paymentCommandService.confirmPayment(paymentId, "sub_test");

        assertThat(confirmed.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(paymentCompletedRabbitPublisher, never()).publish(any());
    }
}
